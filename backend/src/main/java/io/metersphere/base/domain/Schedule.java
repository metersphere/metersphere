package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Schedule implements Serializable {
    private String id;

    private String key;

    private String type;

    private String value;

    private String group;

    private String job;

    private Boolean enable;

    private String resourceId;

    private String userId;

    private String customData;

    private static final long serialVersionUID = 1L;
}