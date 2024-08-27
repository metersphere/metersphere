package io.metersphere.api.utils;

import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.ExtractPostProcessor;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.api.processor.extract.MsExtract;
import org.apache.commons.collections4.CollectionUtils;
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
}
