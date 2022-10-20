package io.metersphere.xpack.track.dto;

import lombok.Data;

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
     * TRUE: 创建时间之前
     */
    private boolean pre;
}
