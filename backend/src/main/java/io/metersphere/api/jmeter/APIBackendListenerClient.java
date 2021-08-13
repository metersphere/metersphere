package io.metersphere.api.jmeter;


import io.metersphere.commons.utils.CommonBeanFactory;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.Serializable;
import java.util.List;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    public final static String TEST_ID = "ms.test.id";

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        APIBackendListenerHandler apiBackendListenerHandler =
                CommonBeanFactory.getBean(APIBackendListenerHandler.class);
        apiBackendListenerHandler.handleSetupTest(context);
        super.setupTest(context);
    }

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        APIBackendListenerHandler apiBackendListenerHandler =
                CommonBeanFactory.getBean(APIBackendListenerHandler.class);
        apiBackendListenerHandler.handleSampleResults(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        APIBackendListenerHandler apiBackendListenerHandler =
                CommonBeanFactory.getBean(APIBackendListenerHandler.class);
        apiBackendListenerHandler.handleTeardownTest();
        super.teardownTest(context);
    }

}
