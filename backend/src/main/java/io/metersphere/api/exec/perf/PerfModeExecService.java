package io.metersphere.api.exec.perf;

import io.metersphere.base.domain.TestPlanLoadCaseWithBLOBs;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PerfModeExecService {
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;

    public void serial(RunTestPlanRequest request) {
        TestPlanLoadCaseWithBLOBs loadCase = testPlanLoadCaseMapper.selectByPrimaryKey(request.getTestPlanLoadId());
        if (loadCase != null) {
            loadCase.setLoadReportId(request.getReportId());
            loadCase.setStatus(TestPlanLoadCaseStatus.run.name());
            request.setId(loadCase.getLoadCaseId());
            try {
                performanceTestService.run(request);
            } catch (Exception e) {
                loadCase.setStatus(TestPlanLoadCaseStatus.error.name());
                MSException.throwException(e);
            }
            //更新关联处的报告
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(loadCase);
        }
    }

    public void parallel(List<RunTestPlanRequest> requests) {
        for (RunTestPlanRequest request : requests) {
            this.serial(request);
        }
    }
}
