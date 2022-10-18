package io.metersphere.api.parse.scenario;

import io.metersphere.api.dto.automation.EsbDataStruct;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/15 5:00 下午
 * @Description
 */
public class EsbDataParser {
    public static String esbData2XmlByParamStruct(List<EsbDataStruct> esbDataList, String[] paramArr) {
        String xmlString = StringUtils.EMPTY;
        try {
            if (esbDataList == null || esbDataList.isEmpty()) {
                return xmlString;
            }
            Document document = DocumentHelper.createDocument();
            EsbDataStruct dataStruct = selectEsbDataStructByNameStruct(esbDataList, paramArr, 0);
            if (dataStruct != null) {
                dataStruct.genXmlElementByDocument(document);
                xmlString = document.getRootElement().asXML();

                // 设置XML文档格式
                OutputFormat outputFormat = OutputFormat.createPrettyPrint();
                // 设置XML编码方式,即是用指定的编码方式保存XML文档到字符串(String),这里也可以指定为GBK或是ISO8859-1
                outputFormat.setEncoding(StandardCharsets.UTF_8.name());
                //outputFormat.setSuppressDeclaration(true); //是否生产xml头
                outputFormat.setIndent(true); //设置是否缩进
                outputFormat.setNewlines(true); //设置是否换行

                try {
                    // stringWriter字符串是用来保存XML文档的
                    StringWriter stringWriter = new StringWriter();
                    // xmlWriter是用来把XML文档写入字符串的(工具)
                    XMLWriter xmlWriter = new XMLWriter(stringWriter, outputFormat);
                    // 把创建好的XML文档写入字符串
                    xmlWriter.write(document);

                    // 打印字符串,即是XML文档
                    xmlString = stringWriter.toString();
                    xmlWriter.close();
                } catch (IOException e) {
                    LogUtil.error(e);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (StringUtils.isEmpty(xmlString)) {
            xmlString = StringUtils.EMPTY;
        } else {
            xmlString = xmlString.replaceAll("  ", StringUtils.EMPTY);
        }
        return xmlString;
    }

    //根据参数结构，递归查询数据
    private static EsbDataStruct selectEsbDataStructByNameStruct(List<EsbDataStruct> esbDataList, String[] paramArr, int index) {
        EsbDataStruct returnData = null;
        if (paramArr.length > index) {
            String param = paramArr[index];
            for (EsbDataStruct dataStuct : esbDataList) {
                if (StringUtils.equals(dataStuct.getName(), param)) {
                    int newIndex = index + 1;
                    if (paramArr.length > newIndex && dataStuct.getChildren() != null) {
                        EsbDataStruct childElement = selectEsbDataStructByNameStruct(dataStuct.getChildren(), paramArr, newIndex);
                        if (childElement != null) {
                            returnData = dataStuct.copy(false);
                            returnData.getChildren().add(childElement);
                        }
                    } else {
                        returnData = dataStuct.copy(true);
                    }
                } else if (index == 0) {
                    //如果是第一个节点不符合，则遍历子节点是否有符合的。
                    int newIndex = index;
                    EsbDataStruct itemData = selectEsbDataStructByNameStruct(dataStuct.getChildren(), paramArr, newIndex);
                    if (itemData != null) {
                        returnData = itemData;
                    }
                }
            }
        }
        return returnData;
    }
}
