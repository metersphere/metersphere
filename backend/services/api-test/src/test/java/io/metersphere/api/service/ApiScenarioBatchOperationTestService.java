package io.metersphere.api.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.scenario.ApiScenarioBatchCopyRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public void checkBatchCopy(List<String> sourceScenarioIds, List<String> copyScenarioIdList, int reCopyCountInModule, ApiScenarioBatchCopyRequest request) {
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
}
