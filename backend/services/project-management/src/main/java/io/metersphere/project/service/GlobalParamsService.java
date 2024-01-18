package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.GlobalParams;
import io.metersphere.project.dto.environment.GlobalParamsDTO;
import io.metersphere.project.dto.environment.GlobalParamsRequest;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.domain.ProjectParameter;
import io.metersphere.sdk.domain.ProjectParameterExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.ProjectParameterMapper;
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
    private ProjectParameterMapper projectParameterMapper;
    @Resource
    private ProjectMapper projectMapper;

    public ProjectParameter add(GlobalParamsRequest globalParamsRequest, String userId) {
        ProjectParameter projectParameter = new ProjectParameter();
        projectParameter.setProjectId(globalParamsRequest.getProjectId());
        checkExist(globalParamsRequest.getProjectId());
        checkProjectExist(globalParamsRequest.getProjectId());
        projectParameter.setId(IDGenerator.nextStr());
        projectParameter.setCreateUser(userId);
        projectParameter.setUpdateUser(userId);
        projectParameter.setCreateTime(System.currentTimeMillis());
        projectParameter.setUpdateTime(System.currentTimeMillis());
        projectParameter.setParameters(JSON.toJSONBytes(globalParamsRequest.getGlobalParams()));
        projectParameterMapper.insert(projectParameter);
        globalParamsRequest.setId(projectParameter.getId());
        return projectParameter;
    }

    public ProjectParameter update(GlobalParamsRequest globalParamsRequest, String userId) {
        ProjectParameter projectParameter = new ProjectParameter();
        projectParameter.setProjectId(globalParamsRequest.getProjectId());
        checkDataExist(globalParamsRequest.getProjectId());
        checkProjectExist(globalParamsRequest.getProjectId());
        projectParameter.setId(globalParamsRequest.getId());
        projectParameter.setUpdateUser(userId);
        projectParameter.setUpdateTime(System.currentTimeMillis());
        projectParameter.setParameters(JSON.toJSONBytes(globalParamsRequest.getGlobalParams()));
        projectParameterMapper.updateByPrimaryKeySelective(projectParameter);
        return projectParameter;
    }

    private void checkDataExist(String projectId) {
        ProjectParameterExample example = new ProjectParameterExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameter> projectParameter = projectParameterMapper.selectByExample(example);
        if (projectParameter.isEmpty()) {
            throw new MSException(Translator.get("global_parameters_is_not_exist"));
        }
    }

    public GlobalParamsDTO get(String projectId) {
        ProjectParameterExample example = new ProjectParameterExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameter> projectParameters = projectParameterMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(projectParameters)) {
            GlobalParamsDTO globalParamsDTO = new GlobalParamsDTO();
            globalParamsDTO.setProjectId(projectId);
            globalParamsDTO.setId(projectParameters.getFirst().getId());
            globalParamsDTO.setGlobalParams(JSON.parseObject(new String(projectParameters.getFirst().getParameters()), GlobalParams.class));
            return globalParamsDTO;
        } else {
            return null;
        }

    }

    private void checkExist(String projectId) {
        ProjectParameterExample example = new ProjectParameterExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectParameter> projectParameters = projectParameterMapper.selectByExample(example);
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
            ProjectParameterExample projectParameterExample = new ProjectParameterExample();
            projectParameterExample.createCriteria().andProjectIdEqualTo(projectId);
            List<ProjectParameter> projectParameters = projectParameterMapper.selectByExampleWithBLOBs(projectParameterExample);
            byte[] bytes = new byte[0];
            if (CollectionUtils.isNotEmpty(projectParameters)) {
                GlobalParamsDTO globalParamsDTO = new GlobalParamsDTO();
                globalParamsDTO.setProjectId(projectId);
                globalParamsDTO.setId(projectParameters.getFirst().getId());
                globalParamsDTO.setGlobalParams(JSON.parseObject(new String(projectParameters.getFirst().getParameters()), GlobalParams.class));
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
                ProjectParameterExample projectParameterExample = new ProjectParameterExample();
                projectParameterExample.createCriteria().andProjectIdEqualTo(currentProjectId);
                if (projectParameterMapper.countByExample(projectParameterExample) > 0) {
                    projectParameterMapper.deleteByExample(projectParameterExample);
                }
                ProjectParameter projectParameter = new ProjectParameter();
                projectParameter.setId(IDGenerator.nextStr());
                projectParameter.setProjectId(currentProjectId);
                projectParameter.setCreateUser(userId);
                projectParameter.setUpdateUser(userId);
                projectParameter.setCreateTime(System.currentTimeMillis());
                projectParameter.setUpdateTime(System.currentTimeMillis());
                projectParameter.setParameters(JSON.toJSONBytes(globalParamsDTO.getGlobalParams()));
                projectParameterMapper.insert(projectParameter);
            } catch (Exception e) {
                LogUtils.error("获取文件输入流异常", e);
                throw new RuntimeException("获取文件输入流异常", e);
            }
        }
    }
}
