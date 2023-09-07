package io.metersphere.project.mapper;

import io.metersphere.sdk.dto.OptionDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtProjectTestResourcePoolMapper {

    /**
     * 获取当前项目资源池列表
     * @param orgId
     * @return
     */
    List<OptionDTO> getResourcePoolList(@Param("orgId") String orgId);
}
