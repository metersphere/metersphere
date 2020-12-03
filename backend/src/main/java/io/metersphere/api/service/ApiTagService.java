package io.metersphere.api.service;

import io.metersphere.api.dto.automation.ApiApiRequest;
import io.metersphere.api.dto.automation.SaveApiTagRequest;
import io.metersphere.base.domain.ApiTag;
import io.metersphere.base.domain.ApiTagExample;
import io.metersphere.base.mapper.ApiTagMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTagService {
    @Resource
    private ApiTagMapper apiTagMapper;

    public List<ApiTag> list(ApiApiRequest request) {
        ApiTagExample example = new ApiTagExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId());
        return apiTagMapper.selectByExample(example);
    }

    public void create(SaveApiTagRequest request) {
        checkNameExist(request);
        final ApiTag apiTag = new ApiTag();
        apiTag.setId(request.getId());
        apiTag.setName(request.getName());
        apiTag.setProjectId(request.getProjectId());
        apiTag.setCreateTime(System.currentTimeMillis());
        apiTag.setUpdateTime(System.currentTimeMillis());
        if (request.getUserId() == null) {
            apiTag.setUserId(SessionUtils.getUserId());
        } else {
            apiTag.setUserId(request.getUserId());
        }
        apiTagMapper.insert(apiTag);
    }

    public void update(SaveApiTagRequest request) {
        checkNameExist(request);
        final ApiTag apiTag = new ApiTag();
        apiTag.setId(request.getId());
        apiTag.setName(request.getName());
        apiTag.setProjectId(request.getProjectId());
        apiTag.setUpdateTime(System.currentTimeMillis());
        apiTag.setUserId(request.getUserId());
        apiTagMapper.updateByPrimaryKeySelective(apiTag);
    }

    public void delete(String id) {
        apiTagMapper.deleteByPrimaryKey(id);
    }

    private void checkNameExist(SaveApiTagRequest request) {
        ApiTagExample example = new ApiTagExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId())
                .andIdNotEqualTo(request.getId());
        if (apiTagMapper.countByExample(example) > 0) {
            MSException.throwException("名称不能重复");
        }
    }
}
