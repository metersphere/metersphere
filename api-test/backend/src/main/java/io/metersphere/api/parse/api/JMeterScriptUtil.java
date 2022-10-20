package io.metersphere.api.parse.api;

import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.GlobalScriptConfig;
import io.metersphere.api.dto.scenario.environment.GlobalScriptFilterRequest;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class JMeterScriptUtil {

    /**
     * 判断脚本是否被过滤
     *
     * @param filterProtocols 要过滤掉的请求类型
     * @param protocol        当前请求类型
     * @return
     */
    public static boolean isScriptFilter(List<String> filterProtocols, String protocol) {
        if (!CollectionUtils.isEmpty(filterProtocols)) {
            return filterProtocols.contains(protocol);
        } else {
            return false;
        }
    }

    public static MsJSR223PreProcessor getPreScript(EnvironmentConfig envConfig) {
        if (envConfig != null && envConfig.getPreProcessor() != null && StringUtils.isNotEmpty(envConfig.getPreProcessor().getScript())) {
            MsJSR223PreProcessor preProcessor = new MsJSR223PreProcessor();
            if (envConfig.getPreProcessor() != null) {
                BeanUtils.copyBean(preProcessor, envConfig.getPreProcessor());
            }
            return preProcessor;
        } else {
            return null;
        }
    }

    public static MsJSR223PostProcessor getPostScript(EnvironmentConfig envConfig) {
        if (envConfig != null && envConfig.getPostProcessor() != null && StringUtils.isNotEmpty(envConfig.getPostProcessor().getScript())) {
            MsJSR223PostProcessor post = new MsJSR223PostProcessor();
            if (envConfig.getPostProcessor() != null) {
                BeanUtils.copyBean(post, envConfig.getPostProcessor());
            }
            return post;
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
     * @param protocol             请求类型
     * @param environmentId        环境ID
     * @param config               参数配置
     */
    public static void setScriptByEnvironmentConfig(EnvironmentConfig envConfig, HashTree samplerHashTree, String protocol, String environmentId, ParameterConfig config, boolean isAfterPrivateScript) {
        GlobalScriptConfig globalScriptConfig = envConfig != null ? envConfig.getGlobalScriptConfig() : null;
        MsJSR223PreProcessor preProcessor = JMeterScriptUtil.getPreScript(envConfig);
        MsJSR223PostProcessor postProcessor = JMeterScriptUtil.getPostScript(envConfig);
        setScript(globalScriptConfig, protocol, isAfterPrivateScript, environmentId, config, samplerHashTree, preProcessor, postProcessor);

    }

    public static void setScript(GlobalScriptConfig globalScriptConfig, String protocol, boolean isAfterPrivateScript, String environmentId, ParameterConfig config,
                                 HashTree samplerHashTree, MsJSR223PreProcessor preProcessor, MsJSR223PostProcessor postProcessor) {
        boolean isPreScriptExecAfterPrivateScript = globalScriptConfig == null ? false : globalScriptConfig.isPreScriptExecAfterPrivateScript();
        boolean isPostScriptExecAfterPrivateScript = globalScriptConfig == null ? false : globalScriptConfig.isPostScriptExecAfterPrivateScript();
        List<String> preFilterProtocol = globalScriptConfig == null ? null : globalScriptConfig.getFilterRequestPreScript();
        List<String> postFilterProtocol = globalScriptConfig == null ? null : globalScriptConfig.getFilterRequestPostScript();
        boolean globalPreScriptIsFilter = JMeterScriptUtil.isScriptFilter(preFilterProtocol, protocol);
        boolean globalPostScriptIsFilter = JMeterScriptUtil.isScriptFilter(postFilterProtocol, protocol);
        if (isAfterPrivateScript) {
            if (isPreScriptExecAfterPrivateScript && !globalPreScriptIsFilter && preProcessor != null && StringUtils.isNotEmpty(preProcessor.getScript())) {
                addItemHashTree(preProcessor, samplerHashTree, config, environmentId);
            }
            if (isPostScriptExecAfterPrivateScript && !globalPostScriptIsFilter && postProcessor != null && StringUtils.isNotEmpty(postProcessor.getScript())) {
                addItemHashTree(postProcessor, samplerHashTree, config, environmentId);
            }
        } else {
            if (!isPreScriptExecAfterPrivateScript && !globalPreScriptIsFilter && preProcessor != null && StringUtils.isNotEmpty(preProcessor.getScript())) {
                addItemHashTree(preProcessor, samplerHashTree, config, environmentId);
            }
            if (!isPostScriptExecAfterPrivateScript && !globalPostScriptIsFilter && postProcessor != null && StringUtils.isNotEmpty(postProcessor.getScript())) {
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

    public static void setScriptByHttpConfig(HttpConfig httpConfig, HashTree httpSamplerTree, ParameterConfig config, String useEnvironment, String environmentId, boolean isStepAfterElement) {
        MsJSR223PreProcessor preProcessor = httpConfig.getPreProcessor();
        MsJSR223PostProcessor postProcessor = httpConfig.getPostProcessor();
        GlobalScriptConfig globalScriptConfig = httpConfig.getGlobalScriptConfig();

        setScript(globalScriptConfig, GlobalScriptFilterRequest.HTTP.name(), isStepAfterElement, environmentId == null ? useEnvironment : environmentId, config, httpSamplerTree, preProcessor, postProcessor);
    }

}
