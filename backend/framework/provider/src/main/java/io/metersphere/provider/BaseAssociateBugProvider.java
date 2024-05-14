package io.metersphere.provider;

import io.metersphere.dto.BugProviderDTO;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.AssociateBugRequest;
import io.metersphere.request.BugPageProviderRequest;

import java.util.List;

/**
 * @author wx
 */
public interface BaseAssociateBugProvider {

    /**
     * 获取尚未关联的缺陷列表
     *
     * @param sourceType             关联关系表表名
     * @param sourceName             关联关系表主动关联方字段名称
     * @param bugColumnName          缺陷id 在关联关系表的字段名称
     * @param bugPageProviderRequest 缺陷搜索条件
     * @return List<BugProviderDTO>
     */
    List<BugProviderDTO> getBugList(String sourceType, String sourceName, String bugColumnName, BugPageProviderRequest bugPageProviderRequest);


    /**
     * 获取选中的缺陷id 列表
     *
     * @param request request
     * @param deleted deleted
     * @return
     */
    List<String> getSelectBugs(AssociateBugRequest request, boolean deleted);


    /**
     * 关联用例处理
     *
     * @param ids    缺陷id集合
     * @param userId 用户id
     * @param caseId 用例id
     */
    void handleAssociateBug(List<String> ids, String userId, String caseId);

    /**
     * 取消关联缺陷
     *
     * @param id
     */
    void disassociateBug(String id);

    /**
     * 获取用例已关联缺陷列表
     *
     * @param request
     * @return
     */
    List<BugProviderDTO> hasAssociateBugPage(AssociateBugPageRequest request);

    List<BugProviderDTO> hasTestPlanAssociateBugPage(AssociateBugPageRequest request);
}
