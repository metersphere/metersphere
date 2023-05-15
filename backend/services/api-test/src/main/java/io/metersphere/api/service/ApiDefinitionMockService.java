package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;

 /**
 * mock 配置服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiDefinitionMockService extends ServiceImpl<ApiDefinitionMockMapper, ApiDefinitionMock> {
}