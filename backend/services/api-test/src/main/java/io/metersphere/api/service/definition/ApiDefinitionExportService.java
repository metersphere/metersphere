package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiDefinitionBatchRequest;
import io.metersphere.api.dto.definition.ApiDefinitionWithBlob;
import io.metersphere.api.dto.definition.ApiMockWithBlob;
import io.metersphere.api.dto.definition.ApiTestCaseWithBlob;
import io.metersphere.api.dto.export.ApiExportResponse;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.api.MetersphereExportParser;
import io.metersphere.api.parser.api.Swagger3ExportParser;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.utils.CustomFieldUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiDefinitionMockMapper extApiDefinitionMockMapper;
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
        return switch (type.toLowerCase()) {
            case "swagger" -> exportSwagger(request, list, moduleMap);
            case "metersphere" -> exportMetersphere(request, list, moduleMap);
            default -> new ApiExportResponse();
        };
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

    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;

    private ApiExportResponse exportMetersphere(ApiDefinitionBatchRequest request, List<ApiDefinitionWithBlob> list, Map<String, String> moduleMap) {
        try {
            List<String> apiIds = list.stream().map(ApiDefinitionWithBlob::getId).toList();
            List<ApiTestCaseWithBlob> apiTestCaseWithBlobs = new ArrayList<>();
            List<ApiMockWithBlob> apiMockWithBlobs = new ArrayList<>();
            List<ApiTestCaseBlob> apiTestCaseBlobs = new ArrayList<>();
            if (request.isExportApiCase()) {
                apiTestCaseWithBlobs = extApiTestCaseMapper.selectAllDetailByApiIds(apiIds);
            }
            if (request.isExportApiMock()) {
                apiMockWithBlobs = extApiDefinitionMockMapper.selectAllDetailByApiIds(apiIds);
            }
            return new MetersphereExportParser().parse(list, apiTestCaseWithBlobs, apiMockWithBlobs, moduleMap);
        } catch (Exception e) {
            throw new MSException(e);
        }
    }
}
