package io.metersphere.config;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.TestPlanReportContentWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.utils.CompressUtils;
import io.metersphere.interceptor.LikeStringEscapeInterceptor;
import io.metersphere.interceptor.MybatisInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseConfig {
    @Bean
    public MybatisInterceptor dbInterceptor() {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        List<io.metersphere.commons.utils.MybatisInterceptorConfig> configList = new ArrayList<>();
        //这三行不能删除，否则会覆盖sdk中的设置
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(FileContent.class, "file", CompressUtils.class, "zip", "unzip"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(TestResource.class, "configuration"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(AuthSource.class, "configuration"));
        //测试计划报告 api-base-count字段进行解压缩处理
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(TestPlanReportContentWithBLOBs.class, "apiBaseCount", CompressUtils.class, "zipString", "unzipString"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(TestPlanReportContentWithBLOBs.class, "planApiCaseReportStruct", CompressUtils.class, "zipString", "unzipString"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(TestPlanReportContentWithBLOBs.class, "planScenarioReportStruct", CompressUtils.class, "zipString", "unzipString"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(TestPlanReportContentWithBLOBs.class, "planLoadCaseReportStruct", CompressUtils.class, "zipString", "unzipString"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(TestPlanReportContentWithBLOBs.class, "planUiScenarioReportStruct", CompressUtils.class, "zipString", "unzipString"));
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }

    @Bean
    public LikeStringEscapeInterceptor likeStringEscapeInterceptor() {
        return new LikeStringEscapeInterceptor();
    }
}
