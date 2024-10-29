package io.metersphere.api.utils;

import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.assertion.MsAssertion;
import io.metersphere.project.api.assertion.MsResponseBodyAssertion;
import io.metersphere.project.api.processor.ExtractPostProcessor;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.api.processor.extract.MsExtract;
import io.metersphere.project.constants.ScriptLanguageType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;

import java.util.LinkedList;

public class ConverterUtils {
    public static void addPreProcess(AbstractMsTestElement parent, MsProcessor msProcessor) {
        if (CollectionUtils.isEmpty(parent.getChildren())) {
            MsCommonElement msCommonElement = new MsCommonElement();
            msCommonElement.getPreProcessorConfig().getProcessors().add(msProcessor);
            LinkedList<AbstractMsTestElement> children = new LinkedList<>();
            children.add(msCommonElement);
            parent.setChildren(children);
        } else {
            AbstractMsTestElement child = parent.getChildren().getFirst();
            if (child instanceof MsCommonElement msCommonElement) {
                msCommonElement.getPreProcessorConfig().getProcessors().add(msProcessor);
            } else {
                MsCommonElement msCommonElement = new MsCommonElement();
                msCommonElement.getPreProcessorConfig().getProcessors().add(msProcessor);
                parent.getChildren().add(msCommonElement);
            }
        }
    }

    public static void addPostProcess(AbstractMsTestElement parent, MsProcessor msProcessor) {
        if (CollectionUtils.isEmpty(parent.getChildren())) {
            MsCommonElement msCommonElement = new MsCommonElement();
            msCommonElement.getPostProcessorConfig().getProcessors().add(msProcessor);
            LinkedList<AbstractMsTestElement> children = new LinkedList<>();
            children.add(msCommonElement);
            parent.setChildren(children);
        } else {
            AbstractMsTestElement child = parent.getChildren().getFirst();
            if (child instanceof MsCommonElement msCommonElement) {
                msCommonElement.getPostProcessorConfig().getProcessors().add(msProcessor);
            } else {
                MsCommonElement msCommonElement = new MsCommonElement();
                msCommonElement.getPostProcessorConfig().getProcessors().add(msProcessor);
                parent.getChildren().add(msCommonElement);
            }
        }
    }

    public static void addPostExtract(AbstractMsTestElement parent, MsExtract msExtract) {
        if (CollectionUtils.isEmpty(parent.getChildren())) {
            ExtractPostProcessor extractPostProcessor = new ExtractPostProcessor();
            extractPostProcessor.getExtractors().add(msExtract);

            MsCommonElement msCommonElement = new MsCommonElement();
            msCommonElement.getPostProcessorConfig().getProcessors().add(extractPostProcessor);
            LinkedList<AbstractMsTestElement> children = new LinkedList<>();
            children.add(msCommonElement);
            parent.setChildren(children);
        } else {
            AbstractMsTestElement child = parent.getChildren().getFirst();
            if (child instanceof MsCommonElement msCommonElement) {
                ExtractPostProcessor extractPostProcessor = null;
                for (Object processor : msCommonElement.getPostProcessorConfig().getProcessors()) {
                    if (processor instanceof ExtractPostProcessor) {
                        extractPostProcessor = (ExtractPostProcessor) processor;
                        break;
                    }
                }
                if (extractPostProcessor == null) {
                    extractPostProcessor = new ExtractPostProcessor();
                    extractPostProcessor.getExtractors().add(msExtract);
                    msCommonElement.getPostProcessorConfig().getProcessors().add(extractPostProcessor);
                } else {
                    extractPostProcessor.getExtractors().add(msExtract);
                }
            } else {
                ExtractPostProcessor extractPostProcessor = new ExtractPostProcessor();
                extractPostProcessor.getExtractors().add(msExtract);

                MsCommonElement msCommonElement = new MsCommonElement();
                msCommonElement.getPostProcessorConfig().getProcessors().add(extractPostProcessor);
                parent.getChildren().add(msCommonElement);
            }
        }
    }

    public static SQLProcessor genJDBCProcessor(AbstractTestElement element) {
        SQLProcessor msScriptElement = new SQLProcessor();
        msScriptElement.setEnable(element.isEnabled());
        msScriptElement.setName(element.getName());
        msScriptElement.setScript(element.getPropertyAsString("query"));
        try {
            msScriptElement.setQueryTimeout(element.getPropertyAsLong("queryTimeout"));
        } catch (Exception ignore) {
        }
        msScriptElement.setResultVariable(element.getPropertyAsString("resultVariable"));
        msScriptElement.setVariableNames(element.getPropertyAsString("variableNames"));
        return msScriptElement;
    }

    public static void addAssertions(AbstractMsTestElement parent, MsResponseBodyAssertion msResponseBodyAssertion, String assertionType) {
        if (CollectionUtils.isEmpty(parent.getChildren())) {
            MsAssertionConfig extractPostProcessor = new MsAssertionConfig();
            extractPostProcessor.getAssertions().add(msResponseBodyAssertion);
            MsCommonElement msCommonElement = new MsCommonElement();
            msCommonElement.setAssertionConfig(extractPostProcessor);
            LinkedList<AbstractMsTestElement> children = new LinkedList<>();
            children.add(msCommonElement);
            parent.setChildren(children);
        } else {
            AbstractMsTestElement child = parent.getChildren().getFirst();
            if (child instanceof MsCommonElement msCommonElement) {
                MsAssertionConfig assertionConfig = msCommonElement.getAssertionConfig();
                if (assertionConfig == null) {
                    assertionConfig = new MsAssertionConfig();
                    assertionConfig.getAssertions().add(msResponseBodyAssertion);
                    msCommonElement.setAssertionConfig(assertionConfig);
                } else {
                    boolean hasRepBodyAssertion = false;
                    for (MsAssertion assertion : assertionConfig.getAssertions()) {
                        if (assertion instanceof MsResponseBodyAssertion) {
                            hasRepBodyAssertion = true;
                            if ("JSON_PATH".equals(assertionType)) {
                                ((MsResponseBodyAssertion) assertion).setJsonPathAssertion(msResponseBodyAssertion.getJsonPathAssertion());
                            } else if ("XPATH".equals(assertionType)) {
                                ((MsResponseBodyAssertion) assertion).setXpathAssertion(msResponseBodyAssertion.getXpathAssertion());
                            }
                        }
                    }
                    if (!hasRepBodyAssertion) {
                        assertionConfig.getAssertions().add(msResponseBodyAssertion);
                    }
                }
            } else {
                MsAssertionConfig assertionConfig = new MsAssertionConfig();
                assertionConfig.getAssertions().add(msResponseBodyAssertion);
                MsCommonElement msCommonElement = new MsCommonElement();
                msCommonElement.setAssertionConfig(assertionConfig);
                parent.getChildren().add(msCommonElement);
            }
        }

    }

    public static String parseScriptLanguage(String scriptLanguage) {
        if (StringUtils.equalsIgnoreCase(scriptLanguage, "beanshell")) {
            return ScriptLanguageType.BEANSHELL_JSR233.name();
        } else if (StringUtils.equalsIgnoreCase(scriptLanguage, "groovy")) {
            return ScriptLanguageType.GROOVY.name();
        } else if (StringUtils.equalsIgnoreCase(scriptLanguage, "javascript")) {
            return ScriptLanguageType.JAVASCRIPT.name();
        } else if (StringUtils.equalsIgnoreCase(scriptLanguage, "python")) {
            return ScriptLanguageType.PYTHON.name();
        } else {
            return scriptLanguage;
        }
    }
}
