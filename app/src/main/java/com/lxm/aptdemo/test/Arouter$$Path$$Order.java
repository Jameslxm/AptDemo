package com.lxm.aptdemo.test;

import com.lxm.module.annotation.module.RouterBean;
import com.lxm.module.api.core.ARouterLoadPath;
import com.lxm.order.OrderActivity;

import java.util.HashMap;
import java.util.Map;

public class Arouter$$Path$$Order implements ARouterLoadPath {
    @Override
    public Map<String, RouterBean> loadPath() {
        Map<String, RouterBean> pathMap = new HashMap<>();
        pathMap.put("/order/OrderActivity",RouterBean.create(RouterBean.Type.ACTIVITY,
                OrderActivity.class,"/order/OrderActivity","order"
                ));
        return pathMap;
    }
}
