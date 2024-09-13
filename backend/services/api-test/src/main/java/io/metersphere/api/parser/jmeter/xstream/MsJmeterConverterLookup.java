package io.metersphere.api.parser.jmeter.xstream;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.ConverterRegistry;
import com.thoughtworks.xstream.core.Caching;
import com.thoughtworks.xstream.core.util.Cloneables;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jianxing
 * 代码参考 {@link com.thoughtworks.xstream.core.DefaultConverterLookup}
 * 增加了 removeConverter 实现动态移除转换器
 */
public class MsJmeterConverterLookup implements ConverterLookup, ConverterRegistry, Caching {

    private MsPrioritizedList converters = new MsPrioritizedList();
    private transient Map typeToConverterMap;
    private Map serializationMap = null;

    public MsJmeterConverterLookup() {
        this(new HashMap());
    }

    /**
     * Constructs a DefaultConverterLookup with a provided map.
     *
     * @param map the map to use
     * @throws NullPointerException if map is null
     * @since 1.4.11
     */
    public MsJmeterConverterLookup(Map map) {
        typeToConverterMap = map;
        typeToConverterMap.clear();
    }

    /**
     * @deprecated As of 1.3, use {@link #MsJmeterConverterLookup()}
     */
    public MsJmeterConverterLookup(Mapper mapper) {
        this();
    }

    public Converter lookupConverterForType(Class type) {
        Converter cachedConverter = type != null ? (Converter)typeToConverterMap.get(type.getName()) : null;
        if (cachedConverter != null) {
            return cachedConverter;
        }

        final Map errors = new LinkedHashMap();
        Iterator iterator = converters.iterator();
        while (iterator.hasNext()) {
            Converter converter = (Converter)iterator.next();
            try {
                if (converter.canConvert(type)) {
                    if (type != null) {
                        typeToConverterMap.put(type.getName(), converter);
                    }
                    return converter;
                }
            } catch (final RuntimeException e) {
                errors.put(converter.getClass().getName(), e.getMessage());
            } catch (final LinkageError e) {
                errors.put(converter.getClass().getName(), e.getMessage());
            }
        }

        final ConversionException exception = new ConversionException(errors.isEmpty()
                ? "No converter specified"
                : "No converter available");
        exception.add("type", type != null ? type.getName() : "null");
        iterator = errors.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry)iterator.next();
            exception.add("converter", entry.getKey().toString());
            exception.add("message", entry.getValue().toString());
        }
        throw exception;
    }

    public void registerConverter(Converter converter, int priority) {
        typeToConverterMap.clear();
        converters.add(converter, priority);
    }

    public void removeConverter(Class<? extends Converter> converterClass) {
        flushCache();
        Iterator iterator = this.converters.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (converterClass.equals(next.getClass())) {
                iterator.remove();
            }
        }
    }

    public void flushCache() {
        typeToConverterMap.clear();
        Iterator iterator = converters.iterator();
        while (iterator.hasNext()) {
            Converter converter = (Converter)iterator.next();
            if (converter instanceof Caching) {
                ((Caching)converter).flushCache();
            }
        }
    }

    private Object writeReplace() {
        serializationMap = (Map)Cloneables.cloneIfPossible(typeToConverterMap);
        serializationMap.clear();
        return this;
    }

    private Object readResolve() {
        typeToConverterMap = serializationMap == null ? new HashMap() : serializationMap;
        serializationMap = null;
        return this;
    }
}