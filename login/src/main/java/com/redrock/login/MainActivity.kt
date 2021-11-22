package com.redrock.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.redrock.arouter_annotation.ARouter
import com.redrock.arouter_annotation.Parameter
import com.redrock.arouter_api.ParameterManager

@ARouter(path = "/login/MainActivity/")
class MainActivity : AppCompatActivity() {

    @Parameter
    lateinit var para:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ParameterManager.getInstance()
//            .loadParameter(this)
    }
}