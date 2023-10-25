package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.dto.FunctionalCaseDTO;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.sdk.util.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class FunctionalCaseNoticeService {

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    public FunctionalCaseDTO getRelatedUsers(FunctionalCaseCommentRequest functionalCaseCommentRequest){
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseCommentRequest.getCaseId());
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        if (functionalCase!=null) {
            BeanUtils.copyBean(functionalCaseDTO,functionalCase);
        }
        functionalCaseDTO.setRelatedUsers(functionalCaseCommentRequest.getNotifier());
        return functionalCaseDTO;
    }



}
