package io.metersphere.functional.service;


import io.metersphere.functional.dto.CaseReviewDTO;
import org.springframework.stereotype.Service;

/**
 * 用例评审表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
public class CaseReviewService {


    public CaseReviewDTO get(String id) {
        CaseReviewDTO caseReviewDto = new CaseReviewDTO();
        return caseReviewDto;
    }
}