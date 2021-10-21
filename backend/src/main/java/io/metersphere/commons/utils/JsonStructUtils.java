package io.metersphere.commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * JSON数据结构相关的工具类
 *
 * @author song.tianyang
 * @Date 2021/8/16 3:50 下午
 */
public class JsonStructUtils {

    /**
     * 检查一个JSON对象的数据是否被另一个对象匹配（包含）
     *
     * @param sourceObj 目标JSON
     * @param matchObj  要进行匹配的JSON
     * @return
     */
    public static boolean checkJsonObjCompliance(JSONObject sourceObj, JSONObject matchObj) {
        if(sourceObj == null){
            sourceObj = new JSONObject();
        }
        if(matchObj == null){
            matchObj = new JSONObject();
        }
        if (sourceObj .isEmpty() && matchObj.isEmpty()) {
            return true;
        } else if (sourceObj != null && matchObj != null) {
            boolean lastMatchResultIsTrue = false;
            boolean hasNotMatchResult = false;
            try {
                Set<String> matchKeys = matchObj.keySet();
                for (String key : matchKeys) {
                    if (sourceObj.containsKey(key)) {
                        Object sourceObjItem = sourceObj.get(key);
                        Object matchObjItem = matchObj.get(key);
                        lastMatchResultIsTrue = checkObjCompliance(sourceObjItem, matchObjItem);
                        if (!lastMatchResultIsTrue) {
                            hasNotMatchResult = true;
                        }
                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return lastMatchResultIsTrue && !hasNotMatchResult;
        } else {
            return false;
        }
    }

    public static boolean checkJsonArrayCompliance(JSONArray sourceArray, JSONArray matchArray) {
        if (sourceArray == null && matchArray == null) {
            return true;
        } else if (sourceArray != null && matchArray != null && sourceArray.size() >= matchArray.size()) {
            try {
                for (int i = 0; i < matchArray.size(); i++) {
                    Object obj = matchArray.get(i);
                    if (!sourceArray.contains(obj)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkJsonArrayContainsObj(JSONArray sourceArray, JSONObject matchObj) {
        if (sourceArray == null && matchObj == null) {
            return true;
        } else if (sourceArray != null && matchObj != null) {
            try {
                for (int i = 0; i < sourceArray.size(); i++) {
                    Object obj = sourceArray.get(i);
                    if (obj instanceof JSONObject) {
                        boolean isMatch = checkJsonObjCompliance((JSONObject) obj, matchObj);
                        if (isMatch) {
                            return isMatch;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * 检查一个JSON对象的数据集合是否包含另一个对象（包含）
     *
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
                for (int sourceIndex = 0; sourceIndex < sourceArray.size(); sourceIndex++) {
                    JSONObject sourceObj = sourceArray.getJSONObject(sourceIndex);
                    for (String key : matchKeys) {
                        if (sourceObj.containsKey(key)) {
                            Object sourceObjItem = sourceObj.get(key);
                            Object matchObjItem = matchObj.get(key);
                            isMatch = checkObjCompliance(sourceObjItem, matchObjItem);
                            if (!isMatch) {
                                break;
                            }
                        } else {
                            isMatch = false;
                            break;
                        }
                    }

                    if (isMatch) {
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
                            boolean check = checkObjCompliance(sourceItemObj, matchItemObj);
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

    public static void deepParseKeyByJsonObject(JSONObject jsonObject, List<String> keyList) {
        for (String key : jsonObject.keySet()) {
            Object obj = jsonObject.get(key);
            if (obj instanceof JSONArray) {
                deepParseKeyByJsonArray((JSONArray) obj, keyList);
            } else if (obj instanceof JSONObject) {
                deepParseKeyByJsonObject((JSONObject) obj, keyList);
            } else {
                if (!keyList.contains(key)) {
                    keyList.add(key);
                }
            }
        }
    }

    public static void deepParseKeyByJsonArray(JSONArray jsonArray, List<String> keyList) {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object itemObj = jsonArray.get(i);
            if (itemObj instanceof JSONObject) {
                deepParseKeyByJsonObject((JSONObject) itemObj, keyList);
            }
        }
    }

    public static boolean checkJsonCompliance(String sourceJson, String matchJson) {
        boolean isMatch = false;
        try {
            boolean isSourceJsonIsArray = false;
            boolean isMatchJsonIsArray = false;

            JSONValidator sourceValidator = JSONValidator.from(sourceJson);
            JSONValidator matchValidator = JSONValidator.from(matchJson);
            String sourceType = sourceValidator.getType().name();
            String matchType = matchValidator.getType().name();
            if (StringUtils.equalsIgnoreCase(sourceType, "array") && StringUtils.equalsIgnoreCase(matchType, "array")) {
                isSourceJsonIsArray = true;
                isMatchJsonIsArray = true;
            } else if (StringUtils.equalsIgnoreCase(sourceType, "array")) {
                isSourceJsonIsArray = true;
            } else if (StringUtils.equalsIgnoreCase(matchType, "array")) {
                isMatchJsonIsArray = true;
            }
            if (isSourceJsonIsArray && isMatchJsonIsArray) {
                JSONArray sourceArr = JSONArray.parseArray(sourceJson);
                JSONArray compArr = JSONArray.parseArray(matchJson);
                isMatch = checkJsonArrayCompliance(sourceArr, compArr);
            } else if (isSourceJsonIsArray && !isMatchJsonIsArray) {
                JSONArray sourceArr = JSONArray.parseArray(sourceJson);
                JSONObject compObj = JSONObject.parseObject(matchJson);
                isMatch = checkJsonArrayContainsObj(sourceArr, compObj);
            } else if (!isSourceJsonIsArray && !isMatchJsonIsArray) {
                JSONObject sourceObj = JSONObject.parseObject(sourceJson);
                JSONObject compObj = JSONObject.parseObject(matchJson);
                isMatch = checkJsonObjCompliance(sourceObj, compObj);
            } else {
                isMatch = false;
            }

        } catch (Exception e) {

        }
        return isMatch;
    }
}
