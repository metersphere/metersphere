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

package org.apache.jmeter.engine.util;

import io.metersphere.utils.CustomizeFunctionUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * CompoundFunction.
 */
public class CompoundVariable implements Function {
    private static final Logger log = LoggerFactory.getLogger(CompoundVariable.class);

    private String rawParameters;

    private static final FunctionParser functionParser = new FunctionParser();

    // Created during class init; not modified thereafter
    private static final Map<String, Class<? extends Function>> functions = new HashMap<>();

    private boolean hasFunction;
    private boolean isDynamic;

    private String permanentResults;

    // Type is ArrayList, so we can use ArrayList#clone
    private ArrayList<Object> compiledComponents = new ArrayList<>();

    static {
        try {
            final String contain = // Classnames must contain this string [.functions.]
                    JMeterUtils.getProperty("classfinder.functions.contain"); // $NON-NLS-1$
            final String notContain = // Classnames must not contain this string [.gui.]
                    JMeterUtils.getProperty("classfinder.functions.notContain"); // $NON-NLS-1$
            if (contain != null) {
                log.info("Note: Function class names must contain the string: '{}'", contain);
            }
            if (notContain != null) {
                log.info("Note: Function class names must not contain the string: '{}'", notContain);
            }

            List<String> classes = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(),
                    new Class[]{Function.class}, true, contain, notContain);
            for (String clazzName : classes) {
                Function tempFunc = Class.forName(clazzName)
                        .asSubclass(Function.class)
                        .getDeclaredConstructor().newInstance();
                String referenceKey = tempFunc.getReferenceKey();
                if (referenceKey.length() > 0) { // ignore self
                    functions.put(referenceKey, tempFunc.getClass());
                }
            }

            if (functions.isEmpty()) {
                log.warn("Did not find any functions");
            } else {
                log.debug("Function count: {}", functions.size());
            }
        } catch (Exception err) {
            log.error("Exception occurred in static initialization of CompoundVariable.", err);
        }
    }

    public CompoundVariable() {
        hasFunction = false;
    }

    public CompoundVariable(String parameters) {
        this();
        try {
            setParameters(parameters);
        } catch (InvalidVariableException e) {
            // TODO should level be more than debug ?
            log.debug("Invalid variable: {}", parameters, e);
        }
    }

    public String execute() {
        if (isDynamic || permanentResults == null) {
            JMeterContext context = JMeterContextService.getContext();
            SampleResult previousResult = context.getPreviousResult();
            Sampler currentSampler = context.getCurrentSampler();
            return execute(previousResult, currentSampler);
        }
        return permanentResults; // $NON-NLS-1$
    }

    /**
     * Allows the retrieval of the original String prior to it being compiled.
     *
     * @return String
     */
    public String getRawParameters() {
        return rawParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) {
        if (compiledComponents == null || compiledComponents.isEmpty()) {
            return ""; // $NON-NLS-1$
        }

        StringBuilder results = new StringBuilder();
        for (Object item : compiledComponents) {
            if (item instanceof Function) {
                try {
                    results.append(((Function) item).execute(previousResult, currentSampler));
                } catch (InvalidVariableException e) {
                    // TODO should level be more than debug ?
                    log.debug("Invalid variable: {}", item, e);
                }
            } else if (item instanceof SimpleVariable) {
                results.append(((SimpleVariable) item).toString());
            } else {
                results.append(item);
            }
        }
        if (!isDynamic) {
            permanentResults = results.toString();
        }
        return results.toString();
    }

    @SuppressWarnings("unchecked") // clone will produce correct type
    public CompoundVariable getFunction() {
        CompoundVariable func = new CompoundVariable();
        func.compiledComponents = (ArrayList<Object>) compiledComponents.clone();
        func.rawParameters = rawParameters;
        func.hasFunction = hasFunction;
        func.isDynamic = isDynamic;
        return func;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getArgumentDesc() {
        return new ArrayList<>();
    }

    public void clear() {
        // TODO should this also clear isDynamic, rawParameters, permanentResults?
        hasFunction = false;
        compiledComponents.clear();
    }

    public void setParameters(String parameters) throws InvalidVariableException {
        this.rawParameters = parameters;
        if (parameters == null || parameters.length() == 0) {
            return;
        }

        compiledComponents = functionParser.compileString(parameters);
        if (compiledComponents.size() > 1 || !(compiledComponents.get(0) instanceof String)) {
            hasFunction = true;
        }
        permanentResults = null; // To be calculated and cached on first execution
        isDynamic = false;
        for (Object item : compiledComponents) {
            if (item instanceof Function || item instanceof SimpleVariable) {
                isDynamic = true;
                break;
            }
        }
    }

    static Object getNamedFunction(String functionName) throws InvalidVariableException {
        if (functions.containsKey(functionName)) {
            try {
                return functions.get(functionName).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("Exception occurred while instantiating a function: {}", functionName, e); // $NON-NLS-1$
                throw new InvalidVariableException(e);
            }
        }
        Map<String, Class<? extends Function>> customizeFunctions = CustomizeFunctionUtil.getFunctions();
        if (MapUtils.isNotEmpty(customizeFunctions) && customizeFunctions.containsKey(functionName)) {
            try {
                log.info("ms custom function handling", functionName);
                return CustomizeFunctionUtil.getFunctions().get(functionName).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("Exception occurred while instantiating a function: {}", functionName, e); // $NON-NLS-1$
                throw new InvalidVariableException(e);
            }
        }
        return new SimpleVariable(functionName);
    }

    // For use by FunctionHelper
    public static Class<? extends Function> getFunctionClass(String className) {
        return functions.get(className);
    }

    // For use by FunctionHelper
    public static String[] getFunctionNames() {
        return functions.keySet().toArray(new String[functions.size()]);
    }

    public boolean hasFunction() {
        return hasFunction;
    }

    // Dummy methods needed by Function interface

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReferenceKey() {
        return ""; // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
    }
}
