package io.metersphere.api.jmeter;

import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.metadata.service.FileMetadataService;
import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NewDriverManager {
    public static List<String> getJars(List<String> projectIds) {
        FileMetadataService service = CommonBeanFactory.getBean(FileMetadataService.class);
        if (service!=null) {
            return service.getJar(projectIds);
        } else {
            return new LinkedList<>();
        }
    }

    public static void loadJar(RunDefinitionRequest request) {
        // 加载自定义JAR
        MsTestPlan testPlan = (MsTestPlan) request.getTestElement();
        List<String> projectIds = new ArrayList<>();
        projectIds.add(request.getProjectId());
        if(MapUtils.isNotEmpty(request.getEnvironmentMap())){
            request.getEnvironmentMap().forEach((k,v) ->{
                projectIds.add(k);
            });
        }
        testPlan.setJarPaths(getJars(projectIds));
    }
}