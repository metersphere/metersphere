package org.apache.jmeter.assertions;

import org.apache.jmeter.samplers.SampleResult;

public class ErrorReportAssertion extends ResponseAssertion{
    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = super.getResult(response);
        String faliurMessage = result.getFailureMessage();
        if(result.isError() || result.isFailure()){
            faliurMessage = faliurMessage+" Final result is error";
        } else {
            faliurMessage = faliurMessage+" Final result is success";
        }
        result.setError(false);
        result.setFailure(false);
        result.setFailureMessage(faliurMessage);
        return result;
    }
}
