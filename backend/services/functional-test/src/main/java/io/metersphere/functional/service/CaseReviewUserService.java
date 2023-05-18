package io.metersphere.functional.service;


import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.functional.domain.CaseReviewUser;
import io.metersphere.functional.mapper.CaseReviewUserMapper;

/**
 * 评审和评审人中间表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
public class CaseReviewUserService extends ServiceImpl<CaseReviewUserMapper, CaseReviewUser> {

}