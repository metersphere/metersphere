package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 */
public interface ExtOrganizationCustomFieldMapper {
    List<String> getCustomFieldByRefId(@Param("refId") String refId);
}
