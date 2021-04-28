/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main class for JMeter - sets up initial classpath and the loader.
 *
 */
public final class NewDriver {

    private static final String CLASSPATH_SEPARATOR = File.pathSeparator;

    private static final String OS_NAME = System.getProperty("os.name");// $NON-NLS-1$

    private static final String OS_NAME_LC = OS_NAME.toLowerCase(java.util.Locale.ENGLISH);

    private static final String JAVA_CLASS_PATH = "java.class.path";// $NON-NLS-1$

    private static final String JMETER_LOGFILE_SYSTEM_PROPERTY = "jmeter.logfile";// $NON-NLS-1$

    private static final String HEADLESS_MODE_PROPERTY = "java.awt.headless";// $NON-NLS-1$
    /** The class loader to use for loading JMeter classes. */
    private static final DynamicClassLoader loader;

    /** The directory JMeter is installed in. */
    private static final String JMETER_INSTALLATION_DIRECTORY;

    private static final List<Exception> EXCEPTIONS_IN_INIT = new ArrayList<>();

    // 将当前类加载器设置为 loader ，解决由系统类加载器加载的 JMeter 无法动态加载 jar 包问题
    public static void setContextClassLoader() {
        Thread.currentThread().setContextClassLoader(loader);
    }

    public static void loaderClass(String name) {
        try {
            loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static {
        final List<URL> jars = new LinkedList<>();
        final String initiaClasspath = System.getProperty(JAVA_CLASS_PATH);

        // Find JMeter home dir from the initial classpath
        String tmpDir;

        //只从 jmeter.home 加载
        tmpDir = System.getProperty("jmeter.home","");// Allow override $NON-NLS-1$ $NON-NLS-2$
        if (tmpDir.length() == 0) {
            File userDir = new File(System.getProperty("user.dir"));// $NON-NLS-1$
            tmpDir = userDir.getAbsoluteFile().getParent();
        }

        JMETER_INSTALLATION_DIRECTORY=tmpDir;

        /*
         * Does the system support UNC paths? If so, may need to fix them up
         * later
         */
        boolean usesUNC = OS_NAME_LC.startsWith("windows");// $NON-NLS-1$

        // Add standard jar locations to initial classpath
        StringBuilder classpath = new StringBuilder();
        File[] libDirs = new File[] { new File(JMETER_INSTALLATION_DIRECTORY + File.separator + "lib"),// $NON-NLS-1$ $NON-NLS-2$
                new File(JMETER_INSTALLATION_DIRECTORY + File.separator + "lib" + File.separator + "ext"),// $NON-NLS-1$ $NON-NLS-2$
                new File(JMETER_INSTALLATION_DIRECTORY + File.separator + "lib" + File.separator + "junit")};// $NON-NLS-1$ $NON-NLS-2$
        for (File libDir : libDirs) {
            File[] libJars = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (libJars == null) {
                new Throwable("Could not access " + libDir).printStackTrace(); // NOSONAR No logging here
                continue;
            }
            Arrays.sort(libJars); // Bug 50708 Ensure predictable order of jars
            for (File libJar : libJars) {
                try {
                    String s = libJar.getPath();

                    // Fix path to allow the use of UNC URLs
                    if (usesUNC) {
                        if (s.startsWith("\\\\") && !s.startsWith("\\\\\\")) {// $NON-NLS-1$ $NON-NLS-2$
                            s = "\\\\" + s;// $NON-NLS-1$
                        } else if (s.startsWith("//") && !s.startsWith("///")) {// $NON-NLS-1$ $NON-NLS-2$
                            s = "//" + s;// $NON-NLS-1$
                        }
                    } // usesUNC

                    jars.add(new File(s).toURI().toURL());// See Java bug 4496398
                    classpath.append(CLASSPATH_SEPARATOR);
                    classpath.append(s);
                } catch (MalformedURLException e) { // NOSONAR
                    EXCEPTIONS_IN_INIT.add(new Exception("Error adding jar:"+libJar.getAbsolutePath(), e));
                }
            }
        }

        // ClassFinder needs the classpath
        System.setProperty(JAVA_CLASS_PATH, initiaClasspath + classpath.toString());
        loader = AccessController.doPrivileged(
                (PrivilegedAction<DynamicClassLoader>) () ->
                        new DynamicClassLoader(jars.toArray(new URL[jars.size()]), Thread.currentThread().getContextClassLoader())
        );
    }

    /**
     * Prevent instantiation.
     */
    private NewDriver() {
    }

    /**
     * Generate an array of jar files located in a directory.
     * Jar files located in sub directories will not be added.
     *
     * @param dir to search for the jar files.
     */
    private static File[] listJars(File dir) {
        if (dir.isDirectory()) {
            return dir.listFiles((f, name) -> {
                if (name.endsWith(".jar")) {// $NON-NLS-1$
                    File jar = new File(f, name);
                    return jar.isFile() && jar.canRead();
                }
                return false;
            });
        }
        return new File[0];
    }

    /**
     * Add a URL to the loader classpath only; does not update the system classpath.
     *
     * @param path to be added.
     * @throws MalformedURLException when <code>path</code> points to an invalid url
     */
    public static void addURL(String path) throws MalformedURLException {
        File furl = new File(path);
        loader.addURL(furl.toURI().toURL()); // See Java bug 4496398
        File[] jars = listJars(furl);
        for (File jar : jars) {
            loader.addURL(jar.toURI().toURL()); // See Java bug 4496398
        }
    }

    /**
     * Add a URL to the loader classpath only; does not update the system
     * classpath.
     *
     * @param url
     *            The {@link URL} to add to the classpath
     */
    public static void addURL(URL url) {
        loader.addURL(url);
    }

    /**
     * Add a directory or jar to the loader and system classpaths.
     *
     * @param path
     *            to add to the loader and system classpath
     * @throws MalformedURLException
     *             if <code>path</code> can not be transformed to a valid
     *             {@link URL}
     */
    public static void addPath(String path) throws MalformedURLException {
        File file = new File(path);
        // Ensure that directory URLs end in "/"
        if (file.isDirectory() && !path.endsWith("/")) {// $NON-NLS-1$
            file = new File(path + "/");// $NON-NLS-1$
        }
        loader.addURL(file.toURI().toURL()); // See Java bug 4496398
        StringBuilder sb = new StringBuilder(System.getProperty(JAVA_CLASS_PATH));
        sb.append(CLASSPATH_SEPARATOR);
        sb.append(path);
        File[] jars = listJars(file);
        for (File jar : jars) {
            loader.addURL(jar.toURI().toURL()); // See Java bug 4496398
            sb.append(CLASSPATH_SEPARATOR);
            sb.append(jar.getPath());
        }

        // ClassFinder needs this
        System.setProperty(JAVA_CLASS_PATH,sb.toString());
    }

    /**
     * Get the directory where JMeter is installed. This is the absolute path
     * name.
     *
     * @return the directory where JMeter is installed.
     */
    public static String getJMeterDir() {
        return JMETER_INSTALLATION_DIRECTORY;
    }

    /**
     * The main program which actually runs JMeter.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        if(!EXCEPTIONS_IN_INIT.isEmpty()) {
            System.err.println("Configuration error during init, see exceptions:"+exceptionsToString(EXCEPTIONS_IN_INIT)); // NOSONAR Intentional System.err use
        } else {
            Thread.currentThread().setContextClassLoader(loader);


            setLoggingProperties(args);

            try {
                // Only set property if it has not been set explicitely
                if(System.getProperty(HEADLESS_MODE_PROPERTY) == null && shouldBeHeadless(args)) {
                    System.setProperty(HEADLESS_MODE_PROPERTY, "true");
                }
                Class<?> initialClass = loader.loadClass("org.apache.jmeter.JMeter");// $NON-NLS-1$
                Object instance = initialClass.getDeclaredConstructor().newInstance();
                Method startup = initialClass.getMethod("start", new Class[] { new String[0].getClass() });// $NON-NLS-1$
                startup.invoke(instance, new Object[] { args });
            } catch(Throwable e){ // NOSONAR We want to log home directory in case of exception
                e.printStackTrace(); // NOSONAR No logger at this step
                System.err.println("JMeter home directory was detected as: "+JMETER_INSTALLATION_DIRECTORY); // NOSONAR Intentional System.err use
            }
        }
    }

    /**
     * @param exceptionsInInit List of {@link Exception}
     * @return String
     */
    private static String exceptionsToString(List<Exception> exceptionsInInit) {
        StringBuilder builder = new StringBuilder();
        for (Exception exception : exceptionsInInit) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printWriter); // NOSONAR
            builder.append(stringWriter.toString())
                    .append("\r\n");
        }
        return builder.toString();
    }

