package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.service.CreateProjectResourceService;
import io.metersphere.system.service.SystemParameterService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateEnvironmentResourceService implements CreateProjectResourceService {

    public static final String MOCK_EVN_NAME = "Mock环境";
    private static final String MOCK_EVN_SOCKET = "/mock-server/";
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    @Resource
    private ProjectMapper projectMapper;

    @Override
    public void createResources(String projectId) {
        // 创建默认mock环境
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andMockEqualTo(true);
        if (environmentMapper.countByExample(example) == 0) {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            Environment environment = new Environment();
            environment.setId(IDGenerator.nextStr());
            environment.setCreateUser(project.getCreateUser());
            environment.setName(MOCK_EVN_NAME);
            environment.setMock(true);
            environment.setPos(5000L);
            environment.setProjectId(projectId);
            environment.setCreateTime(System.currentTimeMillis());
            environment.setUpdateUser(project.getCreateUser());
            environment.setUpdateTime(System.currentTimeMillis());
            environmentMapper.insert(environment);
            SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
            if (systemParameterService != null) {
                EnvironmentConfig environmentConfig = new EnvironmentConfig();
                List<HttpConfig> httpConfigs = new ArrayList<>();
                BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
                String baseUrl = baseSystemConfigDTO.getUrl();
                if (StringUtils.isNotEmpty(baseUrl)) {
                    if (CollectionUtils.isEmpty(httpConfigs)) {
                        HttpConfig httpConfig = new HttpConfig();
                        httpConfig.setHostname(StringUtils.join(baseUrl, MOCK_EVN_SOCKET, project.getNum()));
                        httpConfig.setDescription("根据系统配置的当前站点自动生成");
                        httpConfigs.add(httpConfig);
                    }
                }
                environmentConfig.setHttpConfig(httpConfigs);
                EnvironmentBlob environmentBlob = new EnvironmentBlob();
                environmentBlob.setId(environment.getId());
                environmentBlob.setConfig(JSON.toJSONBytes(environmentConfig));
                environmentBlobMapper.insert(environmentBlob);
            }
        }


    }
}
