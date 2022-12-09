package io.metersphere.xpack.track.dto.request;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.CustomFieldResourceDTO;

import io.metersphere.xpack.track.dto.PlatformStatusDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class IssuesUpdateRequest extends IssuesWithBLOBs {
    private String content;
    private String workspaceId;

    private List<String> tapdUsers;
    private List<CustomFieldResourceDTO> addFields;
    private List<CustomFieldResourceDTO> editFields;
    private List<CustomFieldItemDTO> requestFields;
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
    private String azureDevopsCreateTime;

    private PlatformStatusDTO transitions;

    /**
     * 缺陷附件上传更新的文件数据
     */
    private List<FileMetadata> updatedFileList;

    /**
     * 复制缺陷时原始缺陷ID
     */
    private String copyIssueId;

    /**
     * 关联文件管理引用ID
      */
    private List<String> relateFileMetaIds = new ArrayList<>();

    /**
     * 取消关联文件应用ID
     */
    private List<String> unRelateFileMetaIds = new ArrayList<>();

    private List<String> batchDeleteIds;

    private Boolean batchDeleteAll;
}
