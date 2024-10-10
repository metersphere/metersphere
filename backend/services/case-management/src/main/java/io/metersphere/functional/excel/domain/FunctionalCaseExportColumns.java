package io.metersphere.functional.excel.domain;

import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseExportColumns {
    private LinkedHashMap<String, String> systemColumns = new LinkedHashMap<>();
    private LinkedHashMap<String, String> otherColumns = new LinkedHashMap<>();
    private LinkedHashMap<String, String> customColumns = new LinkedHashMap<>();

    public FunctionalCaseExportColumns() {
        // 系统字段
        systemColumns.put("name", Translator.get("case.export.system.columns.name"));
        systemColumns.put("num", Translator.get("case.export.system.columns.id"));
        systemColumns.put("prerequisite", Translator.get("case.export.system.columns.prerequisite"));
        systemColumns.put("module", Translator.get("case.export.system.columns.module"));
        systemColumns.put("text_description", Translator.get("case.export.system.columns.text_description"));
        systemColumns.put("expected_result", Translator.get("case.export.system.columns.expected_result"));
        systemColumns.put("tags", Translator.get("xmind_tags"));
        systemColumns.put("description", Translator.get("xmind_description"));


        // 其他字段
        otherColumns.put("last_execute_result", Translator.get("case.export.system.other.columns.last_execute_result"));
        otherColumns.put("review_status", Translator.get("case.export.system.other.columns.review_status"));
        otherColumns.put("create_user", Translator.get("case.export.system.other.columns.create_user"));
        otherColumns.put("create_time", Translator.get("case.export.system.other.columns.create_time"));
        otherColumns.put("update_user", Translator.get("case.export.system.other.columns.update_user"));
        otherColumns.put("update_time", Translator.get("case.export.system.other.columns.update_time"));
        otherColumns.put("case_comment", Translator.get("case.export.system.other.columns.case_comment"));
        otherColumns.put("execute_comment", Translator.get("case.export.system.other.columns.execute_comment"));
        otherColumns.put("review_comment", Translator.get("case.export.system.other.columns.review_comment"));

    }

    public void initCustomColumns(List<TemplateCustomFieldDTO> headerCustomFields) {
        headerCustomFields.forEach(item -> {
            if (!StringUtils.equalsIgnoreCase(item.getFieldName(), Translator.get("custom_field.functional_priority"))) {
                customColumns.put(item.getFieldId(), item.getFieldName());
            } else {
                systemColumns.put(item.getFieldId(), item.getFieldName());
            }
        });
    }
}
