package io.metersphere.project.dto;

import io.metersphere.sdk.util.Pager;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FileTableResult {
    @Schema(description = "表格数据")
    Pager<List<FileInformationDTO>> tableData;

    @Schema(description = "模块统计")
    Map<String, Long> moduleCount = new HashMap<>();
}
