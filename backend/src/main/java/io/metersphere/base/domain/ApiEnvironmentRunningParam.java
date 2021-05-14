package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiEnvironmentRunningParam implements Serializable {
    private String id;

    private String apiEnviromentId;

    private String key;

    private Long createTime;

    private Long updateTime;

    private String createUserId;

    private String updateUserId;

    private String value;

    private static final long serialVersionUID = 1L;
}