package io.metersphere.plugin.platform.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class PlatformBugUpdateDTO {

    /**
     * 平台缺陷唯一标识
     */
    private String platformBugKey;
    /**
     * 平台缺陷标题
     */
    private String platformTitle;
    /**
     * 平台缺陷处理人
     */
    private String platformHandleUser;
    /**
     * 平台缺陷状态
     */
    private String platformStatus;
    /**
     * 平台描述
     */
    private String platformDescription;
    /**
     * 平台自定义字段值
     */
    private Map<String, String> platformCustomFieldMap;
}
