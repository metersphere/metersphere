package io.metersphere.api.jmeter;

import org.apache.jmeter.assertions.AssertionResult;
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
        System.out.println(context.getParameter("id"));
        sampleResults.forEach(result -> {
            for (AssertionResult assertionResult : result.getAssertionResults()) {
                System.out.println(assertionResult.getName() + ": " + assertionResult.isError());
                System.out.println(assertionResult.getName() + ": " + assertionResult.isFailure());
                System.out.println(assertionResult.getName() + ": " + assertionResult.getFailureMessage());
            }

            println("getSampleLabel", result.getSampleLabel());
            println("getErrorCount", result.getErrorCount());
            println("getRequestHeaders", result.getRequestHeaders());
            println("getResponseHeaders", result.getResponseHeaders());
            println("getSampleLabel", result.getSampleLabel());
            println("getSampleLabel", result.getSampleLabel());
            println("getResponseCode", result.getResponseCode());
            println("getResponseCode size", result.getResponseData().length);
            println("getLatency", result.getLatency());
            println("end - start", result.getEndTime() - result.getStartTime());
            println("getTimeStamp", result.getTimeStamp());
            println("getTime", result.getTime());
        });
        System.err.println(count.addAndGet(sampleResults.size()));
    }

    private void println(String name, Object value) {
        System.out.println(name + ": " + value);
    }
}
