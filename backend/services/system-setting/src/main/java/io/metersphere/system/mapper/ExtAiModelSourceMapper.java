package io.metersphere.system.mapper;

import io.metersphere.system.domain.AiModelSource;
import io.metersphere.system.dto.request.ai.AiModelSourceRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtAiModelSourceMapper {

    List<AiModelSource> list(@Param("request") AiModelSourceRequest aiModelSourceRequest);

    List<OptionDTO> sourceNameList(@Param("owner") String owner);

}