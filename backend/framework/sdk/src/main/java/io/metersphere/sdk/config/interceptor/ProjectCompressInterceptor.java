package io.metersphere.sdk.config.interceptor;

import io.metersphere.project.domain.*;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.sdk.util.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ProjectCompressInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> projectCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(FileModuleBlob.class, "repositoryDesc", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(ProjectExtend.class, "issueConfig", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FileMetadataBlob.class, "gitInfo", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FakeErrorBlob.class, "description", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FakeErrorBlob.class, "content", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(CustomFunctionBlob.class, "script", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(CustomFunctionBlob.class, "result", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(CustomFunctionBlob.class, "params", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(CustomFieldTemplate.class, "defaultValue", CompressUtils.class, "zip", "unzip"));

        return configList;
    }
}
