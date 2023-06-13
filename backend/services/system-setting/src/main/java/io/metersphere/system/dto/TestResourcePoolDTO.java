package io.metersphere.system.dto;

import io.metersphere.system.domain.TestResource;
import io.metersphere.system.domain.TestResourcePool;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestResourcePoolDTO extends TestResourcePool {
    private List<TestResource> testResources;

}
