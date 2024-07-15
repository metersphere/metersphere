package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.domain.ApiDefinitionModuleExample;
import io.metersphere.api.dto.definition.ApiDefinitionBatchRequest;
import io.metersphere.api.dto.definition.ApiDefinitionWithBlob;
import io.metersphere.api.dto.export.ApiExportResponse;
import io.metersphere.api.mapper.ApiDefinitionModuleMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.parser.api.Swagger3ExportParser;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.exception.MSException;
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
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ProjectMapper projectMapper;

    public ApiExportResponse export(ApiDefinitionBatchRequest request, String type, String userId) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId(), request.getProtocols(), false, userId);
        if (CollectionUtils.isEmpty(ids)) {
            return new ApiExportResponse();
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
