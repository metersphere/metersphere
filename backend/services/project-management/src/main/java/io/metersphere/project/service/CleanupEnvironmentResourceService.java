package io.metersphere.project.service;

import io.metersphere.sdk.domain.*;
import io.metersphere.sdk.mapper.*;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CleanupEnvironmentResourceService implements CleanupProjectResourceService {

    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    @Resource
    private ProjectParameterMapper projectParametersMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private EnvironmentGroupRelationMapper environmentGroupRelationMapper;

    @Override
    public void deleteResources(String projectId) {
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(projectId);
        environmentMapper.deleteByExample(environmentExample);
        environmentBlobMapper.deleteByPrimaryKey(projectId);
        ProjectParameterExample projectExample = new ProjectParameterExample();
        projectExample.createCriteria().andProjectIdEqualTo(projectId);
        projectParametersMapper.deleteByExample(projectExample);
        EnvironmentGroupExample environmentGroupExample = new EnvironmentGroupExample();
        environmentGroupExample.createCriteria().andProjectIdEqualTo(projectId);
        List<EnvironmentGroup> environmentGroups = environmentGroupMapper.selectByExample(environmentGroupExample);
        if (!environmentGroups.isEmpty()) {
            //取所有的id
            List<String> ids = environmentGroups.stream().map(EnvironmentGroup::getId).toList();
            EnvironmentGroupRelationExample environmentGroupRelationExample = new EnvironmentGroupRelationExample();
            environmentGroupRelationExample.createCriteria().andEnvironmentGroupIdIn(ids);
            environmentGroupRelationMapper.deleteByExample(environmentGroupRelationExample);
        }
        environmentMapper.deleteByExample(environmentExample);
        LogUtils.info("删除当前项目[" + projectId + "]相关环境资源");

    }

}
