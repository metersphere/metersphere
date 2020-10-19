package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Notice implements Serializable {
    private String id;

    private String event;

    private String testId;

    private String enable;

    private String type;

    private String userId;

    private static final long serialVersionUID = 1L;
}