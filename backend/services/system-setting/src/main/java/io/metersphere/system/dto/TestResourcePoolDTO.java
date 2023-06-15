package io.metersphere.system.dto;

import io.metersphere.system.domain.TestResourcePool;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TestResourcePoolDTO extends TestResourcePool {

  private String configuration;

}
