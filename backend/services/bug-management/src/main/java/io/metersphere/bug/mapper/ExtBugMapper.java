package io.metersphere.bug.mapper;

import io.metersphere.bug.dto.BugDTO;
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
}
