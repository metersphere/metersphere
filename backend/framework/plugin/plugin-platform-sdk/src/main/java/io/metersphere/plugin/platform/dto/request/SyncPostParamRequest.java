package io.metersphere.plugin.platform.dto.request;

import io.metersphere.plugin.platform.dto.PlatformAttachment;
import io.metersphere.plugin.platform.dto.response.PlatformBugDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SyncPostParamRequest {

    /**
     * 需要同步的缺陷集合
     */
    List<PlatformBugDTO> needSyncBugs;

    /**
     * 需要处理的缺陷集合
     */
    Map<String, List<PlatformAttachment>> attachmentMap;
}
