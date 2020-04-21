package io.metersphere;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.processor.AbstractSummaryConsumer;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.Top5ErrorsBySamplerConsumer;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.StringTokenizer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DashboardConsumerTest {
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    private AbstractSummaryConsumer<?> summaryConsumer;
    // data array can't be initialized in the init()
    private static String[] data = {"1527089951383", "0", "Read-compute", "200", "OK", "setupRegion 1-1", "true", "", "492", "0", "1", "1",
            "null", "0", "0", "0"};
    private SampleMetadata sampleMetaData = createTestMetaData();

    @Before
    public void init() {
        summaryConsumer = new Top5ErrorsBySamplerConsumer();
//        timeGraphConsumer.setTitle("graph title");

        JMeterUtils.loadJMeterProperties("jmeter.properties"); // 这个路径不存在
    }

    @Test
    public void test() {
        SampleContext sampleContext = new SampleContext();
        sampleContext.setWorkingDirectory(FileUtils.getTempDirectory());

        summaryConsumer.setSampleContext(sampleContext);
        Sample sample = new Sample(0, sampleMetaData, data);
        summaryConsumer.startConsuming();
        summaryConsumer.consume(sample, 0);
        summaryConsumer.stopConsuming();

        System.out.println(sampleContext.getData());
    }

    @Test
    public void test2() {
        int row = 0;
        SampleContext sampleContext = new SampleContext();
//        sampleContext.setWorkingDirectory(new File("/tmp/test_report/"));
        summaryConsumer.setSampleContext(sampleContext);

        summaryConsumer.startConsuming();
        LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey("7fa4dd2d-d42a-46de-92bf-10feec4c6ccf");
        String content = report.getContent();
        StringTokenizer tokenizer = new StringTokenizer(content, "\n");
        // 去掉第一行
        tokenizer.nextToken();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            String[] data = line.split(",", -1);
            Sample sample = new Sample(row++, sampleMetaData, data);
            summaryConsumer.consume(sample, 0);
        }
        summaryConsumer.stopConsuming();

        System.out.println("+++++++++" + sampleContext.getData());
    }


    // Create a static SampleMetadataObject
    private SampleMetadata createTestMetaData() {
        String columnsString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";
        columnsString = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

        String[] columns = new String[17];
        int lastComa = 0;
        int columnIndex = 0;
        for (int i = 0; i < columnsString.length(); i++) {
            if (columnsString.charAt(i) == ',') {
                columns[columnIndex] = columnsString.substring(lastComa, i);
                lastComa = i + 1;
                columnIndex++;
            } else if (i + 1 == columnsString.length()) {
                columns[columnIndex] = columnsString.substring(lastComa, i + 1);
            }
        }
        return new SampleMetadata(',', columns);
    }

}
