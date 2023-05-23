package io.metersphere.utils;

import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.environment.utils.XMLUtils;
import io.metersphere.metadata.service.FileCenter;
import io.metersphere.metadata.vo.FileRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.List;

//Jmx解析工具类
public class JmxParseUtil {
    public static boolean isJmxHasScriptByFiles(List<MultipartFile> files) {
        if (CollectionUtils.isNotEmpty(files)) {
            for (MultipartFile file : files) {
                if (jmxMultipartFileHasScript(file)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isJmxHasScriptByStorage(List<FileMetadataWithBLOBs> fileMetadataWithBLOBs) {
        for (FileMetadataWithBLOBs fileMetadata : fileMetadataWithBLOBs) {
            if (StringUtils.equalsIgnoreCase(fileMetadata.getType(), "jmx")) {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(fileMetadata.getName());
                fileRequest.setProjectId(fileMetadata.getProjectId());
                fileRequest.setResourceId(fileMetadata.getId());
                fileRequest.setStorage(fileMetadata.getStorage());
                fileRequest.setResourceType(fileMetadata.getResourceType());
                fileRequest.setType(fileMetadata.getType());
                fileRequest.setPath(fileMetadata.getPath());
                fileRequest.setFileAttachInfoByString(fileMetadata.getAttachInfo());

                if (jmxStorageFileHasScript(fileRequest)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean jmxMultipartFileHasScript(MultipartFile file) {
        if (file != null) {
            try {
                String fileName = file.getOriginalFilename();
                if (StringUtils.endsWithIgnoreCase(fileName, ".jmx")) {
                    return jmxBytesHasScript(file.getBytes());
                }
            } catch (Exception e) {
                LogUtil.error("检查上传的jmx文件是否含有脚本数据失败", e);
            }
        }
        return false;
    }

    private static boolean jmxStorageFileHasScript(FileRequest fileRequest) {
        try {
            return jmxBytesHasScript(FileCenter.getRepository(fileRequest.getStorage()).getFile(fileRequest));
        } catch (Exception e) {
            LogUtil.error("下载jmx文件解析是否含有脚本数据失败", e);
        }
        return false;
    }

    private static boolean jmxBytesHasScript(byte[] jmxFileByte) throws Exception {
        if (jmxFileByte != null) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            XMLUtils.setExpandEntityReferencesFalse(documentBuilderFactory);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            final Document document = builder.parse(new InputSource(new ByteArrayInputStream(jmxFileByte)));
            final Element jmxDoc = document.getDocumentElement();
            NodeList childNodes = jmxDoc.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element) {
                    if (hashTreeNodeHasScript((Element) node)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hashTreeNodeHasScript(Element hashTree) {
        if (invalid(hashTree)) {
            return false;
        }
        if (hashTree.getChildNodes().getLength() > 0) {
            final NodeList childNodes = hashTree.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element elementItem) {
                    if (invalid(elementItem)) {
                        continue;
                    }
                    if (nodeIsScript(elementItem)) {
                        return true;
                    } else {
                        //递归子节点是否有脚本
                        if (hashTreeNodeHasScript(elementItem)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean invalid(Element ele) {
        return !StringUtils.isBlank(ele.getAttribute("enabled")) && !Boolean.parseBoolean(ele.getAttribute("enabled"));
    }

    private static boolean nodeIsScript(Node node) {
        if (StringUtils.containsAnyIgnoreCase(node.getNodeName(), "JSR223", "Processor")) {
            //JSR223 或者 Processor，判断里面是否有Script阶段。 存在即为脚本
            NodeList childNodes = node.getChildNodes();
            for (int index = 0; index < childNodes.getLength(); index++) {
                Node childNode = childNodes.item(index);
                NamedNodeMap namedNodeMap = childNode.getAttributes();
                if (namedNodeMap != null) {
                    for (int i = 0; i < namedNodeMap.getLength(); i++) {
                        Node nameNodeItem = namedNodeMap.item(i);
                        if (StringUtils.equalsIgnoreCase(nameNodeItem.getNodeValue(), "script")) {
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }
}
