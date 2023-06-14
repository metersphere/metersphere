package io.metersphere.system.service;

import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.request.OrganizationDeleteRequest;
import io.metersphere.system.request.OrganizationRequest;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface OrganizationService {
    /**
     * 获取组织列表
     * @param organizationRequest 列表请求参数
     * @return 列表数据
     */
    List<OrganizationDTO> list(OrganizationRequest organizationRequest);

    /**
     * 新增组织
     * @param organizationDTO 组织信息
     * @return 组织信息
     */
    OrganizationDTO add(OrganizationDTO organizationDTO);

    /**
     * 更新组织
     * @param organizationDTO 组织信息
     */
    void update(OrganizationDTO organizationDTO);

    /**
     * 删除组织
     * @param organizationDeleteRequest 组织删除参数
     */
    void delete(OrganizationDeleteRequest  organizationDeleteRequest);

    /**
     * 恢复组织
     * @param id 组织ID
     */
    void undelete(String id);

    /**
     * 启用组织
     * @param id 组织ID
     */
    void enable(String id);

    /**
     * 禁用组织
     * @param id 组织ID
     */
    void disable(String id);

    /**
     * 获取默认组织信息
     * @return 默认组织信息
     */
    OrganizationDTO getDefault();
}
