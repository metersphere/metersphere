package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiEnvironmentConfig;
import io.metersphere.api.mapper.ApiEnvironmentConfigMapper;
 /**
 * 接口定义环境服务实现类
 * @date : 2023-5-15
 */
@Service
public class ApiEnvironmentConfigService extends ServiceImpl<ApiEnvironmentConfigMapper, ApiEnvironmentConfig> {
}