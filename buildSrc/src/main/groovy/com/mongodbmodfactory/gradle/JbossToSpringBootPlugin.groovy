package com.mongodbmodfactory.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

class JbossToSpringBootPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        // Create extension for configuration
        def extension = project.extensions.create('jbossToSpringBoot', JbossToSpringBootExtension)

        // Create source set for generated code
        def sourceSets = project.extensions.getByType(SourceSetContainer)
        def generatedSourceSet = sourceSets.create('generated')
        generatedSourceSet.java.srcDir("${project.buildDir}/generated/sources/spring-boss")

        // Add generated source set to main source set
        sourceSets.main.compileClasspath += generatedSourceSet.output
        sourceSets.main.runtimeClasspath += generatedSourceSet.output

        // Create task to transform controllers
        project.tasks.register('transformControllers', TransformControllersTask) { task ->
            task.rootPackage = extension.rootPackage
            task.sourceDir = project.file('src/main/resources/source-files')
            task.outputDir = project.file("${project.buildDir}/generated/sources/spring-boss")
            task.dependsOn(project.tasks.named('processResources'))
        }

        // Make compileJava depend on transformControllers
        project.tasks.named('compileJava') {
            dependsOn 'transformControllers'
        }
    }
} 