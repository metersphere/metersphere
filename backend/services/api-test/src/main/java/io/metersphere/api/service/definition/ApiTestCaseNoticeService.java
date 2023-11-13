package io.metersphere.api.service.definition;

import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import io.metersphere.system.utils.SessionUtils;
import org.springframework.stereotype.Service;

@Service
public class ApiTestCaseNoticeService {


    public ApiDefinitionCaseDTO getCaseDTO(ApiTestCaseAddRequest request) {
        String userId = SessionUtils.getUserId();
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        caseDTO.setCaseName(request.getName());
        caseDTO.setCaseStatus(request.getStatus());
        caseDTO.setCaseCreateUser(userId);
        caseDTO.setCaseUpdateUser(userId);
        return caseDTO;
    }


}
