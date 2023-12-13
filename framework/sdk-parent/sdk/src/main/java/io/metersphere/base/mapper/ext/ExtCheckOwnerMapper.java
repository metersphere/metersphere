package io.metersphere.base.mapper.ext;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCheckOwnerMapper {
    boolean checkoutOwner(@Param("table") String resourceType, @Param("projectId") String projectId, @Param("ids") List ids);
}
