package io.metersphere.commons.constants;

import java.util.ArrayList;
import java.util.List;

public class ElementConstants {
    public static final String HASH_TREE = "hashTree";
    public static final String CLAZZ_NAME = "clazzName";
    public static final String SCENARIO = "scenario";
    public static final String SCENARIO_UPPER = "SCENARIO";
    public static final String HTTP_SAMPLER = "HTTPSamplerProxy";
    public static final String TCP_SAMPLER = "TCPSampler";
    public static final String DUBBO_SAMPLER = "DubboSampler";
    public static final String JDBC_SAMPLER = "JDBCSampler";
    public static final String JSR223 = "JSR223Processor";
    public static final String JSR223_PRE = "JSR223PreProcessor";
    public static final String JSR223_POST = "JSR223PostProcessor";
    public static final String JDBC_PRE = "JDBCPreProcessor";
    public static final String JDBC_POST = "JDBCPostProcessor";
    public static final String JMETER_ELE = "JmeterElement";
    public static final String TEST_PLAN = "TestPlan";
    public static final String THREAD_GROUP = "ThreadGroup";
    public static final String DNS_CACHE = "DNSCacheManager";
    public static final String DEBUG_SAMPLER = "DebugSampler";
    public static final String AUTH_MANAGER = "AuthManager";
    public static final String ABS_SAMPLER = "AbstractSampler";
    public static final String IF_CONTROLLER = "IfController";
    public static final String TRANSACTION_CONTROLLER = "TransactionController";
    public static final String LOOP_CONTROLLER = "LoopController";
    public static final String CONSTANT_TIMER = "ConstantTimer";
    public static final String ASSERTIONS = "Assertions";
    public static final String EXTRACT = "Extract";
    public static final String STEP_CREATED = "Created";
    public static final String INDEX = "index";
    public static final String ID = "id";
    public static final String REF_ENABLE = "refEnable";

    public final static List<String> REQUESTS = new ArrayList<String>() {{
        this.add(ElementConstants.HTTP_SAMPLER);
        this.add(ElementConstants.DUBBO_SAMPLER);
        this.add(ElementConstants.JDBC_SAMPLER);
        this.add(ElementConstants.TCP_SAMPLER);
        this.add(ElementConstants.JSR223);
        this.add(ElementConstants.JSR223_PRE);
        this.add(ElementConstants.JSR223_POST);
        this.add(ElementConstants.JDBC_PRE);
        this.add(ElementConstants.JDBC_POST);
        this.add(ElementConstants.JMETER_ELE);
        this.add(ElementConstants.TEST_PLAN);
        this.add(ElementConstants.THREAD_GROUP);
        this.add(ElementConstants.DNS_CACHE);
        this.add(ElementConstants.DEBUG_SAMPLER);
        this.add(ElementConstants.AUTH_MANAGER);
        this.add(ElementConstants.ABS_SAMPLER);
    }};

    public static final String SCRIPT = "script";
    public static final String BEANSHELL = "beanshell";
    public static final String IS_REF = "isRef";
    public static final String FILE_ID = "fileId";

    public static final String RESOURCE_ID = "resourceId";
    public static final String FILENAME = "filename";
    public static final String COVER = "COVER";
    public static final String MS_KEYSTORE_FILE_PATH = "MS-KEYSTORE-FILE-PATH";
    public static final String MS_KEYSTORE_FILE_PASSWORD = "MS-KEYSTORE-FILE-PASSWORD";
    public static final String VIRTUAL_STEPS = "VIRTUAL_STEPS";
    public static final String REF = "$ref";
    public static final String TYPE = "type";

}
