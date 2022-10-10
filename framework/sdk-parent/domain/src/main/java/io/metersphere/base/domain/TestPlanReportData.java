package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanReportData implements Serializable {
    private String id;

    private String testPlanReportId;

    private static final long serialVersionUID = 1L;
}