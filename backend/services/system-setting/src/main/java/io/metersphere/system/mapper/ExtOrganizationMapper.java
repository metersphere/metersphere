package io.metersphere.system.mapper;

import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.request.OrganizationDeleteRequest;
import io.metersphere.system.request.OrganizationRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtOrganizationMapper {

    /**
     * 查询组织列表
     * @param organizationRequest 列表请求参数
     * @return 组织列表数据
     */
    List<OrganizationDTO> list(@Param("request") OrganizationRequest organizationRequest);

    /**
     * 删除组织
     * @param organizationDeleteRequest 组织删除参数
     */
    void delete(@Param("request") OrganizationDeleteRequest organizationDeleteRequest);

    /**
     * 恢复组织
     * @param id 组织ID
     */
    void undelete(String id);

    /**
     * 更新组织启用/禁用状态
     * @param id 组织ID
     * @param enable 是否启用
     */
    void updateEnable(String id, boolean enable);
}
