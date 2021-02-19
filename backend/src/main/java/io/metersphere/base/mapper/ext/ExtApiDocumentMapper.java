package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.ApiDocumentRequest;
import io.metersphere.api.dto.ApiDocumentSimpleInfoDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDocumentMapper {
    List<ApiDocumentSimpleInfoDTO> findApiDocumentSimpleInfoByRequest(@Param("request") ApiDocumentRequest request);
}