    /*
     * Set logging related system properties.
     */
    private static void setLoggingProperties(String[] args) {
        String jmLogFile = getCommandLineArgument(args, 'j', "jmeterlogfile");// $NON-NLS-1$ $NON-NLS-2$

        if (jmLogFile != null && !jmLogFile.isEmpty()) {
            jmLogFile = replaceDateFormatInFileName(jmLogFile);
            System.setProperty(JMETER_LOGFILE_SYSTEM_PROPERTY, jmLogFile);// $NON-NLS-1$
        } else if (System.getProperty(JMETER_LOGFILE_SYSTEM_PROPERTY) == null) {// $NON-NLS-1$
            System.setProperty(JMETER_LOGFILE_SYSTEM_PROPERTY, "jmeter.log");// $NON-NLS-1$ $NON-NLS-2$
        }

        String jmLogConf = getCommandLineArgument(args, 'i', "jmeterlogconf");// $NON-NLS-1$ $NON-NLS-2$
        File logConfFile = null;

        if (jmLogConf != null && !jmLogConf.isEmpty()) {
            logConfFile = new File(jmLogConf);
        } else if (System.getProperty("log4j.configurationFile") == null) {// $NON-NLS-1$
            logConfFile = new File("log4j2.xml");// $NON-NLS-1$
            if (!logConfFile.isFile()) {
                logConfFile = new File(JMETER_INSTALLATION_DIRECTORY, "bin" + File.separator + "log4j2.xml");// $NON-NLS-1$ $NON-NLS-2$
            }
        }

        if (logConfFile != null) {
            System.setProperty("log4j.configurationFile", logConfFile.toURI().toString());// $NON-NLS-1$
        }
    }

