package com.andjacoco.demo;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.jacoco.agent.rt.CodeCoverageManager;

/**
 * FileName: BaseActivity
 * Author: zhihao.wu@ttpai.cn
 * Date: 2020/9/23
 * Description:
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG","BaseActivity onDestroy");
        CodeCoverageManager.generateCoverageFile();

    }
}
