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
public class FunctionalCaseExcelDataCn extends FunctionalCaseExcelData {

    @ColumnWidth(50)
    @ExcelProperty("ID")
    @NotRequired
    private String num;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("用例名称")
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 100)
    @ExcelProperty("所属模块")
    @ColumnWidth(30)
    private String module;

    @ColumnWidth(50)
    @ExcelProperty("标签")
    @NotRequired
    @Length(min = 0, max = 1000)
    private String tags;

    @ColumnWidth(50)
    @ExcelProperty("前置条件")
    private String prerequisite;

    @ColumnWidth(50)
    @ExcelProperty("备注")
    private String description;

    @ColumnWidth(50)
    @ExcelProperty("步骤描述")
    private String textDescription;

    @ColumnWidth(50)
    @ExcelProperty("预期结果")
    private String expectedResult;

    @ColumnWidth(50)
    @ExcelProperty("编辑模式")
    @NotRequired
    @Pattern(regexp = "(^TEXT$)|(^STEP$)|(.{0})", message = "{test_case_step_model_validate}")
    private String caseEditType;

    @Override
    public List<List<String>> getHead(List<TemplateCustomFieldDTO> customFields) {
        return super.getHead(customFields, Locale.SIMPLIFIED_CHINESE);
    }
}
