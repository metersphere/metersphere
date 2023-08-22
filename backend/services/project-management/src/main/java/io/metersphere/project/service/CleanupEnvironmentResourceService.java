package io.metersphere.project.service;

import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.ProjectParametersExample;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.mapper.ProjectParametersMapper;
import io.metersphere.sdk.service.CleanupProjectResourceService;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CleanupEnvironmentResourceService implements CleanupProjectResourceService {

    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    @Resource
    private ProjectParametersMapper projectParametersMapper;

    @Override
    public void deleteResources(String projectId) {
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(projectId);
        environmentMapper.deleteByExample(environmentExample);
        environmentBlobMapper.deleteByPrimaryKey(projectId);
        ProjectParametersExample projectExample = new ProjectParametersExample();
        projectExample.createCriteria().andProjectIdEqualTo(projectId);
        projectParametersMapper.deleteByExample(projectExample);
        LogUtils.info("删除当前项目[" + projectId + "]相关环境资源");
    }

    @Override
    public void cleanReportResources(String projectId) {

    }

}
