package io.metersphere.functional.dto;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class BaseFunctionalCaseBatchDTO extends TableBatchProcessDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块id")
    private List<String> moduleIds;

    @Schema(description = "版本id")
    private String versionId;

    @Schema(description = "版本来源")
    private String refId;
}
