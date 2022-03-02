package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class CustomField implements Serializable {
    private String id;

    private String name;

    private String scene;

    private String type;

    private String remark;

    private Boolean system;

    private Boolean global;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private String projectId;

    private Boolean thirdPart;

    private String options;

    private static final long serialVersionUID = 1L;
}
