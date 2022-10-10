package io.metersphere.service;

import io.metersphere.base.domain.Workspace;
import io.metersphere.base.domain.WorkspaceExample;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.base.mapper.ext.BaseUserGroupMapper;
import io.metersphere.dto.RelatedSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseWorkspaceService {
    @Resource
    private BaseUserGroupMapper baseUserGroupMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;

    public List<Workspace> getWorkspaceListByUserId(String userId) {
        List<RelatedSource> relatedSource = baseUserGroupMapper.getRelatedSource(userId);
        List<String> wsIds = relatedSource
                .stream()
                .map(RelatedSource::getWorkspaceId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(wsIds)) {
            return new ArrayList<>();
        }
        WorkspaceExample workspaceExample = new WorkspaceExample();
        workspaceExample.createCriteria().andIdIn(wsIds);
        return workspaceMapper.selectByExample(workspaceExample);
    }
}
