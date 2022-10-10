package io.metersphere.utils;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class JSONObject extends JSON {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private final Map<String, Object> map;

    public JSONObject() {
        this(DEFAULT_INITIAL_CAPACITY, false);
    }

    public JSONObject(Map<String, Object> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null.");
        }
        this.map = map;
    }

    public JSONObject(boolean ordered) {
        this(DEFAULT_INITIAL_CAPACITY, ordered);
    }

    public JSONObject(int initialCapacity) {
        this(initialCapacity, false);
    }

    public JSONObject(int initialCapacity, boolean ordered) {
        if (ordered) {
            map = new LinkedHashMap<String, Object>(initialCapacity);
        } else {
            map = new HashMap<String, Object>(initialCapacity);
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        boolean result = map.containsKey(key);
        if (!result) {
            if (key instanceof Number
                    || key instanceof Character
                    || key instanceof Boolean
                    || key instanceof UUID
            ) {
                result = map.containsKey(key.toString());
            }
        }
        return result;
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Object get(Object key) {
        Object val = map.get(key);

        if (val == null) {
            if (key instanceof Number
                    || key instanceof Character
                    || key instanceof Boolean
                    || key instanceof UUID
            ) {
                val = map.get(key.toString());
            }
        }

        return val;
    }

    public Object getOrDefault(Object key, Object defaultValue) {
        Object v;
        return ((v = get(key)) != null) ? v : defaultValue;
    }

    public JSONObject getJSONObject(String key) {
        Object value = map.get(key);

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof Map) {
            return new JSONObject((Map) value);
        }

        if (value instanceof String) {
            return JSON.parseObject(String.valueOf(value), JSONObject.class);
        }
        return new JSONObject();
    }

    public JSONArray getJSONArray(String key) {
        Object value = map.get(key);

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        if (value instanceof List) {
            return new JSONArray((List) value);
        }

        if (value instanceof String) {
            return JSON.parseObject((String) value, JSONArray.class);
        }

        return JSON.parseObject((String) value, JSONArray.class);
    }


    public Boolean getBoolean(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        return TypeUtils.castToBoolean(value);
    }

    public byte[] getBytes(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        return TypeUtils.castToBytes(value);
    }

    public boolean getBooleanValue(String key) {
        Object value = get(key);

        Boolean booleanVal = TypeUtils.castToBoolean(value);
        if (booleanVal == null) {
            return false;
        }

        return booleanVal.booleanValue();
    }

    public Byte getByte(String key) {
        Object value = get(key);

        return TypeUtils.castToByte(value);
    }

    public byte getByteValue(String key) {
        Object value = get(key);

        Byte byteVal = TypeUtils.castToByte(value);
        if (byteVal == null) {
            return 0;
        }

        return byteVal.byteValue();
    }

    public Short getShort(String key) {
        Object value = get(key);

        return TypeUtils.castToShort(value);
    }

    public short getShortValue(String key) {
        Object value = get(key);

        Short shortVal = TypeUtils.castToShort(value);
        if (shortVal == null) {
            return 0;
        }

        return shortVal.shortValue();
    }

    public Integer getInteger(String key) {
        Object value = get(key);

        return TypeUtils.castToInt(value);
    }

    public int getIntValue(String key) {
        Object value = get(key);

        Integer intVal = TypeUtils.castToInt(value);
        if (intVal == null) {
            return 0;
        }

        return intVal.intValue();
    }

    public Long getLong(String key) {
        Object value = get(key);

        return TypeUtils.castToLong(value);
    }

    public long getLongValue(String key) {
        Object value = get(key);

        Long longVal = TypeUtils.castToLong(value);
        if (longVal == null) {
            return 0L;
        }

        return longVal.longValue();
    }

    public Float getFloat(String key) {
        Object value = get(key);

        return TypeUtils.castToFloat(value);
    }

    public float getFloatValue(String key) {
        Object value = get(key);

        Float floatValue = TypeUtils.castToFloat(value);
        if (floatValue == null) {
            return 0F;
        }

        return floatValue.floatValue();
    }

    public Double getDouble(String key) {
        Object value = get(key);

        return TypeUtils.castToDouble(value);
    }

    public double getDoubleValue(String key) {
        Object value = get(key);

        Double doubleValue = TypeUtils.castToDouble(value);
        if (doubleValue == null) {
            return 0D;
        }

        return doubleValue.doubleValue();
    }


    public String getString(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public JSONObject fluentPut(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    public JSONObject fluentPutAll(Map<? extends String, ?> m) {
        map.putAll(m);
        return this;
    }

    public void clear() {
        map.clear();
    }

    public JSONObject fluentClear() {
        map.clear();
        return this;
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public JSONObject fluentRemove(Object key) {
        map.remove(key);
        return this;
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<Object> values() {
        return map.values();
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public JSONObject clone() {
        return new JSONObject(map instanceof LinkedHashMap //
                ? new LinkedHashMap<String, Object>(map) //
                : new HashMap<String, Object>(map)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof JSONObject) {
            return this.map.equals(((JSONObject) obj).map);
        }

        return this.map.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }


    public Map<String, Object> getInnerMap() {
        return this.map;
    }

    public static JSONObject parseObject(String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                MSException.throwException("value is null");
            }
            Map<String, Object> map = JSON.parseObject(value, Map.class);
            return new JSONObject(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
