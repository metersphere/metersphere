package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiDefinitionMockConfig;
import io.metersphere.api.mapper.ApiDefinitionMockConfigMapper;

 /**
 * mock期望值配置服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiDefinitionMockConfigService extends ServiceImpl<ApiDefinitionMockConfigMapper, ApiDefinitionMockConfig> {
}