package com.lxm.module.api;

import com.lxm.module.annotation.module.RouterBean;

import java.util.Map;

public interface ARouterLoadPath {
    Map<String, RouterBean> loadPath();

}
