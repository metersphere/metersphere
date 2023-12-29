package io.metersphere.api.mapper;

import io.metersphere.sdk.domain.ShareInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtShareInfoMapper {

    List<ShareInfo> selectByShareTypeAndShareApiIdWithBLOBs(@Param("shareType") String shareType, @Param("customData") byte[] customData, @Param("lang") String lang);
}
