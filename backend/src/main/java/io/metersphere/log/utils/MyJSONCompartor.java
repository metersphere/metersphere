package io.metersphere.log.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

import java.util.List;
import java.util.Map;

public class MyJSONCompartor {

    public static void compareJSON(String src, String target) {
        JSONObject so = JSON.parseObject(src);
        JSONObject to = JSON.parseObject(target);
        for (String key : so.keySet()) {
            Object value = so.get(key);
            Object v2 = to.get(key);
            if (value instanceof Map) {
                compareJSON(JSON.toJSONString(value), JSON.toJSONString(v2));
            } else if (value instanceof List) {
                List list = (List<Map<String, Object>>) value;
                List list2 = (List<Map<String, Object>>) v2;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof List || list.get(i) instanceof Map) {
                        compareJSON(JSON.toJSONString(list.get(i)), JSON.toJSONString(list2.get(i)));
                    } else {//如果是这种 LIst，值不是Map也不是List,就可以直接比较值 "phoneNumList": ["10086"，"10084"]
                        if (!list.get(i).equals(list2.get(i))) {
                            System.err.println("key " + key + "值不一样，期望" + list.get(i) + "，实际" + list2.get(i));
                        }
                    }
                }
            } else {
                if (null == value) {
                    System.err.println("key " + key + "值是null！！！！！！！！！！！");
                } else if (!value.equals(v2)) {
                    System.err.println("key " + key + "值不一样，期望" + value + "，实际" + v2);
                }
            }
        }
    }


    public static void main(String[] args) {
        final String json1 = "{\"type\":\"HTTPSamplerProxy\",\"id\":\"9cf1ef83-099b-4a57-a229-2250e9b12372\",\"name\":\"测试添加\",\"label\":\"HTTPSamplerProxy\",\"active\":false,\"enable\":true,\"hashTree\":[],\"customizeReq\":false,\"mockEnvironment\":false,\"protocol\":\"HTTP\",\"method\":\"GET\",\"path\":\"/test\",\"connectTimeout\":\"6000\",\"responseTimeout\":\"6000\",\"headers\":[{\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"Accept\",\"required\":true,\"valid\":true,\"value\":\"a\"},{\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"Accept-Charset\",\"required\":true,\"valid\":true,\"value\":\"b\"},{\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"valid\":false}],\"body\":{\"binary\":[],\"json\":false,\"kV\":true,\"kvs\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@character\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@datetime\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"oldKV\":false,\"type\":\"Form Data\",\"valid\":true,\"xml\":false},\"rest\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"cx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"c1\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"ca\",\"required\":false,\"type\":\"text\",\"valid\":true,\"value\":\"c2\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"followRedirects\":true,\"doMultipartPost\":false,\"arguments\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"test\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"1\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"pa\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"2\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}]}\n";
        final String json2 = "{\"type\":\"HTTPSamplerProxy\",\"id\":\"9cf1ef83-099b-4a57-a229-2250e9b12372\",\"name\":\"测试添加\",\"label\":\"HTTPSamplerProxy\",\"active\":false,\"enable\":true,\"hashTree\":[],\"customizeReq\":false,\"mockEnvironment\":false,\"protocol\":\"HTTP\",\"method\":\"GET\",\"path\":\"/test\",\"connectTimeout\":\"6000\",\"responseTimeout\":\"6000\",\"headers\":[{\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"Accept\",\"required\":true,\"valid\":true,\"value\":\"a1\"},{\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"Accept-Charset\",\"required\":true,\"valid\":true,\"value\":\"b2\"},{\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"valid\":false}],\"body\":{\"binary\":[],\"json\":false,\"kV\":true,\"kvs\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@character\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@datetime\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"oldKV\":false,\"type\":\"Form Data\",\"valid\":true,\"xml\":false},\"rest\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"cx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"c13\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"ca\",\"required\":false,\"type\":\"text\",\"valid\":true,\"value\":\"c22\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"followRedirects\":true,\"doMultipartPost\":false,\"arguments\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"test\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"11\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"pa\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"22\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}]}\n";

        String json4 ="{\"type\":\"HTTPSamplerProxy\",\"id\":\"9cf1ef83-099b-4a57-a229-2250e9b12372\",\"name\":\"测试添加\",\"label\":\"HTTPSamplerProxy\",\"active\":false,\"enable\":true,\"hashTree\":[],\"customizeReq\":false,\"mockEnvironment\":false,\"protocol\":\"HTTP\",\"method\":\"GET\",\"path\":\"/test\",\"connectTimeout\":\"6000\",\"responseTimeout\":\"6000\",\"headers\":[{\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"Accept\",\"required\":true,\"valid\":true,\"value\":\"a1\"},{\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"Accept-Charset\",\"required\":true,\"valid\":true,\"value\":\"b2\"},{\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"valid\":false}],\"body\":{\"binary\":[],\"json\":false,\"kV\":true,\"kvs\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@character\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@datetime\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"oldKV\":false,\"type\":\"Form Data\",\"valid\":true,\"xml\":false},\"rest\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"cx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"c13\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"ca\",\"required\":false,\"type\":\"text\",\"valid\":true,\"value\":\"c22\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"followRedirects\":true,\"doMultipartPost\":false,\"arguments\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"test\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"11\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"pa\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"22\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}]}\n";
        String json5 ="{\"type\":\"HTTPSamplerProxy\",\"id\":\"9cf1ef83-099b-4a57-a229-2250e9b12372\",\"name\":\"测试添加\",\"label\":\"HTTPSamplerProxy\",\"active\":false,\"enable\":true,\"hashTree\":[],\"customizeReq\":false,\"mockEnvironment\":false,\"protocol\":\"HTTP\",\"method\":\"GET\",\"path\":\"/test\",\"connectTimeout\":\"6000\",\"responseTimeout\":\"6000\",\"headers\":[{\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"Accept\",\"required\":true,\"valid\":true,\"value\":\"a1\"},{\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"valid\":false}],\"body\":{\"binary\":[],\"json\":false,\"kV\":true,\"kvs\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@character\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"axx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"@datetime\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"oldKV\":false,\"type\":\"Form Data\",\"valid\":true,\"xml\":false},\"rest\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"cx\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"c13\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"ca\",\"required\":false,\"type\":\"text\",\"valid\":true,\"value\":\"c22\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}],\"followRedirects\":true,\"doMultipartPost\":false,\"arguments\":[{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"test\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"11\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"pa\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"22\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"name\":\"cc\",\"required\":true,\"type\":\"text\",\"valid\":true,\"value\":\"cc\"},{\"contentType\":\"text/plain\",\"enable\":true,\"encode\":true,\"file\":false,\"required\":true,\"type\":\"text\",\"valid\":false}]}\n";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode source = mapper.readTree(json1);
            JsonNode target = mapper.readTree(json2);


            JsonNode patch2 = JsonDiff.asJson( target,  source);
            JsonNode patch = JsonDiff.asJson( source,  target);

            System.out.println( patch.toString());
            System.out.println( patch2.toString());

        } catch (Exception e) {

        }


    }

}
