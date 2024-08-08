package io.metersphere.functional.config;

import io.metersphere.functional.dto.FunctionalCaseMindDTO;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FunctionalCaseManageInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> functionalCaseManageCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(FunctionalCaseMindDTO.class, "steps", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseMindDTO.class, "executeSteps", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseMindDTO.class, "textDescription", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseMindDTO.class, "expectedResult", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseMindDTO.class, "prerequisite", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseMindDTO.class, "description", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(FunctionalCaseMindDTO.class, "content", CompressUtils.class, "zip", "unzip"));


        return configList;
    }
}
