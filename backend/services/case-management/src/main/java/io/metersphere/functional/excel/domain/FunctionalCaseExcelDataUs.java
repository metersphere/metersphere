package io.metersphere.functional.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.metersphere.functional.excel.annotation.NotRequired;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Locale;

/**
 * @author wx
 */
@Data
@ColumnWidth(15)
public class FunctionalCaseExcelDataUs extends FunctionalCaseExcelData {

    @ColumnWidth(50)
    @ExcelProperty("ID")
    @NotRequired
    private String num;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("Name")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 100)
    @ExcelProperty("Module")
    @ColumnWidth(30)
    private String module;

    @ColumnWidth(50)
    @ExcelProperty("Tag")
    @NotRequired
    @Length(min = 0, max = 1000)
    private String tags;

    @ColumnWidth(50)
    @ExcelProperty("Prerequisite")
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("Description")
    private String description;

    @ColumnWidth(50)
    @ExcelProperty("Text description")
    private String textDescription;

    @ColumnWidth(50)
    @ExcelProperty("Expected result")
    private String expectedResult;

    @ColumnWidth(50)
    @ExcelProperty("Case edit type")
    @NotRequired
    @Pattern(regexp = "(^TEXT$)|(^STEP$)|(.{0})", message = "{test_case_step_model_validate}")
    private String caseEditType;

    @Override
    public List<List<String>> getHead(List<TemplateCustomFieldDTO> customFields) {
        return super.getHead(customFields, Locale.US);
    }
}
