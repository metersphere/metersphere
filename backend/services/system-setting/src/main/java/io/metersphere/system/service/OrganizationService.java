package io.metersphere.system.service;

import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.request.OrganizationMemberRequest;
import io.metersphere.system.request.OrganizationRequest;

import java.util.List;

/**
 * @author song-cc-rock
 * 组织功能(非XPACK)
 */
public interface OrganizationService {

    /**
     * 获取组织列表
     * @param organizationRequest 列表请求参数
     * @return 列表数据
     */
    List<OrganizationDTO> list(OrganizationRequest organizationRequest);

    /**
     * 获取系统下所有组织
     * @return 列表数据
     */
    List<OrganizationDTO> listAll();

    /**
     * 获取默认组织信息
     * @return 默认组织信息
     */
    OrganizationDTO getDefault();

    /**
     * 获取组织成员列表
     * @param organizationRequest 组织成员列表请求参数
     * @return 组织成员列表
     */
    List<UserExtend> listMember(OrganizationRequest organizationRequest);

    /**
     * 添加组织成员
     * @param organizationMemberRequest 添加组织成员请求参数
     */
    void addMember(OrganizationMemberRequest organizationMemberRequest);

    /**
     * 移除组织成员
     * @param organizationId 组织ID
     * @param userId 成员ID
     */
    void removeMember(String organizationId, String userId);
}
