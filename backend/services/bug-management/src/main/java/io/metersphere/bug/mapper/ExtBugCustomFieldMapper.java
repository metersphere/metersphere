package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugCustomField;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtBugCustomFieldMapper {

    /**
     * 获取缺陷存在的自定义字段值
     * @param bugIds 缺陷集合
     * @param projectId 项目ID
     * @return 缺陷自定义字段值
     */
    List<BugCustomFieldDTO> getBugExistCustomFields(@Param("ids") List<String> bugIds, @Param("projectId") String projectId);

    /**
     * 获取缺陷所有自定义字段映射值
     * @param bugIds 缺陷集合
     * @param projectId 项目ID
     * @return 缺陷自定义字段值
     */
    List<BugCustomFieldDTO> getBugAllCustomFields(@Param("ids") List<String> bugIds, @Param("projectId") String projectId);

    /**
     * 批量插入缺陷自定义字段值
     * @param customFields 自定义字段值集合
     */
    void batchInsert(@Param("list") List<BugCustomField> customFields);
}
