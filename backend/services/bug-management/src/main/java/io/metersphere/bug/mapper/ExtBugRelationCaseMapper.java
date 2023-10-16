package io.metersphere.bug.mapper;

import io.metersphere.bug.dto.BugRelationCaseCountDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtBugRelationCaseMapper {

    /**
     * 统计缺陷关联的用例数量
     * @param bugIds 缺陷ID集合
     * @return 缺陷关联DTO
     */
    List<BugRelationCaseCountDTO> countRelationCases(@Param("ids") List<String> bugIds);
}
