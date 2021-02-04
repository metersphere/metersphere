package io.metersphere.performance.job;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.LoadTestReportDetailExample;
import io.metersphere.base.domain.LoadTestReportExample;
import io.metersphere.base.mapper.LoadTestReportDetailMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class LoadTestReportDetailCleanJob {
    @Resource
    private LoadTestReportDetailMapper loadTestReportDetailMapper;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    /**
     * 每天处理一次清理任务
     */
    @QuartzScheduled(cron = "0 0 1 * * ?")
    public void cleanCompletedTestDetail() {
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andStatusEqualTo(PerformanceTestStatus.Completed.name());
        List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(example);
        loadTestReports.forEach(report -> {
            // 清理文件
            LoadTestReportDetailExample example2 = new LoadTestReportDetailExample();
            example2.createCriteria().andReportIdEqualTo(report.getId());
            loadTestReportDetailMapper.deleteByExample(example2);
        });
    }
}
