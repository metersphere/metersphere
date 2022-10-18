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

import io.metersphere.api.dto.MsgDTO;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.RequestResult;
import io.metersphere.jmeter.JMeterBase;
import io.metersphere.utils.JMeterVars;
import io.metersphere.utils.LoggerUtil;
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
public class MsDebugListener extends AbstractListenerElement implements SampleListener, Clearable, Serializable,
        TestStateListener, Remoteable, NoThreadClone {

    private static final String ERROR_LOGGING = "MsResultCollector.error_logging"; // $NON-NLS-1$

    private static final String SUCCESS_ONLY_LOGGING = "MsResultCollector.success_only_logging"; // $NON-NLS-1$

    private static final String TEST_IS_LOCAL = "*local*"; // $NON-NLS-1$

    public static final String TEST_END = "MS_TEST_END";

    @Override
    public Object clone() {
        MsDebugListener clone = (MsDebugListener) super.clone();
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

    public boolean isSampleWanted(boolean success, SampleResult result) {
        boolean errorOnly = isErrorLogging();
        boolean successOnly = isSuccessOnlyLogging();
        return isSampleWanted(success, errorOnly, successOnly) && !StringUtils.containsIgnoreCase(result.getSampleLabel(), "MS_CLEAR_LOOPS_VAR_");
    }

    public static boolean isSampleWanted(boolean success, boolean errorOnly,
                                         boolean successOnly) {
        return (!errorOnly && !successOnly) ||
                (success && successOnly) ||
                (!success && errorOnly);
    }

    @Override
    public void testEnded(String host) {
        LoggerUtil.info("Debug TestEnded " + this.getName());
        MsgDTO dto = new MsgDTO();
        dto.setExecEnd(false);
        dto.setContent(TEST_END);
        dto.setReportId("send." + this.getName());
        dto.setToReport(this.getName());
        LoggerUtil.debug("send. " + this.getName());
        WebSocketUtil.sendMessageSingle(dto);
        WebSocketUtil.onClose(this.getName());
        PoolExecBlockingQueueUtil.offer(this.getName());
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
            MsgDTO dto = new MsgDTO();
            dto.setContent(e.getThreadGroup());
            dto.setReportId("send." + this.getName());
            dto.setToReport(this.getName());
            LoggerUtil.debug("send. " + this.getName());
            WebSocketUtil.sendMessageSingle(dto);
        } catch (Exception ex) {
            LoggerUtil.error("消息推送失败：", ex);
        }
    }

    @Override
    public void sampleStopped(SampleEvent e) {
    }

    @Override
    public void sampleOccurred(SampleEvent event) {
        SampleResult result = event.getResult();
        this.setVars(result);
        if (isSampleWanted(result.isSuccessful(), result) && !StringUtils.equals(result.getSampleLabel(), RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME)) {
            RequestResult requestResult = JMeterBase.getRequestResult(result);
            if (requestResult != null && ResultParseUtil.isNotAutoGenerateSampler(requestResult)) {
                MsgDTO dto = new MsgDTO();
                dto.setExecEnd(false);
                dto.setReportId("send." + this.getName());
                dto.setToReport(this.getName());

                String console = FixedCapacityUtil.getJmeterLogger(this.getName(), false);
                if (StringUtils.isNotEmpty(requestResult.getName()) && requestResult.getName().startsWith("Transaction=")) {
                    requestResult.getSubRequestResults().forEach(transactionResult -> {
                        transactionResult.getResponseResult().setConsole(console);
                        //对响应内容进行进一步解析和处理。
                        RequestResultExpandDTO expandDTO = ResponseUtil.parseByRequestResult(transactionResult);
                        dto.setContent("result_" + JSON.toJSONString(expandDTO));
                        WebSocketUtil.sendMessageSingle(dto);
                    });
                } else {
                    requestResult.getResponseResult().setConsole(console);
                    //对响应内容进行进一步解析和处理。
                    RequestResultExpandDTO expandDTO = ResponseUtil.parseByRequestResult(requestResult);
                    dto.setContent("result_" + JSON.toJSONString(expandDTO));
                    WebSocketUtil.sendMessageSingle(dto);
                }
                LoggerUtil.debug("send. " + this.getName());
            }
        }
    }

    private void setVars(SampleResult result) {
        if (StringUtils.isNotEmpty(result.getSampleLabel()) && result.getSampleLabel().startsWith("Transaction=")) {
            for (int i = 0; i < result.getSubResults().length; i++) {
                SampleResult subResult = result.getSubResults()[i];
                this.setVars(subResult);
            }
        }
        JMeterVariables variables = JMeterVars.get(result.getResourceId());
        if (variables != null && CollectionUtils.isNotEmpty(variables.entrySet())) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                builder.append(entry.getKey()).append("：").append(entry.getValue()).append(StringUtils.LF);
            }
            if (StringUtils.isNotEmpty(builder)) {
                result.setExtVars(builder.toString());
            }
        }
    }

    @Override
    public void clearData() {
    }
}
