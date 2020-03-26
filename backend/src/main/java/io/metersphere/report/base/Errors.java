package io.metersphere.report.base;

public class Errors {

    private String errorType;
    private String errorNumber;
    private String precentOfErrors;
    private String precentOfAllSamples;

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorNumber() {
        return errorNumber;
    }

    public void setErrorNumber(String errorNumber) {
        this.errorNumber = errorNumber;
    }

    public String getPrecentOfErrors() {
        return precentOfErrors;
    }

    public void setPrecentOfErrors(String precentOfErrors) {
        this.precentOfErrors = precentOfErrors;
    }

    public String getPrecentOfAllSamples() {
        return precentOfAllSamples;
    }

    public void setPrecentOfAllSamples(String precentOfAllSamples) {
        this.precentOfAllSamples = precentOfAllSamples;
    }
}
