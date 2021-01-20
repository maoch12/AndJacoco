package com.ttp.and_jacoco.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.WorkResult

class ClassCopyTask extends DefaultTask {

    def variantName

    @TaskAction
    def doCopy() {
        println('start copy classes')
        def javaDir = "${project.projectDir}${File.separator}classes${File.separator}java"
        def kotlinDir = "${project.projectDir}${File.separator}classes${File.separator}kotlin"
        project.delete(javaDir)
        project.mkdir(javaDir)
        project.delete(kotlinDir)
        project.mkdir(kotlinDir)
        def buildJavaDir = "${project.buildDir}${File.separator}intermediates${File.separator}javac${File.separator}${variantName}${File.separator}classes"
        def buildKotlinDir = "${project.buildDir}${File.separator}tmp${File.separator}kotlin-classes${File.separator}${variantName}"

        project.copy {
            from buildJavaDir
            into javaDir
            exclude { details ->
                (details.file.name == 'R.class' ||
                        details.file.name == 'R2.class' ||
                        details.file.name == 'BR.class' ||
                        details.file.name.startsWith('DataBind') ||
                        details.file.name.startsWith('R$'))
            }
        }

        project.copy {
            from buildKotlinDir
            into kotlinDir
            exclude { details ->
                (details.file.name.endsWith('.kotlin_module'))
            }
        }

        //由于class文件被ignore了，所以要加上-f
        "git add -f ${project.projectDir}${File.separator}classes${File.separator}".execute().waitFor()
    }

}