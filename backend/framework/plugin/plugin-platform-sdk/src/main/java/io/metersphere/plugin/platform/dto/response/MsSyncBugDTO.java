package io.metersphere.plugin.platform.dto.response;

import lombok.Data;

@Data
public class MsSyncBugDTO {

    /**
     * 缺陷ID
     */
    private String id;

    /**
     * 缺陷业务ID
     */
    private Integer num;

    /**
     * 缺陷标题
     */
    private String title;

    /**
     * 缺陷状态
     */
    private String status;

    /**
     * 缺陷处理人
     */
    private String handleUser;

    /**
     * 缺陷处理人集合
     */
    private String handleUsers;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 删除人
     */
    private String deleteUser;

    /**
     * 删除时间
     */
    private Long deleteTime;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 所属项目
     */
    private String projectId;

    /**
     * 所属平台
     */
    private String platform;

    /**
     * 第三方平台缺陷ID
     */
    private String platformBugId;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 缺陷描述
     */
    private String description;


    /**
     * 缺陷位置
     */
    private Long pos;
}
