package io.metersphere.bug.dto;

import lombok.Data;

import java.util.List;

@Data
public class BugHistoryContentDTO extends BugDTO {

    /**
     * 缺陷自定义字段
     */
    private List<BugCustomFieldDTO> customFields;

    /**
     * 缺陷内容
     */
    private String description;

    /**
     * 附件名称集合
     */
    private List<String> attachmentNames;
}
