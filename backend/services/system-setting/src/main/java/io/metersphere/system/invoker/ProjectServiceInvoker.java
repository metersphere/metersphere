package io.metersphere.system.invoker;

import io.metersphere.sdk.service.CleanupProjectResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectServiceInvoker {
    private final List<CleanupProjectResourceService> cleanupProjectResourceServices;

    @Autowired
    public ProjectServiceInvoker(List<CleanupProjectResourceService> services) {
        this.cleanupProjectResourceServices = services;
    }

    public void invokeServices(String projectId) {
        for (CleanupProjectResourceService service : cleanupProjectResourceServices) {
            service.deleteResources(projectId);
        }
    }
}
