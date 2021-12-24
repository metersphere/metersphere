package io.metersphere.service;


import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.base.domain.CustomFunction;
import io.metersphere.base.domain.CustomFunctionExample;
import io.metersphere.base.domain.CustomFunctionWithBLOBs;
import io.metersphere.base.mapper.CustomFunctionMapper;
import io.metersphere.base.mapper.ext.ExtCustomFunctionMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.CustomFunctionRequest;
import io.metersphere.dto.MsExecResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFunctionService {


    @Resource
    private CustomFunctionMapper customFunctionMapper;
    @Resource
    private ExtCustomFunctionMapper extCustomFunctionMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;

    public CustomFunctionWithBLOBs save(CustomFunctionRequest request) {
        request.setId(UUID.randomUUID().toString());
        request.setCreateUser(SessionUtils.getUserId());
        request.setProjectId(SessionUtils.getCurrentProjectId());

        checkFuncExist(request);

        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.insert(request);
        return request;
    }

    private void checkFuncExist(CustomFunctionRequest request) {
        String id = request.getId();
        String name = request.getName();
        CustomFunctionExample example = new CustomFunctionExample();
        CustomFunctionExample.Criteria criteria = example
                .createCriteria()
                .andProjectIdEqualTo(request.getProjectId())
                .andNameEqualTo(name);
        if (StringUtils.isNotBlank(id)) {
            criteria.andIdNotEqualTo(id);
        }
        if (customFunctionMapper.countByExample(example) > 0) {
            MSException.throwException("自定义函数名称已存在！");
        }
    }

    public void delete(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        customFunctionMapper.deleteByPrimaryKey(id);
    }

    public List<CustomFunction> query(CustomFunctionRequest request) {
        String projectId = request.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            projectId = SessionUtils.getCurrentProjectId();
            request.setProjectId(projectId);
        }
        return extCustomFunctionMapper.queryAll(request);
    }

    public void update(CustomFunctionRequest request) {
        checkFuncExist(request);
        request.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.updateByPrimaryKeySelective(request);
    }

    public CustomFunctionWithBLOBs copy(String id) {
        CustomFunctionWithBLOBs blob = customFunctionMapper.selectByPrimaryKey(id);
        if (blob == null) {
            MSException.throwException("copy fail, source obj is null.");
        }
        CustomFunctionWithBLOBs copyBlob = new CustomFunctionWithBLOBs();
        BeanUtils.copyBean(copyBlob, blob);
        String copyId = UUID.randomUUID().toString();
        String copyNameId = copyId.substring(0, 3);
        String copyName = blob.getName() + "_" + copyNameId + "_COPY";
        copyBlob.setId(copyId);
        copyBlob.setName(copyName);
        copyBlob.setCreateUser(SessionUtils.getUserId());
        copyBlob.setCreateTime(System.currentTimeMillis());
        copyBlob.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.insert(copyBlob);
        return copyBlob;
    }

    public CustomFunctionWithBLOBs get(String id) {
        if (StringUtils.isBlank(id)) {
            return new CustomFunctionWithBLOBs();
        }
        return customFunctionMapper.selectByPrimaryKey(id);
    }

    public MsExecResponseDTO run(RunDefinitionRequest request) {
        request.setDebug(true);
        return apiDefinitionService.run(request, new LinkedList<>());
    }
}
