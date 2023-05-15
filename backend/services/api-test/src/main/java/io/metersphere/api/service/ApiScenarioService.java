package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.mapper.ApiScenarioMapper;

 /**
 * 场景服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiScenarioService extends ServiceImpl<ApiScenarioMapper, ApiScenario> {
}