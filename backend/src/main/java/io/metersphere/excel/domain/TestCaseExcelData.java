package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class TestCaseExcelData {

    @NotBlank
    @Length(max=1000)
    @ExcelProperty("所属模块")
    @Pattern(regexp = "^(?!.*//).*$", message = "格式不正确")
    private String nodePath;

    @NotBlank
    @Length(max=50)
    @ExcelProperty("用例名称")
    private String name;

    @NotBlank
    @ExcelProperty("用例类型")
    @Pattern(regexp = "(^functional$)|(^performance$)|(^api$)", message = "必须为functional、performance、api")
    private String type;

    @NotBlank
    @ExcelProperty("维护人")
    private String maintainer;

    @NotBlank
    @ExcelProperty("优先级")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "必须为P0、P1、P2、P3")
    private String priority;

    @NotBlank
    @ExcelProperty("测试方式")
    @Pattern(regexp = "(^manual$)|(^auto$)", message = "必须为manual、auto")
    private String method;

    @ExcelProperty("前置条件")
    @Length(min=0, max=1000)
    private String prerequisite;

    @ExcelProperty("备注")
    @Length(max=1000)
    private String remark;

    @ExcelProperty("步骤描述")
    @Length(max=1000)
    private String stepDesc;

    @ExcelProperty("预期结果")
    @Length(max=1000)
    private String stepResult;

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public void setPrerequisite(String prerequisite) {
        this.prerequisite = prerequisite;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStepDesc() {
        return stepDesc;
    }

    public void setStepDesc(String stepDesc) {
        this.stepDesc = stepDesc;
    }

    public String getStepResult() {
        return stepResult;
    }

    public void setStepResult(String stepResult) {
        this.stepResult = stepResult;
    }

}
