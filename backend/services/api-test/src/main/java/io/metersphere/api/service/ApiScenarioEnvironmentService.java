package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiScenarioEnvironment;
import io.metersphere.api.mapper.ApiScenarioEnvironmentMapper;

 /**
 * 场景环境服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiScenarioEnvironmentService extends ServiceImpl<ApiScenarioEnvironmentMapper, ApiScenarioEnvironment> {
}