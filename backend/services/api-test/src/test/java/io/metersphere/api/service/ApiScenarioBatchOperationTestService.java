package io.metersphere.api.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.scenario.ApiScenarioBatchCopyMoveRequest;
import io.metersphere.api.job.ApiScenarioScheduleJob;
import io.metersphere.api.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.mapper.ExtScheduleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.junit.jupiter.api.Assertions;
import org.quartz.Scheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ApiScenarioBatchOperationTestService {

    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioBlobMapper apiScenarioBlobMapper;
    @Resource
    private ApiScenarioStepMapper apiScenarioStepMapper;
    @Resource
    private ApiScenarioStepBlobMapper apiScenarioStepBlobMapper;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ExtScheduleMapper extScheduleMapper;

    @Resource
    private Scheduler scheduler;

    @Resource
    private ProjectMapper projectMapper;

    public void removeApiModule(String projectId) {
        String[] projectModule = new String[]{"workstation", "testPlan", "bugManagement", "caseManagement", "uiTest", "loadTest"};
        Project updateProject = new Project();
        updateProject.setId(projectId);
        updateProject.setModuleSetting(JSON.toJSONString(projectModule));
        projectMapper.updateByPrimaryKeySelective(updateProject);
    }

    public void resetProjectModule(String projectId) {
        String[] projectModule = new String[]{"workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"};
        Project updateProject = new Project();
        updateProject.setId(projectId);
        updateProject.setModuleSetting(JSON.toJSONString(projectModule));
        projectMapper.updateByPrimaryKeySelective(updateProject);
    }

    /**
     * 检查批量编辑
     *
     * @param sourceScenarioIds   涉及到的场景id
     * @param reCopyCountInModule 关联之前模块里是数据数量
     * @param request
     */
    public void checkBatchCopy(List<String> sourceScenarioIds, List<String> copyScenarioIdList, int reCopyCountInModule, ApiScenarioBatchCopyMoveRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andModuleIdEqualTo(request.getTargetModuleId()).andIdIn(copyScenarioIdList);
        List<ApiScenario> copyScenarios = apiScenarioMapper.selectByExample(example);
        Assertions.assertEquals(copyScenarios.size(), sourceScenarioIds.size());
        //检查总数据量
        example.clear();
        example.createCriteria().andModuleIdEqualTo(request.getTargetModuleId());
        List<ApiScenario> allScenarioList = apiScenarioMapper.selectByExample(example);
        Assertions.assertEquals(allScenarioList.size(), reCopyCountInModule + sourceScenarioIds.size());

        //不存在copy_copy_开头的数据、复制之前如果存在 -num  会把这个num去掉
        for (ApiScenario apiScenario : allScenarioList) {
            String name = apiScenario.getName();
            Assertions.assertTrue(name.length() <= 255);
            Assertions.assertFalse(name.startsWith("copy_copy_"));
            if (name.startsWith("copy_")) {
                Assertions.assertFalse(name.split("_").length > 3);
            }
        }

        List<String> copyScenarioIds = copyScenarios.stream().map(ApiScenario::getId).toList();
        //检查blob
        ApiScenarioBlobExample sourceBlobExample = new ApiScenarioBlobExample();
        sourceBlobExample.createCriteria().andIdIn(sourceScenarioIds);
        ApiScenarioBlobExample copyBlobExample = new ApiScenarioBlobExample();
        copyBlobExample.createCriteria().andIdIn(copyScenarioIds);
        Assertions.assertEquals(apiScenarioBlobMapper.countByExample(sourceBlobExample), apiScenarioBlobMapper.countByExample(copyBlobExample));

        //检查step
        ApiScenarioStepExample sourceStepExample = new ApiScenarioStepExample();
        sourceStepExample.createCriteria().andScenarioIdIn(sourceScenarioIds);
        ApiScenarioStepExample copyStepExample = new ApiScenarioStepExample();
        copyStepExample.createCriteria().andScenarioIdIn(copyScenarioIds);
        Assertions.assertEquals(apiScenarioStepMapper.countByExample(sourceStepExample), apiScenarioStepMapper.countByExample(copyStepExample));

        //检查step_blob
        ApiScenarioStepBlobExample sourceStepBlobExample = new ApiScenarioStepBlobExample();
        sourceStepBlobExample.createCriteria().andScenarioIdIn(sourceScenarioIds);
        ApiScenarioStepBlobExample copyStepBlobExample = new ApiScenarioStepBlobExample();
        copyStepBlobExample.createCriteria().andScenarioIdIn(copyScenarioIds);
        Assertions.assertEquals(apiScenarioStepBlobMapper.countByExample(sourceStepBlobExample), apiScenarioStepBlobMapper.countByExample(copyStepBlobExample));

        //检查fileRequest
        ApiFileResourceExample sourceFileExample = new ApiFileResourceExample();
        sourceFileExample.createCriteria().andResourceIdIn(sourceScenarioIds);
        ApiFileResourceExample copyFileExample = new ApiFileResourceExample();
        copyFileExample.createCriteria().andResourceIdIn(copyScenarioIds);
        Assertions.assertEquals(apiFileResourceMapper.countByExample(sourceFileExample), apiFileResourceMapper.countByExample(copyFileExample));
    }


    // 批量移动检查
    public void checkBatchMove(List<String> moveScenarioIds, int countInModuleBeforeMove, Map<String, Long> oldModuleDataCount, ApiScenarioBatchCopyMoveRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andModuleIdEqualTo(request.getTargetModuleId()).andIdIn(moveScenarioIds);
        List<ApiScenario> moveScenarios = apiScenarioMapper.selectByExample(example);
        Assertions.assertEquals(moveScenarios.size(), moveScenarioIds.size());
        //检查总数据量
        example.clear();
        example.createCriteria().andModuleIdEqualTo(request.getTargetModuleId());
        List<ApiScenario> allScenarioList = apiScenarioMapper.selectByExample(example);
        Assertions.assertEquals(allScenarioList.size(), countInModuleBeforeMove + moveScenarioIds.size());
        if (MapUtils.isNotEmpty(oldModuleDataCount)) {
            oldModuleDataCount.forEach((moduleId, moduleCount) -> {
                example.clear();
                example.createCriteria().andModuleIdEqualTo(moduleId);
                Assertions.assertEquals(apiScenarioMapper.countByExample(example), moduleCount);
            });
        }
    }

    /**
     * 检查批量回收相关的操作
     *
     * @param sourceScenarioIds 涉及到的场景id
     * @param isDelete          是否是删除状态
     */
    public void checkBatchGCOperation(List<String> sourceScenarioIds, boolean isDelete) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andDeletedEqualTo(isDelete).andIdIn(sourceScenarioIds);
        List<ApiScenario> copyScenarios = apiScenarioMapper.selectByExample(example);
        Assertions.assertEquals(copyScenarios.size(), sourceScenarioIds.size());
    }

    public void updateNameToTestByModuleId(String moduleId) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andModuleIdEqualTo(moduleId);
        List<ApiScenario> allScenarioList = apiScenarioMapper.selectByExample(example);
        List<ApiScenario> updateList = new ArrayList<>();
        for (ApiScenario apiScenario : allScenarioList) {
            String name = apiScenario.getName();
            while (name.length() < 255) {
                name = name + "_";
            }
            ApiScenario updateScenario = new ApiScenario();
            updateScenario.setModuleId(apiScenario.getId());
            updateScenario.setName(name);
            updateList.add(updateScenario);
        }
        for (ApiScenario apiScenario : updateList) {
            apiScenarioMapper.updateByPrimaryKeySelective(apiScenario);
        }
    }

    public void checkBatchDelete(List<String> deleteScenarioIds) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(deleteScenarioIds);

        Assertions.assertEquals(apiScenarioMapper.countByExample(example), 0);


        //检查blob
        ApiScenarioBlobExample sourceBlobExample = new ApiScenarioBlobExample();
        sourceBlobExample.createCriteria().andIdIn(deleteScenarioIds);
        Assertions.assertEquals(apiScenarioBlobMapper.countByExample(sourceBlobExample), 0);

        //检查step
        ApiScenarioStepExample sourceStepExample = new ApiScenarioStepExample();
        sourceStepExample.createCriteria().andScenarioIdIn(deleteScenarioIds);
        Assertions.assertEquals(apiScenarioStepMapper.countByExample(sourceStepExample), 0);

        //检查step_blob
        ApiScenarioStepBlobExample sourceStepBlobExample = new ApiScenarioStepBlobExample();
        sourceStepBlobExample.createCriteria().andScenarioIdIn(deleteScenarioIds);
        Assertions.assertEquals(apiScenarioStepBlobMapper.countByExample(sourceStepBlobExample), 0);

        //检查fileRequest
        ApiFileResourceExample sourceFileExample = new ApiFileResourceExample();
        sourceFileExample.createCriteria().andResourceIdIn(deleteScenarioIds);
        Assertions.assertEquals(apiFileResourceMapper.countByExample(sourceFileExample), 0);
    }

    /*
    校验定时任务是否成功开启：
        1.schedule表中存在数据，且开启状态符合isEnable
        2.开启状态下：    qrtz_triggers 和 qrtz_cron_triggers 表存在对应的数据
        3.关闭状态下：    qrtz_triggers 和 qrtz_cron_triggers 表不存在对应的数据
     */
    public void checkSchedule(String scheduleId, String resourceId, boolean isEnable) throws Exception {
        Assertions.assertEquals(extScheduleMapper.countByIdAndEnable(scheduleId, isEnable), 1L);
        Assertions.assertEquals(scheduler.checkExists(ApiScenarioScheduleJob.getJobKey(resourceId)), isEnable);
    }

    public void checkSchedule(String resourceId, boolean isEnable) throws Exception {
        Assertions.assertEquals(extScheduleMapper.countByResourceId(resourceId), 1L);
        Assertions.assertEquals(scheduler.checkExists(ApiScenarioScheduleJob.getJobKey(resourceId)), isEnable);
    }

    public void checkScheduleIsRemove(String resourceId) throws Exception {
        Assertions.assertEquals(extScheduleMapper.countByResourceId(resourceId), 0L);
        Assertions.assertEquals(scheduler.checkExists(ApiScenarioScheduleJob.getJobKey(resourceId)), false);
    }
}
