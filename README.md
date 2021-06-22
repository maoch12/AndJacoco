# AndJacoco
AndJacoco 是用于Android App的增量代码测试覆盖率工具，基于jacoco源码修改而来。相比于原版jacoco全量测试，AndJacoco只针对于
增量代码的覆盖测试。通过配置要对比的分支，得到两分支差异代码，来实现只对增量代码插入。输出html报告供查看。
### 接入
因为在运行时会把ec数据文件上传到服务器，编译时会去下载，得到ec，所以要先配置服务器。  
1、服务器布在局域网即可，服务器源码在WebServer 项目，把WebServer.war 放在tomcat 启动即可。  
2、在项目根目录的build.gradle添加jitpack仓库与插件  
```
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        //见 github release 最新版
        classpath 'com.github.ttpai.AndJacoco:plugin:0.0.5'
    }
}

allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

在app/build.gradle中应用插件

```
apply plugin: 'com.ttp.and_jacoco'

//代码覆盖配置
jacocoCoverageConfig {
    jacocoEnable true //开关
    branchName 'main'//要对比的分支名
    host="http://10.10.17.105:8080"//下载服务host
    execDir "${project.buildDir.absolutePath}/outputs/coverage"//ec 下载存放路径
    sourceDirectories = getAllJavaDir() //源码路径
    classDirectories = ["${rootProject.projectDir.absolutePath}/app/classes"] //classes 路径
    gitPushShell="${project.projectDir}/shell/gitPushShell.sh" //提交git 命令
    copyClassShell="${project.projectDir}/shell/pullDiffClass.sh" //copy classes 命令
    includes = ['com.andjacoco.demo'] //要 包含的class 包名,数组
    excludeClass = { // return true 表示要排除的class
//        println("exclude it=${it}")
        return false
    }
    excludeMethod = {//return true 表示要排除此方法
        println("excludeMethod it=${it}")
        return false
    }
}
buildTypes {
        release {
            buildConfigField "String", "host", "\"${jacocoCoverageConfig.host}\""
        }
        debug{
            buildConfigField "String", "host", "\"${jacocoCoverageConfig.host}\""
        }
    }


def ArrayList<String> getAllJavaDir() {
    //获取所有module 的源码路径
    Set<Project> projects = project.rootProject.subprojects
    List<String> javaDir = new ArrayList<>(projects.size())
    projects.forEach {
        javaDir.add("$it.projectDir/src/main/java")
    }
    return javaDir
}

dependencies {

    debugImplementation "com.github.ttpai.AndJacoco:rt:0.0.5"
    releaseImplementation "com.github.ttpai.AndJacoco:rt-no-op:0.0.5"
}

```
jacocoCoverageConfig 是代码覆盖的配置。  
jacocoEnable： 是总开关，开启会copy class,执行 git命令等，插入代码。线上包建议关闭。  
branchName: 要对比的分支名，一般为线上稳定分支，如master，用于切换到该分支copy class  
host: 运行时ec 数据文件的上传与下载服务器，应确保是同一个
execDir：生成报告时，从服务器下载的ec 文件存放目录  
classDirectories：class 存放路径，enable开启时会copy class 到该目录  
gitPushShell、copyClassShell：开启时会执行git 命令，建议复制app/shell 文件夹到你的项目中，再对具体命令修改。  
includes：要保存的class 包名，建议只保存自己包名的class。当这些class 有差异时才会插入代码。  
excludeClass：就算是你项目的包名，可能还要过滤某些自动生成的class,例如 DataBinding....。return true表示过滤  
excludeMethod：过滤某些方法，因为在编译时，会自动生成某些方法。如带 $ 的虚方法。  
reportDirectory：报告输出目录，默认为 `"${project.buildDir.getAbsolutePath()}/outputs/report"`

rt 是运行时的库，rt-no-op 是空代码实现，用于正式包编译不报错

在Application中
```java
@Override
public void onCreate() {
    super.onCreate();
    //初始化，会上传上次数据
    CodeCoverageManager.init(app, BuildConfig.host);
    //uploadData 上传上次保存的数据
    CodeCoverageManager.uploadData();

}

```
在合适的时机调用 `CodeCoverageManager.generateCoverageFile(); `,例如activity.onDestroy 中

```

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存这次运行时的数据到本地
        CodeCoverageManager.generateCoverageFile();

    }
```
详细见demo源码。  

运行一会，打开关闭几个activity，重启app,这时app 会把上次的 ec 文件上传到服务器。
### 生成报告
执行 ./gradlew generateReport 任务生成报告，报告生成目录 app/builds/outputs/report，打开index.html，就可以看见本次的覆盖率报告了。


原理：[Android 增量代码覆盖实践](https://blog.csdn.net/u010521645/article/details/112780673)
