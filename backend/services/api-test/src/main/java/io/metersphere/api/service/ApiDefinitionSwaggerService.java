package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiDefinitionSwagger;
import io.metersphere.api.mapper.ApiDefinitionSwaggerMapper;

 /**
 * 定时同步配置服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiDefinitionSwaggerService extends ServiceImpl<ApiDefinitionSwaggerMapper, ApiDefinitionSwagger> {
}