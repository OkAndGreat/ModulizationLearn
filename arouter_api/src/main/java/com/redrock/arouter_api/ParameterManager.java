package com.redrock.arouter_api;

import android.app.Activity;
import android.util.LruCache;

import com.redrock.arouter_annotation.ParameterGet;

public class ParameterManager {

    private static ParameterManager instance;

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    private final LruCache<String, ParameterGet> cache;

    private ParameterManager() {
        cache = new LruCache<>(100);
    }

    static final String FILE_SUFFIX_NAME = "$$Parameter";


    public void loadParameter(Activity activity) {
        String className = activity.getClass().getName();

        ParameterGet parameterLoad = cache.get(className);

        if (null == parameterLoad) {
            try {
                // 类加载Personal_MainActivity + $$Parameter
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                // 用接口parameterLoad = 接口的实现类Personal_MainActivity
                parameterLoad = (ParameterGet) aClass.newInstance();
                cache.put(className, parameterLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            parameterLoad.getParameter(activity);
        }


    }
}
