package com.lxm.module.api.core;

import java.util.Map;

public interface ARouterLoadGroup {
    Map<String,Class<? extends ARouterLoadPath>> loadGroup();

}
