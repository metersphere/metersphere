package io.metersphere.system.mapper;

import io.metersphere.system.dto.request.ai.AiModelSourceCreateNameDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtAiModelSourceMapper {

    List<AiModelSourceCreateNameDTO> list(@Param("request") AiModelSourceRequest aiModelSourceRequest);

    List<OptionDTO> enableSourceNameList(@Param("userId") String userId);

    List<OptionDTO> enablePersonalSourceNameList(@Param("userId") String userId);

}