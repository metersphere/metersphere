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

package org.apache.jmeter.assertions;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.utils.DocumentUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private static final boolean USE_JAVA_REGEX = !JMeterUtils.getPropDefault(
            "jmeter.regex.engine", "oro").equalsIgnoreCase("oro");

    private static ThreadLocal<DecimalFormat> decimalFormatter =
            ThreadLocal.withInitial(JSONPathAssertion::createDecimalFormat);

    private static DecimalFormat createDecimalFormat() {
        DecimalFormat decimalFormatter = new DecimalFormat("#.#");
        decimalFormatter.setMaximumFractionDigits(340); // java.text.DecimalFormat.DOUBLE_FRACTION_DIGITS == 340
        decimalFormatter.setMinimumFractionDigits(1);
        return decimalFormatter;
    }

    public String getOption() {
        return getPropertyAsString("ASS_OPTION");
    }

    public String getElementCondition() {
        return getPropertyAsString("ElementCondition");
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
    private static final String KEY_PRE = "[]";

    private void doAssert(String jsonString) {
        Object value = JsonPath.read(jsonString, getJsonPath());

        if (!isJsonValidationBool()) {
            if (value instanceof JSONArray) {
                JSONArray arrayValue = (JSONArray) value;
                if (arrayValue.isEmpty() && !JsonPath.isPathDefinite(getJsonPath())) {
                    throw new IllegalStateException("JSONPath is indefinite and the extracted Value is an empty Array." +
                            " Please use an assertion value, to be sure to get a correct result. " + getExpectedValue());
                }
            }
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

        if (this.isExpectNull()) {
            throw new IllegalStateException(String.format("Value expected to be null, but found '%s'", value));
        } else {
            String msg = "";
            if (this.isUseRegex()) {
                msg = "Value expected to match regexp '%s', but it did not match: '%s'";
            } else if (StringUtils.isNotEmpty(getOption()) && !this.isEquals(value)) {
                switch (getOption()) {
                    case "CONTAINS":
                        msg = "Value contains to be '%s', but found '%s'";
                        break;
                    case "NOT_CONTAINS":
                        msg = "Value not contains to be '%s', but found '%s'";
                        break;
                    case "EQUALS":
                        msg = "Value equals to be '%s', but found '%s'";
                        break;
                    case "NOT_EQUALS":
                        msg = "Value not equals to be '%s', but found '%s'";
                        break;
                    case "GT":
                        msg = "Value > '%s', but found '%s'";
                        break;
                    case "LT":
                        msg = "Value < '%s', but found '%s'";
                        break;
                    case "DOCUMENT":
                        msg = DocumentUtils.documentMsg(this.getName(), value, this.getElementCondition());
                        break;
                }
            } else {
                msg = "Value expected to be '%s', but found '%s'";
            }
            throw new IllegalStateException(String.format(msg, this.getExpectedValue(), DocumentUtils.objectToString(value, decimalFormatter)));
        }
    }


    private boolean arrayMatched(JSONArray value) {

        List<Boolean> result = new ArrayList<>();
        for (Object subj : value.toArray()) {
            if (!StringUtils.equalsAnyIgnoreCase(getOption(), "NOT_CONTAINS","EQUALS")) {
                if (subj == null && this.isExpectNull() || isEquals(subj)) {
                    return true;
                }
            } else {
                result.add(isEquals(subj));
            }
        }
        if (CollectionUtils.isNotEmpty(result) && StringUtils.equalsAnyIgnoreCase(getOption(), "NOT_CONTAINS", "EQUALS")) {
            if (result.stream().filter(item -> item == true).collect(Collectors.toList()).size() == result.size()) {
                return true;
            } else {
                return false;
            }
        }

        return isEquals(value);
    }

    private boolean isGt(String v1, String v2) {
        try {
            BigDecimal value1 = new BigDecimal(v1);
            BigDecimal value2 = new BigDecimal(v2);
            return value1.compareTo(value2) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isLt(String v1, String v2) {
        try {
            BigDecimal value1 = new BigDecimal(v1);
            BigDecimal value2 = new BigDecimal(v2);
            return value1.compareTo(value2) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isEquals(Object subj) {
        String str = DocumentUtils.objectToString(subj, decimalFormatter);
        if (StringUtils.equals(str,KEY_PRE)) {
            return false;
        }
        if (isUseRegex()) {
            if (USE_JAVA_REGEX) {
                return JMeterUtils.compilePattern(getExpectedValue()).matcher(str).matches();
            } else {
                Pattern pattern = JMeterUtils.getPatternCache().getPattern(getExpectedValue());
                return JMeterUtils.getMatcher().matches(str, pattern);
            }
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
                        refFlag = valueEquals(str, getExpectedValue());
                        break;
                    case "NOT_EQUALS":
                        refFlag = valueNotEquals(str, getExpectedValue());
                        break;
                    case "GT":
                        refFlag = isGt(str, getExpectedValue());
                        break;
                    case "LT":
                        refFlag = isLt(str, getExpectedValue());
                        break;
                    case "DOCUMENT":
                        refFlag = DocumentUtils.documentChecked(subj, this.getElementCondition(), decimalFormatter);
                        break;
                }
                return refFlag;
            }
            Object expected = JSONValue.parse(getExpectedValue());
            return Objects.equals(expected, subj);
        }
    }

    private static boolean valueEquals(String v1, String v2) {
        try {
            Number number1 = NumberUtils.createBigDecimal(v1);
            Number number2 = NumberUtils.createBigDecimal(v2);
            log.info("number1:    " + number1 + " number2:    " + number2);
            log.info("number1.equals(number2):    " + number1.equals(number2));
            return number1.equals(number2);
        } catch (Exception e) {
            return StringUtils.equals(v1, v2);
        }
    }

    private static boolean valueNotEquals(String v1, String v2) {
        try {
            Number number1 = NumberUtils.createBigDecimal(v1);
            Number number2 = NumberUtils.createBigDecimal(v2);
            return !number1.equals(number2);
        } catch (Exception e) {
            return !StringUtils.equals(v1, v2);
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
            str = new JSONObject((Map<String, ?>) subj).toJSONString();
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
