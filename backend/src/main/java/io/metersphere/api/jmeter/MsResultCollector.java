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

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.service.MsResultService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.RequestResult;
import io.metersphere.jmeter.JMeterBase;
import io.metersphere.utils.JMeterVars;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.websocket.c.to.c.WebSocketUtils;
import io.metersphere.websocket.c.to.c.util.MsgDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.JMeterVariables;

import java.io.Serializable;
import java.util.Map;

/**
 * 实时结果监听
 */
public class MsResultCollector extends AbstractListenerElement implements SampleListener, Clearable, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final String ERROR_LOGGING = "MsResultCollector.error_logging"; // $NON-NLS-1$

    private static final String SUCCESS_ONLY_LOGGING = "MsResultCollector.success_only_logging"; // $NON-NLS-1$

    private static final String TEST_IS_LOCAL = "*local*"; // $NON-NLS-1$

    public static final String TEST_END = "MS_TEST_END";

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
        LoggerUtil.debug("TestEnded " + this.getName());
        MsgDto dto = new MsgDto();
        dto.setExecEnd(false);
        dto.setContent(TEST_END);
        dto.setReportId("send." + this.getName());
        dto.setToReport(this.getName());
        LoggerUtil.debug("send. " + this.getName());
        WebSocketUtils.sendMessageSingle(dto);
        WebSocketUtils.onClose(this.getName());
    }

    @Override
    public void testStarted(String host) {
        LogUtil.debug("TestStarted " + this.getName());

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
        try {
            MsgDto dto = new MsgDto();
            dto.setContent(e.getThreadGroup());
            dto.setReportId("send." + this.getName());
            dto.setToReport(this.getName());
            LoggerUtil.debug("send. " + this.getName());
            WebSocketUtils.sendMessageSingle(dto);
        } catch (Exception ex) {
            LoggerUtil.error("消息推送失败：" + ex.getMessage());
        }
    }

    @Override
    public void sampleStopped(SampleEvent e) {
    }

    @Override
    public void sampleOccurred(SampleEvent event) {
        SampleResult result = event.getResult();
        JMeterVariables variables = JMeterVars.get(result.hashCode());
        if (variables != null && CollectionUtils.isNotEmpty(variables.entrySet())) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                builder.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
            if (StringUtils.isNotEmpty(builder)) {
                result.setExtVars(builder.toString());
            }
        }
        if (isSampleWanted(result.isSuccessful()) && !StringUtils.equals(result.getSampleLabel(), RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME)) {
            RequestResult requestResult = JMeterBase.getRequestResult(result);
            if (requestResult != null) {
                if (StringUtils.isNotEmpty(requestResult.getName()) && requestResult.getName().startsWith("Transaction=") && CollectionUtils.isEmpty(requestResult.getSubRequestResults())) {
                    LoggerUtil.debug("进入合并事物，暂不处理");
                } else {
                    requestResult.getResponseResult().setConsole(CommonBeanFactory.getBean(MsResultService.class).getJmeterLogger(this.getName()));
                    MsgDto dto = new MsgDto();
                    dto.setExecEnd(false);
                    dto.setContent("result_" + JSON.toJSONString(requestResult));
                    dto.setReportId("send." + this.getName());
                    dto.setToReport(this.getName());
                    LoggerUtil.debug("send. " + this.getName());
                    WebSocketUtils.sendMessageSingle(dto);
                }
            }
        }
    }

    @Override
    public void clearData() {
    }
}
