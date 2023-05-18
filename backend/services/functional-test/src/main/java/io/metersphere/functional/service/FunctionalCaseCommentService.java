package io.metersphere.functional.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.mapper.FunctionalCaseCommentMapper;
import org.springframework.stereotype.Service;

@Service
public class FunctionalCaseCommentService extends ServiceImpl<FunctionalCaseCommentMapper, FunctionalCaseComment> {
}
