package io.metersphere.system.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 */
public interface ExtOrganizationTemplateMapper {
    List<String> getTemplateIdByRefId(@Param("refId") String refId);
}
