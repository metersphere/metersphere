package io.metersphere.commons.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.metersphere.commons.json.BasicConstant;

import java.util.LinkedList;
import java.util.List;

public class EnumPropertyUtil {
    public static List<Object> analyzeEnumProperty(JsonObject object) {
        List<Object> list = new LinkedList<>();
        String jsonStr = null;
        try {
            JsonArray enumValues = object.get(BasicConstant.ENUM).getAsJsonArray();
            for (JsonElement enumValueElem : enumValues) {
                String enumValue = enumValueElem.getAsString();
                list.add(enumValue);
            }
        } catch (Exception e) {
            jsonStr = object.get(BasicConstant.ENUM).getAsString();
        }
        if (jsonStr != null && list.isEmpty()) {
            String[] arrays = jsonStr.split("\n");
            for (String str : arrays) {
                list.add(str);
            }
        }
        return list;
    }

}
