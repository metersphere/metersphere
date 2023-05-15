package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiDefinitionTemplate;
import io.metersphere.api.mapper.ApiDefinitionTemplateMapper;

 /**
 * API模版表服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiDefinitionTemplateService extends ServiceImpl<ApiDefinitionTemplateMapper, ApiDefinitionTemplate> {
}