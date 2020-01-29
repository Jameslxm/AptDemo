package com.lxm.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lxm.module.annotation.ARouter;

@ARouter(path = "/personal/PersonalActivity")
public class PersonalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
    }
}
