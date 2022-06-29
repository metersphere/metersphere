package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UiElement implements Serializable {
    private String id;

    private Integer num;

    private String moduleId;

    private String projectId;

    private String name;

    private String locationType;

    private String location;

    private String createUser;

    private String updateUser;

    private String versionId;

    private String refId;

    private Long order;

    private Boolean latest;

    private String description;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}