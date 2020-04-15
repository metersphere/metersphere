package io.metersphere;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import io.metersphere.base.domain.LoadTestReportDetail;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportDetailMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.report.base.Metric;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.Reader;
import java.io.StringReader;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportContentTests {
    @Resource
    private LoadTestReportDetailMapper loadTestReportDetailMapper;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    @Test
    public void test1() {
        String reportId = "ba972086-7d74-4f58-99b0-9c014114fd99";
        LoadTestReportDetail loadTestReportDetail = loadTestReportDetailMapper.selectByPrimaryKey(reportId);
        LoadTestReportWithBLOBs loadTestReportWithBLOBs = loadTestReportMapper.selectByPrimaryKey(reportId);

        HeaderColumnNameMappingStrategy<Metric> ms = new HeaderColumnNameMappingStrategy<>();
        ms.setType(Metric.class);
        try (Reader reader = new StringReader(loadTestReportDetail.getContent())) {
            CsvToBean<Metric> cb = new CsvToBeanBuilder<Metric>(reader)
                    .withType(Metric.class)
                    .withSkipLines(0)
                    .withMappingStrategy(ms)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            System.out.println(cb.parse().size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try (Reader reader = new StringReader(loadTestReportWithBLOBs.getContent())) {
            CsvToBean<Metric> cb = new CsvToBeanBuilder<Metric>(reader)
                    .withType(Metric.class)
                    .withSkipLines(0)
                    .withMappingStrategy(ms)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            System.out.println(cb.parse().size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
