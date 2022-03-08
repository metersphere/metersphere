package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UiElement implements Serializable {
    private String id;

    private String moduleId;

    private String projectId;

    private String name;

    private String locationType;

    private String location;

    private String createUser;

    private String versionId;

    private String refId;

    private Boolean latest;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}