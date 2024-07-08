package io.metersphere.api.service.definition;

import io.metersphere.api.dto.definition.ApiDefinitionBatchRequest;
import io.metersphere.api.dto.definition.ApiDefinitionWithBlob;
import io.metersphere.api.dto.export.ApiExportResponse;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.parser.api.Swagger3ExportParser;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.exception.MSException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


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
    private ProjectMapper projectMapper;

    public ApiExportResponse export(ApiDefinitionBatchRequest request, String type, String userId) {
        List<String> ids = apiDefinitionService.getBatchApiIds(request, request.getProjectId(), request.getProtocols(), false, userId);
        List<ApiDefinitionWithBlob> list = extApiDefinitionMapper.selectApiDefinitionWithBlob(ids);
        switch (type) {
            case "swagger":
                return exportSwagger(request, list);
            default:
                return new ApiExportResponse();
        }

    }

    private ApiExportResponse exportSwagger(ApiDefinitionBatchRequest request, List<ApiDefinitionWithBlob> list) {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        Swagger3ExportParser swagger3Parser = new Swagger3ExportParser();
        try {
            return swagger3Parser.parse(list, project);
        } catch (Exception e) {
            throw new MSException(e);
        }
    }
}
