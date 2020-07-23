package de.unia.se.plantestic

import com.google.common.io.Resources
import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator
import org.eclipse.acceleo.parser.compiler.AcceleoCompiler
import org.eclipse.emf.common.util.BasicMonitor
import org.eclipse.emf.common.util.Monitor
import org.eclipse.emf.ecore.EObject
import java.io.File
import java.io.IOException
import java.nio.file.Paths

class TomlAcceleoGenerator : AbstractAcceleoGenerator() {

    private val moduleName = "generateTomlConfig.mtl"
    private val templateNames = arrayOf("generateTomlTemplate")
    private val acceleoTransformationsInputFolder = Resources.getResource("config-generation").path
    private val compiledAcceleoTransformationsOutputFolder = Paths.get(System.getProperty("user.dir"),
        "build", "classes", "kotlin", "main", "de", "unia", "se", "plantestic").toString()

    override fun initialize(model: EObject, targetFolder: File, arguments: List<Any>) {
        println("Compiling .mtl file for generating toml from Rest assured Model")
        val acceleoCompiler = AcceleoCompiler()

        acceleoCompiler.setSourceFolder(acceleoTransformationsInputFolder)
        acceleoCompiler.setOutputFolder(compiledAcceleoTransformationsOutputFolder)
        acceleoCompiler.setBinaryResource(false)
        acceleoCompiler.execute()

        super.initialize(model, targetFolder, arguments)
    }

    @Throws(IOException::class)
    override fun doGenerate(monitor: Monitor) {
        println("Generating code from Rest assured Model for toml template")
        super.doGenerate(BasicMonitor())
    }

    override fun getModuleName(): String {
        return moduleName
    }

    override fun getTemplateNames(): Array<String> {
        return templateNames
    }
}
