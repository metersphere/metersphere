package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.share.ApiDocumentRequest;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ShareInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtShareInfoMapper {
    List<String> findIdByRequest(@Param("request") ApiDocumentRequest request);

    List<ApiDefinitionWithBLOBs> findApiDocumentSimpleInfoByRequest(@Param("request") ApiDocumentRequest request);

    List<ShareInfo> selectByShareTypeAndShareApiIdWithBLOBs(@Param("shareType") String shareType, @Param("customData") String customData);
}
