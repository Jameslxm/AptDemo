package com.lxm.module.compiler.utils;

public class Constants {
    public static final String AROUTER_ANNOTATION_TYPES = "com.lxm.module.annotation.ARouter";
    public static final String PARAMETER_ANNOTATION_TYPES = "com.lxm.module.annotation.Parameter";
    //每个子模块名
    public static final String MODULE_NAME = "moduleName";
    //用于存放APT生成的类文件
    public static final String APT_PACKAGE = "packageNameForAPT";
    public static final String ACTIVITY = "android.app.Activity";

    public static final String BASE_PACKAGE = "com.lxm.module.api";
    public static final String AROUTE_GROUP = BASE_PACKAGE + ".core.ARouterLoadGroup";
    public static final String AROUTE_PATH = BASE_PACKAGE + ".core.ARouterLoadPath";
    public static final String PARAMETER_LOAD = BASE_PACKAGE + ".core.ParameterLoad";
    public static final String PATH_METHOD_NAME = "loadPath";
    public static final String GROUP_METHOD_NAME = "loadGroup";

    public static final String PATH_PARAMETER_NAME = "pathMap";
    public static final String GROUP_PARAMETER_NAME = "grouphMap";
    public static final String PATH_FILE_NAME = "Arouter$$Path$$";
    public static final String GROUP_FILE_NAME = "Arouter$$Group$$";

    public static final String PARAMETER_NAME = "target";
    public static final String PARAMETER_FILE_NAME = "$$Parameter";
    public static final String PARAMETER_METHOD_NAME = "loadParameter";
    public static final String STRING = "java.lang.String";
}
