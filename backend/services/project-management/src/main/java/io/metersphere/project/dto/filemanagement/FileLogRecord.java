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
    //请求方法 POST/GET
    @NotBlank
    private String requestMethod;
    //触发log记录的请求路径
    @NotBlank
    private String requestUrl;
    //log所属记录模块
    @NotBlank
    private String logModule;
    //log所属项目ID
    @NotBlank
    private String projectId;
}
