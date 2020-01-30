package com.lxm.module.compiler.factory;

import com.lxm.module.annotation.Parameter;
import com.lxm.module.compiler.utils.Constants;
import com.lxm.module.compiler.utils.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.lang.reflect.Type;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ParameterFactory {
    private static final String CONTENT = "$T t = ($T)target";
    private MethodSpec.Builder methodBuilder;
    private Messager messager;
    private Types typeUtils;
    private ClassName className;

    public ParameterFactory(Builder builder) {
        messager = builder.messager;
        className = builder.className;
        typeUtils = builder.typeUtils;

        methodBuilder = MethodSpec.methodBuilder(Constants.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }

    public void addFirstStatement(){
        methodBuilder.addStatement(CONTENT,className,className);
    }
    public MethodSpec build(){
        return methodBuilder.build();
    }
    public void buildStatement(Element element){
        TypeMirror typeMirror = element.asType();
        //获取TypeKind枚举类型的序列号
        int type = typeMirror.getKind().ordinal();
        //获取属性名
        String fieldName = element.getSimpleName().toString();
        //获取注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();
        annotationValue = EmptyUtils.isEmpty(annotationValue) ? fieldName : annotationValue;
        //最终拼接的前缀
        String finalValue = "t." + annotationValue;
        //t.s = getIntent().
        String methodContent = finalValue + " = t.getIntent().";
        if(type == TypeKind.INT.ordinal()){
            methodContent += "getIntExtra($S,"+finalValue+")";
        }else if(type == TypeKind.BOOLEAN.ordinal()){
            methodContent += "getBooleanExtra($S," + finalValue + ")";
        }else{
            if(typeMirror.toString().equalsIgnoreCase(Constants.STRING)){
                methodContent += "getStringExtra($S)";
            }
        }
        if(methodContent.endsWith(")")){
            methodBuilder.addStatement(methodContent,annotationValue);
        }else {
            messager.printMessage(Diagnostic.Kind.ERROR,"目前暂支持String、int、boolean传参");
        }
    }
    public static class Builder{
        private Messager messager;
        private Elements elementUtils;
        private Types typeUtils;
        private ClassName className;
        private ParameterSpec parameterSpec;
        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setElementUtils(Elements elementUtils) {
            this.elementUtils = elementUtils;
            return this;
        }

        public Builder setTypeUtils(Types typeUtils) {
            this.typeUtils = typeUtils;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
            return;
        }

        public ParameterFactory build() {

            if(parameterSpec == null){
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }
            if (className == null){
                throw new IllegalArgumentException("方法内容中className为空");
            }
            if(messager == null){
                throw new IllegalArgumentException("messager为空，messager用来报告错误、警告和其他显示的信息");
            }
            return new ParameterFactory(this);
        }
    }
}
