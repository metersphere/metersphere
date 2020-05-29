package io.metersphere.dto;

import io.metersphere.base.domain.LoadTest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadTestDTO extends LoadTest {
    private String projectName;
    private String userName;
}
