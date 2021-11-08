package com.redrock.arouter_compiler;

import com.google.auto.service.AutoService;
import com.redrock.arouter_annotation.ARouter;
import com.redrock.arouter_compiler.utils.ProcessorConfig;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    private String options;

    // 做初始化工作，就相当于 Activity中的 onCreate函数一样的作用
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        // 只有接受到 App壳 传递过来的书籍，才能证明我们的 APT环境搭建完成
        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        String aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
        }
    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     *
     * @param set              使用了支持处理注解的节点集合
     * @param roundEnvironment 当前或是之前的运行环境,可以通过该对象查找的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的地方呀");
            return false;
        }

        // 获取所有被 @ARouter 注解的 元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        // 遍历所有的类节点
        for (Element element : elements) {
            // 获取类节点，获取包节点 （com.xiangxue.xxxxxx）
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();

            // 获取简单类名，例如：MainActivity
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>> 被@ARetuer注解的类有：" + className); // 打印出 就证明APT没有问题

            // 下面是练习 JavaPoet

            /**
             * package com.example.helloworld;
             *
             * public final class HelloWorld {
             *   public static void main(String[] args) {
             *     System.out.println("Hello, JavaPoet!");
             *   }
             * }
             */

            // 下毒，现在是 无法生成 【作业】

            // 1.方法
            /*MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                    .build();

            // 2.类
            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(mainMethod)
                    .build();

            // 3.包
            JavaFile packagef = JavaFile.builder("com.derry.study", helloWorld).build();

            // 去生成
            try {
                packagef.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "生成失败，请检查代码...");
            }*/

            // 先JavaPoet 写一个简单示例，方法--->类--> 包，是倒序写的思路哦
            /*
             package com.example.helloworld;

             public final class HelloWorld {

                 public static void main(String[] args) {
                    System.out.println("Hello, JavaPoet!");
                 }

             }

             */
            // 方法
            MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(void.class)
                    .addParameter(System[].class, "args")
                    // 增加main方法里面的内容
                    .addStatement("$T.out.println($S)", System.class, "AAAAAAAAAAA!")
                    .build();
            // 类
            TypeSpec testClass = TypeSpec.classBuilder("Test")
                    .addMethod(mainMethod)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .build();

            // 包
            // Program type already present: com.xiangxue.test22.Test
            JavaFile packagef = JavaFile.builder("com.xiangxue.test22", testClass).build();

            try {
                packagef.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "生成Test文件时失败，异常:" + e.getMessage());
            }
        }
        return false;
    }
}
