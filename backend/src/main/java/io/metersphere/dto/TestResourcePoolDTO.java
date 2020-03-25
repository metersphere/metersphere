package io.metersphere.dto;

import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;

import java.util.List;

public class TestResourcePoolDTO extends TestResourcePool {

    private List<TestResource> resources;

    public List<TestResource> getResources() {
        return resources;
    }

    public void setResources(List<TestResource> resources) {
        this.resources = resources;
    }
}
