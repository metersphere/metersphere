package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "测试计划执行记录详细内容(拆分成下面4张表)")
@Table("test_plan_execute_record_blob")
@Data
public class TestPlanExecuteRecordBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_execute_record_blob.test_plan_execute_record.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划执行记录ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanExecuteRecord;


    @ApiModelProperty(name = "执行配置")
    private String runConfig;


    @ApiModelProperty(name = "接口用例执行信息")
    private String apiCases;


    @ApiModelProperty(name = "场景执行信息")
    private String scenarios;


    @ApiModelProperty(name = "性能用例执行信息")
    private String loadCases;


    @ApiModelProperty(name = "UI用例执行信息")
    private String uiCases;


}