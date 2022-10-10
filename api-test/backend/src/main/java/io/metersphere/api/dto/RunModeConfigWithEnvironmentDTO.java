package io.metersphere.api.dto;

import io.metersphere.dto.RunModeConfigDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RunModeConfigWithEnvironmentDTO extends RunModeConfigDTO {
    // 接口/用例在运行时采用的环境信息 <项目ID , <环境ID>>
    private Map<String, List<String>> executionEnvironmentMap;
}
