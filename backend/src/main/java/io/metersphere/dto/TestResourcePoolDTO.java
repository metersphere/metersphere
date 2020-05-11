package io.metersphere.dto;

import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestResourcePoolDTO extends TestResourcePool {

    private List<TestResource> resources;

}
