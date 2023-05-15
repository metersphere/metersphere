package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiSyncConfig;
import io.metersphere.api.mapper.ApiSyncConfigMapper;

 /**
 * 接口同步用例配置服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiSyncConfigService extends ServiceImpl<ApiSyncConfigMapper, ApiSyncConfig> {
}