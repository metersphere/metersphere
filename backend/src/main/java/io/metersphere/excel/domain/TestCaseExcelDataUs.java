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
public class TestCaseExcelDataUs extends TestCaseExcelData {

//    @ExcelProperty("ID")
//    @NotRequired
//    private Integer num;

    @ColumnWidth(50)
    @ExcelProperty("ID")
    @NotRequired
    private String customNum;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("Name")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ExcelProperty("Module")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("Maintainer")
    private String maintainer;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("Priority")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "{test_case_priority_validate}")
    private String priority;

    @ColumnWidth(50)
    @ExcelProperty("Tag")
    @NotRequired
    @Length(min = 0, max = 1000)
    private String tags;

//    @NotBlank(message = "{cannot_be_null}")
//    @ExcelProperty("Method")
//    @Pattern(regexp = "(^manual$)|(^auto$)", message = "{test_case_method_validate}")
//    private String method;

    @ColumnWidth(50)
    @ExcelProperty("Prerequisite")
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("Remark")
    private String remark;

    @ColumnWidth(50)
    @ExcelProperty("Step description")
    private String stepDesc;

    @ColumnWidth(50)
    @ExcelProperty("Step result")
    private String stepResult;

    @ColumnWidth(50)
    @ExcelProperty("Case status")
    private String status;

    @ColumnWidth(50)
    @ExcelProperty("Edit Model")
    @NotRequired
    @Pattern(regexp = "(^TEXT$)|(^STEP$)", message = "{test_case_step_model_validate}")
    private String stepModel;
}
