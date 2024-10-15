package io.metersphere.api.dto.export;

import io.metersphere.api.dto.converter.ApiDefinitionExportDetail;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Data
public class MetersphereApiDefinitionExportResponse extends ApiDefinitionExportResponse {

    private List<ApiDefinitionExportDetail> apiDefinitions = new ArrayList<>();

}
