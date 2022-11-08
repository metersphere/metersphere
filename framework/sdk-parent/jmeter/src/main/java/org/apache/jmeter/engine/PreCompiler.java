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

package org.apache.jmeter.engine;

import io.metersphere.utils.CustomizeFunctionUtil;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.visualizers.backend.Backend;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Class to replace function and variable references in the test tree.
 *
 */
public class PreCompiler implements HashTreeTraverser {
    private static final Logger log = LoggerFactory.getLogger(PreCompiler.class);

    private final ValueReplacer replacer;

    //   Used by both StandardJMeterEngine and ClientJMeterEngine.
//   In the latter case, only ResultCollectors are updated,
//   as only these are relevant to the client, and updating
//   other elements causes all sorts of problems.
    private final boolean isClientSide; // skip certain processing for remote tests

    private JMeterVariables clientSideVariables;

    public PreCompiler() {
        replacer = new ValueReplacer();
        isClientSide = false;
    }

    public PreCompiler(boolean remote) {
        replacer = new ValueReplacer();
        isClientSide = remote;
    }

    /** {@inheritDoc} */
    @Override
    public void addNode(Object node, HashTree subTree) {
        if(isClientSide) {
            if(node instanceof ResultCollector || node instanceof Backend) {
                try {
                    replacer.replaceValues((TestElement) node);
                } catch (InvalidVariableException e) {
                    log.error("invalid variables in node {}", ((TestElement)node).getName(), e);
                }
            }

            if (node instanceof TestPlan) {
                this.clientSideVariables = createVars((TestPlan)node);
            }

            if (node instanceof Arguments) {
                // Don't store User Defined Variables in the context for client side
                Map<String, String> args = createArgumentsMap((Arguments) node);
                clientSideVariables.putAll(args);
            }

        } else {
            if(node instanceof TestElement) {
                try {
                    replacer.replaceValues((TestElement) node);
                } catch (InvalidVariableException e) {
                    log.error("invalid variables in node {}", ((TestElement)node).getName(), e);
                }
            }

            if (node instanceof TestPlan) {
                JMeterVariables vars = createVars((TestPlan)node);
                JMeterContextService.getContext().setVariables(vars);
                // 加载自定义函数
                CustomizeFunctionUtil.initCustomizeClass((TestPlan) node);
            }

            if (node instanceof Arguments) {
                Map<String, String> args = createArgumentsMap((Arguments) node);
                JMeterContextService.getContext().getVariables().putAll(args);
            }
        }
    }

    /**
     * Create Map of Arguments
     * @param arguments {@link Arguments}
     * @return {@link Map}
     */
    private Map<String, String> createArgumentsMap(Arguments arguments) {
        arguments.setRunningVersion(true);
        Map<String, String> args = arguments.getArgumentsAsMap();
        replacer.addVariables(args);
        return args;
    }

    /**
     * Create variables for testPlan
     * @param testPlan {@link JMeterVariables}
     * @return {@link JMeterVariables}
     */
    private JMeterVariables createVars(TestPlan testPlan) {
        testPlan.prepareForPreCompile(); //A hack to make user-defined variables in the testplan element more dynamic
        Map<String, String> args = testPlan.getUserDefinedVariables();
        replacer.setUserDefinedVariables(args);
        JMeterVariables vars = new JMeterVariables();
        vars.putAll(args);
        return vars;
    }

    /** {@inheritDoc} */
    @Override
    public void subtractNode() {
    }

    /** {@inheritDoc} */
    @Override
    public void processPath() {
    }

    /**
     * @return the clientSideVariables
     */
    public JMeterVariables getClientSideVariables() {
        return clientSideVariables;
    }
}
