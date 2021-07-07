package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.metersphere.excel.annotation.NotRequired;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ColumnWidth(15)
public class TestCaseExcelDataCn extends TestCaseExcelData {

//    @ExcelProperty("ID")
//    @NotRequired
//    private Integer num;

    @ColumnWidth(50)
    @ExcelProperty("ID")
    @NotRequired
    private String customNum;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("用例名称")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ExcelProperty("所属模块")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty(value = "责任人")
    private String maintainer;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("用例等级")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "{test_case_priority_validate}")
    private String priority;

    @ColumnWidth(50)
    @ExcelProperty("标签")
    @NotRequired
    @Length(min = 0, max = 1000)
    private String tags;

//    @NotBlank(message = "{cannot_be_null}")
//    @ExcelProperty("测试方式")
//    @Pattern(regexp = "(^manual$)|(^auto$)", message = "{test_case_method_validate}")
//    private String method;

    @ColumnWidth(50)
    @ExcelProperty("前置条件")
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("备注")
    private String remark;

    @ColumnWidth(50)
    @ExcelProperty("步骤描述")
    private String stepDesc;

    @ColumnWidth(50)
    @ExcelProperty("预期结果")
    private String stepResult;

    @ColumnWidth(50)
    @ExcelProperty("用例状态")
    private String status;


    @ColumnWidth(50)
    @ExcelProperty("编辑模式")
    @NotRequired
    @Pattern(regexp = "(^TEXT$)|(^STEP$)", message = "{test_case_step_model_validate}")
    private String stepModel;
}
