package io.metersphere.plan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.dto.TestPlanQueryConditions;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanManagementService {
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanModuleService testPlanModuleService;

    public Map<String, Long> moduleCount(TestPlanTableRequest request) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        TestPlanQueryConditions testPlanQueryConditions = new TestPlanQueryConditions(null, request.getProjectId(), request);
        List<ModuleCountDTO> moduleCountDTOList = extTestPlanMapper.countModuleIdByKeywordAndFileType(testPlanQueryConditions);
        Map<String, Long> moduleCountMap = testPlanModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);

        return moduleCountMap;
    }

    public Pager<List<TestPlanResponse>> page(TestPlanTableRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "t.update_time desc");
        return PageUtils.setPageInfo(page, this.getTableList(request));
    }

    private List<TestPlanResponse> getTableList(TestPlanTableRequest request) {
        List<TestPlanResponse> testPlanResponses = extTestPlanMapper.selectByConditions(new TestPlanQueryConditions(request.getModuleIds(), request.getProjectId(), request));
        testPlanResponses.forEach(item -> {
            item.setModuleName(testPlanModuleService.getNameById(item.getModuleId()));
            //todo 定时任务相关信息处理
        });
        return testPlanResponses;
    }
}
