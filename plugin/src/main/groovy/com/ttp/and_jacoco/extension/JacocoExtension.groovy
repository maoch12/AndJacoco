package com.ttp.and_jacoco.extension

class JacocoExtension {
    //jacoco开关，false时不会进行probe插桩
    boolean jacocoEnable
    //需要对比的分支名
    String branchName
    //exec文件路径，支持多个ec文件，自动合并
    String execDir
    //源码目录，支持多个源码
    List<String> sourceDirectories
    //class目录，支持多个class目录
    List<String> classDirectories
    //需要插桩的文件
    List<String> includes
    //生成报告的目录
    String reportDirectory
    //git 提交命令
    String gitPushShell
    //复制class 的shell
    String copyClassShell
    //git-bash的路径，插件会自动寻找路径，如果找不到，建议自行配置
    private String gitBashPath
    //下载ec 的服务器
    String host


    /**
     * 类过滤器 返回 true 的将会被过滤
     * exclude{*      it="/com/ttp/xxx.class"
     *     return it.endsWith(".a")
     *}*/
    Closure excludeClass
    /**
     *
     * 方法过滤器 返回true 的将会被过滤
     * exclude{*     it = MethodInfo
     *}*/
    Closure excludeMethod

    String getGitBashPath() {
        if (gitBashPath == null || gitBashPath.isEmpty()) {
            Process process = 'where git'.execute()
            String path = process.inputStream.text
            process.closeStreams()
            String[] paths = path.split('\n')
            String temp = ''
            paths.each {
                File file = new File(it)
                File gitBash = new File(file.getParentFile().getParent() + File.separator + 'git-bash.exe')
                println("GitBashPath:$gitBash exist:${gitBash.exists()}")
                if (gitBash.exists()) {
                    temp = gitBash.absolutePath
                    return temp
                }
            }
            return temp
        }
        return gitBashPath
    }
}