package com.redrock.arouter_compiler;

import com.google.auto.service.AutoService;
import com.redrock.arouter_annotation.ARouter;
import com.redrock.arouter_annotation.RouterBean;
import com.redrock.arouter_compiler.utils.ProcessorConfig;
import com.redrock.arouter_compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 同学们注意：编码此类，记住就是一个字（细心，细心，细心），出了问题debug真的不好调试
 */

// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)

// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({ProcessorConfig.AROUTER_PACKAGE})

// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)

// 注解处理器接收的参数
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})

public class ARouterProcessor extends AbstractProcessor {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    private String aptPackage; // 各个模块传递过来的目录 用于统一存放 apt生成的文件

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    private String options;

    /**
     * 设计一级路由和二级路由是为了检索效率，提高性能
     */
    // Map<"personal", List<RouterBean>>
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();

    // Map<"personal", "ARouter$$Path$$personal.class">
    private Map<String, String> mAllGroupMap = new HashMap<>();

    // 做初始化工作，就相当于 Activity中的 onCreate函数一样的作用
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        // 只有接受到 App壳 传递过来的书籍，才能证明我们的 APT环境搭建完成
        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
        String aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 是否为null...");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的地方呀");
            return false;
        }

        // 获取所有被 @ARouter 注解的 元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        //用来判断是否为activity
        TypeMirror activityMirror = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE).asType();

        // 遍历所有的类节点
        for (Element element : elements) {
            // 获取被@ARetuer注解的类
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>> 被@ARetuer注解的类有：" + className);

            // 拿到注解以获得注解中用户自定义的信息
            ARouter aRouter = element.getAnnotation(ARouter.class);

            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();

            // ARouter注解的类 必须继承 Activity,利用typeMirror进行判断
            TypeMirror elementMirror = element.asType();
            if (typeTool.isSubtype(elementMirror, activityMirror)) { // activityMirror  android.app.Activity描述信息
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY); // 最终证明是 Activity
            } else {
                throw new RuntimeException("@ARouter注解目前仅限用于Activity类之上");
            }

            if (checkRouterPath(routerBean)) {
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());

                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            }
        }//for循环结束后 就对所有用Arouter注释过的类的基本信息做好了检查和记录

        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH); // ARouterPath描述
        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP); // ARouterGroup描述

        //要先创建二级路由再创建一级路由，因为一级路由会使用到二级路由
        try {
            createPathFile(pathType); // 生成 Path类
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH模板时，异常了 e:" + e.getMessage());
        }

        try {
            createGroupFile(groupType, pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成GROUP模板时，异常了 e:" + e.getMessage());
        }

        return false;
    }

    /**
     * 要生成的文件代码
     * public class ARouter$$Path$$personal implements ARouterPath {
     *
     * @param pathType
     * @Override public Map<String, RouterBean> getPathMap() {
     * Map<String, RouterBean> pathMap = new HashMap<>();
     * pathMap.put("/personal/Personal_Main2Activity", RouterBean.create();
     * pathMap.put("/personal/Personal_MainActivity", RouterBean.create());
     * return pathMap;
     * }
     * }
     */
    private void createPathFile(TypeElement pathType) throws IOException {
        // 判断 map仓库中，是否有需要生成的文件
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return;
        }

        // Map<String, RouterBean>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),         // Map
                ClassName.get(String.class),      // Map<String,
                ClassName.get(RouterBean.class)   // Map<String, RouterBean>
        );

        // 遍历组 app,order,personal
        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) { // personal
            // 1.方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class) // 给方法上添加注解  @Override
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .returns(methodReturn) // 把Map<String, RouterBean> 加入方法返回
                    ;

            // Map<String, RouterBean> pathMap = new HashMap<>();变量是$N 类是$T
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),           // Map
                    ClassName.get(String.class),        // Map<String,
                    ClassName.get(RouterBean.class),    // Map<String, RouterBean>
                    ProcessorConfig.PATH_VAR1,          // Map<String, RouterBean> pathMap
                    ClassName.get(HashMap.class)        // Map<String, RouterBean> pathMap = new HashMap<>();
            );

            // 可能存在多个，所以需要循环
            // pathMap.put("/personal/Personal_Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
            // Personal_Main2Activity.class);
            // pathMap.put("/personal/Personal_MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY));
            List<RouterBean> pathList = entry.getValue();

            //$L == TypeEnum.ACTIVITY

            // personal 的细节
            for (RouterBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1, // pathMap.put
                        bean.getPath(), // "/personal/Personal_Main2Activity"
                        ClassName.get(RouterBean.class), // RouterBean
                        ClassName.get(RouterBean.TypeEnum.class), // RouterBean.Type
                        bean.getTypeEnum(), // 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) bean.getElement()), // MainActivity.class Main2Activity.class
                        bean.getPath(), // 路径名
                        bean.getGroup() // 组名
                );
            }

            // return pathMap;
            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            //因为是implements ，所以 方法和类要合为一体生成

            //最终生成的类文件名  ARouter$$Path$$personal
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    aptPackage + "." + finalClassName);

            // 生成类文件：ARouter$$Path$$personal
            JavaFile.builder(aptPackage, // 包名  APT 存放的路径
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(pathType)) // 实现ARouterLoadPath接口  implements ARouterPath==pathType
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件

            // 仓库二 缓存二  非常重要一步，注意：PATH 路径文件生成出来了，才能赋值路由组mAllGroupMap
            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
    }

    /**
     * 要生成的文件代码
     * public class ARouter$$Group$$personal implements ARouterGroup {
     *
     * @param groupType
     * @param pathType
     * @Override public Map<String, Class<? extends ARouterPath>> getGroupMap() {
     * Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
     * groupMap.put("personal", ARouter$$Path$$personal.class);
     * return groupMap;
     * }
     * }
     */
    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) return;

        //Map<String, Class<? extends ARouterPath>>
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),        // Map
                ClassName.get(String.class),    // Map<String,

                // Class<? extends ARouterPath>>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        // ? extends ARouterPath
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))) // ? extends ARouterLoadPath
        );

        // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME) // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(methodReturns); // 方法返回值

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),

                // Class<? extends ARouterPath> 难度
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        //  groupMap.put("personal", ARouter$$Path$$personal.class);
        //	groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(), // order, personal ,app
                    ClassName.get(aptPackage, entry.getValue()));
        }

        // return groupMap;
        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        // 最终生成的类文件名 ARouter$$Group$$ + personal
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        JavaFile.builder(aptPackage, // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(groupType)) // 实现ARouterLoadGroup接口 implements ARouterGroup
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件
    }

    //检查用户自定义的路由路径是否正确
    private boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup(); //如 "app"   "order"   "personal"
        String path = bean.getPath();  //如 "/app/MainActivity"   "/order/Order_MainActivity"

        // @ARouter注解中的path值，必须要以 / 开头
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        // @ARouter注解中的group有赋值情况
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }

        // 如果没有问题，返回true
        return true;
    }

}
