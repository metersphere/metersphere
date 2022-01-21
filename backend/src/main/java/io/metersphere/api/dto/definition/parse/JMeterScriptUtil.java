package io.metersphere.api.dto.definition.parse;

import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.GlobalScriptConfig;
import io.metersphere.api.dto.scenario.environment.GlobalScriptFilterRequest;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class JMeterScriptUtil {

    /**
     * 判断脚本是否被过滤
     *
     * @param filterProtocals 要过滤掉的请求类型
     * @param protocal        当前请求类型
     * @return
     */
    public static boolean isScriptFilter(List<String> filterProtocals, String protocal) {
        if (!CollectionUtils.isEmpty(filterProtocals)) {
            return filterProtocals.contains(protocal);
        } else {
            return false;
        }
    }

    public static MsJSR223PreProcessor getPreScript(EnvironmentConfig envConfig) {
        if (envConfig != null && envConfig.getPreProcessor() != null && StringUtils.isNotEmpty(envConfig.getPreProcessor().getScript())) {
            return envConfig.getPreProcessor();
        } else {
            return null;
        }
    }

    public static MsJSR223PostProcessor getPostScript(EnvironmentConfig envConfig) {
        if (envConfig != null && envConfig.getPostProcessor() != null && StringUtils.isNotEmpty(envConfig.getPostProcessor().getScript())) {
            return envConfig.getPostProcessor();
        } else {
            return null;
        }
    }

    /**
     * Samper中放置脚本
     *
     * @param envConfig            环境配置信息
     * @param samplerHashTree      sampler的hashtree
     * @param isAfterPrivateScript 是否将脚本放置在sampler的私有脚本之后
     * @param protocal             请求类型
     * @param environmentId        环境ID
     * @param config               参数配置
     */
    public static void setScript(EnvironmentConfig envConfig, HashTree samplerHashTree, String protocal, String environmentId, ParameterConfig config, boolean isAfterPrivateScript) {
        GlobalScriptConfig globalScriptConfig = envConfig != null ? envConfig.getGlobalScriptConfig() : null;

        boolean isPreScriptExecAfterPrivateScript = globalScriptConfig == null ? false : globalScriptConfig.isPreScriptExecAfterPrivateScript();
        boolean isPostScriptExecAfterPrivateScript = globalScriptConfig == null ? false : globalScriptConfig.isPostScriptExecAfterPrivateScript();
        List<String> preFilterProtocal = globalScriptConfig == null ? null : globalScriptConfig.getFilterRequestPreScript();
        List<String> postFilterProtocal = globalScriptConfig == null ? null : globalScriptConfig.getFilterRequestPostScript();
        MsJSR223PreProcessor preProcessor = JMeterScriptUtil.getPreScript(envConfig);
        MsJSR223PostProcessor postProcessor = JMeterScriptUtil.getPostScript(envConfig);
        boolean globalPreScriptIsFilter = JMeterScriptUtil.isScriptFilter(preFilterProtocal, protocal);
        boolean globalPostScriptIsFilter = JMeterScriptUtil.isScriptFilter(postFilterProtocal, protocal);
        if (isAfterPrivateScript) {
            if (isPreScriptExecAfterPrivateScript && !globalPreScriptIsFilter) {
                addItemHashTree(preProcessor, samplerHashTree, config, environmentId);
            }
            if (isPostScriptExecAfterPrivateScript && !globalPostScriptIsFilter) {
                addItemHashTree(postProcessor, samplerHashTree, config, environmentId);
            }
        } else {
            if (!isPreScriptExecAfterPrivateScript && !globalPreScriptIsFilter) {
                addItemHashTree(preProcessor, samplerHashTree, config, environmentId);
            }
            if (!isPostScriptExecAfterPrivateScript && !globalPostScriptIsFilter) {
                addItemHashTree(postProcessor, samplerHashTree, config, environmentId);
            }
        }
    }

    private static void addItemHashTree(MsTestElement element, HashTree samplerHashTree, ParameterConfig config, String environmentId) {
        if (element != null) {
            element.setEnvironmentId(element.getEnvironmentId() == null ? environmentId : element.getEnvironmentId());
            element.toHashTree(samplerHashTree, element.getHashTree(), config);
        }
    }

    public static void setHttpScript(HttpConfig httpConfig, HashTree httpSamplerTree, ParameterConfig config, String useEnvironment, String environmentId) {
        MsJSR223PreProcessor preProcessor = httpConfig.getPreProcessor();
        MsJSR223PostProcessor postProcessor = httpConfig.getPostProcessor();
        GlobalScriptConfig globalScriptConfig = httpConfig.getGlobalScriptConfig();
        List<String> filterPreProtocal = globalScriptConfig == null ? null : globalScriptConfig.getFilterRequestPreScript();
        List<String> filterPostProtocal = globalScriptConfig == null ? null : globalScriptConfig.getFilterRequestPostScript();

        boolean filterPre = JMeterScriptUtil.isScriptFilter(filterPreProtocal, GlobalScriptFilterRequest.HTTP.name());
        boolean filterPost = JMeterScriptUtil.isScriptFilter(filterPostProtocal, GlobalScriptFilterRequest.HTTP.name());
        boolean isPreScriptExecAfterPrivateScript = globalScriptConfig == null ? false : globalScriptConfig.isPreScriptExecAfterPrivateScript();
        boolean isPostScriptExecAfterPrivateScript = globalScriptConfig == null ? false : globalScriptConfig.isPostScriptExecAfterPrivateScript();

        if (!filterPre && preProcessor != null && StringUtils.isNotEmpty(preProcessor.getScript())) {
            if ((isPreScriptExecAfterPrivateScript) || (!isPreScriptExecAfterPrivateScript)) {
                if (preProcessor.getEnvironmentId() == null) {
                    if (environmentId == null) {
                        preProcessor.setEnvironmentId(useEnvironment);
                    } else {
                        preProcessor.setEnvironmentId(useEnvironment);
                    }
                }
                preProcessor.toHashTree(httpSamplerTree, preProcessor.getHashTree(), config);
            }
        }
        if (!filterPost && postProcessor != null && StringUtils.isNotEmpty(postProcessor.getScript())) {
            if ((isPostScriptExecAfterPrivateScript) || (!isPostScriptExecAfterPrivateScript)) {
                if (postProcessor.getEnvironmentId() == null) {
                    if (environmentId == null) {
                        postProcessor.setEnvironmentId(useEnvironment);
                    } else {
                        postProcessor.setEnvironmentId(environmentId);
                    }
                }
                postProcessor.toHashTree(httpSamplerTree, postProcessor.getHashTree(), config);
            }
        }
    }
}
