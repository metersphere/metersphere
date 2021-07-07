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

package io.metersphere.api.jmeter;

import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.service.MsResultService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;

import java.io.Serializable;

/**
 * 实时结果监听
 */
public class MsResultCollector extends AbstractListenerElement implements SampleListener, Clearable, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final String ERROR_LOGGING = "MsResultCollector.error_logging"; // $NON-NLS-1$

    private static final String SUCCESS_ONLY_LOGGING = "MsResultCollector.success_only_logging"; // $NON-NLS-1$

    private static final String TEST_IS_LOCAL = "*local*"; // $NON-NLS-1$

    public static final String TEST_END = "MS_TEST_END";

    private MsResultService msResultService;

    @Override
    public Object clone() {
        MsResultCollector clone = (MsResultCollector) super.clone();
        return clone;
    }

    public boolean isErrorLogging() {
        return getPropertyAsBoolean(ERROR_LOGGING);
    }

    public final void setSuccessOnlyLogging(boolean value) {
        if (value) {
            setProperty(new BooleanProperty(SUCCESS_ONLY_LOGGING, true));
        } else {
            removeProperty(SUCCESS_ONLY_LOGGING);
        }
    }

    /**
     * Get the state of successful only logging
     *
     * @return Flag whether only successful samples should be logged
     */
    public boolean isSuccessOnlyLogging() {
        return getPropertyAsBoolean(SUCCESS_ONLY_LOGGING, false);
    }

    public boolean isSampleWanted(boolean success) {
        boolean errorOnly = isErrorLogging();
        boolean successOnly = isSuccessOnlyLogging();
        return isSampleWanted(success, errorOnly, successOnly);
    }

    public static boolean isSampleWanted(boolean success, boolean errorOnly,
                                         boolean successOnly) {
        return (!errorOnly && !successOnly) ||
                (success && successOnly) ||
                (!success && errorOnly);
    }

    @Override
    public void testEnded(String host) {
        LogUtil.debug("TestEnded " + this.getName());
        SampleResult result = new SampleResult();
        result.setResponseCode(TEST_END);
        msResultService.setCache(this.getName(), result);
    }

    @Override
    public void testStarted(String host) {
        LogUtil.debug("TestStarted " + this.getName());
        msResultService = CommonBeanFactory.getBean(MsResultService.class);
        if (msResultService == null) {
            LogUtil.error("testResultService is required");
        }
    }

    @Override
    public void testEnded() {
        testEnded(TEST_IS_LOCAL);
    }

    @Override
    public void testStarted() {
        testStarted(TEST_IS_LOCAL);
    }

    @Override
    public void sampleStarted(SampleEvent e) {
    }

    @Override
    public void sampleStopped(SampleEvent e) {
    }

    @Override
    public void sampleOccurred(SampleEvent event) {
        SampleResult result = event.getResult();
        if (isSampleWanted(result.isSuccessful()) && !StringUtils.equals(result.getSampleLabel(), RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME)) {
            msResultService.setCache(this.getName(), result);
        }
    }

    @Override
    public void clearData() {
    }
}
