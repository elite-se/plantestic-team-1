package de.unia.se.plantestic

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.joor.Reflect
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

object Main {

    private val IS_WINDOWS = System.getProperty("os.name").contains("indow")

    class Cli : CliktCommand(
        printHelpOnEmptyArgs = true,
        help = "Plantestic is a tool that transforms UML sequence diagrams of REST APIs into Java unit tests.",
        epilog = """
        |The UML sequence diagrams must be specified with PlantUML.
        |Arrows between actors are considered as requests and responses.
        |Actors are considered services. The actor with the first outgoing arrow is considered the client.
        |Test cases emulate requests of the client against other actors.
        |
        |DIAGRAM FORMAT
        |==============
        |
        |The text on arrows need to follow a certain format to be recognized as requests or responses.
        |For requests, this is:
        |
        |   <METHOD> "<PATH>" (<PARAM_NAME> : "<PARAM_VALUE>"[, ...])
        |
        |Where <METHOD> is one of 'GET', 'POST', 'PUT', 'DELETE', 'PATCH'
        |<PATH> is the relative URL.
        |Params are parameter pairs. Depending on the type of request, they will either be used as query or form params.
        |
        |All values that are in quotes can contain arbitrary variable substitutions of the form "$\{VARIABLE_NAME\}".
        |Variables can either be imported via the configuration file or from previous responses.
        |
        |Similarly, responses need to follow the following schema:
        |
        |<CODE>
        |  or
        |<CODE> - (<VARIABLE_NAME> : "<XPATH_TO_VARIABLE>"[, ...])
        |
        |Where <CODE> is one or a comma-separated list of allowed HTTP response codes.
        |<XPATH_TO_VARIABLE> is a path to a certain value in a JSON or XML body following the XPATH scheme.
        |The value of <XPATH_TO_VARIABLE> will be checked for being present and will then be assigned to
        |<VARIABLE_NAME> for later use.
        |
        |Plantestic also allows conditional requests. They should be put in UML "alt" blocks within PlantUML.
        |The condition might be any valid JavaScript code with any arbitrary templating variables.
        |Before the code will be evaluated, the templating engine will replace all variables in "$\{VAR\}".
        |The condition should return true or false.
        |
        |CONFIG FILE
        |===========
        |
        |Because your test might contain sensitive data or data you quickly want to change between tests,
        |Plantestic also supports config files in .toml format.
        |You can define arbitrary variables and their values in the [Templating] section.
        |Every service might additionally get values for "path", "username" and "password".
        |
        |Example:
        |--------
        |
        |[Templating]
        |id = "asdf"
        |important_value = 5
        |
        |[ServiceA]
        |path = "www.example.com"
        |username = "admin"
        |password = "admin"
        |
        |LEGAL
        |=====
        |
        |This software is licensed under Apache 2.0 license and was developed by 
        |Andreas Zimmerer, Stefan Grafberger, Fiona Guerin, Daniela Neupert and Michelle Martin.
        """.trimMargin()) {

        private val input: String by option(help = "Path to the PlantUML file containing the API specification.")
            .required()
        private val output: String by option(help = "Output folder where the test cases should be written to. Default is './plantestic-test'")
            .default("./test-suite/tests")
        private val execute: Boolean? by option(help = "Run the pipeline and execute the test").flag(default = false)
        private val config: String? by option(help = ".toml file which is to be used by the pipeline")

        override fun run() {
            val inputFile = File(input).normalize()
            val outputFolder = File(output).normalize()
            val tempOutputFolder = File("$output/temp").normalize()

            if (!inputFile.exists()) {
                echo("Input file ${inputFile.absolutePath} does not exist.")
                return
            }

            echo("###Welcome to the plantestic pipeline###")
            runTransformationPipeline(inputFile, tempOutputFolder)

            val generatedSourceFile = tempOutputFolder.listFiles()?.first()
            if (generatedSourceFile == null) throw Exception("Something went wrong with generating the file")
            //echo("Generated source file is $generatedSourceFile")
            val targetString = outputFolder.absolutePath + "/" + generatedSourceFile.name
            //echo("Target file is $targetString")
            Files.move(generatedSourceFile.toPath(), Paths.get(targetString), StandardCopyOption.REPLACE_EXISTING)
            val targetFile = File(targetString)
            echo("Generated test ${targetFile.path}")

            if (execute == null || execute == false) return
            if (config == null) {
                echo("Config file must be defined if the execute flag is used.")
                return
            }
            val configFile = File(config).normalize()
            if (!configFile.exists()) {
                echo("Config file ${configFile.absolutePath} does not exist.")
                return
            }
            executeTestCase(targetFile, configFile)
            print("The test was successful")
        }
    }

    fun runTransformationPipeline(inputFile: File, outputFolder: File) {
        MetaModelSetup.doSetup()

        val pumlDiagramModel = PumlParser.parse(inputFile.absolutePath)

        val requestResponsePairsModel = M2MTransformer.transformPuml2ReqRes(pumlDiagramModel)
        val restAssuredModel = M2MTransformer.transformReqRes2RestAssured(requestResponsePairsModel)

        println("Generating code into $outputFolder")
        AcceleoCodeGenerator.generateCode(restAssuredModel, outputFolder)
    }

    fun executeTestCase(targetFile: File, configFile: File) {
        //Files.move(inputFile.toPath(), path)

        // move file to resources/user-pipeline/inputFile.name
        // move the config file to the correct location
        // execute that code with its toml
        val path_fix: String = if (IS_WINDOWS) configFile.path[0].toString() else ""

        val compiledTest = Reflect.compile(
            targetFile.nameWithoutExtension,
            targetFile.readText()
        ).create()
        compiledTest.call("setConfigFilePath", configFile.path)
        compiledTest.call("setupConfig")
        try {
            compiledTest.call("test")
        }
        catch (e: org.joor.ReflectException) {
            // Connection exception
            println("The tested system refused to connect!")
            println(e.cause!!.cause)
            exitProcess(1)
        }
        print("The test was successful")
    }

    @JvmStatic
    fun main(args: Array<String>) = Cli().main(args)
}
