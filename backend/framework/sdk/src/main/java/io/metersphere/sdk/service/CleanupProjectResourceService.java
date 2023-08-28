package io.metersphere.sdk.service;

/**
 * 清理项目资源
 */
public interface CleanupProjectResourceService {
    void deleteResources(String projectId);


    /**
     * 清理报告资源
     * @param projectId
     */
    void cleanReportResources(String projectId);
}
