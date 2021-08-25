package io.metersphere.api.jmeter;

import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    public final static String TEST_ID = "ms.test.id";

    public String runMode = ApiRunMode.RUN.name();

    private final List<SampleResult> queue = new ArrayList<>();

    // 测试ID
    private String testId;

    private String debugReportId;

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        setParam(context);
        super.setupTest(context);
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        queue.addAll(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        APIBackendListenerHandler apiBackendListenerHandler =
                CommonBeanFactory.getBean(APIBackendListenerHandler.class);
        apiBackendListenerHandler.handleTeardownTest(queue, this.runMode, this.testId, this.debugReportId);
        super.teardownTest(context);
    }

    private void setParam(BackendListenerContext context) {
        this.testId = context.getParameter(APIBackendListenerClient.TEST_ID);
        this.runMode = context.getParameter("runMode");
        this.debugReportId = context.getParameter("debugReportId");
        if (StringUtils.isBlank(this.runMode)) {
            this.runMode = ApiRunMode.RUN.name();
        }
    }

}
