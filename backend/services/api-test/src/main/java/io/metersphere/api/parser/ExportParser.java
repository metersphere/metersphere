package io.metersphere.api.parser;

import io.metersphere.api.dto.definition.ApiDefinitionWithBlob;
import io.metersphere.project.domain.Project;

import java.util.List;

public interface ExportParser<T> {
    T parse(List<ApiDefinitionWithBlob> list, Project project) throws Exception;
}
