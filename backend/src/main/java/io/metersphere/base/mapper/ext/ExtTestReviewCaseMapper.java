package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestReviewCaseDTO;
import io.metersphere.track.request.testreview.QueryCaseReviewRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ExtTestReviewCaseMapper {

    List<TestReviewCaseDTO> list(@Param("request") QueryCaseReviewRequest request);

    List<String> getStatusByReviewId(String reviewId);

    List<String> findRelateTestReviewId(@Param("userId") String userId, @Param("workspaceId") String workspaceId, @Param("projectId") String projectId);

    /**
     * 根据项目 ids 查询 TestReviewCaseDTO 列表
     *
     * @param ids project id list
     * @return List<TestReviewCaseDTO>
     */
    List<TestReviewCaseDTO> listTestCaseByProjectIds(@Param("ids") List<String> ids);

    /**
     * 获取 TestReviewTestCase 详细信息
     *
     * @param id TestReviewTestCase id
     * @return TestReviewTestCase 详细信息
     */
    TestReviewCaseDTO get(@Param("id") String id);

    List<String> selectIds(@Param("request") QueryCaseReviewRequest request);

    List<String> selectTestCaseIds(@Param("request") QueryCaseReviewRequest request);

    List<TestReviewCaseDTO> listForMinder(@Param("request") QueryCaseReviewRequest request);
}
