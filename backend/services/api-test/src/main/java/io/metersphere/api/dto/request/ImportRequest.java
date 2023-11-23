package io.metersphere.api.dto.request;

import lombok.Data;

@Data
public class ImportRequest {
    private String id;
    private String name;
    private String moduleId;
    private String environmentId;
    private String projectId;
    private String platform;
    private String swaggerUrl;

    //导入策略
    private String modeId;
    private String userId;
    private String versionId; // 新导入选择的版本
    private String updateVersionId; // 覆盖导入已存在的接口选择的版本
    private String defaultVersion;
    //调用类型
    private String type;
    // 是否开启自定义ID
    private Boolean openCustomNum = false;
    // 是否覆盖模块
    private Boolean coverModule;
    // 当前协议
    private String protocol;
    //上传文件来源，目前用于辨别是否是idea插件
    private String origin;
}
