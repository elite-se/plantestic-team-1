package de.unia.se.plantestic

import org.eclipse.emf.common.util.BasicMonitor
import org.eclipse.emf.ecore.EObject
import java.io.File

object AcceleoTomlGenerator {

    /**
     * Generates the Rest Assured Code based on a Rest Assured EObject.
     * @param inputModel The Rest Assured EObject
     * @param targetFolder The folder to write the generated code into
     */
    fun generateToml(inputModel: EObject, targetFolder: File) {
        val tomlAcceleoGenerator = TomlAcceleoGenerator()
        tomlAcceleoGenerator.initialize(inputModel, targetFolder, emptyList())
        tomlAcceleoGenerator.doGenerate(BasicMonitor())
    }
}
