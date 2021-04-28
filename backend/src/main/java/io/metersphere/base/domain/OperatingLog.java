package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class OperatingLog implements Serializable {
    private String id;

    private String projectId;

    private String operMethod;

    private String operUser;

    private String sourceId;

    private String operType;

    private String operModule;

    private String operTitle;

    private Long operTime;

    private String operContent;

    private static final long serialVersionUID = 1L;
}