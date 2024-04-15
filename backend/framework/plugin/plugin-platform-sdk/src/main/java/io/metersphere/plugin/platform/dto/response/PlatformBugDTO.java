package io.metersphere.plugin.platform.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PlatformBugDTO extends MsSyncBugDTO {
    /**
     * 自定义字段集合(双向同步需要)
     */
    private List<PlatformCustomFieldItemDTO> customFieldList;

    /**
     * 是否平台默认模板(默认模板时, 第三方自定义字段默认都需同步)
     */
    private Boolean platformDefaultTemplate;

    /**
     * 缺陷同步所需处理的平台自定义字段ID(同步第三方平台到MS时需要, 非默认模板时使用)
     */
    private List<PlatformCustomFieldItemDTO> needSyncCustomFields;

    /**
     * 缺陷同步需要下载的第三方富文本图片{key: name}
     * key: 唯一文件流获取的ID
     * name: 文件名称
     */
    private Map<String, String> richTextImageMap;
}