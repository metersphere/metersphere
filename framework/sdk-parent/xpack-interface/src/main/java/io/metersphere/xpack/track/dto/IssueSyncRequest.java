package io.metersphere.xpack.track.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author songcc
 */
@Data
public class IssueSyncRequest {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 缺陷创建时间
     */
    private Long createTime;
    /**
     * 同步人
     */
    private String operateUser;

    /**
     * TRUE: 创建时间之前
     */
    private boolean pre;

    private String defaultCustomFields;

    private Map<String, List<PlatformAttachment>> attachmentMap = new HashMap<>();

    private Consumer<Map> handleSyncFunc;
}
