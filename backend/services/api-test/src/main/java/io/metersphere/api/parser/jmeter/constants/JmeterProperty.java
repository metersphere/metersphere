package io.metersphere.api.parser.jmeter.constants;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  19:45
 */
public class JmeterProperty {
    public static final String SCRIPT = "script";
    public static final String CACHE_KEY = "cacheKey";
    public static final String SCRIPT_LANGUAGE = "scriptLanguage";
    public final static String ASS_OPTION = "ASS_OPTION";
    public final static String BEAN_SHELL_ASSERTION_QUERY = "BeanShellAssertion.query";
    public final static String BEAN_SHELL_SAMPLER_QUERY = "BeanShellSampler.query";
    public final static String FILE_ENCODING = "fileEncoding";

    public class CSVDataSetProperty {
        public static final String IGNORE_FIRST_LINE = "ignoreFirstLine";
        public static final String STOP_THREAD = "stopThread";
        public static final String FILE_NAME = "filename";
        public static final String SHARE_MODE = "shareMode";
        public static final String RECYCLE = "recycle";
        public static final String DELIMITER = "delimiter";
        public static final String QUOTED_DATA = "quotedData";
        public static final String SHARE_MODE_GROUP = "shareMode.group";
        public static final String SHARE_MODE_THREAD = "shareMode.thread";
        public static final String VARIABLE_NAMES = "variableNames";

    }
}
