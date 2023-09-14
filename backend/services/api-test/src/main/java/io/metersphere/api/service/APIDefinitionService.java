package io.metersphere.api.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ListRequestDTO;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class APIDefinitionService {

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    public ApiDefinitionDTO create(ApiDefinitionDTO request, List<MultipartFile> bodyFiles) {
        return request;
    }

    public List<ApiDefinition> list(@NotNull ListRequestDTO request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        ApiDefinitionExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(request.getProjectId());
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike("%" + request.getName() + "%");
        }
        if (StringUtils.isNotBlank(request.getProtocol())) {
            criteria.andProtocolEqualTo(request.getProtocol());
        }
        return apiDefinitionMapper.selectByExample(example);
    }

    public void batchDelete(List<String> ids) {

    }
}
