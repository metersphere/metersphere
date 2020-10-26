package io.metersphere.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

public class JsonPathUtils {

    public static List<HashMap> getListJson(String jsonString) {

        JSONObject jsonObject =JSONObject.parseObject(jsonString);
        List<HashMap> allJsons =new ArrayList<>();

        // 获取到所有jsonpath后，获取所有的key
        List<String> jsonPaths = JSONPath.paths(jsonObject).keySet()
            .stream()
            .collect(Collectors.toList());
        //去掉根节点key
        List<String> parentNode = new ArrayList<>();
        //根节点key
        parentNode.add("/");
        //循环获取父节点key，只保留叶子节点
        for (int i = 0; i < jsonPaths.size(); i++) {
            if (jsonPaths.get(i).lastIndexOf("/") > 0) {
                parentNode.add(jsonPaths.get(i).substring(0, jsonPaths.get(i).lastIndexOf("/")));
            }
        }

        //remove父节点key
        for (String parentNodeJsonPath : parentNode) {
            jsonPaths.remove(parentNodeJsonPath);
        }

        List<String> jsonPathList = new ArrayList<>();
        Iterator<String> jsonPath = jsonPaths.iterator();
        //将/替换为点.
        while (jsonPath.hasNext()) {
            Map<String,String> item = new HashMap<>();


            String o_json_path = "$" + jsonPath.next().replaceAll("/", ".");
            String value = JSONPath.eval(jsonObject,o_json_path).toString();

            if(o_json_path.toLowerCase().contains("id")) {
                continue;
            }


            if(value.equals("") || value.equals("[]") || o_json_path.equals("")) {
                continue;
            }

            String json_path = formatJson(o_json_path);



            //System.out.println(json_path);



            item.put("json_path", json_path);
            item.put("json_value", addEscapeForString(value));
            allJsons.add((HashMap)item);

            jsonPathList.add(json_path);
        }
        //排序
        Collections.sort(jsonPathList);
        return allJsons;
    }

    private static String  formatJson(String json_path){

        String ret="";
        // 正则表达式
        String reg =  ".(\\d{1,3}).{0,1}";

        Boolean change_flag = false;
        Matcher m1 = Pattern.compile(reg).matcher(json_path);


        String newStr="";
        int rest = 0;
        String tail = "";
        while (m1.find()) {

            int start = m1.start();
            int end = m1.end() - 1;
            if(json_path.charAt(start) != '.' || json_path.charAt(end) != '.') {
                continue;
            }


            newStr += json_path.substring(rest,m1.start()) +"[*]." ;

            rest = m1.end();
            tail = json_path.substring(m1.end());
            change_flag = true;
        }


        if(change_flag) {
            ret = newStr + tail;
        } else {
            ret = json_path;
        }



        return ret;


    }

    private static String addEscapeForString(String input) {

        String ret="";


        String reg =  "[?*/]";

        Boolean change_flag = false;
        Matcher m1 = Pattern.compile(reg).matcher(input);


        String newStr="";
        int rest = 0;
        String tail = "";
        while (m1.find()) {

            int start = m1.start();
            int end = m1.end() - 1;



            newStr += input.substring(rest,m1.start()) + "\\" + m1.group(0) ;

            rest = m1.end();
            tail = input.substring(m1.end());
            change_flag = true;

        }
        if(change_flag) {
            ret = newStr + tail;
        } else {
            ret = input;
        }

        return ret;


    }




}
