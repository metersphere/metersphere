package io.metersphere.commons.utils;

public class MybatisInterceptorConfig {
    private String modelName;
    private String attrName;
    private String attrNameForList;
    private String interceptorClass;
    private String interceptorMethod;
    private String undoClass;
    private String undoMethod;


    public MybatisInterceptorConfig() {
    }

    /**
     * 用时需谨慎！！！！！
     * 主要配置多个的时候，参数少一点
     *
     * @param modelName
     * @param attrName
     */
    public MybatisInterceptorConfig(String modelName, String attrName) {
        this.modelName = modelName;
        this.attrName = attrName;
        this.interceptorClass = "io.metersphere.commons.utils.EncryptUtils";
        this.interceptorMethod = "aesEncrypt";
        this.undoClass = "io.metersphere.commons.utils.EncryptUtils";
        this.undoMethod = "aesDecrypt";
    }

    public MybatisInterceptorConfig(String modelName, String attrName, String attrNameForList) {
        this.modelName = modelName;
        this.attrName = attrName;
        this.attrNameForList = attrNameForList;
        this.interceptorClass = "io.metersphere.commons.utils.EncryptUtils";
        this.interceptorMethod = "aesEncrypt";
        this.undoClass = "io.metersphere.commons.utils.EncryptUtils";
        this.undoMethod = "aesDecrypt";
    }

    public MybatisInterceptorConfig(String modelName, String attrName, String interceptorClass, String interceptorMethod, String undoMethod) {
        this.modelName = modelName;
        this.attrName = attrName;
        this.interceptorClass = interceptorClass;
        this.interceptorMethod = interceptorMethod;
        this.undoClass = interceptorClass;
        this.undoMethod = undoMethod;
    }

    public MybatisInterceptorConfig(String modelName, String attrName, String attrNameForList, String interceptorClass, String interceptorMethod, String undoMethod) {
        this.modelName = modelName;
        this.attrName = attrName;
        this.attrNameForList = attrNameForList;
        this.interceptorClass = interceptorClass;
        this.interceptorMethod = interceptorMethod;
        this.undoClass = interceptorClass;
        this.undoMethod = undoMethod;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrNameForList() {
        return attrNameForList;
    }

    public void setAttrNameForList(String attrNameForList) {
        this.attrNameForList = attrNameForList;
    }

    public String getInterceptorMethod() {
        return interceptorMethod;
    }

    public void setInterceptorMethod(String interceptorMethod) {
        this.interceptorMethod = interceptorMethod;
    }

    public String getUndoMethod() {
        return undoMethod;
    }

    public void setUndoMethod(String undoMethod) {
        this.undoMethod = undoMethod;
    }

    public String getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

    public String getUndoClass() {
        return undoClass;
    }

    public void setUndoClass(String undoClass) {
        this.undoClass = undoClass;
    }
}
