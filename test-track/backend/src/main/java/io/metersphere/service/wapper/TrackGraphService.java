package io.metersphere.service.wapper;


import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.RelationshipGraphData;
import io.metersphere.xpack.graph.GraphService;
import io.metersphere.xpack.graph.request.GraphBatchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class TrackGraphService {
    @Resource
    ExtTestCaseMapper extTestCaseMapper;

    public RelationshipGraphData getGraphData(String id, String type) {
        GraphService graphService = CommonBeanFactory.getBean(GraphService.class);
        return graphService.getGraphData(id, extTestCaseMapper::getTestCaseForGraph);
    }

    public RelationshipGraphData getGraphDataByCondition(GraphBatchRequest request, String type) {
        request.getCondition().setNotEqStatus("Trash");
        GraphService graphService = CommonBeanFactory.getBean(GraphService.class);
        return graphService.getGraphDataByCondition(request, extTestCaseMapper::selectIds, extTestCaseMapper::getTestCaseForGraph);
    }
}
