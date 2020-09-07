package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Notice implements Serializable {
    private String event;

    private String testId;

    private String name;

    private String email;

    private String enable;

    private static final long serialVersionUID = 1L;
}