package io.metersphere.system.config.interceptor;

import io.metersphere.project.domain.CustomFunctionBlob;
import io.metersphere.project.domain.FileModuleRepository;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.sdk.util.EncryptUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ProjectInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> projectCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(FileModuleRepository.class, "token", EncryptUtils.class, "aesEncrypt", "aesDecrypt"));
        configList.add(new MybatisInterceptorConfig(CustomFunctionBlob.class, "script", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(CustomFunctionBlob.class, "result", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(CustomFunctionBlob.class, "params", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(EnvironmentBlob.class, "config", CompressUtils.class, "zip", "unzip"));

        return configList;
    }
}
