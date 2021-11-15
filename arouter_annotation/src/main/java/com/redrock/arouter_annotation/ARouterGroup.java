package com.redrock.arouter_annotation;

import java.util.Map;

public interface ARouterGroup {

    Map<String, Class<? extends ARouterPath>> getGroupMap();

}
