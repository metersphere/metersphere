package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.domain.ApiDefinitionModuleExample;
import io.metersphere.api.dto.definition.ApiDefinitionBatchRequest;
import io.metersphere.api.dto.definition.ApiDefinitionWithBlob;
import io.metersphere.api.dto.export.ApiExportResponse;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiDefinitionModuleMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.parser.api.Swagger3ExportParser;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.utils.CustomFieldUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author wx
 */
@Service
public class ApiDefinitionExportService {


    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    public ApiExportResponse export(ApiDefinitionBatchRequest request, String type, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), List.of(ModuleConstants.NODE_PROTOCOL_HTTP), false, userId);
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        List<ApiDefinitionWithBlob> list = extApiDefinitionMapper.selectApiDefinitionWithBlob(ids);
        List<String> moduleIds = list.stream().map(ApiDefinitionWithBlob::getModuleId).toList();
        ApiDefinitionModuleExample example = new ApiDefinitionModuleExample();
        example.createCriteria().andIdIn(moduleIds);
        List<ApiDefinitionModule> definitionModules = apiDefinitionModuleMapper.selectByExample(example);
        Map<String, String> moduleMap = definitionModules.stream().collect(Collectors.toMap(ApiDefinitionModule::getId, ApiDefinitionModule::getName));
        switch (type) {
            case "swagger":
                return exportSwagger(request, list, moduleMap);
            default:
                return new ApiExportResponse();
        }

    }

    private List<String> getBatchApiIds(ApiDefinitionBatchRequest request, String projectId, List<String> protocols, boolean deleted, String userId) {
        if (request.isSelectAll()) {
            CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request.getCondition(), userId);
            List<String> ids = extApiDefinitionMapper.getIds(request, projectId, protocols, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            ApiDefinitionExample definitionExample = new ApiDefinitionExample();
            definitionExample.createCriteria().andIdIn(request.getSelectIds()).andProtocolIn(protocols).andDeletedEqualTo(deleted);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(definitionExample);
            return apiDefinitions.stream().map(ApiDefinition::getId).toList();
        }
    }

    private ApiExportResponse exportSwagger(ApiDefinitionBatchRequest request, List<ApiDefinitionWithBlob> list, Map<String, String> moduleMap) {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        Swagger3ExportParser swagger3Parser = new Swagger3ExportParser();
        try {
            return swagger3Parser.parse(list, project, moduleMap);
        } catch (Exception e) {
            throw new MSException(e);
        }
    }
}
