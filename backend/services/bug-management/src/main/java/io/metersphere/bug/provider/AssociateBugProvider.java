package io.metersphere.bug.provider;


import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.bug.mapper.ExtBugMapper;
import io.metersphere.bug.service.BugRelateCaseService;
import io.metersphere.dto.BugProviderDTO;
import io.metersphere.provider.BaseAssociateBugProvider;
import io.metersphere.request.AssociateBugRequest;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
    private BugRelateCaseService bugRelateCaseService;


    @Override
    public List<BugProviderDTO> getBugList(String sourceType, String sourceName, String bugColumnName, BugPageProviderRequest bugPageProviderRequest) {
        return extBugMapper.listByProviderRequest(sourceType, sourceName, bugColumnName, bugPageProviderRequest, false);
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
        bugRelateCaseService.unRelate(id);
    }
}
