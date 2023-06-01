package io.metersphere.functional.service;


import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.dto.CaseReviewDto;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.sdk.util.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用例评审表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
public class CaseReviewService {

    @Resource
    private CaseReviewMapper caseReviewMapper;

    public void addCaseReview(CaseReview caseReview) {
        caseReviewMapper.insertSelective(caseReview);
    }

    public void delCaseReview(CaseReview caseReview) {
        caseReviewMapper.deleteByPrimaryKey(caseReview.getId());
    }

    public CaseReviewDto get(String id) {
        CaseReviewDto caseReviewDto = new CaseReviewDto();
        CaseReview caseReview = caseReviewMapper.selectByPrimaryKey(id);
        if (caseReview == null) {
            return null;
        }
        BeanUtils.copyBean(caseReviewDto, caseReview);
        return caseReviewDto;
    }
}