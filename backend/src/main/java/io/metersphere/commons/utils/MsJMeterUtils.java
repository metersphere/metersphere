package io.metersphere.commons.utils;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

import java.io.InputStream;
import java.util.Properties;

public class MsJMeterUtils {
    /**
     * Load the JMeter properties file; if not found, then
     * default to "org/apache/jmeter/jmeter.properties" from the classpath
     *
     * <p>
     * c.f. loadProperties
     *
     * @param file Name of the file from which the JMeter properties should be loaded
     */
    public static void loadJMeterProperties(String file) {
        InputStream is = null;
        try {
            JMeterUtils.loadJMeterProperties(file);
        } catch (Exception e) {
            try {
                Properties p = new Properties(System.getProperties());
                // In jar file classpath is
                is = ClassLoader.getSystemResourceAsStream(
                        "BOOT-INF/classes/org/apache/jmeter/jmeter.properties"); // $NON-NLS-1$
                if (is == null) {
                    throw new RuntimeException("Could not read JMeter properties file:" + file);
                }
                p.load(is);

                FieldUtils.writeStaticField(JMeterUtils.class, "appProperties", p, true);
            } catch (Exception ex) {
                throw new RuntimeException("Could not read JMeter properties file:" + file);
            }
        } finally {
            JOrphanUtils.closeQuietly(is);
        }
    }

}
