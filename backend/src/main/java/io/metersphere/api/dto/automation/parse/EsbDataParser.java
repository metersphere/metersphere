package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.api.dto.automation.EsbDataStruct;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/15 5:00 下午
 * @Description
 */
public class EsbDataParser {
    public static String esbData2XmlByParamStruct(List<EsbDataStruct> esbDataList, String[] paramArr) {
        String xmlString = "";
        try {
            if (esbDataList == null || esbDataList.isEmpty()) {
                return xmlString;
            }
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = DocumentHelper.createDocument();
            EsbDataStruct dataStruct = selectEsbDataStructByNameStruct(esbDataList, paramArr, 0);
            if (dataStruct != null) {
                dataStruct.genXmlElementByDocument(document);
                xmlString = document.getRootElement().asXML();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(xmlString)) {
            xmlString = "";
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
                }else if(index == 0){
                    //如果是第一个节点不符合，则遍历子节点是否有符合的。
                    int newIndex = index;
                    EsbDataStruct itemData = selectEsbDataStructByNameStruct(dataStuct.getChildren(), paramArr, newIndex);
                    if(itemData != null ){
                        returnData = itemData;
                    }
                }
            }
        }
        return returnData;
    }

    public static void main(String[] args) {
        String str = "[{\"systemName\":\"\",\"children\":[{\"systemName\":\"\",\"children\":[{\"systemName\":\"\",\"children\":[],\"name\":\"CardNo\",\"description\":\"\",\"type\":\"string\",\"contentType\":\"30\",\"uuid\":\"295f4\",\"value\":\"627713288321\",\"required\":true,\"status\":\"\"},{\"name\":\"AccoutNo\",\"systemName\":\"\",\"status\":\"\",\"type\":\"string\",\"contentType\":\"6\",\"required\":false,\"description\":\"\",\"uuid\":\"3e8ef\",\"children\":[],\"value\":\"371421321\"}],\"name\":\"HEAD\",\"description\":\"\",\"type\":\"[object]\",\"contentType\":\"\",\"uuid\":\"55483\",\"required\":false,\"status\":\"\"},{\"name\":\"Body\",\"systemName\":\"\",\"status\":\"\",\"type\":\"[object]\",\"contentType\":\"\",\"required\":false,\"description\":\"\",\"uuid\":\"a088b\",\"children\":[{\"name\":\"returnFlag\",\"systemName\":\"\",\"status\":\"\",\"type\":\"string\",\"contentType\":\"2\",\"required\":false,\"description\":\"\",\"uuid\":\"76d75\",\"children\":[],\"value\":\"1\"}]}],\"name\":\"SERVICE\",\"description\":\"\",\"type\":\"[object]\",\"contentType\":\"\",\"uuid\":\"faf95\",\"required\":false,\"status\":\"\"}]";
        List<EsbDataStruct> list = JSONArray.parseArray(str, EsbDataStruct.class);
        String[] paramArr = new String[]{"HEAD"};
        System.out.println(esbData2XmlByParamStruct(list, paramArr));

        paramArr = new String[]{"SERVICE"};
        System.out.println(esbData2XmlByParamStruct(list, paramArr));

        paramArr = new String[]{"Body"};
        System.out.println(esbData2XmlByParamStruct(list, paramArr));

        paramArr = new String[]{"SERVICE","Body"};
        System.out.println(esbData2XmlByParamStruct(list, paramArr));

    }

}