    private static boolean shouldBeHeadless(String[] args) {
        for (String arg : args) {
            if("-n".equals(arg) || "-s".equals(arg) || "-g".equals(arg)) {
                return true;
            }
        }
        return false;
    }
    /*
     * Find command line argument option value by the id and name.
     */
    private static String getCommandLineArgument(String[] args, int id, String name) {
        final String shortArgName = "-" + ((char) id);// $NON-NLS-1$
        final String longArgName = "--" + name;// $NON-NLS-1$

        String value = null;

        for (int i = 0; i < args.length; i++) {
            if ((shortArgName.equals(args[i]) && i < args.length - 1)
                    || longArgName.equals(args[i])) {
                if (!args[i + 1].startsWith("-")) {// $NON-NLS-1$
                    value = args[i + 1];
                }
                break;
            } else if (!shortArgName.equals(args[i]) && args[i].startsWith(shortArgName)) {
                value = args[i].substring(shortArgName.length());
                break;
            }
        }

        return value;
    }

    /*
     * If the fileName contains at least one set of paired single-quotes, reformat using DateFormat
     */
    private static String replaceDateFormatInFileName(String fileName) {
        try {
            StringBuilder builder = new StringBuilder();

            final Date date = new Date();
            int fromIndex = 0;
            int begin = fileName.indexOf('\'', fromIndex);// $NON-NLS-1$
            int end;

            String format;
            SimpleDateFormat dateFormat;

            while (begin != -1) {
                builder.append(fileName.substring(fromIndex, begin));

                fromIndex = begin + 1;
                end = fileName.indexOf('\'', fromIndex);// $NON-NLS-1$
                if (end == -1) {
                    throw new IllegalArgumentException("Invalid pairs of single-quotes in the file name: " + fileName);// $NON-NLS-1$
                }

                format = fileName.substring(begin + 1, end);
                dateFormat = new SimpleDateFormat(format);
                builder.append(dateFormat.format(date));

                fromIndex = end + 1;
                begin = fileName.indexOf('\'', fromIndex);// $NON-NLS-1$
            }

            if (fromIndex < fileName.length() - 1) {
                builder.append(fileName.substring(fromIndex));
            }

            return builder.toString();
        } catch (Exception ex) {
            System.err.println("Error replacing date format in file name:"+fileName+", error:"+ex.getMessage()); // NOSONAR
        }

        return fileName;
    }
}
