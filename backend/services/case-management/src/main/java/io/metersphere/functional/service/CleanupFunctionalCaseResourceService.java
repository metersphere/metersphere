package io.metersphere.functional.service;

import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author wx
 */
@Component
public class CleanupFunctionalCaseResourceService implements CleanupProjectResourceService {

    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Resource
    private DeleteFunctionalCaseService deleteFunctionalCaseService;


    @Override
    public void deleteResources(String projectId) {
        List<String> ids = extFunctionalCaseMapper.getFunctionalCaseIds(projectId);
        if (CollectionUtils.isNotEmpty(ids)) {
            deleteFunctionalCaseService.deleteFunctionalCaseResource(ids, projectId);
        }
    }

    @Override
    public void cleanReportResources(String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关报告资源");
    }
}
