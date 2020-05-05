package io.metersphere.api.jmeter;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    private final AtomicInteger count = new AtomicInteger();

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        System.out.println(context);
        System.out.println(sampleResults.get(0).getAssertionResults());
        System.err.println(count.addAndGet(sampleResults.size()));
    }
}
