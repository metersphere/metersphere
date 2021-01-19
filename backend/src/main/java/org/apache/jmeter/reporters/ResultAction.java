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

package org.apache.jmeter.reporters;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.OnErrorTestElement;
import org.apache.jmeter.threads.JMeterContext.TestLogicalAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * ResultAction - take action based on the status of the last Result
 */
public class ResultAction extends OnErrorTestElement implements Serializable, SampleListener {

    private static final long serialVersionUID = 242L;

    private static final Logger log = LoggerFactory.getLogger(ResultAction.class);

    /**
     * Constructor is initially called once for each occurrence in the test plan
     * For GUI, several more instances are created Then clear is called at start
     * of test Called several times during test startup The name will not
     * necessarily have been set at this point.
     */
    public ResultAction() {
        super();
    }

    /**
     * Examine the sample(s) and take appropriate action
     *
     * @see SampleListener#sampleOccurred(SampleEvent)
     */
    @Override
    public void sampleOccurred(SampleEvent e) {
        SampleResult s = e.getResult();
        if (log.isDebugEnabled()) {
            log.debug("ResultStatusHandler {} for {} OK? {}", getName(), s.getSampleLabel(), s.isSuccessful());
        }
        if (!s.isSuccessful()) {
            if (isStopTestNow()) {
                s.setStopTestNow(true);
            } else if (isStopTest()) {
                s.setStopTest(true);
            } else if (isStopThread()) {
                s.setStopThread(true);
            } else if (isStartNextThreadLoop()) {
                s.setTestLogicalAction(TestLogicalAction.START_NEXT_ITERATION_OF_THREAD);
            } else if (isStartNextIterationOfCurrentLoop()) {
                s.setTestLogicalAction(TestLogicalAction.START_NEXT_ITERATION_OF_CURRENT_LOOP);
            } else if (isBreakCurrentLoop()) {
                s.setTestLogicalAction(TestLogicalAction.BREAK_CURRENT_LOOP);
            }
        } else {
            if (getErrorAction() == 1000) {
                s.setTestLogicalAction(TestLogicalAction.BREAK_CURRENT_LOOP);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sampleStarted(SampleEvent e) {
        // not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sampleStopped(SampleEvent e) {
        // not used
    }

}
