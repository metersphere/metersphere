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

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import io.metersphere.api.dto.definition.request.assertions.document.Condition;
import io.metersphere.api.dto.definition.request.assertions.document.ElementCondition;
import net.minidev.json.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Checks if the result is a well-formed XML content using {@link XMLReader}
 */
public class XMLAssertion extends AbstractTestElement implements Serializable, Assertion, ThreadListener {
    private static final Logger log = LoggerFactory.getLogger(XMLAssertion.class);
    private static ThreadLocal<DecimalFormat> decimalFormatter = ThreadLocal.withInitial(XMLAssertion::createDecimalFormat);

    private static final long serialVersionUID = 242L;

    public String getXmlPath() {
        return this.getPropertyAsString("XML_PATH");
    }

    public String getExpectedValue() {
        return this.getPropertyAsString("EXPECTED_VALUE");
    }

    public String getCondition() {
        return getPropertyAsString("ElementCondition");
    }

    private static DecimalFormat createDecimalFormat() {
        DecimalFormat decimalFormatter = new DecimalFormat("#.#");
        decimalFormatter.setMaximumFractionDigits(340);
        decimalFormatter.setMinimumFractionDigits(1);
        return decimalFormatter;
    }

    // one builder for all requests in a thread
    private static final ThreadLocal<XMLReader> XML_READER = new ThreadLocal<XMLReader>() {
        @Override
        protected XMLReader initialValue() {
            try {
                XMLReader reader = SAXParserFactory.newInstance()
                        .newSAXParser()
                        .getXMLReader();
                reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                return reader;
            } catch (SAXException | ParserConfigurationException e) {
                log.error("Error initializing XMLReader in XMLAssertion", e);
                return null;
            }
        }
    };

    /**
     * Returns the result of the Assertion.
     * Here it checks whether the Sample data is XML.
     * If so an AssertionResult containing a FailureMessage will be returned.
     * Otherwise the returned AssertionResult will reflect the success of the Sample.
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        // no error as default
        AssertionResult result = new AssertionResult(getName());
        String resultData = response.getResponseDataAsString();
        if (resultData.length() == 0) {
            return result.setResultForNull();
        }
        result.setFailure(false);
        XMLReader builder = XML_READER.get();
        if (builder != null) {
            try {
                builder.setErrorHandler(new LogErrorHandler());
                builder.parse(new InputSource(new StringReader(resultData)));
                try {
                    JSONObject xmlJSONObj = XML.toJSONObject(resultData);
                    String jsonPrettyPrintString = xmlJSONObj.toString(4);
                    doAssert(jsonPrettyPrintString);
                } catch (Exception e) {
                    result.setError(true);
                    result.setFailure(true);
                    result.setFailureMessage(e.getMessage());
                }
            } catch (SAXException | IOException e) {
                result.setError(true);
                result.setFailure(true);
                result.setFailureMessage(e.getMessage());
            }
        } else {
            result.setError(true);
            result.setFailureMessage("Cannot initialize XMLReader in element:" + getName() + ", check jmeter.log file");
        }
        return result;
    }


    private void doAssert(String jsonString) {
        Object value = JsonPath.read(jsonString, this.getXmlPath(), new Predicate[0]);
        if (value instanceof JSONArray) {
            if (this.arrayMatched((JSONArray) value)) {
                return;
            }
        }
        if (!this.isEquals(value)) {
            String msg = (StringUtils.isNotEmpty(this.getName()) ? this.getName().split("==")[1] : "") + "校验失败，返回数据：" + (value != null ? value.toString() : "");
            throw new IllegalStateException(String.format(msg, this.getExpectedValue(), objectToString(value)));
        }
    }


    private boolean isEquals(Object subj) {
        String str = objectToString(subj);
        return isDocument(str);
    }

    private boolean arrayMatched(JSONArray value) {
        if (value.isEmpty() && "[]".equals(this.getExpectedValue())) {
            return true;
        } else {
            Object[] var2 = value.toArray();
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Object subj = var2[var4];
                if (subj == null || this.isEquals(subj)) {
                    return true;
                }
            }

            return this.isEquals(value);
        }
    }

    private String ifValue(Object value) {
        if (value != null) {
            return value.toString();
        }
        return "";
    }

    private boolean isDocument(String resValue) {
        String condition = this.getCondition();
        if (StringUtils.isNotEmpty(condition)) {
            ElementCondition elementCondition = JSON.parseObject(condition, ElementCondition.class);
            boolean isTrue = true;
            if (CollectionUtils.isNotEmpty(elementCondition.getConditions())) {
                for (Condition item : elementCondition.getConditions()) {
                    String expectedValue = ifValue(item.getValue());
                    switch (item.getKey()) {
                        case "value_eq":
                            isTrue = StringUtils.equals(resValue, expectedValue);
                            break;
                        case "value_not_eq":
                            isTrue = !StringUtils.equals(resValue, expectedValue);
                            break;
                        case "value_in":
                            isTrue = StringUtils.contains(resValue, expectedValue);
                            break;
                        case "length_eq":
                            isTrue = (StringUtils.isNotEmpty(resValue) && StringUtils.isNotEmpty(expectedValue) && resValue.length() == expectedValue.length())
                                    || (StringUtils.isEmpty(resValue) && StringUtils.isEmpty(expectedValue));
                            break;
                        case "length_not_eq":
                            isTrue = (StringUtils.isNotEmpty(resValue) && StringUtils.isNotEmpty(expectedValue) && resValue.length() != expectedValue.length())
                                    || (StringUtils.isEmpty(resValue) || StringUtils.isEmpty(expectedValue));
                            break;
                        case "length_gt":
                            isTrue = (StringUtils.isNotEmpty(resValue) && StringUtils.isNotEmpty(expectedValue) && resValue.length() > expectedValue.length())
                                    || (StringUtils.isNotEmpty(resValue) && StringUtils.isEmpty(expectedValue));
                            break;
                        case "length_lt":
                            isTrue = (StringUtils.isNotEmpty(resValue) && StringUtils.isNotEmpty(expectedValue) && resValue.length() < expectedValue.length())
                                    || (StringUtils.isEmpty(resValue) || StringUtils.isEmpty(expectedValue));
                            break;
                        case "regular":
                            Pattern pattern = JMeterUtils.getPatternCache().getPattern(this.getExpectedValue());
                            isTrue = JMeterUtils.getMatcher().matches(resValue, pattern);
                            break;
                    }
                    if (!isTrue) {
                        break;
                    }
                }
            }
            return isTrue;
        }
        return true;
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

    @Override
    public void threadStarted() {
        // nothing to do on thread start
    }

    @Override
    public void threadFinished() {
        XML_READER.remove();
    }
}
