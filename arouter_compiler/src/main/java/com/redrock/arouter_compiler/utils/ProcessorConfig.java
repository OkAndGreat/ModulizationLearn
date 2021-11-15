package com.redrock.arouter_compiler.utils;

public interface ProcessorConfig {

    // @ARouter注解 的 包名 + 类名
    String AROUTER_PACKAGE =  "com.redrock.arouter_annotation.ARouter";

    // 接收参数的TAG标记
    String OPTIONS = "moduleName"; // 同学们：目的是接收 每个module名称
    String APT_PACKAGE = "packageNameForAPT"; // 同学们：目的是接收 包名（APT 存放的包名）

    // Activity全类名
    String ACTIVITY_PACKAGE = "android.app.Activity";

    // ARouter api 包名
    String AROUTER_API_PACKAGE = "com.redrock.arouter_api";

    //Arouter annotation 包名
    String AROUTER_ANNOTATION_PACKAGE = "com.redrock.arouter_annotation";

    //  ARouterGroup 高层标准
    String AROUTER_API_GROUP = AROUTER_ANNOTATION_PACKAGE + ".ARouterGroup";

    //  ARouterPath 高层标准
    String AROUTER_API_PATH = AROUTER_ANNOTATION_PACKAGE + ".ARouterPath";

    // 路由组，中的 Path 里面的 方法名
    String PATH_METHOD_NAME = "getPathMap";

    // 路由组，中的 Group 里面的 方法名
    String GROUP_METHOD_NAME = "getGroupMap";

    // 路由组，中的 Path 里面 的 变量名 1
    String PATH_VAR1 = "pathMap";

    // 路由组，中的 Group 里面 的 变量名 1
    String GROUP_VAR1 = "groupMap";

    // 路由组，PATH 最终要生成的 文件名
    String PATH_FILE_NAME = "ARouter$$Path$$";

    // 路由组，GROUP 最终要生成的 文件名
    String GROUP_FILE_NAME = "ARouter$$Group$$";
}
