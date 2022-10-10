package io.metersphere.commons.utils;


import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
        if (sourceObj == null) {
            sourceObj = new JSONObject();
        }
        if (matchObj == null) {
            matchObj = new JSONObject();
        }
        if (JSONUtil.isEmpty(matchObj)) {
            return true;
        } else if (sourceObj != null && matchObj != null) {
            boolean lastMatchResultIsTrue = false;
            boolean hasNotMatchResult = false;
            try {
                Set<String> matchKeys = matchObj.keySet();
                for (String key : matchKeys) {
                    if (sourceObj.has(key)) {
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
                LogUtil.error(e);
            }
            return lastMatchResultIsTrue && !hasNotMatchResult;
        } else {
            return false;
        }
    }

    public static boolean checkJsonArrayCompliance(JSONArray sourceArray, JSONArray matchArray) {
        if (sourceArray == null) {
            sourceArray = new JSONArray();
        }
        if (matchArray == null) {
            matchArray = new JSONArray();
        }
        if ((sourceArray == null || sourceArray.length() == 0) && (matchArray == null || matchArray.length() == 0)) {
            return true;
        } else if (sourceArray != null && matchArray != null && sourceArray.length() >= matchArray.length()) {
            try {
                for (int i = 0; i < matchArray.length(); i++) {
                    Object obj = matchArray.get(i);
                    if (!sourceArray.toList().contains(obj)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
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
                for (int i = 0; i < sourceArray.length(); i++) {
                    Object obj = sourceArray.get(i);
                    if (obj instanceof JSONObject) {
                        boolean isMatch = checkJsonObjCompliance((JSONObject) obj, matchObj);
                        if (isMatch) {
                            return isMatch;
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
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
                for (int sourceIndex = 0; sourceIndex < sourceArray.length(); sourceIndex++) {
                    JSONObject sourceObj = sourceArray.getJSONObject(sourceIndex);
                    for (String key : matchKeys) {
                        if (sourceObj.has(key)) {
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
                LogUtil.error(e);
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
                if (matchArr.length() > sourceArr.length()) {
                    return false;
                } else {
                    for (int i = 0; i < matchArr.length(); i++) {
                        boolean finalChack = false;
                        sourceForeach:
                        for (int j = i; j < sourceArr.length(); j++) {
                            Object matchItemObj = matchArr.get(i);
                            Object sourceItemObj = sourceArr.get(j);
                            boolean check = checkObjCompliance(sourceItemObj, matchItemObj);
                            if (check) {
                                finalChack = true;
                                break;
                            }
                        }
                        if (!finalChack) {
                            return false;
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
        for (int i = 0; i < jsonArray.length(); i++) {
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
                JSONArray sourceArr = JSONUtil.parseArray(sourceJson);
                JSONArray compArr = JSONUtil.parseArray(matchJson);
                isMatch = checkJsonArrayCompliance(sourceArr, compArr);
            } else if (isSourceJsonIsArray && !isMatchJsonIsArray) {
                JSONArray sourceArr = JSONUtil.parseArray(sourceJson);
                JSONObject compObj = JSONUtil.parseObject(matchJson);
                isMatch = checkJsonArrayContainsObj(sourceArr, compObj);
            } else if (!isSourceJsonIsArray && !isMatchJsonIsArray) {
                JSONObject sourceObj = JSONUtil.parseObject(sourceJson);
                JSONObject compObj = JSONUtil.parseObject(matchJson);
                isMatch = checkJsonObjCompliance(sourceObj, compObj);
            } else {
                isMatch = false;
            }

        } catch (Exception e) {
            LogUtil.error(e);
        }
        return isMatch;
    }
}
