package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiTestCaseFollow;
import io.metersphere.api.mapper.ApiTestCaseFollowMapper;

 /**
 * 接口用例关注人服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiTestCaseFollowService extends ServiceImpl<ApiTestCaseFollowMapper, ApiTestCaseFollow> {
}