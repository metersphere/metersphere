package io.metersphere.api.parse;

import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;

import java.util.List;

public abstract class SwaggerAbstractParser extends ApiImportAbstractParser {

    protected void buildModule(ApiModule parentModule, ApiDefinitionWithBLOBs apiDefinition, List<String> tags) {
        if (tags != null) {
            tags.forEach(tag -> {
                ApiModule module = buildModule(parentModule, tag);
                apiDefinition.setModuleId(module.getId());
            });
        }
    }

}
