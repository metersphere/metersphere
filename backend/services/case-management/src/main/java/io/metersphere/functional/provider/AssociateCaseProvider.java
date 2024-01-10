package io.metersphere.functional.provider;

import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssociateCaseProvider implements BaseAssociateCaseProvider {

    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Override
    public List<TestCaseProviderDTO> listUnRelatedTestCaseList(TestCasePageProviderRequest testCasePageProviderRequest) {
        return extFunctionalCaseMapper.listUnRelatedCaseWithBug(testCasePageProviderRequest, false);
    }

    @Override
    public List<String> getRelatedIdsByParam(AssociateOtherCaseRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> relatedIds = extFunctionalCaseMapper.getSelectIdsByAssociateParam(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                relatedIds = relatedIds.stream().filter(id -> !request.getExcludeIds().contains(id)).toList();
            }
            return relatedIds;
        } else {
            return request.getSelectIds();
        }
    }
}
