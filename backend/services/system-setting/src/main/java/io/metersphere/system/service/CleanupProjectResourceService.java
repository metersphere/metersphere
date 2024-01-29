package io.metersphere.system.service;

/**
 * 清理项目资源
 */
public interface CleanupProjectResourceService {

    void deleteResources(String projectId);

}
