package com.lxm.module.annotation.module;

import javax.lang.model.element.Element;

public class RouterBean {


    public enum Type{
        ACTIVITY
    }
    private Type type;
    private Element element;
    private Class<?> clazz;
    private String group;
    private String path;
    private RouterBean(Builder builder) {

        this.element = builder.element;
        this.path = builder.path;
        this.group = builder.group;

    }
    private RouterBean(Type type, Class<?> clazz, String path, String group) {
        this.type = type;
        this.clazz = clazz;
        this.path = path;
        this.group = group;
    }

    public static RouterBean create(Type type,Class<?> clazz,String path,String group){
        return new RouterBean(type,clazz,path,group);
    }

    public Type getType() {
        return type;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Element getElement() {
        return element;
    }

    public final static class Builder{
        private Element element;
        private Class<?> clazz;
        private String group;
        private String path;

        public Builder setElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder setClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }
        public RouterBean build(){

            if (path == null || path.length() == 0){
                throw new IllegalArgumentException("path不能为空。如：/app/MainActivity");
            }
            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "group='" + group + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
