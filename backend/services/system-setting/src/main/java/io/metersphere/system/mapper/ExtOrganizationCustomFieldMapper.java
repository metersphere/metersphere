package io.metersphere.system.mapper;

import io.metersphere.system.dto.sdk.OptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jianxing
 */
public interface ExtOrganizationCustomFieldMapper {
    List<String> getCustomFieldByRefId(@Param("refId") String refId);

    List<OptionDTO> getCustomFieldOptions(@Param("ids") List<String> ids);
}
