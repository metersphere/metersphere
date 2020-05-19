package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@ColumnWidth(15)
public class TestCaseExcelData {

    @NotBlank
    @Length(max=50)
    @ExcelProperty("{test_case_name}")
    private String name;

    @NotBlank
    @Length(max=1000)
    @ExcelProperty("{test_case_module}")
    @ColumnWidth(30)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @NotBlank
    @ExcelProperty("{test_case_type}")
    @Pattern(regexp = "(^functional$)|(^performance$)|(^api$)", message = "{test_case_type_validate}")
    private String type;

    @NotBlank
    @ExcelProperty("{test_case_maintainer}")
    private String maintainer;

    @NotBlank
    @ExcelProperty("{test_case_priority}")
    @Pattern(regexp = "(^P0$)|(^P1$)|(^P2$)|(^P3$)", message = "{test_case_priority_validate}")
    private String priority;

    @NotBlank
    @ExcelProperty("{test_case_method}")
    @Pattern(regexp = "(^manual$)|(^auto$)", message = "{test_case_method_validate}")
    private String method;

    @ColumnWidth(50)
    @ExcelProperty("{test_case_prerequisite}")
    @Length(min=0, max=1000)
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("{test_case_remark}")
    @Length(max=1000)
    private String remark;

    @ColumnWidth(50)
    @ExcelProperty("{test_case_step_desc}")
    @Length(max=1000)
    private String stepDesc;

    @ColumnWidth(50)
    @ExcelProperty("{test_case_step_result}")
    @Length(max=1000)
    private String stepResult;
}
