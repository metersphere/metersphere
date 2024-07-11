package io.metersphere.service.wapper;

import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.dto.RelationshipGraphData;
import io.metersphere.request.GraphBatchRequest;
import io.metersphere.service.GraphService;
import io.metersphere.service.ServiceUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TrackGraphService {
    @Resource
    ExtTestCaseMapper extTestCaseMapper;
    @Resource
    GraphService graphService;

    public RelationshipGraphData getGraphData(String id, String type) {
        return graphService.getGraphData(id, extTestCaseMapper::getTestCaseForGraph);
    }

    public RelationshipGraphData getGraphDataByCondition(GraphBatchRequest request, String type) {
        request.getCondition().setNotEqStatus("Trash");
        ServiceUtils.buildCombineTagsToSupportMultiple(request.getCondition());
        return graphService.getGraphDataByCondition(request, extTestCaseMapper::selectIds, extTestCaseMapper::getTestCaseForGraph);
    }
}
