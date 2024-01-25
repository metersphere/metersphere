package io.metersphere.bug.mapper;

import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.bug.dto.response.BugRelateCaseCountDTO;
import io.metersphere.bug.dto.response.BugRelateCaseDTO;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.AssociateCaseModuleRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author song-cc-rock
 */
public interface ExtBugRelateCaseMapper {

    /**
     * 获取缺陷关联的用例模块树
     * @param request 请求参数
     * @param deleted 是否删除状态
     * @return 模块树集合
     */
    List<BaseTreeNode> getRelateCaseModule(@Param("request") AssociateCaseModuleRequest request, @Param("deleted") boolean deleted);

    /**
     * 获取缺陷关联的用例模块树数量
     * @param request 请求参数
     * @param deleted 是否删除状态
     * @return 模块树数量
     */
    List<ModuleCountDTO> countRelateCaseModuleTree(@Param("request") TestCasePageProviderRequest request, @Param("deleted") boolean deleted);

    /**
     * 统计缺陷关联的用例数量
     *
     * @param bugIds 缺陷ID集合
     * @return 缺陷关联DTO
     */
    List<BugRelateCaseCountDTO> countRelationCases(@Param("ids") List<String> bugIds);

    /**
     * 缺陷关联用例列表查询
     *
     * @param request 请求参数
     * @return 缺陷关联用例列表
     */
    List<BugRelateCaseDTO> list(@Param("request") BugRelatedCasePageRequest request);

    /**
     * 根据CaseId获取关联的Case
     * @param id 用例ID
     * @param sourceType 用例类型
     * @return 用例关联DTO
     */
    BugRelateCaseDTO getRelateCase(@Param("id") String id, @Param("sourceType") String sourceType);

    /**
     * 获取关联的缺陷
     * @param request 关联请求参数
     * @param sort 排序
     * @return 缺陷集合
     */
    List<BugProviderDTO> getAssociateBugs(@Param("request") AssociateBugPageRequest request, @Param("sort") String sort);
}
