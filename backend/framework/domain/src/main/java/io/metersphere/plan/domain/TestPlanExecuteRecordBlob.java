package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "测试计划执行记录详细内容(拆分成下面4张表)")
@Table("test_plan_execute_record_blob")
@Data
public class TestPlanExecuteRecordBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_execute_record_blob.test_plan_execute_record.not_blank}", groups = {Updated.class})
    @Schema(title = "测试计划执行记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPlanExecuteRecord;


    @Schema(title = "执行配置")
    private String runConfig;


    @Schema(title = "接口用例执行信息")
    private String apiCases;


    @Schema(title = "场景执行信息")
    private String scenarios;


    @Schema(title = "性能用例执行信息")
    private String loadCases;


    @Schema(title = "UI用例执行信息")
    private String uiCases;


}