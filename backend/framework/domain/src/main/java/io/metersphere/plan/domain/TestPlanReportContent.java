package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "测试计划报告内容")
@Table("test_plan_report_content")
@Data
public class TestPlanReportContent implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_report_content.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_report_content.test_plan_report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report_content.test_plan_report_id.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划报告ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String testPlanReportId;


    @Schema(title = "总结", allowableValues = "range[1, 2000]")
    private String summary;


    @Schema(title = "报告内容")
    private byte[] content;


}