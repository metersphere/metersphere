package io.metersphere.base.domain;

import java.io.Serializable;

public class LoadTestReportResult implements Serializable {
    private Long id;

    private String reportId;

    private String reportKey;

    private String reportValue;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId == null ? null : reportId.trim();
    }

    public String getReportKey() {
        return reportKey;
    }

    public void setReportKey(String reportKey) {
        this.reportKey = reportKey == null ? null : reportKey.trim();
    }

    public String getReportValue() {
        return reportValue;
    }

    public void setReportValue(String reportValue) {
        this.reportValue = reportValue == null ? null : reportValue.trim();
    }
}