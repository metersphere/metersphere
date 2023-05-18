package io.metersphere.performance.parse;

import io.metersphere.base.domain.FileContent;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.XMLUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

    public static boolean isJmxHasScriptByStorage(List<FileContent> fileContentList) {
        for (FileContent fileContent : fileContentList) {
            try {
                return jmxBytesHasScript(fileContent.getFile());
            } catch (Exception e) {
                LogUtil.error("下载jmx文件解析是否含有脚本数据失败", e);
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
                if (node instanceof Element) {
                    Element elementItem = (Element) node;
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
        return StringUtils.containsAnyIgnoreCase(node.getNodeName(), "JSR223", "Processor");
    }
}
