package io.metersphere.bug.dto.request;

import io.metersphere.bug.domain.Bug;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author song-cc-rock
 */
@Data
public class BugEditRequest extends Bug {

    @Schema(description = "缺陷内容")
    private String description;

    @Schema(description = "自定义字段集合")
    private Map<String, Object> customFieldMap;

    @Schema(description = "删除的本地附件集合")
    private List<String> deleteLocalFileIds;

    @Schema(description = "取消关联附件集合")
    private List<String> unLinkFileIds;

    @Schema(description = "关联附件集合")
    private List<String> linkFileIds;
}
