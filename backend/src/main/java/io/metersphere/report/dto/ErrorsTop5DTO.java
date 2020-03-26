package io.metersphere.report.dto;

import io.metersphere.report.base.ErrorsTop5;

import java.util.List;

public class ErrorsTop5DTO {

    private List<ErrorsTop5> errorsTop5List;
    private String label;
    private String totalSamples;
    private String totalErrors;
    private String error1;
    private String error1Size;
    private String error2;
    private String error2Size;
    private String error3;
    private String error3Size;
    private String error4;
    private String error4Size;
    private String error5;
    private String error5Size;

    public List<ErrorsTop5> getErrorsTop5List() {
        return errorsTop5List;
    }

    public void setErrorsTop5List(List<ErrorsTop5> errorsTop5List) {
        this.errorsTop5List = errorsTop5List;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(String totalSamples) {
        this.totalSamples = totalSamples;
    }

    public String getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(String totalErrors) {
        this.totalErrors = totalErrors;
    }

    public String getError1() {
        return error1;
    }

    public void setError1(String error1) {
        this.error1 = error1;
    }

    public String getError1Size() {
        return error1Size;
    }

    public void setError1Size(String error1Size) {
        this.error1Size = error1Size;
    }

    public String getError2() {
        return error2;
    }

    public void setError2(String error2) {
        this.error2 = error2;
    }

    public String getError2Size() {
        return error2Size;
    }

    public void setError2Size(String error2Size) {
        this.error2Size = error2Size;
    }

    public String getError3() {
        return error3;
    }

    public void setError3(String error3) {
        this.error3 = error3;
    }

    public String getError3Size() {
        return error3Size;
    }

    public void setError3Size(String error3Size) {
        this.error3Size = error3Size;
    }

    public String getError4() {
        return error4;
    }

    public void setError4(String error4) {
        this.error4 = error4;
    }

    public String getError4Size() {
        return error4Size;
    }

    public void setError4Size(String error4Size) {
        this.error4Size = error4Size;
    }

    public String getError5() {
        return error5;
    }

    public void setError5(String error5) {
        this.error5 = error5;
    }

    public String getError5Size() {
        return error5Size;
    }

    public void setError5Size(String error5Size) {
        this.error5Size = error5Size;
    }
}
