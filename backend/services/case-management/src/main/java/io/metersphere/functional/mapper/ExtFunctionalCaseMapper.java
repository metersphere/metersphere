package io.metersphere.functional.mapper;

import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.request.*;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.ProjectCountDTO;
import io.metersphere.project.dto.ProjectUserCreateCount;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.interceptor.BaseConditionFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

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

    @BaseConditionFilter
    List<FunctionalCasePageDTO> list(@Param("request") FunctionalCasePageRequest request, @Param("deleted") boolean deleted, @Param("isRepeat") boolean isRepeat);

    void recoverCase(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("time") long time);

    @BaseConditionFilter
    List<String> getIds(@Param("request") BaseFunctionalCaseBatchDTO request, @Param("projectId") String projectId, @Param("deleted") boolean deleted);

    void batchDelete(@Param("ids") List<String> ids, @Param("userId") String userId);

    List<FunctionalCase> getLogInfo(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<String> getRefIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    @BaseConditionFilter
    void batchMoveModule(@Param("request") FunctionalCaseBatchMoveRequest request, @Param("ids") List<String> ids, @Param("userId") String userId);

    List<FunctionalCase> getTagsByIds(@Param("ids") List<String> ids);


    void batchUpdate(@Param("functionalCase") FunctionalCase functionalCase, @Param("ids") List<String> ids);

    void recoverCaseByRefIds(@Param("refIds") List<String> refIds, @Param("userId") String userId, @Param("time") long time);

    @BaseConditionFilter
    List<ModuleCountDTO> countModuleIdByRequest(@Param("request") FunctionalCasePageRequest request, @Param("deleted") boolean deleted);

    @BaseConditionFilter
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
    @BaseConditionFilter
    List<TestCaseProviderDTO> listUnRelatedCaseWithBug(@Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted, @Param("sort") String sort);

    /**
     * 根据关联条件获取关联的用例ID
     *
     * @param request 关联参数
     * @param deleted 是否删除状态
     * @return 关联的用例ID集合
     */
    @BaseConditionFilter
    List<String> getSelectIdsByAssociateParam(@Param("request") AssociateOtherCaseRequest request, @Param("deleted") boolean deleted);

    /**
     * 根据模块ID获取脑图展示数据
     */
    List<FunctionalCaseMindDTO> getMinderCaseList(@Param("request") FunctionalCaseMindRequest request, @Param("deleted") boolean deleted);

    List<FunctionalCaseCustomField> getCaseCustomFieldList(@Param("request") FunctionalCaseMindRequest request, @Param("deleted") boolean deleted, @Param("fieldIds") List<String> fieldIds);

    List<FunctionalCaseCustomField> getCaseCustomFieldListByCollection(@Param("request") FunctionalCaseCollectionMindRequest request, @Param("deleted") boolean deleted, @Param("fieldIds") List<String> fieldIds);
    /**
     * 根据模块ID获取用例评审脑图展示数据
     */
    List<FunctionalCaseMindDTO> getMinderCaseReviewList(@Param("request") FunctionalCaseReviewMindRequest request, @Param("deleted") boolean delete, @Param("userId") String userId, @Param("viewStatusUserId") String viewStatusUserId, @Param("sort") String sort);

    List<FunctionalCaseMindDTO> getMinderTestPlanList(@Param("request") FunctionalCasePlanMindRequest request, @Param("deleted") boolean delete, @Param("sort") String sort);

    List<FunctionalCaseMindDTO> getMinderCollectionList(@Param("request") FunctionalCaseCollectionMindRequest request, @Param("deleted") boolean delete, @Param("sort") String sort);


    List<BaseTreeNode> selectBaseMindNodeByProjectId(@Param("projectId") String projectId);

    List<FunctionalCase> selectAllFunctionalCase(@Param("isRepeat") boolean isRepeat, @Param("projectId") String projectId, @Param("testPlanId") String testPlanId);

    List<FunctionalCase> getListBySelectModules(@Param("isRepeat") boolean isRepeat, @Param("projectId") String projectId, @Param("moduleIds") List<String> moduleIds, @Param("testPlanId") String testPlanId);

    List<FunctionalCase> getListBySelectIds(@Param("projectId") String projectId, @Param("ids") List<String> ids, @Param("testPlanId") String testPlanId);

    List<FunctionalCase> getProjectIdByIds(@Param("ids") List<String> ids);

    List<ProjectCountDTO> projectCaseCount(@Param("projectIds") Set<String> projectIds, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userId") String userId);

    List<ProjectUserCreateCount> userCreateCaseCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("userIds") Set<String> userIds);

    List<FunctionalCaseStatisticDTO> getStatisticListByProjectId(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    long caseTestCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    long simpleCaseCount(@Param("projectId") String projectId, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

}
