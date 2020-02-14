package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.OrganizationMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.controller.request.testplan.DeleteTestPlanRequest;
import io.metersphere.controller.request.testplan.QueryTestPlanRequest;
import io.metersphere.dto.LoadTestDTO;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestService {
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private ProjectMapper projectMapper;

    // 测试，模拟数据
    @PostConstruct
    public void initData() {
        if (!CollectionUtils.isEmpty(loadTestMapper.selectByExample(null))) {
            return;
        }

        final List<Project> projects = projectMapper.selectByExample(null);

        for (int i = 0; i < 100; i++) {
            final LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
            loadTest.setId(UUID.randomUUID().toString());
            loadTest.setName("load test " + i);
            loadTest.setProjectId(projects.get(RandomUtils.nextInt(0, projects.size())).getId());
            loadTest.setCreateTime(System.currentTimeMillis());
            loadTest.setUpdateTime(System.currentTimeMillis());
            loadTest.setScenarioDefinition(UUID.randomUUID().toString());
            loadTest.setDescription(UUID.randomUUID().toString());
            loadTestMapper.insert(loadTest);
        }
    }

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        return extLoadTestMapper.list(null);
    }

    public void delete(DeleteTestPlanRequest request) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andIdEqualTo(request.getId());

        loadTestMapper.deleteByExample(loadTestExample);
    }
}
