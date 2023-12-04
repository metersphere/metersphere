package io.metersphere.functional.service;


import io.metersphere.functional.dto.FunctionalCaseReviewDTO;
import io.metersphere.functional.mapper.ExtCaseReviewFunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseReviewListRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<FunctionalCaseReviewDTO> getFunctionalCaseReviewPage(FunctionalCaseReviewListRequest request) {
       return extCaseReviewFunctionalCaseMapper.list(request);
    }
}