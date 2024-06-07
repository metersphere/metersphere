package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseExample;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRequest;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseRecoverService {

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;

    public void batchRecover(ApiTestCaseBatchRequest request, String userId) {

        List<String> ids = doSelectIds(request, true);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        //检查用例的接口是否存在  需要分开两批处理  一个是接口被删的  一个是只恢复用例的
        List<String> definitionIds = extApiTestCaseMapper.selectIdsByCaseIds(ids);
        if (CollectionUtils.isNotEmpty(definitionIds)) {
            List<String> apiIds = extApiDefinitionMapper.selectIdsByIdsAndDeleted(definitionIds, true);
            apiDefinitionService.handleRecoverApiDefinition(apiIds, userId, request.getProjectId(), true);
            definitionIds.removeAll(apiIds);
            if (CollectionUtils.isNotEmpty(definitionIds)) {
                //接口被删的用例
                List<String> caseIds = extApiTestCaseMapper.getCaseIds(definitionIds, true);
                ids.retainAll(caseIds);
            }
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andIdIn(ids).andDeletedEqualTo(true);
            List<ApiTestCase> caseList = apiTestCaseMapper.selectByExample(example);
            apiTestCaseService.batchRecover(caseList, userId, request.getProjectId());
        }

    }

    public List<String> doSelectIds(ApiTestCaseBatchRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> ids = extApiTestCaseMapper.getIds(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }
}
