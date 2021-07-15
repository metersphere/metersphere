//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.jmeter.assertions;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;

public class JSONPathAssertion extends AbstractTestElement implements Serializable, Assertion, ThreadListener {
    private static final Logger log = LoggerFactory.getLogger(JSONPathAssertion.class);
    private static final long serialVersionUID = 2L;
    public static final String JSONPATH = "JSON_PATH";
    public static final String EXPECTEDVALUE = "EXPECTED_VALUE";
    public static final String JSONVALIDATION = "JSONVALIDATION";
    public static final String EXPECT_NULL = "EXPECT_NULL";
    public static final String INVERT = "INVERT";
    public static final String ISREGEX = "ISREGEX";
    private static ThreadLocal<DecimalFormat> decimalFormatter = ThreadLocal.withInitial(JSONPathAssertion::createDecimalFormat);

    public JSONPathAssertion() {
    }

    private static DecimalFormat createDecimalFormat() {
        DecimalFormat decimalFormatter = new DecimalFormat("#.#");
        decimalFormatter.setMaximumFractionDigits(340);
        decimalFormatter.setMinimumFractionDigits(1);
        return decimalFormatter;
    }

    public String getOption() {
        return getPropertyAsString("ASS_OPTION");
    }

    public String getJsonPath() {
        return this.getPropertyAsString("JSON_PATH");
    }

    public void setJsonPath(String jsonPath) {
        this.setProperty("JSON_PATH", jsonPath);
    }

    public String getExpectedValue() {
        return this.getPropertyAsString("EXPECTED_VALUE");
    }

    public void setExpectedValue(String expectedValue) {
        this.setProperty("EXPECTED_VALUE", expectedValue);
    }

    public void setJsonValidationBool(boolean jsonValidation) {
        this.setProperty("JSONVALIDATION", jsonValidation);
    }

    public void setExpectNull(boolean val) {
        this.setProperty("EXPECT_NULL", val);
    }

    public boolean isExpectNull() {
        return this.getPropertyAsBoolean("EXPECT_NULL");
    }

    public boolean isJsonValidationBool() {
        return this.getPropertyAsBoolean("JSONVALIDATION");
    }

    public void setInvert(boolean invert) {
        this.setProperty("INVERT", invert);
    }

    public boolean isInvert() {
        return this.getPropertyAsBoolean("INVERT");
    }

    public void setIsRegex(boolean flag) {
        this.setProperty("ISREGEX", flag);
    }

    public boolean isUseRegex() {
        return this.getPropertyAsBoolean("ISREGEX", true);
    }

    private void doAssert(String jsonString) {
        Object value = JsonPath.read(jsonString, this.getJsonPath(), new Predicate[0]);
        if (this.isJsonValidationBool()) {
            if (value instanceof JSONArray) {
                if (this.arrayMatched((JSONArray) value)) {
                    return;
                }
            } else if (this.isExpectNull() && value == null || this.isEquals(value)) {
                return;
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
                    }
                } else {
                    msg = "Value expected to be '%s', but found '%s'";
                }
                throw new IllegalStateException(String.format(msg, this.getExpectedValue(), objectToString(value)));
            }
        }
    }

    private boolean arrayMatched(JSONArray value) {
        if (value.isEmpty() && "[]".equals(this.getExpectedValue())) {
            return true;
        } else {
            Object[] var2 = value.toArray();
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Object subj = var2[var4];
                if (subj == null && this.isExpectNull() || this.isEquals(subj)) {
                    return true;
                }
            }

            return this.isEquals(value);
        }
    }

    private boolean isEquals(Object subj) {
        String str = objectToString(subj);
        if (this.isUseRegex()) {
            Pattern pattern = JMeterUtils.getPatternCache().getPattern(this.getExpectedValue());
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
                        refFlag = !str.equals(getExpectedValue());
                        break;
                }
                return refFlag;
            }
            return str.equals(this.getExpectedValue());
        }
    }

    public AssertionResult getResult(SampleResult samplerResult) {
        AssertionResult result = new AssertionResult(this.getName());
        String responseData = samplerResult.getResponseDataAsString();
        if (responseData.isEmpty()) {
            return result.setResultForNull();
        } else {
            result.setFailure(false);
            result.setFailureMessage("");
            if (!this.isInvert()) {
                try {
                    this.doAssert(responseData);
                } catch (Exception var6) {
                    log.debug("Assertion failed", var6);
                    result.setFailure(true);
                    result.setFailureMessage(var6.getMessage());
                }
            } else {
                try {
                    this.doAssert(responseData);
                    result.setFailure(true);
                    if (this.isJsonValidationBool()) {
                        if (this.isExpectNull()) {
                            result.setFailureMessage("Failed that JSONPath " + this.getJsonPath() + " not matches null");
                        } else {
                            result.setFailureMessage("Failed that JSONPath " + this.getJsonPath() + " not matches " + this.getExpectedValue());
                        }
                    } else {
                        result.setFailureMessage("Failed that JSONPath not exists: " + this.getJsonPath());
                    }
                } catch (Exception var5) {
                    log.debug("Assertion failed, as expected", var5);
                }
            }

            return result;
        }
    }

    public static String objectToString(Object subj) {
        String str;
        if (subj == null) {
            str = "null";
        } else if (subj instanceof Map) {
            str = new Gson().toJson(subj);
        } else if (!(subj instanceof Double) && !(subj instanceof Float)) {
            str = subj.toString();
        } else {
            str = ((DecimalFormat) decimalFormatter.get()).format(subj);
        }

        return str;
    }

    public void threadStarted() {
    }

    public void threadFinished() {
        decimalFormatter.remove();
    }
}
