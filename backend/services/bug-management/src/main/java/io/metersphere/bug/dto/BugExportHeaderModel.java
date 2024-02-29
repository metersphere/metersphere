package io.metersphere.bug.dto;

import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugExportHeaderModel {
    //xlsx文件前缀
    private String xlsxFileNamePrefix;

    /**
     * 导出的表头列
     */
    private List<BugExportColumn> exportColumns;

    /**
     * 导出的表头自定义字段(包含选项值)
     */
    private List<TemplateCustomFieldDTO> headerCustomFields;

    /**
     * 导出的表头选项值集合(处理人)
     */
    private Map<String, String> handleUserMap;

    /**
     * 导出的表头选项值集合(状态)
     */
    private Map<String, String> statusMap;
}
