package com.lxm.module.compiler;

import com.google.auto.service.AutoService;
import com.lxm.module.annotation.Parameter;
import com.lxm.module.compiler.factory.ParameterFactory;
import com.lxm.module.compiler.utils.Constants;
import com.lxm.module.compiler.utils.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
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

//用来生成META-INFO/services/javax.annotation.processing.Processor
@AutoService(Processor.class)
//允许/支持的注解类型，让注解类型处理器处理
@SupportedAnnotationTypes({Constants.PARAMETER_ANNOTATION_TYPES})
//指定JDK编译的版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        if (!EmptyUtils.isEmpty(annotations)) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);
            valueOfParameterMap(elements);
            try {
                createParameterFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private void createParameterFile() throws IOException {
        if (EmptyUtils.isEmpty(tempParameterMap)) return;
        TypeElement parameterType = elementUtils.getTypeElement(Constants.PARAMETER_LOAD);
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, Constants.PARAMETER_NAME).build();
        for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            ClassName className = ClassName.get(typeElement);
            ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                    .setMessager(messager)
                    .setElementUtils(elementUtils)
                    .setTypeUtils(typeUtils)
                    .setClassName(className)
                    .build();
            factory.addFirstStatement();
            for (Element fieldElement : entry.getValue()) {
                factory.buildStatement(fieldElement);
            }
            String finalClassName = typeElement.getSimpleName() + Constants.PARAMETER_FILE_NAME;
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件" + className.packageName()
                    + "." + finalClassName);
            JavaFile.builder(className.packageName(),
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(parameterType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(factory.build())
                            .build())
                    .build()
                    .writeTo(filer);
        }
    }

    private void valueOfParameterMap(Set<? extends Element> elements) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            if (tempParameterMap.containsKey(typeElement)) {
                tempParameterMap.get(typeElement).add(element);
            } else {
                List<Element> fields = new ArrayList<>();
                fields.add(element);
                tempParameterMap.put(typeElement, fields);
            }
        }
    }
}
