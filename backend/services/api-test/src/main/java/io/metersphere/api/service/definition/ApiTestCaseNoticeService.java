package io.metersphere.api.service.definition;

import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import org.springframework.stereotype.Service;

@Service
public class ApiTestCaseNoticeService {


    public ApiDefinitionCaseDTO getCaseDTO(ApiTestCaseAddRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        caseDTO.setCaseName(request.getName());
        caseDTO.setCaseStatus(request.getStatus());
        caseDTO.setCaseCreateUser(null);
        caseDTO.setCaseUpdateUser(null);
        return caseDTO;
    }


}
