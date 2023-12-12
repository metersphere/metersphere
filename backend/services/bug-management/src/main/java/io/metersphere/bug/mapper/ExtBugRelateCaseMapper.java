package io.metersphere.bug.mapper;

import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.bug.dto.response.BugRelateCaseCountDTO;
import io.metersphere.bug.dto.response.BugRelateCaseDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtBugRelateCaseMapper {

    /**
     * 统计缺陷关联的用例数量
     * @param bugIds 缺陷ID集合
     * @return 缺陷关联DTO
     */
    List<BugRelateCaseCountDTO> countRelationCases(@Param("ids") List<String> bugIds);

    /**
     * 缺陷关联用例列表查询
     * @param request 请求参数
     * @return 缺陷关联用例列表
     */
    List<BugRelateCaseDTO> list(@Param("request") BugRelatedCasePageRequest request);
}
