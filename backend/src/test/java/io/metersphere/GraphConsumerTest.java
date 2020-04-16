package io.metersphere;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleMetadata;
import org.apache.jmeter.report.dashboard.JsonizerVisitor;
import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ResultData;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.graph.AbstractOverTimeGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;
import java.util.StringTokenizer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GraphConsumerTest {
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    private AbstractOverTimeGraphConsumer timeGraphConsumer;
    // data array can't be initialized in the init()
    private static String[] data = {"1527089951383", "0", "Read-compute", "200", "OK", "setupRegion 1-1", "true", "", "492", "0", "1", "1",
            "null", "0", "0", "0"};
    private SampleMetadata sampleMetaData = createTestMetaData();

    @Before
    public void init() {
        timeGraphConsumer = new ActiveThreadsGraphConsumer();
//        timeGraphConsumer.setTitle("graph title");
        timeGraphConsumer.setGranularity(60000);

        JMeterUtils.loadJMeterProperties("jmeter.properties"); // 这个路径不存在
    }

    @Test
    public void test() {
        SampleContext sampleContext = new SampleContext();
        sampleContext.setWorkingDirectory(FileUtils.getTempDirectory());

        timeGraphConsumer.setSampleContext(sampleContext);
        Sample sample = new Sample(0, sampleMetaData, data);
        timeGraphConsumer.initialize();
        timeGraphConsumer.startConsuming();
        timeGraphConsumer.consume(sample, 0);
        timeGraphConsumer.stopConsuming();

        System.out.println(sampleContext.getData());
    }

    @Test
    public void test2() {
        int row = 0;
        SampleContext sampleContext = new SampleContext();
//        sampleContext.setWorkingDirectory(new File("/tmp/test_report/"));
        timeGraphConsumer.setSampleContext(sampleContext);

        timeGraphConsumer.initialize();
        timeGraphConsumer.startConsuming();
        LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey("7fa4dd2d-d42a-46de-92bf-10feec4c6ccf");
        String content = report.getContent();
        StringTokenizer tokenizer = new StringTokenizer(content, "\n");
        // 去掉第一行
        tokenizer.nextToken();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            String[] data = line.split(",", -1);
            Sample sample = new Sample(row++, sampleMetaData, data);
            timeGraphConsumer.consume(sample, 0);
        }
        timeGraphConsumer.stopConsuming();
        Map<String, Object> map = sampleContext.getData();
        for (String key : map.keySet()) {
            MapResultData mapResultData = (MapResultData) map.get(key);
            ResultData maxY = mapResultData.getResult("maxY");
            ListResultData series = (ListResultData) mapResultData.getResult("series");
            if (series.getSize() > 0) {
                MapResultData resultData = (MapResultData) series.get(0);
                ListResultData data = (ListResultData) resultData.getResult("data");
                if (data.getSize() > 0) {
                    for (int i = 0; i < data.getSize(); i++) {
                        ListResultData resultData1 = (ListResultData) data.get(i);
                        String accept = resultData1.accept(new JsonizerVisitor());
                        String[] split = accept.split(",");
                        System.out.println(resultData1);
                        System.out.println(accept);
                    }
                }

            }

        }

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
