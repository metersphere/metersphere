package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestCaseReviewDTO;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewMapper {

    List<TestCaseReviewDTO> list(@Param("request") QueryCaseReviewRequest params);

    List<TestCaseReviewDTO> listByWorkspaceId(@Param("workspaceId") String workspaceId);
}
