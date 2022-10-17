package io.metersphere.commons.utils;

import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.api.exec.engine.EngineSourceParserFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.aspectj.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataFormattingUtil {

    /**
     * 更新jmx数据，处理jmx里的各种参数
     * <p>
     *
     * @param jmx 原JMX文件
     * @return
     * @author song tianyang
     */
    public static JmxInfoDTO updateJmxString(String jmx, boolean saveFile) {
        FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
        jmx = updateJmxMessage(jmx);
        //处理附件
        Map<String, String> attachmentFiles = new HashMap<>();
        List<FileMetadata> fileMetadataList = new ArrayList<>();
        if (saveFile) {
            //获取要转化的文件
            List<String> attachmentFilePathList = new ArrayList<>();
            try {
                Document doc = EngineSourceParserFactory.getDocument(new ByteArrayInputStream(jmx.getBytes(StandardCharsets.UTF_8.name())));
                Element root = doc.getRootElement();
                Element rootHashTreeElement = root.element(ElementConstants.HASH_TREE);
                List<Element> innerHashTreeElementList = rootHashTreeElement.elements(ElementConstants.HASH_TREE);
                for (Element innerHashTreeElement : innerHashTreeElementList) {
                    List<Element> thirdHashTreeElementList = innerHashTreeElement.elements();
                    for (Element element : thirdHashTreeElementList) {
                        //HTTPSamplerProxy， 进行附件转化： 1.elementProp里去掉路径； 2。elementProp->filePath获取路径并读出来
                        attachmentFilePathList.addAll(parseAttachmentFileInfo(element));
                    }
                    //如果存在证书文件，也要匹配出来
                    attachmentFilePathList.addAll(parseAttachmentFileInfo(rootHashTreeElement));
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }

            //去重处理
            if (!CollectionUtils.isEmpty(attachmentFilePathList)) {
                attachmentFilePathList = attachmentFilePathList.stream().distinct().collect(Collectors.toList());
            }
            for (String filePath : attachmentFilePathList) {
                File file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    try {
                        FileMetadata fileMetadata = fileMetadataService.saveFile(FileUtil.readAsByteArray(file), file.getName(), file.length());
                        if (fileMetadata != null) {
                            fileMetadataList.add(fileMetadata);
                            attachmentFiles.put(fileMetadata.getId(), fileMetadata.getName());
                        }
                    } catch (Exception e) {
                        LogUtil.error(e);
                    }
                }
            }
        }

        if (!jmx.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
            jmx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + jmx;
        }

        JmxInfoDTO returnDTO = new JmxInfoDTO("Demo.jmx", jmx, attachmentFiles);
        returnDTO.setFileMetadataList(fileMetadataList);
        return returnDTO;
    }

    private static List<String> parseAttachmentFileInfo(Element parentHashTreeElement) {
        List<String> attachmentFilePathList = new ArrayList<>();
        List<Element> parentElementList = parentHashTreeElement.elements();
        for (Element parentElement : parentElementList) {
            String qname = parentElement.getQName().getName();
            if (StringUtils.equals(qname, "CSVDataSet")) {
                try {
                    List<Element> propElementList = parentElement.elements();
                    for (Element propElement : propElementList) {
                        if (StringUtils.equals("filename", propElement.attributeValue("name"))) {
                            String filePath = propElement.getText();
                            File file = new File(filePath);
                            if (file.exists() && file.isFile()) {
                                attachmentFilePathList.add(filePath);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            } else if (StringUtils.equals(qname, ElementConstants.HTTP_SAMPLER)) {
                List<Element> elementPropElementList = parentElement.elements("elementProp");
                for (Element element : elementPropElementList) {
                    if (StringUtils.equals(element.attributeValue("name"), "HTTPsampler.Files")) {
                        List<Element> collectionPropList = element.elements("collectionProp");
                        for (Element prop : collectionPropList) {
                            List<Element> elementProps = prop.elements();
                            for (Element elementProp : elementProps) {
                                if (StringUtils.equals(elementProp.attributeValue("elementType"), "HTTPFileArg")) {
                                    try {
                                        String filePath = elementProp.attributeValue("name");
                                        File file = new File(filePath);
                                        if (file.exists() && file.isFile()) {
                                            attachmentFilePathList.add(filePath);
                                            String fileName = file.getName();
                                            elementProp.attribute("name").setText(fileName);
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (StringUtils.equals(qname, "KeystoreConfig")) {
                List<Element> stringPropList = parentElement.elements("stringProp");
                for (Element element : stringPropList) {
                    if (StringUtils.equals(element.attributeValue("name"), "MS-KEYSTORE-FILE-PATH")) {

                        try {
                            String filePath = element.getStringValue();
                            File file = new File(filePath);
                            if (file.exists() && file.isFile()) {
                                attachmentFilePathList.add(filePath);
                                String fileName = file.getName();
                                element.setText(fileName);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } else if (StringUtils.equals(qname, ElementConstants.HASH_TREE)) {
                attachmentFilePathList.addAll(parseAttachmentFileInfo(parentElement));
            }
        }

        return attachmentFilePathList;
    }

    private static String updateJmxMessage(String jmx) {
        if (StringUtils.isNotEmpty(jmx)) {
            jmx = StringUtils.replace(jmx, "<DubboSample", "<io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample");
            jmx = StringUtils.replace(jmx, "</DubboSample>", "</io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample>");
            jmx = StringUtils.replace(jmx, " guiclass=\"DubboSampleGui\" ", " guiclass=\"io.github.ningyu.jmeter.plugin.dubbo.gui.DubboSampleGui\" ");
            jmx = StringUtils.replace(jmx, " guiclass=\"DubboDefaultConfigGui\" ", " guiclass=\"io.github.ningyu.jmeter.plugin.dubbo.gui.DubboDefaultConfigGui\" ");
            jmx = StringUtils.replace(jmx, " testclass=\"DubboSample\" ", " testclass=\"io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample\" ");
        }
        return jmx;
    }

    public static JmxInfoDTO getJmxInfoDTO(RunDefinitionRequest runRequest, List<MultipartFile> bodyFiles) {
        BaseEnvironmentService fileMetadataService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
        ParameterConfig config = new ParameterConfig();
        config.setProjectId(runRequest.getProjectId());
        config.setOperating(true);
        config.setOperatingSampleTestName(runRequest.getName());

        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        Map<String, String> map = runRequest.getEnvironmentMap();
        if (map != null && map.size() > 0) {
            ApiTestEnvironmentWithBLOBs environment = fileMetadataService.get(map.get(runRequest.getProjectId()));
            EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
            envConfig.put(runRequest.getProjectId(), env);
            config.setConfig(envConfig);
        }
        HashTree hashTree = runRequest.getTestElement().generateHashTree(config);
        String jmxString = runRequest.getTestElement().getJmx(hashTree);
        //将jmx处理封装为通用方法
        JmxInfoDTO dto = updateJmxString(jmxString, true);
        dto.setName(runRequest.getName() + ".jmx");
        return dto;
    }
}
