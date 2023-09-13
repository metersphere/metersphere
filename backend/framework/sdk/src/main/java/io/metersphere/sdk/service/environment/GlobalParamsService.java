package io.metersphere.sdk.service.environment;

import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.domain.ProjectParameters;
import io.metersphere.sdk.domain.ProjectParametersExample;
import io.metersphere.sdk.dto.environment.GlobalParams;
import io.metersphere.sdk.dto.environment.GlobalParamsRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.ProjectParametersMapper;
import io.metersphere.sdk.uid.UUID;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Transactional
public class GlobalParamsService {

    @Resource
    private ProjectParametersMapper projectParametersMapper;
    @Resource
    private ProjectMapper projectMapper;

    public GlobalParamsRequest add(GlobalParamsRequest globalParamsRequest, String userId) {
        ProjectParameters projectParameters = new ProjectParameters();
        projectParameters.setProjectId(globalParamsRequest.getProjectId());
        checkExist(globalParamsRequest.getProjectId());
        checkProjectExist(globalParamsRequest.getProjectId());
        projectParameters.setId(UUID.randomUUID().toString());
        projectParameters.setCreateUser(userId);
        projectParameters.setUpdateUser(userId);
        projectParameters.setCreateTime(System.currentTimeMillis());
        projectParameters.setUpdateTime(System.currentTimeMillis());
        String params = JSON.toJSONString(globalParamsRequest.getGlobalParams());
        projectParameters.setParameters(params.getBytes());
        projectParametersMapper.insert(projectParameters);
        globalParamsRequest.setId(projectParameters.getId());
        return globalParamsRequest;
    }

    public GlobalParamsRequest update(GlobalParamsRequest globalParamsRequest, String userId) {
        ProjectParameters projectParameters = new ProjectParameters();
        projectParameters.setProjectId(globalParamsRequest.getProjectId());
        checkDataExist(globalParamsRequest.getProjectId());
        checkProjectExist(globalParamsRequest.getProjectId());
        projectParameters.setId(globalParamsRequest.getId());
        projectParameters.setUpdateUser(userId);
        projectParameters.setUpdateTime(System.currentTimeMillis());
        String params = JSON.toJSONString(globalParamsRequest.getGlobalParams());
        projectParameters.setParameters(params.getBytes());
        projectParametersMapper.updateByPrimaryKeySelective(projectParameters);
        return globalParamsRequest;
    }

    private void checkDataExist(String projectId) {
        ProjectParametersExample example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameters> projectParameters = projectParametersMapper.selectByExample(example);
        if (projectParameters.isEmpty()) {
            throw new MSException(Translator.get("global_parameters_is_not_exist"));
        }
    }

    public GlobalParamsRequest get(String projectId) {
        ProjectParametersExample example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameters> projectParametersList = projectParametersMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(projectParametersList)) {
            GlobalParamsRequest globalParamsRequest = new GlobalParamsRequest();
            globalParamsRequest.setProjectId(projectId);
            globalParamsRequest.setId(projectParametersList.get(0).getId());
            globalParamsRequest.setGlobalParams(JSON.parseObject(new String(projectParametersList.get(0).getParameters()), GlobalParams.class));
            return globalParamsRequest;
        } else {
            return null;
        }

    }

    private void checkExist(String projectId) {
        ProjectParametersExample example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameters> projectParameters = projectParametersMapper.selectByExample(example);
        if (!projectParameters.isEmpty()) {
            throw new MSException(Translator.get("global_parameters_already_exist"));
        }
    }

    private void checkProjectExist(String projectId) {
        if (projectMapper.selectByPrimaryKey(projectId) == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }
}
