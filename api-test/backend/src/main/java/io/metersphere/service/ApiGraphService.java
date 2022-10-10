package io.metersphere.service;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.xpack.graph.request.GraphBatchRequest;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.dto.RelationshipGraphData;
import io.metersphere.xpack.graph.GraphService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiGraphService {
    @Resource
    ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    public RelationshipGraphData getGraphData(String id, String type) {
        GraphService graphService = CommonBeanFactory.getBean(GraphService.class);
        if (StringUtils.equals(type, "API")) {
            return graphService.getGraphData(id, extApiDefinitionMapper::getForGraph);
        }
        return new RelationshipGraphData();
    }

    public RelationshipGraphData getGraphDataByCondition(GraphBatchRequest request, String type) {
        GraphService graphService = CommonBeanFactory.getBean(GraphService.class);
        request.getCondition().setNotEqStatus("Trash");
        if (StringUtils.equals(type, "API_SCENARIO")) {
            return graphService.getGraphDataByCondition(request, extApiScenarioMapper::selectIdsByQuery, extApiScenarioMapper::getTestCaseForGraph);
        } else if (StringUtils.equals(type, "API")) {
            return graphService.getGraphDataByCondition(request, extApiDefinitionMapper::selectIds, extApiDefinitionMapper::getForGraph);
        }
        return new RelationshipGraphData();
    }
}
