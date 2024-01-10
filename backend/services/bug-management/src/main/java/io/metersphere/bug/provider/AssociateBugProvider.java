package io.metersphere.bug.provider;


import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.bug.mapper.ExtBugMapper;
import io.metersphere.bug.mapper.ExtBugRelateCaseMapper;
import io.metersphere.bug.service.BugRelateCaseCommonService;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.request.AssociateBugPageRequest;
import io.metersphere.request.AssociateBugRequest;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class AssociateBugProvider implements BaseAssociateBugProvider {
    @Resource
    private ExtBugMapper extBugMapper;
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;
    @Resource
    private BugRelateCaseCommonService bugRelateCaseCommonService;
    @Resource
    private ExtBugRelateCaseMapper extBugRelateCaseMapper;


    @Override
    public List<BugProviderDTO> getBugList(String sourceType, String sourceName, String bugColumnName, BugPageProviderRequest bugPageProviderRequest) {
        return extBugMapper.listByProviderRequest(sourceType, sourceName, bugColumnName, bugPageProviderRequest, false);
        //TODO 需要转义状态和处理人属性
    }

    @Override
    public List<String> getSelectBugs(AssociateBugRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> ids = extBugMapper.getIdsByProvider(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    @Override
    public void handleAssociateBug(List<String> ids, String userId, String caseId) {
        List<BugRelationCase> list = new ArrayList<>();
        ids.forEach(id -> {
            BugRelationCase bugRelationCase = new BugRelationCase();
            bugRelationCase.setId(IDGenerator.nextStr());
            bugRelationCase.setBugId(id);
            bugRelationCase.setCaseId(caseId);
            bugRelationCase.setCaseType("FUNCTIONAL");
            bugRelationCase.setCreateUser(userId);
            bugRelationCase.setCreateTime(System.currentTimeMillis());
            bugRelationCase.setUpdateTime(System.currentTimeMillis());
            list.add(bugRelationCase);
        });
        bugRelationCaseMapper.batchInsert(list);
    }

    @Override
    public void disassociateBug(String id) {
        bugRelateCaseCommonService.unRelate(id);
    }

    @Override
    public List<BugProviderDTO> hasAssociateBugPage(AssociateBugPageRequest request) {
        List<BugProviderDTO> associateBugs = extBugRelateCaseMapper.getAssociateBugs(request, request.getSortString());
        //TODO 需要转义状态和处理人属性
        associateBugs.stream().forEach(item -> {
            if (StringUtils.isNotBlank(item.getTestPlanName())) {
                item.setSource(Translator.get("test_plan_relate"));
            } else {
                item.setSource(Translator.get("direct_related"));
            }
        });
        return associateBugs;
    }
}
