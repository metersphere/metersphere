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
import com.jayway.jsonpath.Predicate;
import io.metersphere.utils.DocumentUtils;
import net.minidev.json.JSONArray;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
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

/**
 * Checks if the result is a well-formed XML content using {@link XMLReader}
 *
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
            String msg = DocumentUtils.documentMsg(this.getName(), value, this.getCondition(), decimalFormatter);
            throw new IllegalStateException(String.format(msg, this.getExpectedValue(), DocumentUtils.objectToString(value, decimalFormatter)));
        }
    }


    private boolean isEquals(Object subj) {
        String str = DocumentUtils.objectToString(subj, decimalFormatter);
        return DocumentUtils.documentChecked(str, this.getCondition(), decimalFormatter);
    }

    private boolean arrayMatched(JSONArray value) {
        if (value.isEmpty() && "[]".equals(getExpectedValue())) {
            return true;
        }

        for (Object subj : value.toArray()) {
            if (subj == null || isEquals(subj)) {
                return true;
            }
        }

        return isEquals(value);
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
