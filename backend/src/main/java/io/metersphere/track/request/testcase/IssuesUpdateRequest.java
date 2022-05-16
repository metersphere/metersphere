package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.base.domain.ext.CustomFieldResource;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IssuesUpdateRequest extends IssuesWithBLOBs {
    private String content;
    private String workspaceId;

    private List<String> tapdUsers;
    private List<CustomFieldResource> addFields;
    private List<CustomFieldResource> editFields;
    /**
     * zentao bug 处理人
     */
    private String zentaoUser;
    private String zentaoAssigned;
    /**
     * zentao bug 影响版本
     */
    private List<String> zentaoBuilds;
    private boolean thirdPartPlatform;

    private List<String> follows;

    private List<String> addResourceIds;
    private List<String> deleteResourceIds;
    private Boolean isPlanEdit = false;
    private String refId;
    /**
     * azure devops bug同步fields
     */
    private String devopsFields;
}
