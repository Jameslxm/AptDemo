package com.lxm.aptdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lxm.aptdemo.test.Arouter$$Group$$Order;
import com.lxm.module.annotation.ARouter;
import com.lxm.module.annotation.module.RouterBean;
import com.lxm.module.api.ARouterLoadGroup;
import com.lxm.module.api.ARouterLoadPath;

import java.util.Map;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpOrder(View view) {
        ARouterLoadGroup loadGroup = new Arouter$$Group$$Order();
        Map<String, Class<? extends ARouterLoadPath>> groupMap = loadGroup.loadGroup();
        Class<? extends ARouterLoadPath> clazz = groupMap.get("order");
        try {
            ARouterLoadPath path = clazz.newInstance();
            Map<String, RouterBean> pathMap = path.loadPath();
            RouterBean routerBean = pathMap.get("/order/OrderActivity");
            if(routerBean != null) {
                Intent intent = new Intent(this, routerBean.getClazz());
                intent.putExtra("name", "lxm");
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
