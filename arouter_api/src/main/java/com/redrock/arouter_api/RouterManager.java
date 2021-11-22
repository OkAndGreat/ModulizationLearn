package com.redrock.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LruCache;

import com.redrock.arouter_annotation.ARouterGroup;
import com.redrock.arouter_annotation.ARouterPath;
import com.redrock.arouter_annotation.RouterBean;


public class RouterManager {

    private String group;
    private String path;

    private static RouterManager instance;

    //单例模式 双重检查
    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    private final LruCache<String, ARouterGroup> groupLruCache;
    private final LruCache<String, ARouterPath> pathLruCache;

    private final static String FILE_GROUP_NAME = "ARouter$$Group$$";

    private RouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }

    public BundleManager build(String path) {

        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(path) || !path.startsWith("/") || path.lastIndexOf("/") == 0 || TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("path写法不正确，正确写法：如 /order/Order_MainActivity");
        }

        this.path = path;
        this.group = finalGroup;

        return new BundleManager(); // Builder设计模式 之前是写里面的， 现在写外面吧
    }

    public void navigation(Context context, BundleManager bundleManager) {
        String groupClassName = context.getPackageName() + "." + FILE_GROUP_NAME + group;

        try {
            ARouterGroup loadGroup = groupLruCache.get(group);

            if (null == loadGroup) {
                Class<?> aClass = Class.forName(groupClassName);
                loadGroup = (ARouterGroup) aClass.newInstance();
                groupLruCache.put(group, loadGroup);
            }

            ARouterPath loadPath = pathLruCache.get(path);
            if (null == loadPath) {
                Class<? extends ARouterPath> clazz = loadGroup.getGroupMap().get(group);

                loadPath = clazz.newInstance();

                pathLruCache.put(path, loadPath);
            }

            RouterBean routerBean = loadPath.getPathMap().get(path);

            if (routerBean != null) {
                if (routerBean.getTypeEnum() == RouterBean.TypeEnum.ACTIVITY) {
                    Intent intent = new Intent(context, routerBean.getMyClass());
                    //在这里利用bundleManager传递参数
                    intent.putExtras(bundleManager.getBundle());
                    context.startActivity(intent, bundleManager.getBundle());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
