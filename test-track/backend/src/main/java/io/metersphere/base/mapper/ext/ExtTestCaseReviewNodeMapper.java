package io.metersphere.base.mapper.ext;

import io.metersphere.dto.TestCaseReviewNodeDTO;
import io.metersphere.request.testreview.QueryCaseReviewRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestCaseReviewNodeMapper {

    List<TestCaseReviewNodeDTO> getCountNodes(@Param("request") QueryCaseReviewRequest request);

    List<TestCaseReviewNodeDTO> getNodeTreeByProjectId(@Param("projectId") String projectId);

    TestCaseReviewNodeDTO getNode(String id);

    void updatePos(String id, Double pos);
}
