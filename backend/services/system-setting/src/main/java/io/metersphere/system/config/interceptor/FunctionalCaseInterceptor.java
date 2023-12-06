package io.metersphere.system.config.interceptor;

import io.metersphere.functional.domain.CaseReviewFunctionalCaseArchive;
import io.metersphere.functional.domain.CaseReviewHistory;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FunctionalCaseInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> functionalCaseCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(CaseReviewFunctionalCaseArchive.class, "content", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseBlob.class, "steps", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseBlob.class, "textDescription", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseBlob.class, "expectedResult", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseBlob.class, "prerequisite", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseBlob.class, "description", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(CaseReviewHistory.class, "content", CompressUtils.class, "zip", "unzip"));

        return configList;
    }
}
