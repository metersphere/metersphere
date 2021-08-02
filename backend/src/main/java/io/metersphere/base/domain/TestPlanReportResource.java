package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanReportResource implements Serializable {
    private String id;

    private String testPlanReportId;

    private String resourceId;

    private String resourceType;

    private String executeResult;

    private static final long serialVersionUID = 1L;
}