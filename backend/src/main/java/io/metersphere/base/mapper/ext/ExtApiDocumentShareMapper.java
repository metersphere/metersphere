package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiDocumentShare;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDocumentShareMapper {
    List<ApiDocumentShare> selectByShareTypeAndShareApiIdWithBLOBs(@Param("shareType") String shareType, @Param("shareApiId") String shareApiIdString);
}
