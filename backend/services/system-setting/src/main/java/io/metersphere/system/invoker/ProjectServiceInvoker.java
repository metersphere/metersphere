package io.metersphere.system.invoker;

import io.metersphere.system.service.CleanupProjectResourceService;
import io.metersphere.system.service.CreateProjectResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectServiceInvoker {
    private final List<CleanupProjectResourceService> cleanupProjectResourceServices;

    private final List<CreateProjectResourceService> createProjectResourceServices;


    @Autowired
    public ProjectServiceInvoker(List<CleanupProjectResourceService> services, List<CreateProjectResourceService> createProjectResourceServices) {
        this.cleanupProjectResourceServices = services;
        this.createProjectResourceServices = createProjectResourceServices;
    }

    public void invokeServices(String projectId) {
        for (CleanupProjectResourceService service : cleanupProjectResourceServices) {
            service.deleteResources(projectId);
        }
    }

    public void invokeCreateServices(String projectId) {
        for (CreateProjectResourceService service : createProjectResourceServices) {
            service.createResources(projectId);
        }
    }
}
