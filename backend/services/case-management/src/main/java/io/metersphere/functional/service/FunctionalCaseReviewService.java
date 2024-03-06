package io.metersphere.functional.service;


import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.dto.FunctionalCaseReviewDTO;
import io.metersphere.functional.mapper.ExtCaseReviewFunctionalCaseMapper;
import io.metersphere.functional.mapper.ExtCaseReviewHistoryMapper;
import io.metersphere.functional.request.FunctionalCaseReviewListRequest;
import io.metersphere.sdk.constants.UserRoleScope;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * 功能用例和其他用例的中间表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseReviewService {

    @Resource
    private ExtCaseReviewFunctionalCaseMapper extCaseReviewFunctionalCaseMapper;

    @Resource
    private ExtCaseReviewHistoryMapper extCaseReviewHistoryMapper;

    public List<FunctionalCaseReviewDTO> getFunctionalCaseReviewPage(FunctionalCaseReviewListRequest request) {
       return extCaseReviewFunctionalCaseMapper.list(request);
    }

    public List<CaseReviewHistoryDTO> getCaseReviewHistory(String caseId) {
        List<CaseReviewHistoryDTO> list = extCaseReviewHistoryMapper.getHistoryListWidthCaseId(caseId, null);
        for (CaseReviewHistoryDTO caseReviewHistoryDTO : list) {
            if (StringUtils.equalsIgnoreCase(caseReviewHistoryDTO.getCreateUser(), UserRoleScope.SYSTEM)) {
                caseReviewHistoryDTO.setUserName(Translator.get("case_review_history.system"));
            }
            if (caseReviewHistoryDTO.getContent() != null) {
                caseReviewHistoryDTO.setContentText(new String(caseReviewHistoryDTO.getContent(), StandardCharsets.UTF_8));
            }
        }
        return list;
    }
}