package io.metersphere.log.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.log.utils.diff.json.GsonDiff;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ReflexObjectUtil {
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static final String DIFF_ADD = "++";

    static final String JSON_START = "{\"root\":";
    static final String JSON_END = "}";
    private static Class<? extends ApiDefinitionDiffUtil> apiDefinitionDiffUtil = null;

    static {
        Set<Class<? extends ApiDefinitionDiffUtil>> subTypes = new Reflections("io.metersphere.commons.utils").getSubTypesOf(ApiDefinitionDiffUtil.class);
        if (CollectionUtils.isNotEmpty(subTypes)) {
            apiDefinitionDiffUtil = subTypes.stream().findFirst().get();
        }
    }

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
                        DetailColumn column = new DetailColumn(columns.get(f.getName()), f.getName(), val, StringUtils.EMPTY);
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
                    DetailColumn column = new DetailColumn(f.getName(), f.getName(), val, StringUtils.EMPTY);
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
            List array = JSON.parseArray(content);
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
        // todo JSON 处理
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
                                Object originalObject = JSON.toJSONString(originalValueArray);
                                oldTags = StringUtils.join(JSON_START, ((originalColumns.get(i) != null && originalObject != null) ? originalObject.toString() : "\"\""), JSON_END);
                            }
                            String newTags = null;
                            if (newValue != null && !StringUtils.equals("null", newValue.toString())) {
                                List<String> newValueArray = JSON.parseArray(newValue.toString(), String.class);
                                if (CollectionUtils.isNotEmpty(newValueArray)) {
                                    Collections.sort(newValueArray);
                                }
                                Object newObject = JSON.toJSONString(newValueArray);
                                newTags = StringUtils.join(JSON_START, ((newColumns.get(i) != null && newObject != null) ? newObject.toString() : "\"\""), JSON_END);
                            }

                            if (StringUtils.isBlank(oldTags) && StringUtils.isBlank(newTags)) {
                                continue;
                            }
                            if (StringUtils.isNotBlank(oldTags) && StringUtils.isNotBlank(newTags)) {
                                String diffStr = diff.diff(oldTags, newTags);
                                String diffValue = diff.apply(newTags, diffStr);
                                column.setDiffValue(diffValue);
                            } else if (StringUtils.isNotBlank(newTags)) {
                                String diffValue = reviverJson(newTags, "root", DIFF_ADD);
                                column.setDiffValue(diffValue);
                            }

                        }
                        // 深度对比
                        else if (StringUtils.equals(module, "API_DEFINITION")) {
                            ApiDefinitionDiffUtil apiDefinitionDiffUtilClass = ConstructorUtils.invokeConstructor(apiDefinitionDiffUtil, null);
                            if (originalColumns.get(i).getColumnName().equals("request")) {
                                String newValue = Objects.toString(column.getNewValue().toString(), "");
                                String oldValue = Objects.toString(column.getOriginalValue(), "");
                                String diff = apiDefinitionDiffUtilClass.diff(newValue, oldValue);
                                if (StringUtils.isBlank(diff)) {
                                    continue;
                                }
                                column.setDiffValue(diff);
                            } else if (originalColumns.get(i).getColumnName().equals("response")) {
                                String newValue = Objects.toString(column.getNewValue().toString(), "");
                                String oldValue = Objects.toString(column.getOriginalValue(), "");
                                String diff = apiDefinitionDiffUtilClass.diffResponse(newValue, oldValue);
                                if (StringUtils.isBlank(diff)) {
                                    continue;
                                }
                                column.setDiffValue(diff);
                            }
                        }
                        // 环境全局前后置脚本深度对比
                        else if (StringUtils.equals(module, "PROJECT_ENVIRONMENT_SETTING")) {
                            if (originalColumns.get(i).getColumnName().equals("config")) {
                                String newValue = Objects.toString(column.getNewValue().toString(), "");
                                String oldValue = Objects.toString(column.getOriginalValue(), "");
                                column.setDiffValue(ApiTestEnvironmentDiffUtil.diff(newValue, oldValue));
                            }
                        }
                        // 不记录服务集成配置信息中密码字段
                        else if (StringUtils.equals(module, OperLogModule.WORKSPACE_SERVICE_INTEGRATION)) {
                            if (StringUtils.equals(column.getColumnName(), "configuration")) {
                                if (column.getNewValue() != null && column.getOriginalValue() == null) {
                                    HashMap<String, Object> newConf = JSON.parseObject((String) column.getNewValue(), new TypeReference<>() {});
                                    newConf.put("password", "******");
                                    column.setNewValue(JSON.toJSONString(newConf));
                                } else if (column.getOriginalValue() != null && column.getNewValue() != null) {
                                    HashMap<String, Object> newConf = JSON.parseObject((String) column.getNewValue(), new TypeReference<>() {});
                                    HashMap<String, Object> oldConf = JSON.parseObject((String) column.getOriginalValue(), new TypeReference<>() {});
                                    if (StringUtils.equals((String)newConf.get("password"), (String)oldConf.get("password"))) {
                                        // 密码未修改，保持一致
                                        oldConf.put("password", "******");
                                        newConf.put("password", "******");
                                    } else {
                                        oldConf.put("password", "******");
                                        newConf.put("password", "*********");
                                    }
                                    column.setOriginalValue(JSON.toJSONString(oldConf));
                                    column.setNewValue(JSON.toJSONString(newConf));
                                }
                            }
                        } else {
                            String newValue = Objects.toString(column.getNewValue(), StringUtils.EMPTY);
                            if (StringUtils.isNotBlank(newValue)) {
                                column.setNewValue(newValue.replaceAll("\\n", StringUtils.SPACE));
                            }
                            String oldValue = Objects.toString(column.getOriginalValue(), StringUtils.EMPTY);
                            if (StringUtils.isNotBlank(oldValue)) {
                                column.setOriginalValue(oldValue.replaceAll("\\n", StringUtils.SPACE));
                            }
                            if (StringUtils.isBlank(newValue) && StringUtils.isBlank(oldValue)) {
                                continue;
                            }
                        }
                        if (StringUtils.isBlank(JSON.toJSONString(column.getNewValue())) && StringUtils.isBlank(JSON.toJSONString(column.getOriginalValue()))) {
                            continue;
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
            String s = arr.optString(i);
            if (option.equals(DIFF_ADD)) {
                s = DIFF_ADD + s;
            }
            arr.put(i, s);
        }
        return obj.toString();
    }


}


