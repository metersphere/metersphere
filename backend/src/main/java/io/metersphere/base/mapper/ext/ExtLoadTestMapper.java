package io.metersphere.base.mapper.ext;

import io.metersphere.dto.LoadTestDTO;

import java.util.List;
import java.util.Map;

public interface ExtLoadTestMapper {
    List<LoadTestDTO> list(Map<String, Object> params);
}
