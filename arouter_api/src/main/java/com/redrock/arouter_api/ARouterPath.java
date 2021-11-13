package com.redrock.arouter_api;


import com.redrock.arouter_annotation.RouterBean;

import java.util.Map;

public interface ARouterPath {

    Map<String, RouterBean> getPathMap();

}
