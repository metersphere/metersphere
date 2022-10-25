package io.metersphere.track.service;

import io.metersphere.base.domain.ProjectVersion;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.plan.request.LoadCaseRequest;
import io.metersphere.service.ServiceUtils;
import io.metersphere.xpack.version.service.ProjectVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PerformanceTestCaseService {

    @Resource
    ExtLoadTestMapper extLoadTestMapper;

    public List<LoadTestDTO> getRelevanceLoadList(LoadCaseRequest request) {
        List<LoadTestDTO> loadTestDTOList = extLoadTestMapper.relevanceLoadList(request);
        buildVersionInfo(loadTestDTOList);
        return loadTestDTOList;
    }

    public void buildVersionInfo(List<LoadTestDTO> loadTests) {
        List<String> versionIds = loadTests.stream().map(LoadTestDTO::getVersionId).collect(Collectors.toList());
        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
        Map<String, String> projectVersionMap = projectVersionService.getProjectVersionByIds(versionIds).stream()
                .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
        loadTests.forEach(loadTest -> {
            loadTest.setVersionName(projectVersionMap.get(loadTest.getVersionId()));
        });
    }
}
