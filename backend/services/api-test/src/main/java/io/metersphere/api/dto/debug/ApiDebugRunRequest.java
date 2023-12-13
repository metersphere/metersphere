package io.metersphere.api.dto.debug;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiDebugRunRequest {
    private String id;
    private String environmentId;

    @Schema(description = "点击调试时尚未保存的文件ID列表")
    private List<String> tempFileIds;

    private String request;
}
