package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiScenarioFollow;
import io.metersphere.api.mapper.ApiScenarioFollowMapper;

 /**
 * 关注记录服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiScenarioFollowService extends ServiceImpl<ApiScenarioFollowMapper, ApiScenarioFollow> {
}