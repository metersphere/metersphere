package io.metersphere.log.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ReflexObjectUtil {

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
        // 得到类对象
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
        for (Field[] fs : fields) {
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                f.setAccessible(true);
                try {
                    if (columns.containsKey(f.getName())) {
                        Object val = f.get(obj);
                        DetailColumn column = new DetailColumn(columns.get(f.getName()), f.getName(), val, "");
                        if (dffColumns.contains(f.getName())) {
                            column.setDepthDff(true);
                        }
                        columnList.add(column);
                    }
                } catch (Exception e) {
                    LogUtil.error(e.getMessage());
                }
            }
        }


        List<String> keys = columns.keySet().stream().collect(Collectors.toList());
        ReflexObjectUtil.order(keys, columnList);
        return columnList;
    }

    public static void order(List<String> orderRegulation, List<DetailColumn> targetList) {
        Collections.sort(targetList, ((o1, o2) -> {
            int io1 = orderRegulation.indexOf(o1.getColumnName());
            int io2 = orderRegulation.indexOf(o2.getColumnName());
            return io1 - io2;
        }));
    }

    public static List<DetailColumn> compared(OperatingLogDetails obj, OperatingLogDetails newObj) {
        List<DetailColumn> comparedColumns = new LinkedList<>();
        try {
            if (obj != null && newObj != null) {
                List<DetailColumn> originalColumns = obj.getColumns();
                List<DetailColumn> newColumns = newObj.getColumns();
                for (int i = 0; i < originalColumns.size(); i++) {
                    if (!StringUtils.equals(JSON.toJSONString(originalColumns.get(i).getOriginalValue()), JSON.toJSONString(newColumns.get(i).getOriginalValue()))) {
                        if (originalColumns.get(i).isDepthDff()) {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode source = mapper.readTree(JSON.toJSONString(originalColumns.get(i).getOriginalValue()));
                            JsonNode target = mapper.readTree(JSON.toJSONString(newColumns.get(i).getOriginalValue()));
                            JsonNode before = JsonDiff.asJson(target, source);
                            JsonNode after = JsonDiff.asJson(source, target);
                            DetailColumn column = new DetailColumn();
                            BeanUtils.copyBean(column, originalColumns.get(i));
                            column.setOriginalValue(before.toString());
                            column.setNewValue(after.toString());
                            comparedColumns.add(column);
                        } else {
                            DetailColumn column = new DetailColumn();
                            BeanUtils.copyBean(column, originalColumns.get(i));
                            column.setNewValue(newColumns.get(i).getOriginalValue());
                            comparedColumns.add(column);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return comparedColumns;
    }
}
