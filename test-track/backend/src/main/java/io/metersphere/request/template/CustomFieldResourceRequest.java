package io.metersphere.request.template;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.ext.BaseCustomFieldResourceMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSession;

import java.util.Map;

@Getter
@Setter
public class CustomFieldResourceRequest {
    private String workspaceId;
    private String projectId;
    private Map<String, CustomField> wdFieldMap;
    private Map<String, CustomField> globalFieldMap;
    private Map<String, CustomField> jiraSyncFieldMap;
    private String resourceType;
    private SqlSession sqlSession;
    private BaseCustomFieldResourceMapper batchMapper;
    private CustomFieldMapper customFieldBatchMapper;
    private boolean enableJiraSync;
}
