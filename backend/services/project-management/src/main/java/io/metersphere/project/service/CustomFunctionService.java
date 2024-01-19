package io.metersphere.project.service;

import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.domain.CustomFunctionExample;
import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.customfunction.request.CustomFunctionPageRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionUpdateRequest;
import io.metersphere.project.enums.CustomFunctionStatus;
import io.metersphere.project.enums.result.ProjectResultCode;
import io.metersphere.project.mapper.CustomFunctionBlobMapper;
import io.metersphere.project.mapper.CustomFunctionMapper;
import io.metersphere.project.mapper.ExtCustomFunctionMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * @author: LAN
 * @date: 2024/1/9 19:38
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFunctionService {

    @Resource
    CustomFunctionMapper customFunctionMapper;

    @Resource
    CustomFunctionBlobMapper customFunctionBlobMapper;

    @Resource
    ExtCustomFunctionMapper extCustomFunctionMapper;

    /**
     * 使用 Autowired
     * 从接口模块注入实现类
     */
    @Autowired
    CustomFunctionRunService customFunctionRunService;

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

    public CustomFunctionDTO get(String id) {
        CustomFunction customFunction = checkCustomFunction(id);
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

    private CustomFunction checkCustomFunction(String id) {
        return ServiceUtils.checkResourceExist(customFunctionMapper.selectByPrimaryKey(id), "resource_not_exist");
    }

    private void checkAddExist(CustomFunction customFunction) {
        CustomFunctionExample example = new CustomFunctionExample();
        example.createCriteria()
                .andNameEqualTo(customFunction.getName());
        if (customFunctionMapper.countByExample(example) > 0) {
            throw new MSException(ProjectResultCode.CUSTOM_FUNCTION_ALREADY_EXIST);
        }
    }

    private void checkUpdateExist(CustomFunction customFunction) {
        CustomFunctionExample example = new CustomFunctionExample();
        example.createCriteria()
                .andIdNotEqualTo(customFunction.getId())
                .andNameEqualTo(customFunction.getName());
        if (customFunctionMapper.countByExample(example) > 0) {
            throw new MSException(ProjectResultCode.CUSTOM_FUNCTION_ALREADY_EXIST);
        }
    }

    public String run(CustomFunctionRunRequest runRequest) {
        return customFunctionRunService.run(runRequest);
    }
}
