package com.lxm.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.lxm.module.annotation.ARouter;
import com.lxm.module.annotation.Parameter;
import com.lxm.module.api.core.ParameterLoad;

@ARouter(path = "/order/OrderActivity")
public class OrderActivity extends AppCompatActivity {
    @Parameter
    int age;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_activity_order);
        ParameterLoad parameterLoad = new OrderActivity$$Parameter();
        parameterLoad.loadParameter(this);
        if(getIntent() != null){
            Log.d("lxm>>>","age:"+age);
        }
    }
}
