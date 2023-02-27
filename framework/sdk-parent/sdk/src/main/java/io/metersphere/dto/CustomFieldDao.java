package io.metersphere.dto;

import io.metersphere.base.domain.CustomField;
import lombok.Data;

@Data
public class CustomFieldDao extends CustomField {
    private Boolean required;

    private Integer order;

    private String defaultValue;

    private String textValue;

    private String value;

    private String customData;

    private String originGlobalId;

    private String key;

    // 支持输入字符时实时调用 optionMethod 方法查询下拉框数据
    // 目前 jira 获取 sprint 字段有使用
    private Boolean inputSearch;

    private String optionMethod;

    // jira 获取 sprint 下拉框选项不全，需要检索才能获取，这里将当前选项的名称保存
    private String optionLabel;
}
