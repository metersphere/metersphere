package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCheckOwnerMapper {
    boolean checkoutOwner(@Param("table") String resourceType, @Param("userId") String userId, @Param("ids") List<String> ids);

    boolean checkoutRelationOwner(@Param("table") String resourceType, @Param("relationTable") String relationType, @Param("userId") String userId, @Param("ids") List<String> ids);

    boolean checkoutOrganizationOwner(@Param("table") String resourceType, @Param("userId") String userId, @Param("ids") List<String> ids);

    boolean checkoutOrganization(@Param("userId") String userId, @Param("ids") List<String> ids);

    boolean checkoutOrganizationOwnerByScope(@Param("table") String resourceType, @Param("column") String resourceColumn, @Param("userId") String userId, @Param("ids") List<String> ids);

    boolean checkoutProjectOwnerByScope(@Param("table") String resourceType, @Param("column") String resourceColumn, @Param("userId") String userId, @Param("ids") List<String> ids);
}
