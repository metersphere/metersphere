/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.testelement;

import io.metersphere.utils.LoggerUtil;
import org.apache.jmeter.NewDriver;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestPlan extends AbstractTestElement implements Serializable, TestStateListener {
    private static final long serialVersionUID = 234L;

    private static final Logger log = LoggerFactory.getLogger(TestPlan.class);

    //+ JMX field names - do not change values
    private static final String FUNCTIONAL_MODE = "TestPlan.functional_mode"; //$NON-NLS-1$

    private static final String USER_DEFINED_VARIABLES = "TestPlan.user_defined_variables"; //$NON-NLS-1$

    private static final String SERIALIZE_THREADGROUPS = "TestPlan.serialize_threadgroups"; //$NON-NLS-1$

    private static final String CLASSPATHS = "TestPlan.user_define_classpath"; //$NON-NLS-1$

    private static final String TEARDOWN_ON_SHUTDOWN = "TestPlan.tearDown_on_shutdown"; //$NON-NLS-1$

    //- JMX field names

    private static final String CLASSPATH_SEPARATOR = ","; //$NON-NLS-1$

    private static final String BASEDIR = "basedir";
    private transient List<AbstractThreadGroup> threadGroups = new ArrayList<>();

    // There's only 1 test plan, so can cache the mode here
    private static volatile boolean functionalMode = false;

    public TestPlan() {
        super();
    }

    public TestPlan(String name) {
        setName(name);
    }

    // create transient item
    protected Object readResolve() {
        threadGroups = new ArrayList<>();
        return this;
    }

    public void prepareForPreCompile() {
        getVariables().setRunningVersion(true);
    }

    /**
     * Fetches the functional mode property
     *
     * @return functional mode
     */
    public boolean isFunctionalMode() {
        return getPropertyAsBoolean(FUNCTIONAL_MODE);
    }

    public void setUserDefinedVariables(Arguments vars) {
        setProperty(new TestElementProperty(USER_DEFINED_VARIABLES, vars));
    }

    public JMeterProperty getUserDefinedVariablesAsProperty() {
        return getProperty(USER_DEFINED_VARIABLES);
    }

    public String getBasedir() {
        return getPropertyAsString(BASEDIR);
    }

    // Does not appear to be used yet
    public void setBasedir(String b) {
        setProperty(BASEDIR, b);
    }

    public Arguments getArguments() {
        return getVariables();
    }

    public Map<String, String> getUserDefinedVariables() {
        Arguments args = getVariables();
        return args.getArgumentsAsMap();
    }

    private Arguments getVariables() {
        Arguments args = (Arguments) getProperty(USER_DEFINED_VARIABLES).getObjectValue();
        if (args == null) {
            args = new Arguments();
            setUserDefinedVariables(args);
        }
        return args;
    }

    public void setFunctionalMode(boolean funcMode) {
        setProperty(new BooleanProperty(FUNCTIONAL_MODE, funcMode));
        setGlobalFunctionalMode(funcMode);
    }

    /**
     * Set JMeter in functional mode
     *
     * @param funcMode boolean functional mode
     */
    private static void setGlobalFunctionalMode(boolean funcMode) {
        functionalMode = funcMode;
    }

    /**
     * Gets the static copy of the functional mode
     *
     * @return mode
     */
    public static boolean getFunctionalMode() {
        return functionalMode;
    }

    public void setSerialized(boolean serializeTGs) {
        setProperty(new BooleanProperty(SERIALIZE_THREADGROUPS, serializeTGs));
    }

    public void setTearDownOnShutdown(boolean tearDown) {
        setProperty(TEARDOWN_ON_SHUTDOWN, tearDown, false);
    }

    public boolean isTearDownOnShutdown() {
        return getPropertyAsBoolean(TEARDOWN_ON_SHUTDOWN, false);
    }

    /**
     * Set the classpath for the test plan. If the classpath is made up from
     * more then one path, the parts must be separated with
     * {@link TestPlan#CLASSPATH_SEPARATOR}.
     *
     * @param text the classpath to be set
     */
    public void setTestPlanClasspath(String text) {
        setProperty(CLASSPATHS, text);
    }

    public void setTestPlanClasspathArray(String[] text) {
        StringBuilder cat = new StringBuilder();
        for (int idx = 0; idx < text.length; idx++) {
            if (idx > 0) {
                cat.append(CLASSPATH_SEPARATOR);
            }
            cat.append(text[idx]);
        }
        this.setTestPlanClasspath(cat.toString());
    }

    public String[] getTestPlanClasspathArray() {
        return JOrphanUtils.split(this.getTestPlanClasspath(), CLASSPATH_SEPARATOR);
    }

    /**
     * Returns the classpath
     *
     * @return classpath
     */
    public String getTestPlanClasspath() {
        return getPropertyAsString(CLASSPATHS);
    }

    /**
     * Fetch the serialize threadgroups property
     *
     * @return serialized setting
     */
    public boolean isSerialized() {
        return getPropertyAsBoolean(SERIALIZE_THREADGROUPS);
    }

    public void addParameter(String name, String value) {
        getVariables().addArgument(name, value);
    }

    @Override
    public void addTestElement(TestElement tg) {
        super.addTestElement(tg);
        if (tg instanceof AbstractThreadGroup && !isRunningVersion()) {
            addThreadGroup((AbstractThreadGroup) tg);
        }
    }

    /**
     * Adds a feature to the AbstractThreadGroup attribute of the TestPlan object.
     *
     * @param group the feature to be added to the AbstractThreadGroup attribute
     */
    public void addThreadGroup(AbstractThreadGroup group) {
        threadGroups.add(group);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        LoggerUtil.info("Test plan " + this.getName() + "test end");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String host) {
        testEnded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
        LoggerUtil.info("测试计划开始执行：" + this.getName());
        if (getBasedir() != null && getBasedir().length() > 0) {
            try {
                FileServer.getFileServer().setBasedir(FileServer.getFileServer().getBaseDir() + getBasedir());
            } catch (IllegalStateException e) {
                log.error("Failed to set file server base dir with {}", getBasedir(), e);
            }
        }
        // we set the classpath
        String[] paths = this.getTestPlanClasspathArray();
        for (String path : paths) {
            try {
                NewDriver.addURL(path);
                log.info("added {} to classpath", path);
            } catch (MalformedURLException e) {
                // TODO Should we continue the test or fail ?
                log.error("Error adding {} to classpath", path, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String host) {
        testStarted();
    }

}
