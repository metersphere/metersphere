package io.metersphere.project.service;

import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.domain.CustomFunctionBlobExample;
import io.metersphere.project.domain.CustomFunctionExample;
import io.metersphere.project.dto.ProjectUserDTO;
import io.metersphere.project.dto.customfunction.CustomFuncColumnsOptionDTO;
import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.customfunction.request.CustomFunctionPageRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionUpdateRequest;
import io.metersphere.project.enums.CustomFunctionStatus;
import io.metersphere.project.enums.result.ProjectResultCode;
import io.metersphere.project.mapper.CustomFunctionBlobMapper;
import io.metersphere.project.mapper.CustomFunctionMapper;
import io.metersphere.project.mapper.ExtCustomFunctionMapper;
import io.metersphere.project.request.ProjectMemberRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.OperationHistory;
import io.metersphere.system.domain.OperationHistoryExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OperationHistoryMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: LAN
 * @date: 2024/1/9 19:38
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFunctionService {

    @Resource
    ProjectMemberService projectMemberService;
    
    @Resource
    CustomFunctionMapper customFunctionMapper;

    @Resource
    CustomFunctionBlobMapper customFunctionBlobMapper;

    @Resource
    ExtCustomFunctionMapper extCustomFunctionMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    OperationHistoryMapper operationHistoryMapper;

    public List<CustomFunctionDTO> getPage(CustomFunctionPageRequest request) {
        List<CustomFunctionDTO> list = extCustomFunctionMapper.list(request);
        if (!CollectionUtils.isEmpty(list)) {
            processCustomFunction(list);
        }
        return list;
    }

    private void processCustomFunction(List<CustomFunctionDTO> list) {
        list.forEach(item -> handleCustomFunctionBlob(item.getId(), item));
    }

    public CustomFunctionDTO getWithCheck(String id) {
        CustomFunction customFunction = checkCustomFunction(id);
        CustomFunctionDTO customFunctionDTO = new CustomFunctionDTO();
        handleCustomFunctionBlob(id, customFunctionDTO);
        BeanUtils.copyBean(customFunctionDTO, customFunction);
        return customFunctionDTO;
    }

    public CustomFunctionDTO get(String id) {
        CustomFunction customFunction = customFunctionMapper.selectByPrimaryKey(id);
        if (customFunction == null) {
            return null;
        }
        CustomFunctionDTO customFunctionDTO = new CustomFunctionDTO();
        handleCustomFunctionBlob(id, customFunctionDTO);
        BeanUtils.copyBean(customFunctionDTO, customFunction);
        return customFunctionDTO;
    }

    public void handleCustomFunctionBlob(String id, CustomFunctionDTO customFunctionDTO) {
        Optional<CustomFunctionBlob> customFunctionBlobOptional = Optional.ofNullable(customFunctionBlobMapper.selectByPrimaryKey(id));
        customFunctionBlobOptional.ifPresent(blob -> {
            customFunctionDTO.setParams(toStringOrDefault(blob.getParams()));
            customFunctionDTO.setScript(toStringOrDefault(blob.getScript()));
            customFunctionDTO.setResult(toStringOrDefault(blob.getResult()));
        });
    }

    private String toStringOrDefault(byte[] bytes) {
        return (bytes != null) ? new String(bytes, StandardCharsets.UTF_8) : null;
    }

    public CustomFunction add(CustomFunctionRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());

        CustomFunction customFunction = new CustomFunction();
        BeanUtils.copyBean(customFunction, request);
        checkAddExist(customFunction);
        customFunction.setId(IDGenerator.nextStr());
        customFunction.setStatus(request.getStatus() != null ? request.getStatus() : CustomFunctionStatus.DRAFT.toString());
        customFunction.setCreateTime(System.currentTimeMillis());
        customFunction.setUpdateTime(System.currentTimeMillis());
        customFunction.setCreateUser(userId);
        customFunction.setUpdateUser(userId);
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            customFunction.setTags(request.getTags());
        }
        customFunctionMapper.insertSelective(customFunction);
        CustomFunctionBlob customFunctionBlob = createCustomFunctionBlob(customFunction, request.getParams(), request.getScript(), request.getResult());
        customFunctionBlobMapper.insertSelective(customFunctionBlob);

        return customFunction;
    }

    public void update(CustomFunctionUpdateRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());

        CustomFunction customFunction = new CustomFunction();
        BeanUtils.copyBean(customFunction, request);
        checkUpdateExist(customFunction);
        customFunction.setUpdateTime(System.currentTimeMillis());
        customFunction.setUpdateUser(userId);
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            customFunction.setTags(request.getTags());
        }
        customFunctionMapper.updateByPrimaryKeySelective(customFunction);
        CustomFunctionBlob customFunctionBlob = createCustomFunctionBlob(customFunction, request.getParams(), request.getScript(), request.getResult());
        customFunctionBlobMapper.updateByPrimaryKeySelective(customFunctionBlob);
    }

    private CustomFunctionBlob createCustomFunctionBlob(CustomFunction customFunction, String params, String script, String result) {
        CustomFunctionBlob customFunctionBlob = new CustomFunctionBlob();
        customFunctionBlob.setId(customFunction.getId());
        customFunctionBlob.setParams(params != null ? params.getBytes() : null);
        customFunctionBlob.setScript(script != null ? script.getBytes() : null);
        customFunctionBlob.setResult(result != null ? result.getBytes() : null);
        return customFunctionBlob;
    }

    public void updateStatus(CustomFunctionUpdateRequest request, String userId) {
        checkCustomFunction(request.getId());
        CustomFunction update = new CustomFunction();
        update.setId(request.getId());
        update.setStatus(request.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setUpdateUser(userId);
        customFunctionMapper.updateByPrimaryKeySelective(update);
    }

    public void delete(String id) {
        checkCustomFunction(id);
        customFunctionMapper.deleteByPrimaryKey(id);
        customFunctionBlobMapper.deleteByPrimaryKey(id);
    }

    /**
     * 公共脚本变更历史分页列表
     * @param request 请求参数
     * @return 变更历史集合
     */
    public List<OperationHistoryDTO> list(OperationHistoryRequest request) {
        OperationHistoryExample example = new OperationHistoryExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andModuleIn(List.of(OperationLogModule.PROJECT_MANAGEMENT_COMMON_SCRIPT))
                .andSourceIdEqualTo(request.getSourceId());
        List<OperationHistory> history = operationHistoryMapper.selectByExample(example);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(history)) {
            return List.of();
        }
        List<String> userIds = history.stream().map(OperationHistory::getCreateUser).toList();
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        Map<String, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        return history.stream().map(h -> {
            OperationHistoryDTO dto = new OperationHistoryDTO();
            BeanUtils.copyBean(dto, h);
            dto.setCreateUserName(userMap.get(h.getCreateUser()) == null ? h.getCreateUser() : userMap.get(h.getCreateUser()));
            if (StringUtils.equals(dto.getType(), OperationLogType.ADD.name())) {
                dto.setType(Translator.get("add"));
            } else if (StringUtils.equals(dto.getType(), OperationLogType.UPDATE.name())) {
                dto.setType(Translator.get("update"));
            } else if (StringUtils.equals(dto.getType(), OperationLogType.DELETE.name())) {
                dto.setType(Translator.get("delete"));
            }
            return dto;
        }).toList();
    }

    private CustomFunction checkCustomFunction(String id) {
        return ServiceUtils.checkResourceExist(customFunctionMapper.selectByPrimaryKey(id), "resource_not_exist");
    }

    private void checkAddExist(CustomFunction customFunction) {
        CustomFunctionExample example = new CustomFunctionExample();
        example.createCriteria()
                .andProjectIdEqualTo(customFunction.getProjectId())
                .andNameEqualTo(customFunction.getName());
        if (customFunctionMapper.countByExample(example) > 0) {
            throw new MSException(ProjectResultCode.CUSTOM_FUNCTION_ALREADY_EXIST);
        }
    }

    private void checkUpdateExist(CustomFunction customFunction) {
        CustomFunctionExample example = new CustomFunctionExample();
        example.createCriteria()
                .andProjectIdEqualTo(customFunction.getProjectId())
                .andIdNotEqualTo(customFunction.getId())
                .andNameEqualTo(customFunction.getName());
        if (customFunctionMapper.countByExample(example) > 0) {
            throw new MSException(ProjectResultCode.CUSTOM_FUNCTION_ALREADY_EXIST);
        }
    }

    public List<CustomFunctionBlob> getBlobByIds(List<String> commonScriptIds) {
        if (CollectionUtils.isEmpty(commonScriptIds)) {
            return List.of();
        }
        CustomFunctionBlobExample example = new CustomFunctionBlobExample();
        example.createCriteria()
                .andIdIn(commonScriptIds);
       return customFunctionBlobMapper.selectByExampleWithBLOBs(example);
    }

    public List<CustomFunction> getByIds(List<String> commonScriptIds) {
        if (CollectionUtils.isEmpty(commonScriptIds)) {
            return List.of();
        }
        CustomFunctionExample example = new CustomFunctionExample();
        example.createCriteria()
                .andIdIn(commonScriptIds);
        return customFunctionMapper.selectByExample(example);
    }


    public CustomFuncColumnsOptionDTO getColumnsOption(String projectId) {
        ProjectMemberRequest request = new ProjectMemberRequest();
        request.setProjectId(projectId);
        List<ProjectUserDTO> projectMembers = projectMemberService.listMember(request);
        List<SelectOption> selectOptions = projectMembers.stream().map(user -> {
            SelectOption option = new SelectOption();
            option.setText(user.getName());
            option.setValue(user.getId());
            return option;
        }).toList();
        return new CustomFuncColumnsOptionDTO(selectOptions);
    }
}
