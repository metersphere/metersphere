package io.metersphere.api.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "API/CASE执行结果详情")
@Table("api_report_blob")
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiReportBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_report_blob.api_report_id.not_blank}", groups = {Updated.class})
    @Schema(title = "接口报告fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Schema(title = "结果内容详情", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] content;

    @Schema(title = "执行环境配置", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] config;

    @Schema(title = "执行过程日志", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] console;

}