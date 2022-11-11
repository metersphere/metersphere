package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.request.issues.IssueExportRequest;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Locale;

@Data
public class IssueExcelDataUs extends IssueExcelData{

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ColumnWidth(100)
    @ExcelProperty("Title")
    private String title;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ColumnWidth(100)
    @ExcelProperty("Description")
    private String description;


    @Override
    public List<List<String>> getHead(Boolean isThirdTemplate, List<CustomFieldDao> customFields, IssueExportRequest request) {
        return super.getHead(isThirdTemplate, customFields, request, Locale.US);
    }
}
