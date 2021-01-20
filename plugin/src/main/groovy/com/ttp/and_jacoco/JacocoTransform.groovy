package com.ttp.and_jacoco

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.ttp.and_jacoco.extension.JacocoExtension
import com.ttp.and_jacoco.task.BranchDiffTask
import com.ttp.and_jacoco.util.Utils
import groovy.io.FileType
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.gradle.api.Project
import org.jacoco.core.diff.DiffAnalyzer
import org.jacoco.core.tools.Util

class JacocoTransform extends Transform {
    Project project

    JacocoExtension jacocoExtension

    JacocoTransform(Project project, JacocoExtension jacocoExtension) {
        this.project = project
        this.jacocoExtension = jacocoExtension
    }

    @Override
    String getName() {
        return "jacoco"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        def dirInputs = new HashSet<>()
        def jarInputs = new HashSet<>()

        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll()
        }

        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                dirInputs.add(dirInput)
            }
            input.jarInputs.each { jarInput ->
                jarInputs.add(jarInput)
            }
        }

        if (!dirInputs.isEmpty() || !jarInputs.isEmpty()) {
            if (jacocoExtension.jacocoEnable) {
                //copy class到 app/classes
                copy(transformInvocation, dirInputs, jarInputs, jacocoExtension.includes)
                //提交classes 到git
                gitPush(jacocoExtension.gitPushShell, "jacoco auto commit")
                //获取差异方法集
                BranchDiffTask branchDiffTask = project.tasks.findByName('generateReport')
                branchDiffTask.pullDiffClasses()
            }
            //对diff方法插入探针
            inject(transformInvocation, dirInputs, jarInputs, jacocoExtension.includes)

        }
    }

    def copy(TransformInvocation transformInvocation, def dirInputs, def jarInputs, List<String> includes) {
        def classDir = "${project.projectDir}/classes"
        ClassCopier copier = new ClassCopier(classDir, includes)
        if (!transformInvocation.incremental) {
            FileUtils.deletePath(new File(classDir))
        }
        if (!dirInputs.isEmpty()) {
            dirInputs.each { dirInput ->
                if (transformInvocation.incremental) {
                    dirInput.changedFiles.each { entry ->
                        File fileInput = entry.getKey()
                        File fileOutputJacoco = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), classDir))
                        Status fileStatus = entry.getValue()

                        switch (fileStatus) {
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (fileInput.isDirectory()) {
                                    return // continue.
                                }
                                copier.doClass(fileInput, fileOutputJacoco)
                                break
                            case Status.REMOVED:
                                if (fileOutputJacoco.exists()) {
                                    if (fileOutputJacoco.isDirectory()) {
                                        fileOutputJacoco.deleteDir()
                                    } else {
                                        fileOutputJacoco.delete()
                                    }
                                    println("REMOVED output file Name:${fileOutputJacoco.name}")
                                }
                                break
                        }
                    }
                } else {
                    dirInput.file.traverse(type: FileType.FILES) { fileInput ->
                        File fileOutputJacoco = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), classDir))
                        copier.doClass(fileInput, fileOutputJacoco)
                    }
                }
            }
        }

        if (!jarInputs.isEmpty()) {
            jarInputs.each { jarInput ->
                File jarInputFile = jarInput.file
                copier.doJar(jarInputFile, null)
            }
        }

    }

    def inject(TransformInvocation transformInvocation, def dirInputs, def jarInputs, List<String> includes) {

        ClassInjector injector = new ClassInjector(includes)
        if (!dirInputs.isEmpty()) {
            dirInputs.each { dirInput ->
                File dirOutput = transformInvocation.outputProvider.getContentLocation(dirInput.getName(),
                        dirInput.getContentTypes(), dirInput.getScopes(),
                        Format.DIRECTORY)
                FileUtils.mkdirs(dirOutput)

                if (transformInvocation.incremental) {
                    dirInput.changedFiles.each { entry ->
                        File fileInput = entry.getKey()
                        File fileOutputTransForm = new File(fileInput.getAbsolutePath().replace(
                                dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                        FileUtils.mkdirs(fileOutputTransForm.parentFile)
                        Status fileStatus = entry.getValue()
                        switch (fileStatus) {
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (fileInput.isDirectory()) {
                                    return // continue.
                                }
                                if (jacocoExtension.jacocoEnable &&
                                        DiffAnalyzer.getInstance().containsClass(getClassName(fileInput))) {
                                    injector.doClass(fileInput, fileOutputTransForm)
                                } else {
                                    FileUtils.copyFile(fileInput, fileOutputTransForm)
                                }
                                break
                            case Status.REMOVED:
                                if (fileOutputTransForm.exists()) {
                                    if (fileOutputTransForm.isDirectory()) {
                                        fileOutputTransForm.deleteDir()
                                    } else {
                                        fileOutputTransForm.delete()
                                    }
                                    println("REMOVED output file Name:${fileOutputTransForm.name}")
                                }
                                break
                        }
                    }
                } else {
                    dirInput.file.traverse(type: FileType.FILES) { fileInput ->
                        File fileOutputTransForm = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                        FileUtils.mkdirs(fileOutputTransForm.parentFile)
                        if (jacocoExtension.jacocoEnable &&
                                DiffAnalyzer.getInstance().containsClass(getClassName(fileInput))) {
                            injector.doClass(fileInput, fileOutputTransForm)
                        } else {
                            FileUtils.copyFile(fileInput, fileOutputTransForm)
                        }
                    }
                }
            }
        }

        if (!jarInputs.isEmpty()) {
            jarInputs.each { jarInput ->
                File jarInputFile = jarInput.file
                File jarOutputFile = transformInvocation.outputProvider.getContentLocation(
                        jarInputFile.getName(), getOutputTypes(), getScopes(), Format.JAR
                )

                FileUtils.mkdirs(jarOutputFile.parentFile)

                switch (jarInput.status) {
                    case Status.NOTCHANGED:
                        if (transformInvocation.incremental) {
                            break
                        }
                    case Status.ADDED:
                    case Status.CHANGED:
                        if (jacocoExtension.jacocoEnable) {
                            injector.doJar(jarInputFile, jarOutputFile)
                        } else {
                            FileUtils.copyFile(jarInputFile, jarOutputFile)
                        }
                        break
                    case Status.REMOVED:
                        if (jarOutputFile.exists()) {
                            jarOutputFile.delete()
                        }
                        break
                }
            }
        }
    }

    def gitPush(String shell, String commitMsg) {
        println("jacoco 执行git命令")
//
        String[] cmds
        if (Utils.windows) {
            cmds = new String[3]
            cmds[0] = jacocoExtension.getGitBashPath()
            cmds[1] = shell
            cmds[2] = commitMsg
        } else {
            cmds = new String[2]
            cmds[0] = shell
            cmds[1] = commitMsg
        }
        println("cmds=" + cmds)
        Process pces = Runtime.getRuntime().exec(cmds)
        String result = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getIn())))
        String error = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getErr())))

        println("jacoco git succ :" + result)
        println("jacoco git error :" + error)

        pces.closeStreams()
    }

    String getUniqueHashName(File fileInput) {
        final String fileInputName = fileInput.getName()
        if (fileInput.isDirectory()) {
            return fileInputName
        }
        final String parentDirPath = fileInput.getParentFile().getAbsolutePath()
        final String pathMD5 = Util.MD5(parentDirPath)
        final int extSepPos = fileInputName.lastIndexOf('.')
        final String fileInputNamePrefix =
                (extSepPos >= 0 ? fileInputName.substring(0, extSepPos) : fileInputName)
        return fileInputNamePrefix + '_' + pathMD5
    }

    def getClassName(File f) {
        return ClassProcessor.filePath2ClassName(f).replaceAll(".class", "")
    }
}