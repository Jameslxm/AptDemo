package com.lxm.aptdemo.test;

import com.lxm.module.api.core.ARouterLoadGroup;
import com.lxm.module.api.core.ARouterLoadPath;

import java.util.HashMap;
import java.util.Map;

public class Arouter$$Group$$Order implements ARouterLoadGroup {
    @Override
    public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        Map<String, Class<? extends ARouterLoadPath>> groupMap = new HashMap<>();
        groupMap.put("order",Arouter$$Path$$Order.class);
        return groupMap;
    }
}
