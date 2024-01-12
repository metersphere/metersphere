package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.GlobalParams;
import io.metersphere.project.dto.environment.GlobalParamsDTO;
import io.metersphere.project.dto.environment.GlobalParamsRequest;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.domain.ProjectParameters;
import io.metersphere.sdk.domain.ProjectParametersExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.ProjectParametersMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;


@Service
@Transactional
public class GlobalParamsService {

    @Resource
    private ProjectParametersMapper projectParametersMapper;
    @Resource
    private ProjectMapper projectMapper;

    public ProjectParameters add(GlobalParamsRequest globalParamsRequest, String userId) {
        ProjectParameters projectParameters = new ProjectParameters();
        projectParameters.setProjectId(globalParamsRequest.getProjectId());
        checkExist(globalParamsRequest.getProjectId());
        checkProjectExist(globalParamsRequest.getProjectId());
        projectParameters.setId(IDGenerator.nextStr());
        projectParameters.setCreateUser(userId);
        projectParameters.setUpdateUser(userId);
        projectParameters.setCreateTime(System.currentTimeMillis());
        projectParameters.setUpdateTime(System.currentTimeMillis());
        projectParameters.setParameters(JSON.toJSONBytes(globalParamsRequest.getGlobalParams()));
        projectParametersMapper.insert(projectParameters);
        globalParamsRequest.setId(projectParameters.getId());
        return projectParameters;
    }

    public ProjectParameters update(GlobalParamsRequest globalParamsRequest, String userId) {
        ProjectParameters projectParameters = new ProjectParameters();
        projectParameters.setProjectId(globalParamsRequest.getProjectId());
        checkDataExist(globalParamsRequest.getProjectId());
        checkProjectExist(globalParamsRequest.getProjectId());
        projectParameters.setId(globalParamsRequest.getId());
        projectParameters.setUpdateUser(userId);
        projectParameters.setUpdateTime(System.currentTimeMillis());
        projectParameters.setParameters(JSON.toJSONBytes(globalParamsRequest.getGlobalParams()));
        projectParametersMapper.updateByPrimaryKeySelective(projectParameters);
        return projectParameters;
    }

    private void checkDataExist(String projectId) {
        ProjectParametersExample example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameters> projectParameters = projectParametersMapper.selectByExample(example);
        if (projectParameters.isEmpty()) {
            throw new MSException(Translator.get("global_parameters_is_not_exist"));
        }
    }

    public GlobalParamsDTO get(String projectId) {
        ProjectParametersExample example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameters> projectParametersList = projectParametersMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(projectParametersList)) {
            GlobalParamsDTO globalParamsDTO = new GlobalParamsDTO();
            globalParamsDTO.setProjectId(projectId);
            globalParamsDTO.setId(projectParametersList.get(0).getId());
            globalParamsDTO.setGlobalParams(JSON.parseObject(new String(projectParametersList.get(0).getParameters()), GlobalParams.class));
            return globalParamsDTO;
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

    public ResponseEntity<byte[]> exportJson(String projectId) {
        try {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            //查询全局参数
            ProjectParametersExample projectParametersExample = new ProjectParametersExample();
            projectParametersExample.createCriteria().andProjectIdEqualTo(projectId);
            List<ProjectParameters> projectParameters = projectParametersMapper.selectByExampleWithBLOBs(projectParametersExample);
            byte[] bytes = new byte[0];
            if (CollectionUtils.isNotEmpty(projectParameters)) {
                GlobalParamsDTO globalParamsDTO = new GlobalParamsDTO();
                globalParamsDTO.setProjectId(projectId);
                globalParamsDTO.setId(projectParameters.get(0).getId());
                globalParamsDTO.setGlobalParams(JSON.parseObject(new String(projectParameters.get(0).getParameters()), GlobalParams.class));
                bytes = JSON.toJSONString(globalParamsDTO).getBytes();
            } else {
                throw new MSException(Translator.get("global_parameters_is_not_exist"));
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + StringUtils.join(project.getName(), "_", Translator.get("global_params")))
                    .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.status(509).body(e.getMessage().getBytes());
        }
    }

    public void importData(MultipartFile file, String userId, String currentProjectId) {
        if (file != null) {
            try {
                InputStream inputStream = file.getInputStream();
                // 读取文件内容
                String content = new String(inputStream.readAllBytes());
                inputStream.close();
                //参数是一个对象
                GlobalParamsDTO globalParamsDTO = JSON.parseObject(content, GlobalParamsDTO.class);
                ProjectParametersExample projectParametersExample = new ProjectParametersExample();
                projectParametersExample.createCriteria().andProjectIdEqualTo(currentProjectId);
                if (projectParametersMapper.countByExample(projectParametersExample) > 0) {
                    projectParametersMapper.deleteByExample(projectParametersExample);
                }
                ProjectParameters projectParameters = new ProjectParameters();
                projectParameters.setId(IDGenerator.nextStr());
                projectParameters.setProjectId(currentProjectId);
                projectParameters.setCreateUser(userId);
                projectParameters.setUpdateUser(userId);
                projectParameters.setCreateTime(System.currentTimeMillis());
                projectParameters.setUpdateTime(System.currentTimeMillis());
                projectParameters.setParameters(JSON.toJSONBytes(globalParamsDTO.getGlobalParams()));
                projectParametersMapper.insert(projectParameters);
            } catch (Exception e) {
                LogUtils.error("获取文件输入流异常", e);
                throw new RuntimeException("获取文件输入流异常", e);
            }
        }
    }
}
