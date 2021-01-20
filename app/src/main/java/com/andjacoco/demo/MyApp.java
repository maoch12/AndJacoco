package com.andjacoco.demo;

import android.app.Application;

import org.jacoco.agent.rt.CodeCoverageManager;

public class MyApp extends Application {
    public static Application app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        int a=0;

        CodeCoverageManager.init(app,BuildConfig.appName,BuildConfig.VERSION_CODE,"http://10.10.17.105:8080");//内网 服务器地址);
        CodeCoverageManager.uploadData();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            CodeCoverageManager.generateCoverageFile();
        }
    }
}
