package io.metersphere.system.mapper;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.*;
import io.metersphere.system.dto.request.OrganizationDeleteRequest;
import io.metersphere.system.dto.request.OrganizationRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtOrganizationMapper {

    /**
     * 查询组织列表
     *
     * @param request 列表请求参数
     * @return 组织列表数据
     */
    List<OrganizationDTO> list(@Param("request") OrganizationRequest request);

    /**
     * 通过组织ID获取项目及成员数量
     *
     * @param ids 组织ID集合
     * @return 项目及成员数量
     */
    List<OrganizationCountDTO> getCountByIds(@Param("ids") List<String> ids);

    /**
     * 获取系统下所有组织
     *
     * @return 组织列表数据
     */
    List<OrganizationDTO> listAll();

    /**
     * 删除组织
     *
     * @param organizationDeleteRequest 组织删除参数
     */
    void delete(@Param("request") OrganizationDeleteRequest organizationDeleteRequest);

    /**
     * 恢复组织
     *
     * @param id 组织ID
     */
    void recover(String id);

    /**
     * 更新组织启用/禁用状态
     *
     * @param id     组织ID
     * @param enable 是否启用
     */
    void updateEnable(String id, boolean enable);

    /**
     * 获取组织成员列表(角色)
     *
     * @param request 组织成员列表请求参数
     * @return 组织成员列表数据
     */
    List<UserExtendDTO> listMember(@Param("request") OrganizationRequest request);


    /**
     * 获取组织成员列表(角色)
     *
     * @param request 组织成员列表请求参数
     * @return 组织成员列表数据
     */
    List<OrgUserExtend> listMemberByOrg(@Param("request") OrganizationRequest request);

    /**
     * 获取组织成员下拉列表(角色)
     *
     * @param keyword 组织成员列表请求参数
     * @return 组织成员列表数据
     */
    List<OptionDisabledDTO> selectListMemberByOrg(@Param("keyword") String keyword);

    /**
     * 获取组织管理员
     *
     * @param orgId 组织ID
     * @return 组织管理员数据
     */
    List<User> getOrgAdminList(String orgId);

    /**
     * 获取组织列表(下拉框)
     *
     * @return 组织列表数据
     */
    List<OrganizationProjectOptionsDTO> selectOrganizationOptions();

    /**
     * 获取组织下拉选项
     *
     * @param ids 组织ID集合
     * @return 组织下拉选项
     */
    List<OptionDTO> getOptionsByIds(@Param("ids") List<String> ids);

    /**
     * 获取用户关联组织ID
     * @param userId 用户ID
     * @return 用户ID集合
     */
    List<String> getRelatedOrganizationIds(@Param("userId") String userId);

    /**
     * 根据项目获取组织名称
     *
     * @return 项目id对应组织名称列表
     */
    List<OptionDTO> getOrgListByProjectIds(@Param("projectIds") List<String> projectIds);
}
