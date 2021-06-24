package io.metersphere.api.dto.definition.parse;

import io.metersphere.api.parse.ApiImportAbstractParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

public abstract class SwaggerAbstractParser extends ApiImportAbstractParser<ApiDefinitionImport> {

    protected void buildModule(ApiModule parentModule, ApiDefinitionWithBLOBs apiDefinition,
                               List<String> tags, String selectModulePath) {
        if (CollectionUtils.isEmpty(tags)) {
            apiDefinition.setModuleId(parentModule.getId());
            apiDefinition.setModulePath(selectModulePath);
        } else {
            tags.forEach(tag -> {
                ApiModule module = ApiDefinitionImportUtil.buildModule(parentModule, tag, this.projectId);
                apiDefinition.setModuleId(module.getId());
                if (StringUtils.isNotBlank(selectModulePath)) {
                    apiDefinition.setModulePath(selectModulePath + "/" + tag);
                } else {
                    apiDefinition.setModulePath("/" + tag);
                }
            });
        }
    }

}
