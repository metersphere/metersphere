package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiDefinitionFollow;
import io.metersphere.api.mapper.ApiDefinitionFollowMapper;

 /**
 * 接口定义关注人服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiDefinitionFollowService extends ServiceImpl<ApiDefinitionFollowMapper, ApiDefinitionFollow> {
}