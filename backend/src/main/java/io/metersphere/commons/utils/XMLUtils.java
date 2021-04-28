package io.metersphere.commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.*;

public class XMLUtils {

    private static void jsonToXmlStr(JSONObject jObj, StringBuffer buffer, StringBuffer tab) {
        Set<Map.Entry<String, Object>> se = jObj.entrySet();
        StringBuffer nowTab = new StringBuffer(tab.toString());
        for (Map.Entry<String, Object> en : se) {
            if ("com.alibaba.fastjson.JSONObject".equals(en.getValue().getClass().getName())) {
                buffer.append(tab).append("<").append(en.getKey()).append(">\n");
                JSONObject jo = jObj.getJSONObject(en.getKey());
                jsonToXmlStr(jo, buffer, nowTab.append("\t"));
                buffer.append(tab).append("</").append(en.getKey()).append(">\n");
            } else if ("com.alibaba.fastjson.JSONArray".equals(en.getValue().getClass().getName())) {
                JSONArray jarray = jObj.getJSONArray(en.getKey());
                for (int i = 0; i < jarray.size(); i++) {
                    buffer.append(tab).append("<").append(en.getKey()).append(">\n");
                    if (StringUtils.isNotBlank(jarray.getString(i))) {
                        JSONObject jsonobject = jarray.getJSONObject(i);
                        jsonToXmlStr(jsonobject, buffer, nowTab.append("\t"));
                        buffer.append(tab).append("</").append(en.getKey()).append(">\n");
                    }
                }
            } else if ("java.lang.String".equals(en.getValue().getClass().getName())) {
                buffer.append(tab).append("<").append(en.getKey()).append(">").append(en.getValue());
                buffer.append("</").append(en.getKey()).append(">\n");
            }
        }
    }

    public static String jsonToXmlStr(JSONObject jObj) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        try {
            jsonToXmlStr(jObj, buffer, new StringBuffer(""));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return buffer.toString();
    }

    //  传入完整的 xml 文本，转换成 json 对象
    public static JSONObject XmlToJson(String xml) {
        JSONObject result = new JSONObject();
        if(xml == null)
            return null;
        List<String> list = preProcessXml(xml);
        try {
            result = (JSONObject) XmlTagToJsonObject(list);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
//            MSException.throwException(Translator.get("illegal_xml_format"));
        }
        return result;
    }

    //  预处理 xml 文本，转换成 tag + data 的列表
    private static List<String> preProcessXml(String xml) {
        int begin = xml.indexOf("?>");
        if(begin != -1) {
            if(begin + 2 >= xml.length()) {
                return null;
            }
            xml = xml.substring(begin + 2);
        }   //  <?xml version="1.0" encoding="utf-8"?> 若存在，则去除
        String rgex = "\\s*";
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(xml);
        xml = m.replaceAll("");
        rgex = ">";
        pattern = Pattern.compile(rgex);
        m = pattern.matcher(xml);
        xml = m.replaceAll("> ");
        rgex = "\\s*</";
        pattern = Pattern.compile(rgex);
        m = pattern.matcher(xml);
        xml = m.replaceAll(" </");
        return Arrays.asList(xml.split(" "));
    }

    //  传入预处理的列表，返回转换成功的 json 对象
    private static Object XmlTagToJsonObject(List<String> list) {
        if(list == null || list.size() == 0)
            return null;
        Stack<String> tagStack = new Stack<>(); //  tag 栈
        Stack<Object> valueStack = new Stack<>();   //  数据栈
        valueStack.push(new JSONObject());  //  最终结果将存放在第一个入栈的元素中
        for(String item : list) {
            String beginTag = isBeginTag(item), endTag = isEndTag(item);    //  判断当前 tag 是开始还是结尾
            if(beginTag != null) {
                tagStack.push(beginTag);
                valueStack.push(new JSONObject());
            } else if(endTag != null) {
                if(endTag.equals(tagStack.peek())) { //  是一对 tag
                    Object topValue = valueStack.peek();
                    if(topValue instanceof String) {    //  栈顶是纯数据 xml 节点
                        valueStack.pop();
                    }
                    valueStack.pop();
                    if(valueStack.peek() instanceof JSONObject) {
                        ((JSONObject) valueStack.peek()).put(tagStack.peek(), topValue);
                    }
                    tagStack.pop();
                }
            } else {
                valueStack.push(item);
            }
        }
        if(valueStack.empty())
            return null;
        return valueStack.peek();
    }

    private static String isEndTag(String tagLine) {
        String rgex = "</(\\w*)>";
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式    
        Matcher m = pattern.matcher(tagLine);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static String isBeginTag(String tagLine) {
        String rgex = "<(\\w*)>";
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式    
        Matcher m = pattern.matcher(tagLine);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

}
