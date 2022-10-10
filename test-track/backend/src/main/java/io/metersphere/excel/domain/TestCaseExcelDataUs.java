package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.excel.annotation.NotRequired;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Locale;


@Data
@ColumnWidth(15)
public class TestCaseExcelDataUs extends TestCaseExcelData {

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

    @ColumnWidth(50)
    @ExcelProperty("Tag")
    @NotRequired
    @Length(min = 0, max = 1000)
    private String tags;

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
    @ExcelProperty("Edit Model")
    @NotRequired
    @Pattern(regexp = "(^TEXT$)|(^STEP$)|(.{0})", message = "{test_case_step_model_validate}")
    private String stepModel;

    @Override
    public List<List<String>> getHead(boolean needNum, List<CustomFieldDao> customFields) {
        return super.getHead(needNum, customFields, Locale.US);
    }
}
