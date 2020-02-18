package io.metersphere.runner.jmx;

import io.metersphere.runner.Engine;
import io.metersphere.runner.EngineContext;
import io.metersphere.runner.EngineThread;
import io.metersphere.runner.jmx.client.DistributedRunner;
import io.metersphere.runner.jmx.client.JmeterProperties;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class JmxEngine extends EngineThread implements Engine {
    private static final Logger log = LoggerFactory.getLogger(JmxEngine.class);
    /// todo：从测试属性中读取
    private final static Integer MAX_DURATION = 60;
    /// todo：从测试属性中读取
    private final static String REMOTE_HOSTS = "127.0.0.1";
    /// todo：jmeter home如何确定
    private final static String jmeterHome = "/opt/fit2cloud/apache-jmeter-5.2.1";
    private static Method readTreeMethod;

    static {
        try {
            readTreeMethod = SaveService.class.getDeclaredMethod("readTree", InputStream.class, File.class);
            readTreeMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // ignore
        }
    }

    private EngineContext context;
    private DistributedRunner runner;

    private static void setMaxTestDuration(HashTree jmxTree) {
        for (HashTree item : jmxTree.values()) {
            Set treeKeys = item.keySet();
            for (Object key : treeKeys) {
                if (key instanceof ThreadGroup) {
                    ((ThreadGroup) key).setProperty(ThreadGroup.SCHEDULER, true);
                    ((ThreadGroup) key).setProperty(ThreadGroup.DURATION, MAX_DURATION);
                }
            }
        }
    }

    @Override
    public String getEngineName() {
        return "JMX";
    }

    @Override
    public boolean init(EngineContext context) {
        this.context = context;

        new JmeterProperties(JmxEngine.jmeterHome).initJmeterProperties();
        FileServer.getFileServer().setBaseForScript(new File(JmxEngine.jmeterHome + File.separator + "nothing"));

        final HashTree jmxTree = loadTree(this.context.getInputStream());
        if (jmxTree == null) {
            return false;
        }

        JMeter.convertSubTree(jmxTree, true);

        setMaxTestDuration(jmxTree);

        this.runner = new DistributedRunner(jmxTree, REMOTE_HOSTS);

        return true;
    }

    @Override
    public void run() {
        try {
            this.runner.run();
        } catch (Throwable e) {
            log.error("run test error, id: " + this.context.getEngineId(), e);
        }
    }

    @Override
    public void stop() {
        super.stop(false);
        this.runner.stop();
    }

    private HashTree loadTree(InputStream inputStream) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return (HashTree) readTreeMethod.invoke(null, bufferedInputStream, null);
        } catch (Exception e) {
            log.error("Failed to load tree", e);
        }

        return null;
    }
}
