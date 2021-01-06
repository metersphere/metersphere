package io.metersphere.api.parse;

import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.base.domain.ApiModule;

import java.util.List;

public abstract class SwaggerAbstractParser extends ApiImportAbstractParser {

    protected void buildModule(ApiModule parentModule, ApiDefinitionResult apiDefinition, List<String> tags, boolean isSaved) {
        if (tags != null) {
            tags.forEach(tag -> {
                ApiModule module = buildModule(parentModule, tag, isSaved);
                apiDefinition.setModuleId(module.getId());
            });
        }
    }

}
