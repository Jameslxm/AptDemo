package com.lxm.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lxm.module.annotation.ARouter;

@ARouter(path = "/order/OrderActivity")
public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_order);
    }
}
