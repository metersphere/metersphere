package io.metersphere.api.dto.share;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/2/7 10:32 上午
 * @Description
 */
@Getter
@Setter
public class ApiDocumentRequest {
    private String projectId;
    private List<String> moduleIds;
    private String shareId;
    private String name;
    private String type;
    private String orderCondition;
    private List<String> apiIdList;
    private boolean trashEnable;
    private String refId;
    private String versionId;
}
