package io.metersphere.system.service;


import io.metersphere.project.domain.Project;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolExample;
import io.metersphere.system.domain.TestResourcePoolOrganization;
import io.metersphere.system.domain.TestResourcePoolOrganizationExample;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.mapper.TestResourcePoolOrganizationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CommonProjectPoolService {

    public static final String API_TEST = "apiTest";
    public static final String TEST_PLAN = "testPlan";

    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;

    public List<TestResourcePool> getOrgTestResourcePools(String organizationId, boolean onlyEnable) {
        List<TestResourcePool> testResourcePools = new ArrayList<>();
        if (StringUtils.isNotBlank(organizationId)) {
            TestResourcePoolOrganizationExample example = new TestResourcePoolOrganizationExample();
            example.createCriteria().andOrgIdEqualTo(organizationId);
            List<TestResourcePoolOrganization> orgPools = testResourcePoolOrganizationMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(orgPools)) {
                List<String> poolIds = orgPools.stream().map(TestResourcePoolOrganization::getTestResourcePoolId).toList();
                TestResourcePoolExample poolExample = new TestResourcePoolExample();
                if (onlyEnable) {
                    poolExample.createCriteria().andIdIn(poolIds).andEnableEqualTo(true).andDeletedEqualTo(false);
                } else {
                    poolExample.createCriteria().andIdIn(poolIds).andDeletedEqualTo(false);
                }
                testResourcePools.addAll(testResourcePoolMapper.selectByExample(poolExample));
            }
        }
        //获取应用全部组织的资源池
        TestResourcePoolExample poolExample = new TestResourcePoolExample();
        if (onlyEnable) {
            poolExample.createCriteria().andAllOrgEqualTo(true).andEnableEqualTo(true).andDeletedEqualTo(false);
        } else {
            poolExample.createCriteria().andAllOrgEqualTo(true).andDeletedEqualTo(false);
        }
        testResourcePools.addAll(testResourcePoolMapper.selectByExample(poolExample));

        testResourcePools = testResourcePools.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        return testResourcePools;
    }

    /**
     * 获取当前项目所有可用资源池
     */
    public List<TestResourcePool> getProjectAllPoolsByEffect(Project project) {
        List<TestResourcePool> testResourcePools = getOrgTestResourcePools(project.getOrganizationId(), true);
        List<String> modulesIds = JSON.parseArray(project.getModuleSetting(), String.class);
        if (modulesIds.contains(API_TEST) || modulesIds.contains(TEST_PLAN)) {
            return testResourcePools;
        } else {
            return new ArrayList<>();
        }
    }
}
