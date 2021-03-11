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

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is main class for JSONPath Assertion which verifies assertion on
 * previous sample result using JSON path expression
 *
 * @since 4.0
 */
public class JSONPathAssertion extends AbstractTestElement implements Serializable, Assertion, ThreadListener {
    private static final Logger log = LoggerFactory.getLogger(JSONPathAssertion.class);
    private static final long serialVersionUID = 2L;
    public static final String JSONPATH = "JSON_PATH";
    public static final String EXPECTEDVALUE = "EXPECTED_VALUE";
    public static final String JSONVALIDATION = "JSONVALIDATION";
    public static final String EXPECT_NULL = "EXPECT_NULL";
    public static final String INVERT = "INVERT";
    public static final String ISREGEX = "ISREGEX";

    private static ThreadLocal<DecimalFormat> decimalFormatter =
            ThreadLocal.withInitial(JSONPathAssertion::createDecimalFormat);

    public String getOption() {
        return getPropertyAsString("ASS_OPTION");
    }

    private static DecimalFormat createDecimalFormat() {
        DecimalFormat decimalFormatter = new DecimalFormat("#.#");
        decimalFormatter.setMaximumFractionDigits(340); // java.text.DecimalFormat.DOUBLE_FRACTION_DIGITS == 340
        decimalFormatter.setMinimumFractionDigits(1);
        return decimalFormatter;
    }

    public String getJsonPath() {
        return getPropertyAsString(JSONPATH);
    }

    public void setJsonPath(String jsonPath) {
        setProperty(JSONPATH, jsonPath);
    }

    public String getExpectedValue() {
        return getPropertyAsString(EXPECTEDVALUE);
    }

    public void setExpectedValue(String expectedValue) {
        setProperty(EXPECTEDVALUE, expectedValue);
    }

    public void setJsonValidationBool(boolean jsonValidation) {
        setProperty(JSONVALIDATION, jsonValidation);
    }

    public void setExpectNull(boolean val) {
        setProperty(EXPECT_NULL, val);
    }

    public boolean isExpectNull() {
        return getPropertyAsBoolean(EXPECT_NULL);
    }

    public boolean isJsonValidationBool() {
        return getPropertyAsBoolean(JSONVALIDATION);
    }

    public void setInvert(boolean invert) {
        setProperty(INVERT, invert);
    }

    public boolean isInvert() {
        return getPropertyAsBoolean(INVERT);
    }

    public void setIsRegex(boolean flag) {
        setProperty(ISREGEX, flag);
    }

    public boolean isUseRegex() {
        return getPropertyAsBoolean(ISREGEX, true);
    }

    private void doAssert(String jsonString) {
        Object value = JsonPath.read(jsonString, getJsonPath());

        if (!isJsonValidationBool()) {
            return;
        }

        if (value instanceof JSONArray) {
            if (arrayMatched((JSONArray) value)) {
                return;
            }
        } else {
            if ((isExpectNull() && value == null)
                    || isEquals(value)) {
                return;
            }
        }

        if (isExpectNull()) {
            throw new IllegalStateException(String.format("Value expected to be null, but found '%s'", value));
        } else {
            String msg;
            if (isUseRegex()) {
                msg = "Value expected to match regexp '%s', but it did not match: '%s'";
            } else {
                msg = "Value expected to be '%s', but found '%s'";
            }
            throw new IllegalStateException(String.format(msg, getExpectedValue(), objectToString(value)));
        }
    }

    private boolean arrayMatched(JSONArray value) {
        if (value.isEmpty() && "[]".equals(getExpectedValue())) {
            return true;
        }

        for (Object subj : value.toArray()) {
            if ((subj == null && isExpectNull())
                    || isEquals(subj)) {
                return true;
            }
        }

        return isEquals(value);
    }

    private boolean isEquals(Object subj) {
        String str = objectToString(subj);
        if (isUseRegex()) {
            Pattern pattern = JMeterUtils.getPatternCache().getPattern(getExpectedValue());
            return JMeterUtils.getMatcher().matches(str, pattern);
        } else {
            if (StringUtils.isNotEmpty(getOption())) {
                boolean refFlag = false;
                switch (getOption()) {
                    case "CONTAINS":
                        refFlag = str.contains(getExpectedValue());
                        break;
                    case "NOT_CONTAINS":
                        refFlag = !str.contains(getExpectedValue());
                        break;
                    case "EQUALS":
                        refFlag = str.equals(getExpectedValue());
                        break;
                    case "NOT_EQUALS":
                        refFlag = !str.contains(getExpectedValue());
                        break;
                }
                return refFlag;
            }
            return str.equals(getExpectedValue());
        }
    }

    @Override
    public AssertionResult getResult(SampleResult samplerResult) {
        AssertionResult result = new AssertionResult(getName());
        String responseData = samplerResult.getResponseDataAsString();
        if (responseData.isEmpty()) {
            return result.setResultForNull();
        }

        result.setFailure(false);
        result.setFailureMessage("");

        if (!isInvert()) {
            try {
                doAssert(responseData);
            } catch (Exception e) {
                log.debug("Assertion failed", e);
                result.setFailure(true);
                result.setFailureMessage(e.getMessage());
            }
        } else {
            try {
                doAssert(responseData);
                result.setFailure(true);
                if (isJsonValidationBool()) {
                    if (isExpectNull()) {
                        result.setFailureMessage("Failed that JSONPath " + getJsonPath() + " not matches null");
                    } else {
                        result.setFailureMessage("Failed that JSONPath " + getJsonPath() + " not matches " + getExpectedValue());
                    }
                } else {
                    result.setFailureMessage("Failed that JSONPath not exists: " + getJsonPath());
                }
            } catch (Exception e) {
                log.debug("Assertion failed, as expected", e);
            }
        }
        return result;
    }

    public static String objectToString(Object subj) {
        String str;
        if (subj == null) {
            str = "null";
        } else if (subj instanceof Map) {
            //noinspection unchecked
            str = new JSONObject((Map<String, Object>) subj).toJSONString();
        } else if (subj instanceof Double || subj instanceof Float) {
            str = decimalFormatter.get().format(subj);
        } else {
            str = subj.toString();
        }
        return str;
    }

    @Override
    public void threadStarted() {
        // nothing to do on thread start
    }

    @Override
    public void threadFinished() {
        decimalFormatter.remove();
    }
}
