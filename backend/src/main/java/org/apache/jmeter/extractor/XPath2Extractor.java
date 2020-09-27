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

package org.apache.jmeter.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.XPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Extracts text from (X)HTML response using XPath query language
 * Example XPath queries:
 * <dl>
 * <dt>/html/head/title</dt>
 * <dd>extracts Title from HTML response</dd>
 * <dt>//form[@name='countryForm']//select[@name='country']/option[text()='Czech Republic'])/@value
 * <dd>extracts value attribute of option element that match text 'Czech Republic'
 * inside of select element with name attribute  'country' inside of
 * form with name attribute 'countryForm'</dd>
 * <dt>//head</dt>
 * <dd>extracts the XML fragment for head node.</dd>
 * <dt>//head/text()</dt>
 * <dd>extracts the text content for head node.</dd>
 * </dl>
 * see org.apache.jmeter.extractor.TestXPathExtractor for unit tests
 */
public class XPath2Extractor
        extends AbstractScopedTestElement
        implements PostProcessor, Serializable {

    private static final Logger log = LoggerFactory.getLogger(XPath2Extractor.class);

    private static final long serialVersionUID = 242L;

    private static final int DEFAULT_VALUE = 0;
    public static final String DEFAULT_VALUE_AS_STRING = Integer.toString(DEFAULT_VALUE);

    private static final String REF_MATCH_NR = "matchNr"; // $NON-NLS-1$

    //+ JMX file attributes
    private static final String XPATH_QUERY = "XPathExtractor2.xpathQuery"; // $NON-NLS-1$
    private static final String REFNAME = "XPathExtractor2.refname"; // $NON-NLS-1$
    private static final String DEFAULT = "XPathExtractor2.default"; // $NON-NLS-1$
    private static final String FRAGMENT = "XPathExtractor2.fragment"; // $NON-NLS-1$
    private static final String NAMESPACES = "XPathExtractor2.namespaces"; // $NON-NLS-1$
    private static final String MATCH_NUMBER = "XPathExtractor2.matchNumber"; // $NON-NLS-1$
    private JMeterVariables regexVars;

    //- JMX file attributes

    private String concat(String s1, String s2) {
        return s1 + "_" + s2; // $NON-NLS-1$
    }

    private String concat(String s1, int i) {
        return s1 + "_" + i; // $NON-NLS-1$
    }

    /**
     * Do the job - extract value from (X)HTML response using XPath Query.
     * Return value as variable defined by REFNAME. Returns DEFAULT value
     * if not found.
     */
    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        final SampleResult previousResult = context.getPreviousResult();
        if (previousResult == null) {
            return;
        }
        JMeterVariables vars = context.getVariables();
        String refName = getRefName();
        vars.put(refName, getDefaultValue());
        final String matchNR = concat(refName, REF_MATCH_NR);
        int prevCount = 0; // number of previous matches
        try {
            prevCount = Integer.parseInt(vars.get(matchNR));
        } catch (NumberFormatException e) {
            // ignored
        }

        vars.put(matchNR, "0"); // In case parse fails // $NON-NLS-1$
        vars.remove(concat(refName, "1")); // In case parse fails // $NON-NLS-1$

        int matchNumber = getMatchNumber();
        List<String> matches = new ArrayList<>();
        try {
            if (isScopeVariable()) {
                String inputString = vars.get(getVariableName());
                if (inputString != null) {
                    if (inputString.length() > 0) {
                        getValuesForXPath(getXPathQuery(), matches, matchNumber, inputString);
                    }
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("No variable '{}' found to process by XPathExtractor '{}', skipping processing",
                                getVariableName(), getName());
                    }
                }
            } else {
                List<SampleResult> samples = getSampleList(previousResult);
                int size = samples.size();
                for (int i = 0; i < size; i++) {
                    getValuesForXPath(getXPathQuery(), matches, matchNumber, previousResult.getResponseDataAsString());
                }
            }
            final int matchCount = matches.size();
            vars.put(matchNR, String.valueOf(matchCount));
            if (matchCount > 0) {
                String value = matches.get(0);
                if (value != null) {
                    vars.put(refName, value);
                }
                for (int i = 0; i < matchCount; i++) {
                    value = matches.get(i);
                    if (value != null) {
                        vars.put(concat(refName, i + 1), matches.get(i));
                    }
                }
            }
            vars.remove(concat(refName, matchCount + 1)); // Just in case
            // Clear any other remaining variables
            for (int i = matchCount + 2; i <= prevCount; i++) {
                vars.remove(concat(refName, i));
            }
            previousResult.addVars(refName,vars.get(refName));
        } catch (Exception e) {// Saxon exception
            if (log.isWarnEnabled()) {
                log.warn("Exception while processing '{}', message:{}", getXPathQuery(), e.getMessage());
            }
            addAssertionFailure(previousResult, e, false);
        }
    }

    private void addAssertionFailure(final SampleResult previousResult,
                                     final Throwable thrown, final boolean setFailed) {
        AssertionResult ass = new AssertionResult(getName()); // $NON-NLS-1$
        ass.setFailure(true);
        ass.setFailureMessage(thrown.getLocalizedMessage() + "\nSee log file for further details.");
        previousResult.addAssertionResult(ass);
        if (setFailed) {
            previousResult.setSuccessful(false);
        }
    }

    /*============= object properties ================*/
    public void setXPathQuery(String val) {
        setProperty(XPATH_QUERY, val);
    }

    public String getXPathQuery() {
        return getPropertyAsString(XPATH_QUERY);
    }

    public void setRefName(String refName) {
        setProperty(REFNAME, refName);
    }

    public String getRefName() {
        return getPropertyAsString(REFNAME);
    }


    public void setDefaultValue(String val) {
        setProperty(DEFAULT, val);
    }

    public String getDefaultValue() {
        return getPropertyAsString(DEFAULT);
    }

    /**
     * Should we return fragment as text, rather than text of fragment?
     *
     * @return true if we should return fragment rather than text
     */
    public boolean getFragment() {
        return getPropertyAsBoolean(FRAGMENT, false);
    }

    /**
     * Should we return fragment as text, rather than text of fragment?
     *
     * @param selected true to return fragment.
     */
    public void setFragment(boolean selected) {
        setProperty(FRAGMENT, selected, false);
    }

    /*================= internal business =================*/

    /**
     * Extract value from String responseData by XPath query.
     *
     * @param query        the query to execute
     * @param matchStrings list of matched strings (may include nulls)
     * @param matchNumber  int Match Number
     * @param responseData String that contains the entire Document
     * @throws SaxonApiException
     * @throws FactoryConfigurationError
     */
    private void getValuesForXPath(String query, List<String> matchStrings, int matchNumber, String responseData)
            throws SaxonApiException, FactoryConfigurationError {
        XPathUtil.putValuesForXPathInListUsingSaxon(responseData, query, matchStrings, getFragment(), matchNumber, getNamespaces());
    }

    /**
     * Set which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @param matchNumber The number of the match to be used
     */
    public void setMatchNumber(int matchNumber) {
        setProperty(new IntegerProperty(MATCH_NUMBER, matchNumber));
    }

    /**
     * Set which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @param matchNumber The number of the match to be used
     */
    public void setMatchNumber(String matchNumber) {
        setProperty(MATCH_NUMBER, matchNumber);
    }

    /**
     * Return which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @return matchNumber The number of the match to be used
     */
    public int getMatchNumber() {
        return getPropertyAsInt(MATCH_NUMBER, DEFAULT_VALUE);
    }

    /**
     * Return which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @return matchNumber The number of the match to be used
     */
    public String getMatchNumberAsString() {
        return getPropertyAsString(MATCH_NUMBER, DEFAULT_VALUE_AS_STRING);
    }

    public void setNamespaces(String namespaces) {
        setProperty(NAMESPACES, namespaces);
    }

    public String getNamespaces() {
        return getPropertyAsString(NAMESPACES);
    }
}