package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;

/**
 * 接口定义详情内容服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiDefinitionBlobService extends ServiceImpl<ApiDefinitionBlobMapper, ApiDefinitionBlob> {
}