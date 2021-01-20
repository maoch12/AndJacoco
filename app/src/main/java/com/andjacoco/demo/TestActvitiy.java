package com.andjacoco.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TestActvitiy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_actvitiy);
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.button:
                Toast.makeText(this,"test",Toast.LENGTH_SHORT);
              break;
            default:

              break;
        }
    }
}