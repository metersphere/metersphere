package io.metersphere.service.scenario;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsHashTreeConstants;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.request.RelationshipEdgeRequest;
import io.metersphere.service.RelationshipEdgeService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiAutomationRelationshipEdgeService {

    @Resource
    private RelationshipEdgeService relationshipEdgeService;

    // 初始化场景关系
    public void initRelationshipEdge(ApiScenarioWithBLOBs preBlobs, ApiScenarioWithBLOBs scenarioWithBLOBs) {
        if (scenarioWithBLOBs == null || StringUtils.isEmpty(scenarioWithBLOBs.getScenarioDefinition())) {
            return;
        }

        // 获取当前场景的关系
        List<String> referenceRelationships = this.contentAnalysis(scenarioWithBLOBs);

        // 获取更新前的关系
        List<String> beforeReferenceRelationships = Collections.emptyList();
        if (preBlobs != null && StringUtils.isNotEmpty(preBlobs.getScenarioDefinition())) {
            beforeReferenceRelationships = this.contentAnalysis(preBlobs);
        }

        // 如果有新增的关系，进行批量保存
        if (CollectionUtils.isNotEmpty(referenceRelationships)) {
            RelationshipEdgeRequest request = new RelationshipEdgeRequest();
            request.setId(scenarioWithBLOBs.getId());
            request.setTargetIds(referenceRelationships);
            request.setType("API_SCENARIO");
            relationshipEdgeService.saveBatch(request);
        }

        // 比较并处理关系
        List<String> removedRelationships = new ArrayList<>(beforeReferenceRelationships);
        removedRelationships.removeAll(referenceRelationships);

        // 如果有删除的关系，启动线程处理
        if (CollectionUtils.isNotEmpty(removedRelationships)) {
            new Thread(() -> relationshipEdgeService.delete(scenarioWithBLOBs.getId(), removedRelationships)).start();
        }
    }

    private List<String> contentAnalysis(ApiScenarioWithBLOBs scenarioWithBLOBs) {
        List<String> referenceRelationships = new ArrayList<>();
        if (scenarioWithBLOBs.getScenarioDefinition().contains("\"referenced\":\"REF\"")) {
            // 深度解析对比，防止是复制的关系
            JSONObject element = JSONUtil.parseObject(scenarioWithBLOBs.getScenarioDefinition());
            // 历史数据处理
            relationships(element.getJSONArray(ElementConstants.HASH_TREE), referenceRelationships);
        }
        return referenceRelationships;
    }

    /**
     * 只找出场景直接依赖
     *
     * @param hashTree
     * @param referenceRelationships
     */
    public static void relationships(JSONArray hashTree, List<String> referenceRelationships) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && element.has(ElementConstants.TYPE)
                    && StringUtils.equals(element.optString(ElementConstants.TYPE), ElementConstants.SCENARIO)
                    && element.has(MsHashTreeConstants.REFERENCED)
                    && StringUtils.equals(element.optString(MsHashTreeConstants.REFERENCED), MsHashTreeConstants.REF)) {
                if (!referenceRelationships.contains(element.optString(MsHashTreeConstants.ID))
                        && element.optString(MsHashTreeConstants.ID).length() < 50) {
                    referenceRelationships.add(element.optString(MsHashTreeConstants.ID));
                }
            } else {
                if (element.has(ElementConstants.HASH_TREE)) {
                    JSONArray elementJSONArray = element.getJSONArray(ElementConstants.HASH_TREE);
                    relationships(elementJSONArray, referenceRelationships);
                }
            }
        }
    }
}
