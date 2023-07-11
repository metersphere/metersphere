package io.metersphere.system.service;

import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.request.OrganizationDeleteRequest;

/**
 * @author song-cc-rock
 * 组织功能
 */
public interface XpackOrganizationService {

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
    void recover(String id);

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
}
