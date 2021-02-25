package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.document.ApiDocumentInfoDTO;
import io.metersphere.api.dto.document.ApiDocumentRequest;
import io.metersphere.api.dto.document.ApiDocumentSimpleInfoDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDocumentMapper {
    List<ApiDocumentInfoDTO> findApiDocumentSimpleInfoByRequest(@Param("request") ApiDocumentRequest request);
}
