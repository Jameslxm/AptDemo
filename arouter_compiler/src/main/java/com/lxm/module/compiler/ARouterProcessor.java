package com.lxm.module.compiler;

import com.google.auto.service.AutoService;
import com.lxm.module.annotation.ARouter;
import com.lxm.module.annotation.module.RouterBean;
import com.lxm.module.compiler.utils.Constants;
import com.lxm.module.compiler.utils.EmptyUtils;
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

//用来生成META-INFO/services/javax.annotation.processing.Processor
@AutoService(Processor.class)
//允许/支持的注解类型，让注解类型处理器处理
@SupportedAnnotationTypes({Constants.AROUTER_ANNOTATION_TYPES})
//指定JDK编译的版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
//接收build.gradle传过来的参数
@SupportedOptions({Constants.MODULE_NAME, Constants.APT_PACKAGE})
public class ARouterProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    //子模块名，如app/order/personal。需要拼接类名时用到（必传）ARouter$$Group$$order
    private String moduleName;
    //包名，用于存放APT生成的类文件
    private String packageNameForAPT;
    private Map<String, List<RouterBean>> tempPathMap = new HashMap<>();
    private Map<String, String> tempGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        Map<String, String> options = processingEnv.getOptions();
        if (!EmptyUtils.isEmpty(options)) {
            moduleName = options.get(Constants.MODULE_NAME);
            packageNameForAPT = options.get(Constants.APT_PACKAGE);
            messager.printMessage(Diagnostic.Kind.NOTE, "moduleName>>>" + moduleName);
            messager.printMessage(Diagnostic.Kind.NOTE, "packageNameForAPT>>>" + packageNameForAPT);
        }
        if (EmptyUtils.isEmpty(moduleName) || EmptyUtils.isEmpty(packageNameForAPT)) {
            throw new RuntimeException("注解处理器需要的参数moduleName或pacakgeNameForAPT为空，请在对应的build.gradle配置参数");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!EmptyUtils.isEmpty(annotations)) {
            //获取所有被@ARouter注解的元素集合
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
            parseElements(elements);
            return true;
        }
        return false;
    }

    private void parseElements(Set<? extends Element> elements) {
        TypeElement activityElement = elementUtils.getTypeElement(Constants.ACTIVITY);
        TypeMirror activityMirror = activityElement.asType();
        //获取每个元素类的信息
        for (Element element : elements) {
            TypeMirror elementMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "遍历的元素信息为：" + elementMirror.toString());

            ARouter aRouter = element.getAnnotation(ARouter.class);
            //封装到实体类
            RouterBean bean = new RouterBean.Builder()
                    .setGroup(aRouter.group())
                    .setPath(aRouter.path())
                    .setElement(element)
                    .build();

            //注解只能写在activity之上
            if (typeUtils.isSubtype(elementMirror, activityMirror)) {
                bean.setType(RouterBean.Type.ACTIVITY);
            } else {
                throw new RuntimeException("@ARouter注解目前仅限用于Activity之上");
            }
            //赋值临时map，存储以上信息
            valueOfPathMap(bean);

        }

        TypeElement groupLoadType = elementUtils.getTypeElement(Constants.AROUTE_GROUP);
        TypeElement pathLoadType = elementUtils.getTypeElement(Constants.AROUTE_PATH);
        //生成路由的详情path文件
        try {
            createPathFile(pathLoadType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //生成路由组Group文件
        try {
            createGroupFile(groupLoadType,pathLoadType);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createPathFile(TypeElement pathLoadType) throws IOException {

        //方法的返回值Map<String, RouterBean>

        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );
        //遍历分组,每个分组创建一个路径类文件。
        for (Map.Entry<String, List<RouterBean>> entry : tempPathMap.entrySet()) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturns);

            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    Constants.PATH_PARAMETER_NAME,
                    HashMap.class);
            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        Constants.PATH_PARAMETER_NAME,
                        bean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.Type.class),
                        bean.getType(),
                        ClassName.get((TypeElement) bean.getElement()),
                        bean.getPath(),
                        bean.getGroup());
            }
            methodBuilder.addStatement("return $N", Constants.PATH_PARAMETER_NAME);
            String finalClassName = Constants.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path文件为：" + packageNameForAPT + "." + finalClassName);
            JavaFile.builder(packageNameForAPT,
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathLoadType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodBuilder.build())
                            .build()
            ).build()
                    .writeTo(filer);
            tempGroupMap.put(entry.getKey(), finalClassName);
        }

    }

    private void createGroupFile(TypeElement groupLoadType, TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(tempGroupMap) || EmptyUtils.isEmpty(tempPathMap)) return;
        //返回值
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType)))
        );
        //public Map<String, Class<? extends ARouterLoadPath>> loadGroup() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);

        //Map<String, Class<? extends ARouterLoadPath>> groupMap = new HashMap<>();
        methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))),
                Constants.GROUP_PARAMETER_NAME,
                HashMap.class);

        //内容配置
        for (Map.Entry<String, String> entry : tempGroupMap.entrySet()) {
            //groupMap.put("order",Arouter$$Path$$Order.class);
            methodBuilder.addStatement("$N.put($S,$T.class)",
                    Constants.GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(packageNameForAPT, entry.getValue()));

        }
        //return groupMap;
        methodBuilder.addStatement("return $N", Constants.GROUP_PARAMETER_NAME);


        String finalClassName = Constants.GROUP_FILE_NAME + moduleName;
        //生成文件

        JavaFile.builder(packageNameForAPT,
                TypeSpec.classBuilder(finalClassName)
                        .addSuperinterface(ClassName.get(groupLoadType))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodBuilder.build())
                        .build()
        )
                .build()
                .writeTo(filer);

    }

    /**
     * tempMap中没有添加，有，不添加
     *
     * @param bean
     */
    private void valueOfPathMap(RouterBean bean) {
        if (checkRouterPath(bean)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean >>>" + bean.toString());
            List<RouterBean> routerBeans = tempPathMap.get(bean.getGroup());
            messager.printMessage(Diagnostic.Kind.NOTE, "====>1");
            if (EmptyUtils.isEmpty(routerBeans)) {
                routerBeans = new ArrayList<>();
                routerBeans.add(bean);
                tempPathMap.put(bean.getGroup(), routerBeans);

            } else {
                for (RouterBean routerBean : routerBeans) {
                    if (!bean.getPath().equalsIgnoreCase(routerBean.getPath())) {
                        routerBeans.add(bean);
                        return;
                    }
                }

            }
            messager.printMessage(Diagnostic.Kind.NOTE, "====>2");

        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
        }


    }

    private boolean checkRouterPath(RouterBean bean) {
        String path = bean.getPath();
        String group = bean.getGroup();
        if (EmptyUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;
        }
        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;
        }
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        if (finalGroup.contains("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范，如：/app/MainActivity");
            return false;
        }
        if (!EmptyUtils.isEmpty(group) && !group.equals(moduleName)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中group值必须和当前子模块名x相同");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }
        return true;
    }
}
