package io.metersphere.log.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.log.utils.diff.json.GsonDiff;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ReflexObjectUtil {
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static final String DIFF_ADD = "++";

    public static List<DetailColumn> getColumns(Object obj, Map<String, String> columns) {
        List<DetailColumn> columnList = new LinkedList<>();
        if (obj == null) {
            return columnList;
        }
        String dffValue = columns.get("ms-dff-col");
        List<String> dffColumns = new LinkedList<>();
        if (StringUtils.isNotEmpty(dffValue)) {
            dffColumns = Arrays.asList(dffValue.split(","));
        }
        // 得到类中的所有属性集合
        List<Field[]> fields = getFields(obj);
        try {
            for (Field[] fs : fields) {
                for (int i = 0; i < fs.length; i++) {
                    Field f = fs[i];
                    f.setAccessible(true);
                    if (columns.containsKey(f.getName())) {
                        Object val = f.get(obj);
                        if (StatusReference.statusMap.containsKey(String.valueOf(val))) {
                            val = StatusReference.statusMap.get(String.valueOf(val));
                        }
                        DetailColumn column = new DetailColumn(columns.get(f.getName()), f.getName(), val, "");
                        if (dffColumns.contains(f.getName())) {
                            column.setDepthDff(true);
                            column.setOriginalValue(formatJson(val));
                        }
                        columnList.add(column);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        List<String> keys = columns.keySet().stream().collect(Collectors.toList());
        ReflexObjectUtil.order(keys, columnList);
        return columnList;
    }

    static Object formatJson(Object val) {
        try {
            JsonObject jsonObject = gson.fromJson(String.valueOf(val), JsonObject.class);
            return gson.toJson(jsonObject);
        } catch (Exception e) {
            return val;
        }
    }

    static List<Field[]> getFields(Object obj) {
        Class clazz = obj.getClass();
        // 得到类中的所有属性集合
        List<Field[]> fields = new LinkedList<>();
        // 遍历所有父类字节码对象
        while (clazz != null) {
            // 获取字节码对象的属性对象数组
            Field[] declaredFields = clazz.getDeclaredFields();
            fields.add(declaredFields);
            // 获得父类的字节码对象
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static List<DetailColumn> getColumns(Object obj) {
        List<DetailColumn> columnList = new LinkedList<>();
        if (obj == null) {
            return columnList;
        }
        // 得到类中的所有属性集合
        List<Field[]> fields = getFields(obj);
        for (Field[] fs : fields) {
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                f.setAccessible(true);
                try {
                    Object val = f.get(obj);
                    DetailColumn column = new DetailColumn(f.getName(), f.getName(), val, "");
                    columnList.add(column);
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        }
        return columnList;
    }

    public static boolean isJsonArray(String content) {
        try {
            JSONArray array = JSON.parseArray(content);
            if (array != null && array.size() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void order(List<String> orderRegulation, List<DetailColumn> targetList) {
        Collections.sort(targetList, ((o1, o2) -> {
            int io1 = orderRegulation.indexOf(o1.getColumnName());
            int io2 = orderRegulation.indexOf(o2.getColumnName());
            return io1 - io2;
        }));
    }

    public static List<DetailColumn> compared(OperatingLogDetails obj, OperatingLogDetails newObj, String module) {
        List<DetailColumn> comparedColumns = new LinkedList<>();
        try {
            if (obj != null && newObj != null) {
                List<DetailColumn> originalColumns = obj.getColumns();
                List<DetailColumn> newColumns = newObj.getColumns();
                for (int i = 0; i < originalColumns.size(); i++) {
                    if (!StringUtils.equals(JSON.toJSONString(originalColumns.get(i).getOriginalValue()), JSON.toJSONString(newColumns.get(i).getOriginalValue()))) {
                        if (originalColumns.get(i).getColumnName().equals("tags")) {
                            if ((originalColumns.get(i).getOriginalValue() == null || StringUtils.isEmpty(originalColumns.get(i).getOriginalValue().toString()))
                                    && StringUtils.equals("[]", newColumns.get(i).getOriginalValue().toString())) {
                                continue;
                            } else if ((newColumns.get(i).getOriginalValue() == null || StringUtils.isEmpty(newColumns.get(i).getOriginalValue().toString()))
                                    && StringUtils.equals("[]", originalColumns.get(i).getOriginalValue().toString())) {
                                continue;
                            }
                        }
                        if (StringUtils.isEmpty(JSON.toJSONString(originalColumns.get(i).getOriginalValue())) && StringUtils.isEmpty(JSON.toJSONString(newColumns.get(i).getOriginalValue()))) {
                            continue;
                        }
                        DetailColumn column = new DetailColumn();
                        BeanUtils.copyBean(column, originalColumns.get(i));
                        column.setNewValue(newColumns.get(i).getOriginalValue());
                        if (StringUtils.isNotEmpty(originalColumns.get(i).getColumnName()) && originalColumns.get(i).getColumnName().equals("tags")) {
                            GsonDiff diff = new GsonDiff();
                            Object originalValue = originalColumns.get(i).getOriginalValue();
                            Object newValue = newColumns.get(i).getOriginalValue();
                            String oldTags = null;
                            if (originalValue != null && !StringUtils.equals("null", originalValue.toString())) {
                                List<String> originalValueArray = JSON.parseArray(originalValue.toString(), String.class);
                                Collections.sort(originalValueArray);
                                Object originalObject = JSON.toJSON(originalValueArray);
                                oldTags = ApiDefinitionDiffUtil.JSON_START + ((originalColumns.get(i) != null && originalObject != null) ? originalObject.toString() : "\"\"") + ApiDefinitionDiffUtil.JSON_END;
                            }
                            List<String> newValueArray = JSON.parseArray(newValue.toString(), String.class);
                            Collections.sort(newValueArray);
                            Object newObject = JSON.toJSON(newValueArray);
                            String newTags = ApiDefinitionDiffUtil.JSON_START + ((newColumns.get(i) != null && newObject != null) ? newObject.toString() : "\"\"") + ApiDefinitionDiffUtil.JSON_END;
                            String diffValue;
                            if (oldTags != null) {
                                String diffStr = diff.diff(oldTags, newTags);
                                diffValue = diff.apply(newTags, diffStr);
                            } else {
                                diffValue = reviverJson(newTags, "root", DIFF_ADD);
                            }
                            column.setDiffValue(diffValue);
                        }
                        // 深度对比
                        else if (StringUtils.equals(module, "API_DEFINITION")) {
                            if (originalColumns.get(i).getColumnName().equals("request")) {
                                String newValue = newColumns.get(i).getOriginalValue().toString();
                                String oldValue = column.getOriginalValue().toString();
                                column.setDiffValue(ApiDefinitionDiffUtil.diff(newValue, oldValue));
                            } else if (originalColumns.get(i).getColumnName().equals("response")) {
                                String newValue = newColumns.get(i).getOriginalValue().toString();
                                String oldValue = column.getOriginalValue().toString();
                                column.setDiffValue(ApiDefinitionDiffUtil.diffResponse(newValue, oldValue));
                            }
                        }
                        // 环境全局前后置脚本深度对比
                        else if(StringUtils.equals(module, "PROJECT_ENVIRONMENT_SETTING")){
                            if (originalColumns.get(i).getColumnName().equals("config")) {
                                String newValue = newColumns.get(i).getOriginalValue().toString();
                                String oldValue = column.getOriginalValue().toString();
                                column.setDiffValue(ApiTestEnvironmentDiffUtil.diff(newValue, oldValue));
                            }
                        }
                        else {
                            String newValue = column.getNewValue().toString();
                            if (StringUtils.isNotEmpty(newValue)) {
                                column.setNewValue(newValue.replaceAll("\\n", " "));
                            }
                            String oldValue = Objects.toString(column.getOriginalValue(), "");
                            if (StringUtils.isNotEmpty(oldValue)) {
                                column.setOriginalValue(oldValue.replaceAll("\\n", " "));
                            }
                        }
                        comparedColumns.add(column);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return comparedColumns;
    }

    private static String reviverJson(String str, String key, String option) {
        JSONObject obj = new JSONObject(str);
        org.json.JSONArray arr = obj.getJSONArray(key);
        for (int i = 0; i < arr.length(); i++) {
            String s = arr.getString(i);
            if (option.equals(DIFF_ADD)) {
                s = DIFF_ADD + s;
            }
            arr.put(i, s);
        }
        return obj.toString();
    }


}


