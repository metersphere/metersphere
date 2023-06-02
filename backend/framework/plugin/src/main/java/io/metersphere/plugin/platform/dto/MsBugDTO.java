package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MsBugDTO {

    private String id;

    private String title;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String reporter;

    private String lastmodify;

    private String platform;

    private String projectId;

    private String creator;

    private String resourceId;

    private Integer num;

    private String platformStatus;

    private String platformId;

    private String description;

    private String customFields;
}
