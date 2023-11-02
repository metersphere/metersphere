package io.metersphere.bug.mapper;

import io.metersphere.bug.dto.BugDTO;
import io.metersphere.bug.dto.BugTagEditDTO;
import io.metersphere.bug.dto.request.BugBatchUpdateRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtBugMapper {

    /**
     * 缺陷列表查询
     *
     * @param request 请求查询参数
     * @return 缺陷列表
     */
    List<BugDTO> list(@Param("request") BugPageRequest request);

    /**
     * 获取缺陷业务ID
     *
     * @param projectId 项目ID
     * @return 最大的业务ID
     */
    Long getMaxNum(String projectId);

    /**
     * 获取缺陷标签列表
     * @param ids 缺陷ID集合
     * @return 缺陷标签列表
     */
    List<BugTagEditDTO> getBugTagList(@Param("ids") List<String> ids);

    /**
     * 批量更新缺陷
     * @param request 请求参数
     * @param ids 缺陷ID集合
     */
    void batchUpdate(@Param("request") BugBatchUpdateRequest request, @Param("ids") List<String> ids);
}
