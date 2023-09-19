package io.metersphere.project.service;

import io.metersphere.project.domain.ProjectVersionExample;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 项目删除-清空版本资源
 *
 * @author song-cc-rock
 */
@Component
public class CleanupVersionResourceService implements CleanupProjectResourceService {

    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Override
    public void deleteResources(String projectId) {
        // 删除所有项目版本
        ProjectVersionExample example = new ProjectVersionExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        projectVersionMapper.deleteByExample(example);
        LogUtils.info("清理当前项目[" + projectId + "]相关版本资源");
    }

    @Override
    public void cleanReportResources(String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关报告资源");
    }
}
