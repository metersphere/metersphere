package io.metersphere.plugin.platform.dto;

import io.metersphere.plugin.platform.dto.response.PlatformBugDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SyncBugResult {
    /**
     * 同步新增的缺陷
     */
    private List<PlatformBugDTO> addBug = new ArrayList<>();
    /**
     * 同步更新的缺陷
     */
    private List<PlatformBugDTO> updateBug = new ArrayList<>();
    /**
     * 同步失败需删除的ID
     */
    private List<String> deleteBugIds = new ArrayList<>();

    /**
     * 需同步的平台附件集合(统一处理) {key: bugId, value: attachmentList}
     */
    private Map<String, List<PlatformAttachment>> attachmentMap = new HashMap<>();
}
