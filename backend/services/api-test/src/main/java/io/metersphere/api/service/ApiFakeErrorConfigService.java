package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiFakeErrorConfig;
import io.metersphere.api.mapper.ApiFakeErrorConfigMapper;

 /**
 * 误报库服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiFakeErrorConfigService extends ServiceImpl<ApiFakeErrorConfigMapper, ApiFakeErrorConfig> {
}