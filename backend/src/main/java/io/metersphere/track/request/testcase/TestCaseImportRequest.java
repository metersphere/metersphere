package io.metersphere.track.request.testcase;

import io.metersphere.dto.CustomFieldDao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class TestCaseImportRequest {
    private String projectId;
    private String userId;
    private String importType;
    private String versionId;
    private boolean ignore;

    private Set<String> userIds;
    /**
     * 已存在用例名称
     */
    private Set<String> testCaseNames;
    private List<CustomFieldDao> customFields;
    private Set<String> savedCustomIds;
    private boolean isUseCustomId;
}
