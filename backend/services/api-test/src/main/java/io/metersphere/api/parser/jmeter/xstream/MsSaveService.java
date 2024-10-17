package io.metersphere.api.parser.jmeter.xstream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.thoughtworks.xstream.converters.*;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;
import com.thoughtworks.xstream.mapper.CachingMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.PluginLoadService;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jmeter.reporters.ResultCollectorHelper;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.NameUpdater;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.util.JMeterError;
import org.apache.jorphan.util.JOrphanUtils;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * @Author: jianxing
 * 代码参考 {@link org.apache.jmeter.save.SaveService}
 * 主要为了接口导入支持动态插件，将 jmx 文件转换成 HashTree
 * 修改点：
 * 1. 因为 ScriptWrapper 不是 public ，所以替换成 MsScriptWrapper
 * 2. StreamWrapper 替换为 MsXStreamWrapper，使用 MsJmeterConverterLookup，实现动态维护 converter
 * 3. 增加 registerConverter 和 removeConverter 方法，动态维护 converter
 * 4. 增加 pluginAliasMap 记录插件中 jmeter 元素的别名
 */
public class MsSaveService {

    private static final Logger log = LoggerFactory.getLogger(MsSaveService.class);

    // Names of DataHolder entries for JTL processing
    public static final String SAMPLE_EVENT_OBJECT = "SampleEvent"; // $NON-NLS-1$
    public static final String RESULTCOLLECTOR_HELPER_OBJECT = "ResultCollectorHelper"; // $NON-NLS-1$

    // Names of DataHolder entries for JMX processing
    public static final String TEST_CLASS_NAME = "TestClassName"; // $NON-NLS-1$

    public static final MsJmeterConverterLookup JMETER_CONVERTER_LOOKUP = new MsJmeterConverterLookup();

    private static final class MsXStreamWrapper extends XStream {
        private MsXStreamWrapper(ReflectionProvider reflectionProvider) {
            super(reflectionProvider, new XppDriver(), new ClassLoaderReference(new CompositeClassLoader()), null,
                    type -> JMETER_CONVERTER_LOOKUP.lookupConverterForType(type),
                    (converter, priority) -> JMETER_CONVERTER_LOOKUP.registerConverter(converter, priority));
        }

        // Override wrapMapper in order to insert the Wrapper in the chain
        @Override
        protected MapperWrapper wrapMapper(MapperWrapper next) {
            // Provide our own aliasing using strings rather than classes
            return new MapperWrapper(next) {
                // Translate alias to classname and then delegate to wrapped class
                @Override
                public Class<?> realClass(String alias) {
                    String fullName = aliasToClass(alias);
                    if (fullName != null) {
                        fullName = NameUpdater.getCurrentName(fullName);
                    }
                    if (fullName == null) {
                        fullName = pluginAliasMap.get(alias);
                    }
                    try {
                        return super.realClass(fullName == null ? alias : fullName);
                    } catch (CannotResolveClassException e) {
                        PluginLoadService pluginLoadService = CommonBeanFactory.getBean(PluginLoadService.class);
                        LogUtils.info(e.getMessage());
                        for (PluginWrapper plugin : pluginLoadService.getMsPluginManager().getPlugins()) {
                            try {
                                Class<?> aClass = plugin.getPluginClassLoader().loadClass(fullName);
                                if (aClass != null) {
                                    return aClass;
                                }
                            } catch (ClassNotFoundException ex) {
                                LogUtils.info(e.getMessage());
                            }
                        }
                    }
                    throw new MSException("无法解析：" + alias);
                }

                // Translate to alias and then delegate to wrapped class
                @Override
                public String serializedClass(@SuppressWarnings("rawtypes") // superclass does not use types
                                              Class type) {
                    if (type == null) {
                        return super.serializedClass(null); // was type, but that caused FindBugs warning
                    }
                    String alias = classToAlias(type.getName());
                    return alias == null ? super.serializedClass(type) : alias;
                }
            };
        }
    }

    private static final XStream JMXSAVER = new MsXStreamWrapper(new PureJavaReflectionProvider());
    private static final XStream JTLSAVER = new MsXStreamWrapper(new PureJavaReflectionProvider());

    static {
        JTLSAVER.setMode(XStream.NO_REFERENCES); // This is needed to stop XStream keeping copies of each class
        JMeterUtils.setupXStreamSecurityPolicy(JMXSAVER);
        JMeterUtils.setupXStreamSecurityPolicy(JTLSAVER);
    }

