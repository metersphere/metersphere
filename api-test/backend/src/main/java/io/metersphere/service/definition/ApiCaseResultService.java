package io.metersphere.service.definition;

import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.LinkedList;
import java.util.Map;

@Service
public class ApiCaseResultService {
    @Resource
    private ExtApiDefinitionExecResultMapper resultMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void batchSave(Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue) {
        if (!executeQueue.isEmpty()) {
            resultMapper.sqlInsert(new LinkedList<>(executeQueue.values()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void batchSave(ApiDefinitionExecResultWithBLOBs result) {
        if (result != null) {
            resultMapper.sqlInsert(new LinkedList<>(){{this.add(result);}});
        }
    }
}
