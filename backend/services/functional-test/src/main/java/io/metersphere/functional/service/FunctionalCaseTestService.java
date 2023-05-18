package io.metersphere.functional.service;


import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.functional.domain.FunctionalCaseTest;
import io.metersphere.functional.mapper.FunctionalCaseTestMapper;


/**
 * 功能用例和其他用例的中间表服务实现类
 *
 * @date : 2023-5-17
 */
@Service
public class FunctionalCaseTestService extends ServiceImpl<FunctionalCaseTestMapper, FunctionalCaseTest> {

}