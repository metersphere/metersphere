package io.metersphere.api.service;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ListRequestDTO;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.system.domain.OperationLog;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ApiDefinitionService {

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

        if (StringUtils.isNotEmpty(request.getPath())) {
            criteria.andPathEqualTo(request.getPath());
        }

        if (StringUtils.isNotEmpty(request.getMethod())) {
            criteria.andMethodEqualTo(request.getMethod());
        }
        if (StringUtils.isNotBlank(request.getProtocol())) {
            criteria.andProtocolEqualTo(request.getProtocol());
        }
        return apiDefinitionMapper.selectByExample(example);
    }

    public void batchDelete(List<String> ids) {

    }

    /**
     * 批量操作日志，由切面触发
     *
     * @param ids
     */
    public List<OperationLog> getLogs(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<OperationLog> logs = new ArrayList<>();
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<ApiDefinition> definitions = apiDefinitionMapper.selectByExample(example);
        definitions.forEach(definition -> {
            OperationLog log = new OperationLog();
            log.setId(UUID.randomUUID().toString());
            log.setCreateUser(definition.getUpdateUser());
            log.setProjectId(definition.getProjectId());
            log.setType(OperationLogType.DELETE.name());
            log.setModule(OperationLogModule.API_DEFINITION);
            log.setSourceId(definition.getId());
            log.setDetails(definition.getName());
            logs.add(log);
        });
        return logs;
    }
}
