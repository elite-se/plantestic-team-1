package de.unia.se.plantestic

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter.OpenApiValidationException
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.specs.StringSpec
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.joor.Reflect
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.io.File

private val wireMockServer = WireMockServer(8080)

class SwaggerResponseValidationTest : StringSpec({
    "One field in object, should be accepted" {
        val body = """{
              "test": "123"
            }"""
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/hello"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("content-type", "application/json")
                    .withBody(body)))

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
        compiledTest.call("test")
    }

    "One field in object, should be rejected" {
        val body = """{
              "test_wrong_name" : "123"
            }"""
        wireMockServer.stubFor(
            WireMock
                .get(WireMock.urlPathMatching("/hello"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("content-type", "application/json")
                    .withBody(body)))

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
        val exception = assertThrows<Exception> {
            compiledTest.call("test")
        }
        assertTrue(exception.cause!!.cause!! is OpenApiValidationException, "Expected exception caused by" +
                " OpenApiValidator, it seems like a different exception occurred")
    }
}) {
    companion object {
        private val MINIMAL_EXAMPLE_INPUT_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("minimal_hello_restassured.xmi")!!.toURI())
        private val MINIMAL_EXAMPLE_CONFIG_FILE = File(Thread.currentThread().contextClassLoader
            .getResource("minimal_hello_config_minimal_swagger.toml")!!.toURI())

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

    override fun beforeTest(description: Description) {
        wireMockServer.start()
    }

    override fun afterTest(description: Description, result: TestResult) {
        wireMockServer.stop()
        wireMockServer.resetAll()
    }
}
