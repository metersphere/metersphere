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


    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        APIBackendListenerResultHandler apiBackendListenerResultHandler =
                CommonBeanFactory.getBean(APIBackendListenerResultHandler.class);
        apiBackendListenerResultHandler.handleSetupTest(context);
        super.setupTest(context);
    }


    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        APIBackendListenerResultHandler apiBackendListenerResultHandler =
                CommonBeanFactory.getBean(APIBackendListenerResultHandler.class);
        apiBackendListenerResultHandler.handleSampleResults(sampleResults, context);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        APIBackendListenerResultHandler apiBackendListenerResultHandler =
                CommonBeanFactory.getBean(APIBackendListenerResultHandler.class);
        apiBackendListenerResultHandler.handleTeardownTest(context);
        super.teardownTest(context);
    }

}
