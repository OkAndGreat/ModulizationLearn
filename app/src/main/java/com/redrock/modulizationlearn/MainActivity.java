package com.redrock.modulizationlearn;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.redrock.arouter_annotation.ARouter;
import com.redrock.arouter_api.RouterManager;


@ARouter(path = "/app/MainActivity/")
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RouterManager.getInstance()
                .build("/login/MainActivity/")
                .withString("str","Parameter")
                .navigation(this);
    }
}
