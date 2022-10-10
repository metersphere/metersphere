package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ShareInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseShareInfoMapper {

    List<ShareInfo> selectByShareTypeAndShareApiIdWithBLOBs(@Param("shareType") String shareType, @Param("customData") String customData);
}
