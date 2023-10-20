package io.metersphere.project.service;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CreateProjectResourceService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 项目创建-初始化版本资源
 *
 * @author song-cc-rock
 */
@Component
public class CreateVersionResourceService implements CreateProjectResourceService {

    public static final String DEFAULT_VERSION = "v1.0";
    public static final String DEFAULT_VERSION_STATUS = "open";

    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;

    @Override
    public void createResources(String projectId) {
        // 初始化版本V1.0, 初始化版本配置项
        ProjectVersion defaultVersion = new ProjectVersion();
        defaultVersion.setId(IDGenerator.nextStr());
        defaultVersion.setProjectId(projectId);
        defaultVersion.setName(DEFAULT_VERSION);
        defaultVersion.setStatus(DEFAULT_VERSION_STATUS);
        defaultVersion.setLatest(true);
        defaultVersion.setCreateTime(System.currentTimeMillis());
        defaultVersion.setCreateUser(InternalUserRole.ADMIN.getValue());
        projectVersionMapper.insert(defaultVersion);
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        projectApplication.setType(ProjectApplicationType.VERSION.VERSION_ENABLE.name());
        projectApplication.setTypeValue("FALSE");
        projectApplicationService.update(projectApplication, "");
        LogUtils.info("初始化当前项目[" + projectId + "]相关版本资源");
    }
}
