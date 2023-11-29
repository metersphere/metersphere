package io.metersphere.project.dto.filemanagement;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FileLogRecord {
    //操作人
    @NotBlank
    private String operator;
    //log所属记录模块
    @NotBlank
    private String logModule;
    //log所属项目ID
    @NotBlank
    private String projectId;
}
