package io.metersphere.api.service;

import io.metersphere.api.service.debug.ApiDebugService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.invoker.FileAssociationUpdateService;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import org.springframework.stereotype.Service;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:48
 */
@Service
public class ApiFileAssociationUpdateService implements FileAssociationUpdateService {

    /**
     * 处理接口关联的文件被更新
     *
     * @param originFileAssociation 原来的文件ID
     * @param newFileMetadata       最新文件
     */
    @Override
    public void handleUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        String sourceType = originFileAssociation.getSourceType();
        switch (sourceType) {
            case FileAssociationSourceUtil.SOURCE_TYPE_API_DEBUG ->
                    CommonBeanFactory.getBean(ApiDebugService.class).handleFileAssociationUpgrade(originFileAssociation, newFileMetadata);
            case FileAssociationSourceUtil.SOURCE_TYPE_API_DEFINITION ->
                    CommonBeanFactory.getBean(ApiDefinitionService.class).handleFileAssociationUpgrade(originFileAssociation, newFileMetadata);
            case FileAssociationSourceUtil.SOURCE_TYPE_API_TEST_CASE ->
                    CommonBeanFactory.getBean(ApiTestCaseService.class).handleFileAssociationUpgrade(originFileAssociation, newFileMetadata);
            case FileAssociationSourceUtil.SOURCE_TYPE_API_SCENARIO ->
                    CommonBeanFactory.getBean(ApiScenarioService.class).handleFileAssociationUpgrade(originFileAssociation, newFileMetadata);
            default -> {
            }
        }
    }

}
