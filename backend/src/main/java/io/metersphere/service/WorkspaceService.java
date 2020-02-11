package io.metersphere.service;

import io.metersphere.base.domain.Workspace;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.commons.MSException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkspaceService {
    @Resource
    private WorkspaceMapper workspaceMapper;

    public Workspace add(Workspace workspace) {
        if (StringUtils.isBlank(workspace.getName())) {
            MSException.throwException("Workspace name cannot be null.");
        }
        // TODO 组织ID 暂无
        if (StringUtils.isBlank(workspace.getOrganizationId())) {
            workspace.setOrganizationId("root");
        }
        long createTime = System.currentTimeMillis();
        workspace.setCreateTime(createTime);
        workspace.setUpdateTime(createTime); // 首次 update time
        workspace.setId(UUID.randomUUID().toString()); // 设置ID
        workspaceMapper.insertSelective(workspace);
        return workspace;
    }
}
