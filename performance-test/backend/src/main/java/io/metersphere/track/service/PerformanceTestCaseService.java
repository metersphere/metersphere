package io.metersphere.track.service;

import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.plan.request.LoadCaseRequest;
import io.metersphere.service.ServiceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PerformanceTestCaseService {

    @Resource
    ExtLoadTestMapper extLoadTestMapper;

    public List<LoadTestDTO> getRelevanceLoadList(LoadCaseRequest request) {
        List<LoadTestDTO> loadTestDTOList = extLoadTestMapper.relevanceLoadList(request);
        ServiceUtils.buildVersionInfo(loadTestDTOList);
        return loadTestDTOList;
    }
}
