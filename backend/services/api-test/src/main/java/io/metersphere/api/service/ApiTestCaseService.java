package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.mapper.ApiTestCaseMapper;

 /**
 * 接口用例服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiTestCaseService extends ServiceImpl<ApiTestCaseMapper, ApiTestCase> {
}