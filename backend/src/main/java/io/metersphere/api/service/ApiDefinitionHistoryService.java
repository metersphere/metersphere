package io.metersphere.api.service;

import io.metersphere.base.domain.ApiDefinitionHistory;
import io.metersphere.base.mapper.ApiDefinitionHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionHistoryService {
    @Resource
    private ApiDefinitionHistoryMapper apiDefinitionHistoryMapper;

    public List<ApiDefinitionHistory> selectByApiDefinitionId(String id){
        return apiDefinitionHistoryMapper.selectByApiDefinitionId(id);
    }
}
