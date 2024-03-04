package io.metersphere.functional.provider;

import io.metersphere.dto.TestCaseProviderDTO;
import io.metersphere.functional.dto.FunctionalCaseCustomFieldDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssociateCaseProvider implements BaseAssociateCaseProvider {

    @Resource
    private FunctionalCaseService functionalCaseService;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Override
    public List<TestCaseProviderDTO> listUnRelatedTestCaseList(TestCasePageProviderRequest testCasePageProviderRequest) {
        List<TestCaseProviderDTO> functionalCases = extFunctionalCaseMapper.listUnRelatedCaseWithBug(testCasePageProviderRequest, false, testCasePageProviderRequest.getSortString());
        if (CollectionUtils.isEmpty(functionalCases)) {
            return new ArrayList<>();
        }
        List<String> ids = functionalCases.stream().map(TestCaseProviderDTO::getId).toList();
        Map<String, List<FunctionalCaseCustomFieldDTO>> caseCustomFiledMap = functionalCaseService.getCaseCustomFiledMap(ids);
        functionalCases.forEach(functionalCase -> {
            List<FunctionalCaseCustomFieldDTO> customFields = caseCustomFiledMap.get(functionalCase.getId());
            Optional<FunctionalCaseCustomFieldDTO> priorityField = customFields.stream().filter(field -> StringUtils.equals(Translator.get("custom_field.functional_priority"), field.getFieldName())).findFirst();
			priorityField.ifPresent(functionalCaseCustomFieldDTO -> functionalCase.setPriority(functionalCaseCustomFieldDTO.getDefaultValue()));
        });
        return functionalCases;
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
