package io.metersphere.api.service;

import io.metersphere.base.mapper.ApiDefinitionHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiAutomationService {
    @Resource
    private ApiDefinitionHistoryMapper apiDefinitionHistoryMapper;

}
