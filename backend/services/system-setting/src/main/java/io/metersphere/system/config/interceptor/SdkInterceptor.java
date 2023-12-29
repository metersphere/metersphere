package io.metersphere.system.config.interceptor;

import io.metersphere.sdk.domain.OperationLogBlob;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SdkInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> sdkCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(OperationLogBlob.class, "originalValue", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(OperationLogBlob.class, "modifiedValue", CompressUtils.class, "zip", "unzip"));
        // ShareInfo
        configList.add(new MybatisInterceptorConfig(ShareInfo.class, "customData", CompressUtils.class, "zipString", "unzipString"));

        return configList;
    }
}
