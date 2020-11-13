package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.ApiDelimitExecResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiTestResult {

    private String id;

    private String name;

    private long responseTime;

    private int error = 0;

    private int success = 0;

    private int totalAssertions = 0;

    private int passAssertions = 0;

    private final List<RequestResult> requestResults = new ArrayList<>();

    public void addRequestResult(RequestResult result) {
        requestResults.add(result);
    }

    public ApiTestResult() {

    }

    public ApiTestResult(ApiDelimitExecResult result) {
        this.id = result.getId();
        this.responseTime = result.getEndTime();
        this.addRequestResult(JSON.parseObject(result.getContent(), RequestResult.class));
    }

    public void addResponseTime(long time) {
        this.responseTime += time;
    }

    public void addError(int count) {
        this.error += count;
    }

    public void addSuccess() {
        this.success++;
    }
    public int getSuccess() {
        return this.success;
    }

    public void addTotalAssertions(int count) {
        this.totalAssertions += count;
    }

    public void addPassAssertions(int count) {
        this.passAssertions += count;
    }
}