    // The XML header, with placeholder for encoding, since that is controlled by property
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"<ph>\"?>"; // $NON-NLS-1$

    // Default file name
    private static final String SAVESERVICE_PROPERTIES_FILE = "saveservice.properties"; // $NON-NLS-1$

    // Property name used to define file name
    private static final String SAVESERVICE_PROPERTIES = "saveservice_properties"; // $NON-NLS-1$

    private static final String JMETER_ELEMENT_ALIAS_PROPERTIES = "jmeter_element_alias.properties"; // $NON-NLS-1$
    private static final Map<String, String> pluginAliasMap = new HashMap<>();

    // Define file format versions
    private static final String VERSION_2_2 = "2.2";  // $NON-NLS-1$

    // Holds the mappings from the saveservice properties file
    // Key: alias Entry: full class name
    // There may be multiple aliases which map to the same class
    private static final Properties aliasToClass = new Properties();

    // Holds the reverse mappings
    // Key: full class name Entry: primary alias
    private static final Properties classToAlias = new Properties();

    // Version information for test plan header
    // This is written to JMX files by ScriptWrapperConverter
    // Also to JTL files by ResultCollector
    private static final String VERSION = "1.2"; // $NON-NLS-1$

    // This is written to JMX files by ScriptWrapperConverter
    private static String propertiesVersion = "";// read from properties file; written to JMX files

    // Must match _version property value in saveservice.properties
    // used to ensure saveservice.properties and SaveService are updated simultaneously
    static final String PROPVERSION = "5.0";// Expected version $NON-NLS-1$

    // Internal information only
    private static String fileVersion = ""; // computed from saveservice.properties file// $NON-NLS-1$

    private static String fileEncoding = ""; // read from properties file// $NON-NLS-1$

    static {
        log.info("Testplan (JMX) version: {}. Testlog (JTL) version: {}", VERSION_2_2, VERSION_2_2);
        initProps();
        checkVersions();
    }

    public static Converter registerConverter(Class<? extends Converter> converterClass) {
        try {
            Converter converter = converterClass.getConstructor(Mapper.class).newInstance(getJMXMapper());
            JMETER_CONVERTER_LOOKUP.registerConverter(converter, 0);
            return converter;
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return null;
    }

    public static void unRegisterConverter(Class<? extends Converter> converterClass) {
        JMETER_CONVERTER_LOOKUP.removeConverter(converterClass);
        if (getJMXMapper() instanceof CachingMapper cachingMapper) {
            // 有插件卸载，则清空缓存，否则，插件重新上传无效
            cachingMapper.flushCache();
        }
        if (getJTLMapper() instanceof CachingMapper cachingMapper) {
            // 有插件卸载，则清空缓存，否则，插件重新上传无效
            cachingMapper.flushCache();
        }
    }

    public static Mapper getJMXMapper() {
        return JMXSAVER.getMapper();
    }

    public static Mapper getJTLMapper() {
        return JTLSAVER.getMapper();
    }

    // Helper method to simplify alias creation from properties
    private static void makeAlias(String aliasList, String clazz) {
        String[] aliases = aliasList.split(","); // Can have multiple aliases for same target classname
        String alias = aliases[0];
        for (String a : aliases) {
            Object old = aliasToClass.setProperty(a, clazz);
            if (old != null) {
                log.error("Duplicate class detected for {}: {} & {}", alias, clazz, old);
            }
        }
        Object oldval = classToAlias.setProperty(clazz, alias);
        if (oldval != null) {
            log.error("Duplicate alias detected for {}: {} & {}", clazz, alias, oldval);
        }
    }

    private static File getSaveServiceFile() {
        String saveServiceProps = JMeterUtils.getPropDefault(SAVESERVICE_PROPERTIES, SAVESERVICE_PROPERTIES_FILE); //$NON-NLS-1$
        if (saveServiceProps.length() > 0) { //$NON-NLS-1$
            return JMeterUtils.findFile(saveServiceProps);
        }
        throw new IllegalStateException("Could not find file configured in saveservice_properties property set to:" + saveServiceProps);
    }

    public static Properties loadProperties() throws IOException {
        Properties nameMap = new Properties();
        File saveServiceFile = getSaveServiceFile();
        if (saveServiceFile.canRead()) {
            try (FileInputStream fis = new FileInputStream(saveServiceFile)) {
                nameMap.load(fis);
            }
        }
        return nameMap;
    }

    public static Properties loadPluginAliasProperties(ClassLoader classLoader) throws IOException {
        Properties nameMap = new Properties();
        InputStream resourceAsStream = classLoader.getResourceAsStream(JMETER_ELEMENT_ALIAS_PROPERTIES);
        if (resourceAsStream == null) {
            return nameMap;
        }
        nameMap.load(resourceAsStream);
        nameMap.forEach((k, v) -> pluginAliasMap.put((String) k, (String) v));
        return nameMap;
    }

    private static String checksum(Properties nameMap) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        // This checksums the actual entries, and it ignores comments and blank lines
        nameMap.entrySet().stream().sorted(
                Comparator.comparing((Map.Entry<Object, Object> e) -> e.getKey().toString())
                        .thenComparing(e -> e.getValue().toString())
        ).forEachOrdered(e -> {
            md.update(e.getKey().toString().getBytes(StandardCharsets.UTF_8));
            md.update(e.getValue().toString().getBytes(StandardCharsets.UTF_8));
        });
        return JOrphanUtils.baToHexString(md.digest());
    }

