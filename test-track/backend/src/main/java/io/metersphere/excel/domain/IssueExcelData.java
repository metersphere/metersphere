package io.metersphere.excel.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.excel.constants.IssueExportHeadField;
import io.metersphere.excel.constants.IssueImportField;
import io.metersphere.request.issues.IssueExportRequest;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

@Data
public class IssueExcelData implements Serializable {

    @ExcelIgnore
    private String id;
    @ExcelIgnore
    private Integer num;
    @ExcelIgnore
    private String platform;
    @ExcelIgnore
    private String creator;
    @ExcelIgnore
    private long caseCount;
    @ExcelIgnore
    private Long createTime;
    @ExcelIgnore
    private Long updateTime;
    @ExcelIgnore
    private String reporter;
    @ExcelIgnore
    private String projectId;
    @ExcelIgnore
    private String platformId;
    @ExcelIgnore
    private String platformStatus;
    @ExcelIgnore
    private String resourceId;
    @ExcelIgnore
    private String resourceName;
    @ExcelIgnore
    private String customFields;
    @ExcelIgnore
    private String comment;
    @ExcelIgnore
    private Boolean addFlag;
    @ExcelIgnore
    private Boolean updateFlag = true;
    @ExcelIgnore
    private String title;
    @ExcelIgnore
    private String description;
    /**
     * 处理人
     * 状态
     * 严重程度
     */
    @ExcelIgnore
    private String processor;
    @ExcelIgnore
    private String status;
    @ExcelIgnore
    private String priority;
    @ExcelIgnore
    Map<String, Object> customData = new LinkedHashMap<>();
    @ExcelIgnore
    List<String> tapdUsers = new ArrayList<>();

    public List<List<String>> getHead(Boolean isThirdTemplate, List<CustomFieldDao> customFields, IssueExportRequest request) {
        return new ArrayList<>();
    }

    public List<List<String>> getHead(Boolean isThirdTemplate, List<CustomFieldDao> customFields, IssueExportRequest request, Locale lang) {
        List<List<String>> heads = new ArrayList<>();
        IssueExportHeadField[] exportHeadFields = IssueExportHeadField.values();
        if (request != null) {
            List<IssueExportRequest.IssueExportHeader> baseHeaders = request.getExportFields().get("baseHeaders");
            List<IssueExportRequest.IssueExportHeader> customHeaders = request.getExportFields().get("customHeaders");
            List<IssueExportRequest.IssueExportHeader> otherHeaders = request.getExportFields().get("otherHeaders");
            baseHeaders.forEach(baseHeader -> {
                for (IssueExportHeadField exportHeadField : exportHeadFields) {
                    if (StringUtils.equals(baseHeader.getId(), exportHeadField.getId())) {
                        heads.add(List.of(exportHeadField.getName()));
                    }
                }
            });
            customHeaders.forEach(customHeader -> {
                heads.add(List.of(customHeader.getName()));
            });
            otherHeaders.forEach(otherHeader -> {
                for (IssueExportHeadField exportHeadField : exportHeadFields) {
                    if (StringUtils.equals(otherHeader.getId(), exportHeadField.getId())) {
                        heads.add(List.of(exportHeadField.getName()));
                    }
                }
            });
        } else {
            if (!isThirdTemplate) {
                IssueImportField[] fields = IssueImportField.values();
                for (IssueImportField field : fields) {
                    heads.add(List.of(field.getFieldLangMap().get(lang)));
                }
            }

            if (CollectionUtils.isNotEmpty(customFields)) {
                for (CustomFieldDao dto : customFields) {
                    heads.add(new ArrayList<>() {{
                        add(dto.getName());
                    }});
                }
            }
        }
        return heads;
    }
}
