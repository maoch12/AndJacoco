package com.andjacoco.demo

import android.os.Bundle
import android.util.Log
import android.widget.TextView

class SecondActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        secondTest()

        findViewById<TextView>(R.id.tv_second).setOnClickListener({
            Log.i("tag", "log")
        })
        SecondHello().sayHello()
//        ThirdHello().sayHello()
        FourthHello().sayHello()
    }

    fun secondTest() {
        Log.i("tag", "asdasd")
    }
}