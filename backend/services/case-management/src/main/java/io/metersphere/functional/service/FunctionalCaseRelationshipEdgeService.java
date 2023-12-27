/**
 * @filename:FunctionalCaseRelationshipEdgeServiceImpl 2023年5月17日
 * @project ms  V3.x
 * Copyright(c) 2018 wx Co. Ltd.
 * All right reserved.
 */
package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseRelationshipEdge;
import io.metersphere.functional.domain.FunctionalCaseRelationshipEdgeExample;
import io.metersphere.functional.mapper.FunctionalCaseRelationshipEdgeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class FunctionalCaseRelationshipEdgeService {

    @Resource
    private FunctionalCaseRelationshipEdgeMapper functionalCaseRelationshipEdgeMapper;


    public List<String> getExcludeIds(String id) {
        List<FunctionalCaseRelationshipEdge> relationshipEdges = getRelationshipEdges(id);
        List<String> ids = relationshipEdges.stream().map(FunctionalCaseRelationshipEdge::getTargetId).collect(Collectors.toList());
        ids.addAll(relationshipEdges.stream().map(FunctionalCaseRelationshipEdge::getSourceId).collect(Collectors.toList()));
        List<String> list = ids.stream().distinct().toList();
        return list;
    }

    private List<FunctionalCaseRelationshipEdge> getRelationshipEdges(String id) {
        FunctionalCaseRelationshipEdgeExample example = new FunctionalCaseRelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(id);
        example.or(
                example.createCriteria()
                        .andTargetIdEqualTo(id)
        );
        return functionalCaseRelationshipEdgeMapper.selectByExample(example);
    }

}