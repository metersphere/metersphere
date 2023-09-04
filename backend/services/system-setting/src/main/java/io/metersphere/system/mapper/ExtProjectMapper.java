package io.metersphere.system.mapper;


import io.metersphere.system.dto.*;
import org.apache.ibatis.annotations.Param;


import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtProjectMapper {
    /**
     * 获取组织项目下拉列表(角色)
     * @param keyword 组织项目列表请求参数
     * @return 组织项目列表数据
     */
    List<IdNameStructureDTO> selectListProjectByOrg(@Param("organizationId") String organizationId, @Param("keyword") String keyword);

}
