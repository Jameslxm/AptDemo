package com.lxm.module.compiler;

import com.google.auto.service.AutoService;
import com.lxm.module.compiler.utils.Constants;

import java.util.Set;
import java.util.logging.Filter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

//用来生成META-INFO/services/javax.annotation.processing.Processor
@AutoService(Processor.class)
//允许/支持的注解类型，让注解类型处理器处理
@SupportedAnnotationTypes({Constants.AROUTER_ANNOTATION_TYPES})
//指定JDK编译的版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ARouterProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        return false;
    }
}
