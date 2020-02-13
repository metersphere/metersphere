package io.metersphere.service;

import io.metersphere.base.domain.Workspace;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.commons.exception.MSException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkspaceService {
    @Resource
    private WorkspaceMapper workspaceMapper;

    public Workspace saveWorkspace(Workspace workspace) {
        if (StringUtils.isBlank(workspace.getName())) {
            MSException.throwException("Workspace name cannot be null.");
        }
        // TODO 组织ID 暂无
        if (StringUtils.isBlank(workspace.getOrganizationId())) {
            workspace.setOrganizationId("root");
        }
        long currentTime = System.currentTimeMillis();
        if (StringUtils.isBlank(workspace.getId())) {
            workspace.setId(UUID.randomUUID().toString()); // 设置ID
            workspace.setCreateTime(currentTime);
            workspace.setUpdateTime(currentTime); // 首次 update time
            workspaceMapper.insertSelective(workspace);
        } else {
            workspace.setUpdateTime(currentTime);
            workspaceMapper.updateByPrimaryKeySelective(workspace);
        }
        return workspace;
    }

    public List<Workspace> getWorkspaceList() {
        return workspaceMapper.selectByExample(null);
    }

    public void deleteWorkspace(String workspaceId) {
        workspaceMapper.deleteByPrimaryKey(workspaceId);
    }
}
