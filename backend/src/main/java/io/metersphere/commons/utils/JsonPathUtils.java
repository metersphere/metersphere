package io.metersphere.commons.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonPathUtils {

    public static List<HashMap> getListJson(String jsonString) {

        JSON jsonObject = jsonString.startsWith("[")?JSONObject.parseArray(jsonString):JSONObject.parseObject(jsonString);
        List<HashMap> allJsons =new ArrayList<>();

        // 获取到所有jsonpath后，获取所有的key
        List<String> jsonPaths = new ArrayList<>(JSONPath.paths(jsonObject).keySet());
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

        Iterator<String> jsonPath = jsonPaths.iterator();
        //将/替换为点.
        while (jsonPath.hasNext()) {
            Map<String,String> item = new HashMap<>();
            String o_json_path = "$" + jsonPath.next().replaceAll("/", ".");
            String value = JSONPath.eval(jsonObject, o_json_path).toString();

            if(value.equals("") || value.equals("[]") || o_json_path.equals("")) {
                continue;
            }

            String json_path = formatJson(o_json_path);

            item.put("json_path", json_path);
            item.put("json_value", addEscapeForString(value));
            allJsons.add((HashMap)item);

        }

        Collections.sort(allJsons, (a, b) ->
                ( (String)a.get("json_path") )
                        .compareTo( (String)b.get("json_path") )
        );

        // 正则特殊字符转义
        allJsons.forEach(item -> {
            item.put("regular_expression", escapeExprSpecialWord((String) item.get("json_value")));
        });

        return allJsons;
    }

    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    private static String  formatJson(String json_path){
        String ret="";
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
            newStr += json_path.substring(rest,m1.start()) +"[" + json_path.substring(start + 1, end) + "]." ;

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
            newStr += input.substring(rest,m1.start()) + "\\" + m1.group(0) ;
            rest = m1.end();
            tail = input.substring(m1.end());
            change_flag = true;
        }
        if (change_flag) {
            ret = newStr + tail;
        } else {
            ret = input;
        }
        return ret;
    }

    /**
     * 检查一个JSON对象的数据是否被另一个对象匹配（包含）
     *
     * @param sourceObj 目标JSON
     * @param matchObj  要进行匹配的JSON
     * @return
     */
    public static boolean checkJsonObjCompliance(JSONObject sourceObj, JSONObject matchObj) {
        if (sourceObj == null && matchObj == null) {
            return true;
        } else if (sourceObj != null && matchObj != null) {
            boolean isMatch = false;
            try {
                Set<String> matchKeys = matchObj.keySet();
                for (String key : matchKeys) {
                    if (sourceObj.containsKey(key)) {
                        Object sourceObjItem = sourceObj.get(key);
                        Object matchObjItem = matchObj.get(key);
                        isMatch = checkObjCompliance(sourceObjItem, matchObjItem);
                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isMatch;
        } else {
            return false;
        }
    }

    /**
     * 检查一个JSON对象的数据集合是否包含另一个对象（包含）
     * @param sourceArray
     * @param matchObj
     * @return
     */
    public static boolean checkJsonArrayCompliance(JSONArray sourceArray, JSONObject matchObj) {
        if (sourceArray == null && matchObj == null) {
            return true;
        } else if (sourceArray != null && matchObj != null) {
            boolean isMatch = false;
            try {
                Set<String> matchKeys = matchObj.keySet();
                for(int sourceIndex = 0;sourceIndex < sourceArray.size();sourceIndex ++){
                    JSONObject sourceObj = sourceArray.getJSONObject(sourceIndex);
                    for (String key : matchKeys) {
                        if (sourceObj.containsKey(key)) {
                            Object sourceObjItem = sourceObj.get(key);
                            Object matchObjItem = matchObj.get(key);
                            isMatch = checkObjCompliance(sourceObjItem, matchObjItem);
                            if(!isMatch){
                                break;
                            }
                        } else {
                            isMatch = false;
                            break;
                        }
                    }

                    if(isMatch){
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return isMatch;
        } else {
            return false;
        }
    }

    private static boolean checkObjCompliance(Object sourceObjItem, Object matchObjItem) {
        if (matchObjItem instanceof JSONObject) {
            if (sourceObjItem instanceof JSONObject) {
                return checkJsonObjCompliance((JSONObject) sourceObjItem, (JSONObject) matchObjItem);
            } else {
                return false;
            }
        } else if (matchObjItem instanceof JSONArray) {
            if (sourceObjItem instanceof JSONArray) {
                JSONArray sourceArr = (JSONArray) sourceObjItem;
                JSONArray matchArr = (JSONArray) matchObjItem;
                //同是arr 可能顺序存在不同。 所以需要循环匹配
                if (matchArr.size() > sourceArr.size()) {
                    return false;
                } else {
                    for (int i = 0; i < matchArr.size(); i++) {
                        for (int j = i; j < sourceArr.size(); j++) {
                            Object matchItemObj = matchArr.get(i);
                            Object sourceItemObj = sourceArr.get(j);
                            boolean check = checkObjCompliance(sourceObjItem, matchObjItem);
                            if (!check) {
                                return check;
                            }
                        }
                    }
                    return true;
                }
            } else {
                return false;
            }
        } else {
            String sourceValues = String.valueOf(sourceObjItem);
            String matchValues = String.valueOf(matchObjItem);
            return StringUtils.equals(sourceValues, matchValues);
        }
    }
}
