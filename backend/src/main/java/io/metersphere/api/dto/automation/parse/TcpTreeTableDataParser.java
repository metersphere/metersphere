package io.metersphere.api.dto.automation.parse;

import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/15 5:00 下午
 * @Description
 */
public class TcpTreeTableDataParser {
    public static String treeTableData2Xml(List<TcpTreeTableDataStruct> treeDataList) {
        String xmlString = "";
        try {
            if (treeDataList == null || treeDataList.isEmpty()) {
                return xmlString;
            }
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = DocumentHelper.createDocument();

            TcpTreeTableDataStruct dataStruct = null;
            if(treeDataList.size()>1){
                dataStruct = new TcpTreeTableDataStruct();
                dataStruct.setName("ROOT");
                dataStruct.setChildren(treeDataList);
            }else {
                dataStruct = treeDataList.get(0);
            }

            dataStruct.genXmlElementByDocument(document);
            xmlString = document.getRootElement().asXML();

            // 设置XML文档格式
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            // 设置XML编码方式,即是用指定的编码方式保存XML文档到字符串(String),这里也可以指定为GBK或是ISO8859-1
            outputFormat.setEncoding("UTF-8");
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
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(xmlString)) {
            xmlString = "";
        } else {
            xmlString = xmlString.replaceAll("  ", "");
        }
        return xmlString;
    }

}
