package io.metersphere.log.vo;

import lombok.Data;

import java.util.List;

@Data
public class OperatingLogDetails {
    private String sourceId;
    private String projectId;
    private String createUser;
    private String title;
    private List<DetailColumn> columns;

    public OperatingLogDetails() {

    }

    public OperatingLogDetails(String sourceId, String projectId, String createUser, List<DetailColumn> columns) {
        this.sourceId = sourceId;
        this.projectId = projectId;
        this.createUser = createUser;
        this.columns = columns;
    }

    public OperatingLogDetails(String sourceId, String projectId, String title, String createUser, List<DetailColumn> columns) {
        this.sourceId = sourceId;
        this.projectId = projectId;
        this.createUser = createUser;
        this.title = title;
        this.columns = columns;
    }
}
