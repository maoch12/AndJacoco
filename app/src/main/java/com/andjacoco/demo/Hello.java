package com.andjacoco.demo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class Hello {
    public static void Toast(Context context, String s) {
        try {
            int a = 0;
            int b = 0;
        } catch (@Nullable Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "" + s.length(), Toast.LENGTH_LONG).show();
    }

    public static void hello(boolean flag) {
        new Thread(() -> {
            int a = 2;
            int b = 2;
            int c = a + b;
            int d = c + 1;
        }).start();
        if (flag) {
            Log.i("aa", "aa");
        } else {
            Log.i("aa", "bb");
        }
    }
}
