package de.unia.se.plantestic

import io.kotlintest.specs.StringSpec
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.joor.Reflect
import java.io.File

class AcceleoGeneratorTest : StringSpec({
    "Transform a Rest Assured EObject input to Java Code for minimal hello" {
        MetaModelSetup.doSetup()

        val pumlInputModelURI = URI.createFileURI(MINIMAL_EXAMPLE_INPUT_FILE.path)
        val pumlInputModel = ResourceSetImpl().getResource(pumlInputModelURI, true).contents[0]

        AcceleoCodeGenerator.generateCode(pumlInputModel, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles()!!.first { f -> f.name == "Test_minimal_hello.java" }
        printCode(generatedSourceFile)
        val compiledTest = Reflect.compile(
            generatedSourceFile.nameWithoutExtension,
            generatedSourceFile.readText()
        )!!.create()!!
        invokeCreation(compiledTest, MINIMAL_EXAMPLE_CONFIG_FILE.path)
    }

    "Transform a Rest Assured EObject input to Java Code for complex hello" {
        MetaModelSetup.doSetup()

        val pumlInputModelURI = URI.createFileURI(COMPLEX_HELLO_INPUT_FILE.path)
        val pumlInputModel = ResourceSetImpl().getResource(pumlInputModelURI, true).contents[0]

        AcceleoCodeGenerator.generateCode(pumlInputModel, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles()!!.first { f -> f.name == "Test_complex_hello.java" }
        printCode(generatedSourceFile)
        val compiledTest = Reflect.compile(
            generatedSourceFile.nameWithoutExtension,
            generatedSourceFile.readText()
        )!!.create()!!
        invokeCreation(compiledTest, COMPLEX_HELLO_CONFIG_FILE.path)
    }

    "Transform a Rest Assured EObject input to Java Code for rerouting" {
        MetaModelSetup.doSetup()

        val pumlInputModelURI = URI.createFileURI(REROUTE_INPUT_FILE.path)
        val pumlInputModel = ResourceSetImpl().getResource(pumlInputModelURI, true).contents[0]

        AcceleoCodeGenerator.generateCode(pumlInputModel, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles()!!.first { f -> f.name == "Test_rerouting.java" }
        printCode(generatedSourceFile)
        val compiledTest = Reflect.compile(
            generatedSourceFile.nameWithoutExtension,
            generatedSourceFile.readText()
        )!!.create()!!
        invokeCreation(compiledTest, REROUTE_CONFIG_FILE.path)
    }

    "Transform a Rest Assured EObject input to Java Code for xcall" {
        MetaModelSetup.doSetup()

        val pumlInputModelURI = URI.createFileURI(XCALL_INPUT_FILE.path)
        val pumlInputModel = ResourceSetImpl().getResource(pumlInputModelURI, true).contents[0]

        AcceleoCodeGenerator.generateCode(pumlInputModel, OUTPUT_FOLDER)

        // Now compile the resulting code to check for syntax errors
        val generatedSourceFile = OUTPUT_FOLDER.listFiles()!!.first { f -> f.name == "Test_xcall.java" }
        printCode(generatedSourceFile)
        val compiledTest = Reflect.compile(
            generatedSourceFile.nameWithoutExtension,
            generatedSourceFile.readText()
        )!!.create()!!
        invokeCreation(compiledTest, XCALL_CONFIG_FILE.path)
    }
}) {
    companion object {
        private val MINIMAL_EXAMPLE_INPUT_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("minimal_hello_restassured.xmi")!!.toURI())
        private val MINIMAL_EXAMPLE_CONFIG_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("minimal_hello_config.toml")!!.toURI())

        private val COMPLEX_HELLO_INPUT_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("complex_hello_restassured.xmi")!!.toURI())
        private val COMPLEX_HELLO_CONFIG_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("complex_hello_config.toml")!!.toURI())

        private val REROUTE_INPUT_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("rerouting_restassured.xmi")!!.toURI())
        private val REROUTE_CONFIG_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("rerouting_config.toml")!!.toURI())

        private val XCALL_INPUT_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("xcall_restassured.xmi")!!.toURI())
        private val XCALL_CONFIG_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("xcall_config.toml")!!.toURI())

        private val OUTPUT_FOLDER = File(Thread.currentThread().contextClassLoader
            .getResource("code-generation")!!.toExternalForm(), "AcceleoGeneratorTest/GeneratedCode")

        fun printCode(file: File) {
            file.readLines().forEach { line -> println(line) }
        }

        fun invokeCreation(compiledTest: Reflect, configFilePath: String) {
            compiledTest.call("setConfigFilePath", configFilePath)
            compiledTest.call("setupConfig")
        }
    }
}
