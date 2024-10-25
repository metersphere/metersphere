package io.metersphere.api.service;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.domain.TestResourcePoolOrganization;
import io.metersphere.system.domain.TestResourcePoolOrganizationExample;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolDTO;
import io.metersphere.system.mapper.TestResourcePoolBlobMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.mapper.TestResourcePoolOrganizationMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-20  11:32
 */
@Service
public class BaseResourcePoolTestService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourcePoolBlobMapper testResourcePoolBlobMapper;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;
    protected static final String DEFAULT_PROJECT_ID = "100001100001";
    protected static final String DEFAULT_ORGANIZATION_ID = "100001";

    public void initProjectResourcePool() {
        TestResourcePool resourcePool = insertResourcePool();
        insertResourcePoolOrg(resourcePool);
        insertResourcePoolProject(resourcePool);
        insertProjectApplication(resourcePool);
    }

    public TestResourcePool insertResourcePool() {
        String id = IDGenerator.nextStr();
        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        testResourcePool.setId(id);
        testResourcePool.setCreateTime(System.currentTimeMillis());
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setDeleted(false);
        testResourcePool.setName("api debug test");
        testResourcePool.setCreateUser("admin");
        testResourcePool.setAllOrg(false);
        testResourcePool.setEnable(true);
        testResourcePool.setType(ResourcePoolTypeEnum.NODE.getName());
        TestResourcePoolBlob testResourcePoolBlob = new TestResourcePoolBlob();
        testResourcePoolBlob.setId(id);
        TestResourceDTO testResourceDTO = new TestResourceDTO();
        TestResourceNodeDTO testResourceNodeDTO = new TestResourceNodeDTO();
        testResourceNodeDTO.setIp(mockServerHost);
        testResourceNodeDTO.setPort(mockServerHostPort + StringUtils.EMPTY);
        testResourceNodeDTO.setConcurrentNumber(10);
        testResourceDTO.setNodesList(List.of(testResourceNodeDTO));
        String configuration = JSON.toJSONString(testResourceDTO);
        testResourcePoolBlob.setConfiguration(configuration.getBytes());
        testResourcePool.setTestResourceDTO(testResourceDTO);

        testResourcePoolMapper.insert(testResourcePool);
        testResourcePoolBlobMapper.insert(testResourcePoolBlob);
        return testResourcePool;
    }

    public synchronized void insertProjectApplication(TestResourcePool resourcePool) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID)
                .andTypeEqualTo(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
        if (projectApplicationMapper.countByExample(example) > 0) {
            return;
        }
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setTypeValue(resourcePool.getId());
        projectApplication.setType(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
        projectApplication.setProjectId(DEFAULT_PROJECT_ID);
        projectApplicationMapper.insert(projectApplication);
    }

    public void insertResourcePoolProject(TestResourcePool resourcePool) {
        ProjectTestResourcePoolExample example = new ProjectTestResourcePoolExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID)
                .andTestResourcePoolIdEqualTo(resourcePool.getId());
        if (projectTestResourcePoolMapper.countByExample(example) == 0) {
            ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
            projectTestResourcePool.setTestResourcePoolId(resourcePool.getId());
            projectTestResourcePool.setProjectId(DEFAULT_PROJECT_ID);
            projectTestResourcePoolMapper.insert(projectTestResourcePool);
        }
    }

    public void insertResourcePoolOrg(TestResourcePool resourcePool) {
        TestResourcePoolOrganizationExample example = new TestResourcePoolOrganizationExample();
        example.createCriteria().andOrgIdEqualTo(DEFAULT_ORGANIZATION_ID)
                .andTestResourcePoolIdEqualTo(resourcePool.getId());
        if (testResourcePoolOrganizationMapper.countByExample(example) == 0) {
            TestResourcePoolOrganization resourcePoolOrganization = new TestResourcePoolOrganization();
            resourcePoolOrganization.setTestResourcePoolId(resourcePool.getId());
            resourcePoolOrganization.setOrgId(DEFAULT_ORGANIZATION_ID);
            resourcePoolOrganization.setId(IDGenerator.nextStr());
            testResourcePoolOrganizationMapper.insert(resourcePoolOrganization);
        }
    }
}
