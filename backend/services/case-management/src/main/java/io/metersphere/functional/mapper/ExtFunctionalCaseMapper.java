package io.metersphere.functional.mapper;

import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.metersphere.functional.dto.FunctionalCaseMindDTO;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.dto.FunctionalCaseVersionDTO;
import io.metersphere.functional.request.*;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtFunctionalCaseMapper {

    Long getPos(@Param("projectId") String projectId);

    void updateFunctionalCaseModule(@Param("refId") String refId, @Param("moduleId") String moduleId);

    List<FunctionalCaseVersionDTO> getFunctionalCaseByRefId(@Param("refId") String refId);

    List<String> getFunctionalCaseIds(@Param("projectId") String projectId);

    void removeToTrashByModuleIds(@Param("moduleIds") List<String> deleteIds, @Param("userId") String userId);

    List<FunctionalCase> checkCaseByModuleIds(@Param("moduleIds") List<String> deleteIds);

    List<FunctionalCasePageDTO> list(@Param("request") FunctionalCasePageRequest request, @Param("deleted") boolean deleted, @Param("isRepeat") boolean isRepeat);

    void recoverCase(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("time") long time);


    List<String> getIds(@Param("request") BaseFunctionalCaseBatchDTO request, @Param("projectId") String projectId, @Param("deleted") boolean deleted);

    void batchDelete(@Param("ids") List<String> ids, @Param("userId") String userId);

    List<FunctionalCase> getLogInfo(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<String> getRefIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    void batchMoveModule(@Param("request") FunctionalCaseBatchMoveRequest request, @Param("ids") List<String> ids, @Param("userId") String userId);

    List<FunctionalCase> getTagsByIds(@Param("ids") List<String> ids);


    void batchUpdate(@Param("functionalCase") FunctionalCase functionalCase, @Param("ids") List<String> ids);

    void recoverCaseByRefIds(@Param("refIds") List<String> refIds, @Param("userId") String userId, @Param("time") long time);

    List<ModuleCountDTO> countModuleIdByRequest(@Param("request") FunctionalCasePageRequest request, @Param("deleted") boolean deleted);

    long caseCount(@Param("request") FunctionalCasePageRequest request, @Param("deleted") boolean deleted);

    Long getPrePos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    Long getLastPos(@Param("projectId") String projectId, @Param("basePos") Long basePos);

    /**
     * 获取缺陷未关联的功能用例列表
     *
     * @param request provider参数
     * @param deleted 是否删除状态
     * @param sort    排序
     * @return 通用的列表Case集合
     */
    List<TestCaseProviderDTO> listUnRelatedCaseWithBug(@Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted, @Param("sort") String sort);

    /**
     * 根据关联条件获取关联的用例ID
     *
     * @param request 关联参数
     * @param deleted 是否删除状态
     * @return 关联的用例ID集合
     */
    List<String> getSelectIdsByAssociateParam(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);

    /**
     * 根据模块ID获取脑图展示数据
     */
    List<FunctionalCaseMindDTO> getMinderCaseList(@Param("request") FunctionalCaseMindRequest request, @Param("deleted") boolean deleted);

    List<FunctionalCaseCustomField> getCaseCustomFieldList(@Param("request") FunctionalCaseMindRequest request, @Param("deleted") boolean deleted, @Param("fieldIds") List<String>fieldIds);


    /**
     * 根据模块ID获取用例评审脑图展示数据
     */
    List<FunctionalCaseMindDTO> getMinderCaseReviewList(@Param("request") FunctionalCaseReviewMindRequest request, @Param("deleted") boolean delete, @Param("userId") String userId, @Param("viewStatusUserId") String viewStatusUserId);

    List<FunctionalCaseMindDTO> getMinderTestPlanList(@Param("request") FunctionalCasePlanMindRequest request, @Param("deleted") boolean delete);

    List<BaseTreeNode> selectBaseMindNodeByProjectId(@Param("projectId")String projectId);
}
