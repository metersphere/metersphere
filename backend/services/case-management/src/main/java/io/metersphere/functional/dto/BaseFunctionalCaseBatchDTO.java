package io.metersphere.functional.dto;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class BaseFunctionalCaseBatchDTO extends TableBatchProcessDTO implements Serializable {

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "匹配模式 所有/任一", allowableValues = {"AND", "OR"})
    private String searchMode = "AND";

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "版本来源")
    private String refId;
}
