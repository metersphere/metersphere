package io.metersphere.system.mapper;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.OrganizationProjectOptionsDto;
import io.metersphere.system.dto.UserExtend;
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
     * 获取系统下所有组织
     * @return 组织列表数据
     */
    List<OrganizationDTO> listAll();

    /**
     * 删除组织
     * @param organizationDeleteRequest 组织删除参数
     */
    void delete(@Param("request") OrganizationDeleteRequest organizationDeleteRequest);

    /**
     * 恢复组织
     * @param id 组织ID
     */
    void recover(String id);

    /**
     * 更新组织启用/禁用状态
     * @param id 组织ID
     * @param enable 是否启用
     */
    void updateEnable(String id, boolean enable);

    /**
     * 获取组织成员列表(角色)
     * @param organizationRequest 组织成员列表请求参数
     * @return 组织成员列表数据
     */
    List<UserExtend> listMember(@Param("request") OrganizationRequest organizationRequest);

    /**
     * 获取组织管理员
     * @param orgId 组织ID
     * @return 组织管理员数据
     */
    List<User> getOrgAdminList(String orgId);

    /**
     * 获取组织列表(下拉框)
     * @return 组织列表数据
     */
    List<OrganizationProjectOptionsDto> selectOrganizationOptions();
}
