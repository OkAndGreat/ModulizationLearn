package com.redrock.arouter_compiler;

import com.google.auto.service.AutoService;
import com.redrock.arouter_annotation.Parameter;
import com.redrock.arouter_compiler.utils.ProcessorConfig;
import com.redrock.arouter_compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;


    private final Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (!ProcessorUtils.isEmpty(set)) {
            // ??????????????? @Parameter ????????? ????????????????????????
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);

            if (!ProcessorUtils.isEmpty(elements)) {
                for (Element element : elements) {

                    // ??????????????????????????????
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                    if (tempParameterMap.containsKey(enclosingElement)) {
                        tempParameterMap.get(enclosingElement).add(element);
                    } else {
                        List<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(enclosingElement, fields);
                    }
                }

                // ???????????????????????????????????????
                if (ProcessorUtils.isEmpty(tempParameterMap)) return true;

                TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);

                // ????????????
                // Object targetParameter
                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

                // ?????????????????????????????? @Parameter?????? ??????????????????
                for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
                    TypeElement typeElement = entry.getKey();

                    // ????????????????????????Activity???????????????????????????
                    if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
                        throw new RuntimeException("@Parameter??????????????????Activity?????????");
                    }

                    ClassName className = ClassName.get(typeElement);

                    ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                            .setMessager(messager)
                            .setClassName(className)
                            .build();

                    factory.addFirstStatement();

                    for (Element element : entry.getValue()) {
                        factory.buildStatement(element);
                    }

                    // ????????????????????????????????????$$Parameter??? ?????????Personal_MainActivity$$Parameter
                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE, "APT??????????????????????????????" +
                            className.packageName() + "." + finalClassName);

                    // ??????????????????????????????PersonalMainActivity$$Parameter
                    try {
                        JavaFile.builder(className.packageName(), // ??????
                                TypeSpec.classBuilder(finalClassName) // ??????
                                        .addSuperinterface(ClassName.get(parameterType)) //  implements ParameterGet ??????ParameterLoad??????
                                        .addModifiers(Modifier.PUBLIC) // public?????????
                                        .addMethod(factory.build()) // ?????????????????????????????? + ????????????
                                        .build()) // ???????????????
                                .build() // JavaFile????????????
                                .writeTo(filer); // ????????????????????????????????????
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return false;
    }
}
