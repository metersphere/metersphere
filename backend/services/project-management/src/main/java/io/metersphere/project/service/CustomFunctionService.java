package io.metersphere.project.service;

import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.domain.CustomFunctionExample;
import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.customfunction.request.CustomFunctionPageRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionUpdateRequest;
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

    public List<CustomFunctionDTO> getPage(CustomFunctionPageRequest request) {
        return extCustomFunctionMapper.list(request);
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
            customFunctionDTO.setParams(new String(blob.getParams(), StandardCharsets.UTF_8));
            customFunctionDTO.setScript(new String(blob.getScript(), StandardCharsets.UTF_8));
            customFunctionDTO.setResult(new String(blob.getResult(), StandardCharsets.UTF_8));
        });
    }

    public CustomFunction add(CustomFunctionRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());

        CustomFunction customFunction = new CustomFunction();
        BeanUtils.copyBean(customFunction, request);
        checkAddExist(customFunction);
        customFunction.setId(IDGenerator.nextStr());
        customFunction.setCreateTime(System.currentTimeMillis());
        customFunction.setUpdateTime(System.currentTimeMillis());
        customFunction.setCreateUser(userId);
        customFunction.setUpdateUser(userId);
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            customFunction.setTags(request.getTags());
        }
        customFunctionMapper.insertSelective(customFunction);
        CustomFunctionBlob customFunctionBlob = new CustomFunctionBlob();
        customFunctionBlob.setId(customFunction.getId());
        if(request.getParams() != null) {
            customFunctionBlob.setParams(request.getParams().getBytes());
        }
        if(request.getScript() != null) {
            customFunctionBlob.setScript(request.getScript().getBytes());
        }
        if(request.getResult() != null) {
            customFunctionBlob.setResult(request.getResult().getBytes());
        }
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
        CustomFunctionBlob customFunctionBlob = new CustomFunctionBlob();
        customFunctionBlob.setId(customFunction.getId());
        if(request.getParams() != null) {
            customFunctionBlob.setParams(request.getParams().getBytes());
        }
        if(request.getScript() != null) {
            customFunctionBlob.setScript(request.getScript().getBytes());
        }
        if(request.getResult() != null) {
            customFunctionBlob.setResult(request.getResult().getBytes());
        }
        customFunctionBlobMapper.updateByPrimaryKeySelective(customFunctionBlob);
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

}
