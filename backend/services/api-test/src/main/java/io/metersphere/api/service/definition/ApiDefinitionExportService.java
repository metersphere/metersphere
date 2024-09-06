package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.domain.ApiDefinitionModuleExample;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.export.ApiExportResponse;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.api.MetersphereExportParser;
import io.metersphere.api.parser.api.Swagger3ExportParser;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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


    public ApiExportResponse export(ApiDefinitionBatchExportRequest request, String type, String userId) {
        List<String> ids = this.getBatchExportApiIds(request, request.getProjectId(), userId);
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        List<ApiDefinitionWithBlob> list = this.selectAndSortByIds(ids);
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

    private List<ApiDefinitionWithBlob> selectAndSortByIds(List<String> ids) {
        Map<String, ApiDefinitionWithBlob> apiMap = extApiDefinitionMapper.selectApiDefinitionWithBlob(ids).stream().collect(Collectors.toMap(ApiDefinitionWithBlob::getId, v -> v));
        return ids.stream().map(apiMap::get).toList();
    }

    private List<String> getBatchExportApiIds(ApiDefinitionBatchExportRequest request, String exportType, String userId) {
        List<String> protocols = request.getProtocols();
        if (StringUtils.equalsIgnoreCase(exportType, "swagger")) {
            protocols = List.of(ModuleConstants.NODE_PROTOCOL_HTTP);
        }

        if (request.isSelectAll()) {
            List<String> ids = extApiDefinitionMapper.getIdsBySort(request, request.getProjectId(), protocols, request.getSortString());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
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

    private ApiExportResponse exportMetersphere(ApiDefinitionBatchExportRequest request, List<ApiDefinitionWithBlob> list, Map<String, String> moduleMap) {
        try {
            List<String> apiIds = list.stream().map(ApiDefinitionWithBlob::getId).toList();
            List<ApiTestCaseWithBlob> apiTestCaseWithBlobs = new ArrayList<>();
            List<ApiMockWithBlob> apiMockWithBlobs = new ArrayList<>();
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