    private static void initProps() {
        // Load the alias properties
        try {
            Properties nameMap = loadProperties();
            try {
                fileVersion = checksum(nameMap);
            } catch (NoSuchAlgorithmException e) {
                log.error("Can't compute checksum for saveservice properties file", e);
                throw new JMeterError("JMeter requires the checksum of saveservice properties file to continue", e);
            }
            // now create the aliases
            for (Map.Entry<Object, Object> me : nameMap.entrySet()) {
                String key = (String) me.getKey();
                String val = (String) me.getValue();
                if (!key.startsWith("_")) { // $NON-NLS-1$
                    makeAlias(key, val);
                } else {
                    // process special keys
                    if (key.equalsIgnoreCase("_version")) { // $NON-NLS-1$
                        propertiesVersion = val;
                        log.info("Using SaveService properties version {}", propertiesVersion);
                    } else if (key.equalsIgnoreCase("_file_version")) { // $NON-NLS-1$
                        log.info("SaveService properties file version is now computed by a checksum,"
                                + "the property _file_version is not used anymore and can be removed.");
                    } else if (key.equalsIgnoreCase("_file_encoding")) { // $NON-NLS-1$
                        fileEncoding = val;
                        log.info("Using SaveService properties file encoding {}", fileEncoding);
                    } else {
                        key = key.substring(1);// Remove the leading "_"
                        registerConverter(key, val);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Bad saveservice properties file", e);
            throw new JMeterError("JMeter requires the saveservice properties file to continue");
        }
    }

    private static void registerConverter(String key, String val) {
        try {
            final String trimmedValue = val.trim();
            boolean useMapper = "collection".equals(trimmedValue) || "mapping".equals(trimmedValue); // $NON-NLS-1$ $NON-NLS-2$
            registerConverter(key, JMXSAVER, useMapper);
            registerConverter(key, JTLSAVER, useMapper);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | IllegalArgumentException |
                 SecurityException | InvocationTargetException | NoSuchMethodException e1) {
            log.warn("Can't register a converter: {}", key, e1);
        }
    }

    /**
     * Register converter.
     *
     * @param key
     * @param jmxsaver
     * @param useMapper
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    private static void registerConverter(String key, XStream jmxsaver, boolean useMapper)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException,
            ClassNotFoundException {
        final Class<? extends Converter> clazz = Class.forName(key).asSubclass(Converter.class);
        if (useMapper) {
            jmxsaver.registerConverter(clazz.getConstructor(Mapper.class).newInstance(jmxsaver.getMapper()));
        } else {
            jmxsaver.registerConverter(clazz.getDeclaredConstructor().newInstance());
        }
    }

    // For converters to use
    public static String aliasToClass(String s) {
        String r = aliasToClass.getProperty(s);
        return r == null ? s : r;
    }

    // For converters to use
    public static String classToAlias(String s) {
        String r = classToAlias.getProperty(s);
        return r == null ? s : r;
    }

    // Called by Save function
    public static void saveTree(HashTree tree, OutputStream out) throws IOException {
        // Get the OutputWriter to use
        OutputStreamWriter outputStreamWriter = getOutputStreamWriter(out);
        writeXmlHeader(outputStreamWriter);
        // Use deprecated method, to avoid duplicating code
        MsScriptWrapper wrapper = new MsScriptWrapper();
        wrapper.testPlan = tree;
        JMXSAVER.toXML(wrapper, outputStreamWriter);
        outputStreamWriter.write('\n');// Ensure terminated properly
        outputStreamWriter.close();
    }

    // Used by Test code
    public static void saveElement(Object el, OutputStream out) throws IOException {
        // Get the OutputWriter to use
        OutputStreamWriter outputStreamWriter = getOutputStreamWriter(out);
        writeXmlHeader(outputStreamWriter);
        // Use deprecated method, to avoid duplicating code
        JMXSAVER.toXML(el, outputStreamWriter);
        outputStreamWriter.close();
    }

    // Used by Test code
    public static Object loadElement(InputStream in) throws IOException {
        // Get the InputReader to use
        InputStreamReader inputStreamReader = getInputStreamReader(in);
        // Use deprecated method, to avoid duplicating code
        Object element = JMXSAVER.fromXML(inputStreamReader);
        inputStreamReader.close();
        return element;
    }

    /**
     * Save a sampleResult to an XML output file using XStream.
     *
     * @param evt    sampleResult wrapped in a sampleEvent
     * @param writer output stream which must be created using {@link #getFileEncoding(String)}
     * @throws IOException when writing data to output fails
     */
    // Used by ResultCollector.sampleOccurred(SampleEvent event)
    public static synchronized void saveSampleResult(SampleEvent evt, Writer writer) throws IOException {
        DataHolder dh = JTLSAVER.newDataHolder();
        dh.put(SAMPLE_EVENT_OBJECT, evt);
        // This is effectively the same as saver.toXML(Object, Writer) except we get to provide the DataHolder
        // Don't know why there is no method for this in the XStream class
        try {
            JTLSAVER.marshal(evt.getResult(), new XppDriver().createWriter(writer), dh);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Failed marshalling:" + (evt.getResult() != null ? showDebuggingInfo(evt.getResult()) : "null"), e);
        }
        writer.write('\n');
    }

    /**
     * @param result SampleResult
     * @return String debugging information
     */
    private static String showDebuggingInfo(SampleResult result) {
        try {
            return "class:" + result.getClass() + ",content:" + ToStringBuilder.reflectionToString(result);
        } catch (Exception e) {
            return "Exception occurred creating debug from event, message:" + e.getMessage();
        }
    }

    // Routines for TestSaveService
    static String getPropertyVersion() {
        return MsSaveService.propertiesVersion;
    }

    static String getFileVersion() {
        return MsSaveService.fileVersion;
    }

    // Allow test code to check for spurious class references
    static List<String> checkClasses() {
        final ClassLoader classLoader = MsSaveService.class.getClassLoader();
        List<String> missingClasses = new ArrayList<>();
        for (Object clazz : classToAlias.keySet()) {
            String name = (String) clazz;
            if (!NameUpdater.isMapped(name)) {// don't bother checking class is present if it is to be updated
                try {
                    Class.forName(name, false, classLoader);
                } catch (ClassNotFoundException e) {
                    log.error("Unexpected entry in saveservice.properties; class does not exist and is not upgraded: {}", name);
                    missingClasses.add(name);
                }
            }
        }
        return missingClasses;
    }

    private static void checkVersions() {
        if (!PROPVERSION.equalsIgnoreCase(propertiesVersion)) {
            log.warn("Bad _version - expected {}, found {}.", PROPVERSION, propertiesVersion);
        }
    }

    /**
     * Read results from JTL file.
     *
     * @param reader                of the file
     * @param resultCollectorHelper helper class to enable TestResultWrapperConverter to deliver the samples
     * @throws IOException if an I/O error occurs
     */
    public static void loadTestResults(InputStream reader, ResultCollectorHelper resultCollectorHelper) throws IOException {
        // Get the InputReader to use
        InputStreamReader inputStreamReader = getInputStreamReader(reader);
        DataHolder dh = JTLSAVER.newDataHolder();
        dh.put(RESULTCOLLECTOR_HELPER_OBJECT, resultCollectorHelper); // Allow TestResultWrapper to feed back the samples
        // This is effectively the same as saver.fromXML(InputStream) except we get to provide the DataHolder
        // Don't know why there is no method for this in the XStream class
        JTLSAVER.unmarshal(new XppDriver().createReader(reader), null, dh);
        inputStreamReader.close();
    }

    /**
     * Load a Test tree (JMX file)
     *
     * @param file the JMX file
     * @return the loaded tree
     * @throws IOException if there is a problem reading the file or processing it
     */
    public static HashTree loadTree(File file) throws IOException {
        log.info("Loading file: {}", file);
        try (InputStream inputStream = new FileInputStream(file);
             BufferedInputStream bufferedInputStream =
                     new BufferedInputStream(inputStream)) {
            return readTree(bufferedInputStream, file);
        }
    }

    /**
     * @param inputStream {@link InputStream}
     * @param file        the JMX file used only for debug, can be null
     * @return the loaded tree
     * @throws IOException if there is a problem reading the file or processing it
     */
    private static HashTree readTree(InputStream inputStream, File file)
            throws IOException {
        MsScriptWrapper wrapper = null;
        try {
            // Get the InputReader to use
            InputStreamReader inputStreamReader = getInputStreamReader(inputStream);
            wrapper = (MsScriptWrapper) JMXSAVER.fromXML(inputStreamReader);
            inputStreamReader.close();
            if (wrapper == null) {
                log.error("Problem loading XML: see above.");
                return null;
            }
            return wrapper.testPlan;
        } catch (CannotResolveClassException | ConversionException | NoClassDefFoundError e) {
            if (file != null) {
                throw new IllegalArgumentException("Problem loading XML from:'" + file.getAbsolutePath() + "'. \nCause:\n" +
                        ExceptionUtils.getRootCauseMessage(e) + "\n\n Detail:" + e, e);
            } else {
                throw new IllegalArgumentException("Problem loading XML. \nCause:\n" +
                        ExceptionUtils.getRootCauseMessage(e) + "\n\n Detail:" + e, e);
            }
        }

    }

    private static InputStreamReader getInputStreamReader(InputStream inStream) {
        // Check if we have a encoding to use from properties
        Charset charset = getFileEncodingCharset();
        return new InputStreamReader(inStream, charset);
    }

    private static OutputStreamWriter getOutputStreamWriter(OutputStream outStream) {
        // Check if we have a encoding to use from properties
        Charset charset = getFileEncodingCharset();
        return new OutputStreamWriter(outStream, charset);
    }

    /**
     * Returns the file Encoding specified in saveservice.properties or the default
     *
     * @param dflt value to return if file encoding was not provided
     * @return file encoding or default
     */
    // Used by ResultCollector when creating output files
    public static String getFileEncoding(String dflt) {
        if (fileEncoding != null && fileEncoding.length() > 0) {
            return fileEncoding;
        } else {
            return dflt;
        }
    }

    // @NotNull
    private static Charset getFileEncodingCharset() {
        // Check if we have a encoding to use from properties
        if (fileEncoding != null && fileEncoding.length() > 0) {
            return Charset.forName(fileEncoding);
        } else {

            // We use the default character set encoding of the JRE
            log.info("fileEncoding not defined - using JRE default");
            return Charset.defaultCharset();
        }
    }

    private static void writeXmlHeader(OutputStreamWriter writer) throws IOException {
        // Write XML header if we have the charset to use for encoding
        Charset charset = getFileEncodingCharset();
        // We do not use getEncoding method of Writer, since that returns
        // the historical name
        String header = XML_HEADER.replaceAll("<ph>", charset.name());
        writer.write(header);
        writer.write('\n');
    }

//  Normal output
//  ---- Debugging information ----
//  required-type       : org.apache.jorphan.collections.ListedHashTree
//  cause-message       : WebServiceSampler : WebServiceSampler
//  class               : org.apache.jmeter.save.ScriptWrapper
//  message             : WebServiceSampler : WebServiceSampler
//  line number         : 929
//  path                : /jmeterTestPlan/hashTree/hashTree/hashTree[4]/hashTree[5]/WebServiceSampler
//  cause-exception     : com.thoughtworks.xstream.alias.CannotResolveClassException
//  -------------------------------

    /**
     * Simplify getMessage() output from XStream ConversionException
     *
     * @param ce - ConversionException to analyse
     * @return string with details of error
     */
    public static String CEtoString(ConversionException ce) {
        return "XStream ConversionException at line: " + ce.get("line number") + "\n" + ce.get("message")
                + "\nPerhaps a missing jar? See log file.";
    }

    public static String getPropertiesVersion() {
        return propertiesVersion;
    }

    public static String getVERSION() {
        return VERSION;
    }
}
