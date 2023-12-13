package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

public interface ExtCheckOwnerMapper {
    boolean checkoutOwner(@Param("table") String resourceType, @Param("projectId") String projectId, @Param("id") String id);
}
