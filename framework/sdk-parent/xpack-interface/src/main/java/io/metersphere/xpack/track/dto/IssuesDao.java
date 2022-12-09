package io.metersphere.xpack.track.dto;

import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldItemDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IssuesDao extends IssuesWithBLOBs {
    private String model;
    private String projectName;
    private String creatorName;
    private String resourceName;
    private long caseCount;
    private List<String> caseIds;
    private String caseId;
    private List<String> tapdUsers;
    private String refType;
    private String refId;
    private List<CustomFieldDao> fields;

    /**
     * 缺陷自定义字段相关
     */
    private List<CustomFieldItemDTO> customFieldList;
    private String customData;
    private String issueId;
    private String fieldId;
    private String fieldName;
    private String fieldType;
    private String fieldValue;

    /**
     * 导出评论
     */
    private String comment;
}
