package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.DeleteAPIReportRequest;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.base.domain.ApiTestWithBLOBs;
import io.metersphere.base.mapper.ApiTestReportMapper;
import io.metersphere.base.mapper.ext.ExtApiTestReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class APIReportService {

    @Resource
    private APITestService apiTestService;
    @Resource
    private ApiTestReportMapper apiTestReportMapper;
    @Resource
    private ExtApiTestReportMapper extApiTestReportMapper;

    public List<APIReportResult> list(QueryAPIReportRequest request) {
        return extApiTestReportMapper.list(request);
    }

    public List<APIReportResult> recentTest(QueryAPIReportRequest request) {
        request.setRecent(true);
        return extApiTestReportMapper.list(request);
    }

    public APIReportResult get(String reportId) {
        return extApiTestReportMapper.get(reportId);
    }

    public List<APIReportResult> listByTestId(String testId) {
        return extApiTestReportMapper.listByTestId(testId);
    }

    public void delete(DeleteAPIReportRequest request) {
        apiTestReportMapper.deleteByPrimaryKey(request.getId());
    }

    public void save(TestResult result) {
        ApiTestWithBLOBs test = apiTestService.get(result.getId());
        ApiTestReport report = new ApiTestReport();
        report.setId(UUID.randomUUID().toString());
        report.setTestId(result.getId());
        report.setName(test.getName());
        report.setDescription(test.getDescription());
        report.setContent(JSONObject.toJSONString(result));
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(APITestStatus.Completed.name());
        apiTestReportMapper.insert(report);
    }
}
