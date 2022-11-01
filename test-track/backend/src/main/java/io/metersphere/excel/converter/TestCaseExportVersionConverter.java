package io.metersphere.excel.converter;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectVersionDTO;
import io.metersphere.dto.TestCaseDTO;
import io.metersphere.xpack.version.service.ProjectVersionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCaseExportVersionConverter implements TestCaseExportConverter {

    private Map<String, String> versionMap = new HashMap<>();

    public TestCaseExportVersionConverter() {
        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
        if (projectVersionService == null) {
            return;
        }
        List<ProjectVersionDTO> projectVersions = projectVersionService.getProjectVersions(SessionUtils.getCurrentProjectId());
        projectVersions.forEach(i -> versionMap.put(i.getId(), i.getName()));
    }

    @Override
    public String parse(TestCaseDTO testCase) {
        return getFromMapOfNullable(versionMap, testCase.getVersionId());
    }
}
