package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseAssociationFileRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件id列表")
    private List<String> fileIds;

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String caseId;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
}
