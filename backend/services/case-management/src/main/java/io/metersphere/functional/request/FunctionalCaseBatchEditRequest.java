package io.metersphere.functional.request;

import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCaseBatchEditRequest extends TableBatchProcessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "是否追加", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean append;

    @Schema(description = "标签")
    private List<String> tags;


    @Schema(description = "自定义字段")
    private CaseCustomFieldDTO customField;
}