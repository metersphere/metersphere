package io.metersphere.project.dto.environment;


import lombok.Data;

@Data
public class BodyFile {
    private String id;
    private String name;
    private String fileId;
    private String projectId;
    private String fileType;
    // 正常/已删除
    private String status;
}
