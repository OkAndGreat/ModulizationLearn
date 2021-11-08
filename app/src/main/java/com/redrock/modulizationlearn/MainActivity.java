package com.redrock.modulizationlearn;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.redrock.arouter_annotation.ARouter;

@ARouter(path = "123")
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
