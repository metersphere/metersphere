package io.metersphere.controller.request.testplan;

public class QueryTestPlanRequest extends TestPlanRequest {
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
