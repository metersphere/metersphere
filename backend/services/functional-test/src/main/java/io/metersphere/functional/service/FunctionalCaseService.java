package io.metersphere.functional.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import org.springframework.stereotype.Service;

@Service
public class FunctionalCaseService extends ServiceImpl<FunctionalCaseMapper, FunctionalCase> {
}
