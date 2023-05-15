package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.mapper.ApiDefinitionMapper;
 /**
 * 接口定义服务实现类
 * @date : 2023-5-15
 */
@Service
public class ApiDefinitionService extends ServiceImpl<ApiDefinitionMapper, ApiDefinition> {
}