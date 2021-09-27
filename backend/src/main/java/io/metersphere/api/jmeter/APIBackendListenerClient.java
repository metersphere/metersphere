package io.metersphere.api.jmeter;

import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    public final static String TEST_ID = "ms.test.id";

    public final static String TEST_REPORT_ID = "ms.test.report.name";

    private final List<SampleResult> queue = new ArrayList<>();

    public String runMode = ApiRunMode.RUN.name();

    // 测试ID
    private String testId;

    private String debugReportId;
    // 只有合并报告是这个有值
    private String setReportId;

    //获得控制台内容
    private PrintStream oldPrintStream = System.out;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    private void setConsole() {
        System.setOut(new PrintStream(bos)); //设置新的out
    }

    private String getConsole() {
        System.setOut(oldPrintStream);
        return bos.toString();
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        setConsole();
        setParam(context);
    }


    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        queue.addAll(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        APIBackendListenerResultHandler apiBackendListenerResultHandler =
                CommonBeanFactory.getBean(APIBackendListenerResultHandler.class);
        MessageCache.executionQueue.remove(testId);
        apiBackendListenerResultHandler.handleTeardownTest(queue, runMode, testId, debugReportId, setReportId, getConsole());
        super.teardownTest(context);
    }

    private void setParam(BackendListenerContext context) {
        this.testId = context.getParameter(TEST_ID);
        this.setReportId = context.getParameter(TEST_REPORT_ID);
        this.runMode = context.getParameter("runMode");
        this.debugReportId = context.getParameter("debugReportId");
        if (StringUtils.isBlank(this.runMode)) {
            this.runMode = ApiRunMode.RUN.name();
        }
    }

}
