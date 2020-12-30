package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestCase implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String priority;

    private String apiDefinitionId;

    private String createUserId;

    private String updateUserId;

    private Long createTime;

    private Long updateTime;

    private Integer num;

    private static final long serialVersionUID = 1L;
}