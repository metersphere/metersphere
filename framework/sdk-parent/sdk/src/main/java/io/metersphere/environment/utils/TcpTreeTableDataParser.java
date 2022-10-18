package io.metersphere.environment.utils;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.environment.dto.MockConfigRequestParams;
import io.metersphere.environment.dto.MockParamConditionEnum;
import io.metersphere.environment.dto.TcpTreeTableDataStruct;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author song.tianyang
 * @Date 2021/3/15 5:00 下午
 * @Description
 */
public class TcpTreeTableDataParser {

    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_OBJECT = "object";

    public static String treeTableData2Xml(List<TcpTreeTableDataStruct> treeDataList) {
        String xmlString = "";
        try {
            if (treeDataList == null || treeDataList.isEmpty()) {
                return xmlString;
            }
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            XMLUtils.setExpandEntityReferencesFalse(factory);
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = DocumentHelper.createDocument();

            TcpTreeTableDataStruct dataStruct = null;
            if (treeDataList.size() > 1) {
                dataStruct = new TcpTreeTableDataStruct();
                dataStruct.setName("ROOT");
                dataStruct.setChildren(treeDataList);
            } else {
                dataStruct = treeDataList.get(0);
            }

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

        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (StringUtils.isEmpty(xmlString)) {
            xmlString = "";
        } else {
            xmlString = xmlString.replaceAll("  ", "");
        }
        return xmlString;
    }

    public static List<TcpTreeTableDataStruct> xml2TreeTableData(String xmlString) {
        List<TcpTreeTableDataStruct> returnList = new ArrayList<>();
        Document document = XMLUtils.stringToDocument(xmlString);
        if (document != null) {
            Element rootElement = document.getRootElement();
            TcpTreeTableDataStruct struct = new TcpTreeTableDataStruct();
            struct.init();
            struct.setName(rootElement.getName());
            List<Element> elements = rootElement.elements();
            if (CollectionUtils.isEmpty(elements)) {
                struct.setValue(rootElement.getStringValue());
                struct.setType(TcpTreeTableDataParser.DATA_TYPE_STRING);
            } else {
                struct.setType(TcpTreeTableDataParser.DATA_TYPE_OBJECT);
                struct.setChildren(docElement2TreeTableData(elements));
            }
            returnList.add(struct);
        }
        if (CollectionUtils.isEmpty(returnList)) {
            MSException.throwException(Translator.get("error_xml_struct"));
        }
        return returnList;
    }

    private static List<TcpTreeTableDataStruct> docElement2TreeTableData(List<Element> elements) {
        List<TcpTreeTableDataStruct> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(elements)) {
            elements.forEach(element -> {
                TcpTreeTableDataStruct struct = new TcpTreeTableDataStruct();
                struct.init();
                struct.setName(element.getName());
                List<Element> children = element.elements();
                if (CollectionUtils.isEmpty(children)) {
                    struct.setValue(element.getStringValue());
                    struct.setType(TcpTreeTableDataParser.DATA_TYPE_STRING);
                } else {
                    struct.setType(TcpTreeTableDataParser.DATA_TYPE_OBJECT);
                    struct.setChildren(docElement2TreeTableData(children));
                }
                returnList.add(struct);
            });
        }
        return returnList;
    }

    public static boolean isMatchTreeTableData(JSONObject sourceObj, List<TcpTreeTableDataStruct> tcpDataList) {
        if (CollectionUtils.isEmpty(tcpDataList)) {
            return true;
        }
        if (sourceObj == null) {
            sourceObj = new JSONObject();
        }

        boolean isMatch = false;
        for (TcpTreeTableDataStruct dataStruct : tcpDataList) {
            if (isMatch) {
                break;
            }
            String key = dataStruct.getName();
            if (sourceObj.has(key)) {
                Object sourceObjItem = sourceObj.get(key);
                if (sourceObjItem instanceof JSONObject) {
                    if (!CollectionUtils.isEmpty(dataStruct.getChildren())) {
                        if (!isMatchTreeTableData((JSONObject) sourceObjItem, dataStruct.getChildren())) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else if (sourceObjItem instanceof JSONArray) {
                    if (!CollectionUtils.isEmpty(dataStruct.getChildren())) {
                        JSONArray jsonArray = (JSONArray) sourceObjItem;
                        boolean hasMatchAny = false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Object itemObj = jsonArray.get(i);
                            if (itemObj instanceof JSONObject) {
                                if (!isMatchTreeTableData((JSONObject) itemObj, dataStruct.getChildren())) {
                                    continue;
                                } else {
                                    hasMatchAny = true;
                                }
                            } else {
                                continue;
                            }
                        }
                        if (!hasMatchAny) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else {
                    String sourceValues = String.valueOf(sourceObjItem);
                    MockConfigRequestParams mockParams = new MockConfigRequestParams();
                    mockParams.setKey(dataStruct.getName());
                    mockParams.setValue(dataStruct.getValue());
                    mockParams.setCondition(dataStruct.getCondition());
                    if (!isValueMatch(sourceValues, mockParams)) {
                        continue;
                    }
                }
                isMatch = true;
            }
        }

        return isMatch;
    }

    public static boolean isValueMatch(String requestParam, MockConfigRequestParams params) {
        if (StringUtils.isBlank(params.getCondition())) {
            params.setCondition(MockParamConditionEnum.VALUE_EQUALS.name());
        }
        if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.VALUE_EQUALS.name())) {
            return StringUtils.equals(requestParam, params.getValue());
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.VALUE_NOT_EQUALS.name())) {
            return !StringUtils.equals(requestParam, params.getValue());
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.VALUE_CONTAINS.name())) {
            return StringUtils.contains(requestParam, params.getValue());
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.LENGTH_EQUALS.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() == length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.LENGTH_NOT_EQUALS.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() != length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.LENGTH_SHOT_THAN.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() < length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.LENGTH_LARGE_THAN.name())) {
            try {
                int length = Integer.parseInt(params.getValue());
                return requestParam.length() > length;
            } catch (Exception e) {
                return false;
            }
        } else if (StringUtils.equals(params.getCondition(), MockParamConditionEnum.REGULAR_MATCH.name())) {
            try {
                Pattern pattern = Pattern.compile(params.getValue());
                Matcher matcher = pattern.matcher(requestParam);
                boolean isMatch = matcher.matches();
                return isMatch;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
