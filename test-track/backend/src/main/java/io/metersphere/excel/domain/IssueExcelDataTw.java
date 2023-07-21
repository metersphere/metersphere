package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.request.issues.IssueExportRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Locale;

@Data
public class IssueExcelDataTw extends IssueExcelData{

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ColumnWidth(100)
    @ExcelProperty("缺陷標題")
    private String title;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ColumnWidth(100)
    @ExcelProperty("缺陷描述")
    private String description;

    @Override
    public List<List<String>> getHead(Boolean isThirdTemplate, List<CustomFieldDao> customFields, IssueExportRequest request) {
        return super.getHead(isThirdTemplate, customFields, request, Locale.TRADITIONAL_CHINESE);
    }
}
