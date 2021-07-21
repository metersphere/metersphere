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

package org.apache.jmeter.samplers;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContext.TestLogicalAction;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// For unit tests, @see TestSampleResult

/**
 * This is a nice packaging for the various information returned from taking a
 * sample of an entry.
 */
public class SampleResult implements Serializable, Cloneable, Searchable {

    private static final long serialVersionUID = 241L;

    // Needs to be accessible from Test code
    static Logger log = LoggerFactory.getLogger(SampleResult.class);

    /**
     * The default encoding to be used if not overridden.
     * The value is ISO-8859-1.
     */
    public static final String DEFAULT_HTTP_ENCODING = StandardCharsets.ISO_8859_1.name();

    private static final String OK_CODE = Integer.toString(HttpURLConnection.HTTP_OK);
    private static final String OK_MSG = "OK"; // $NON-NLS-1$
    private static final String INVALID_CALL_SEQUENCE_MSG = "Invalid call sequence"; // $NON-NLS-1$


    // Bug 33196 - encoding ISO-8859-1 is only suitable for Western countries
    // However the suggested System.getProperty("file.encoding") is Cp1252 on
    // Windows
    // So use a new property with the original value as default
    // needs to be accessible from test code
    /**
     * The default encoding to be used to decode the responseData byte array.
     * The value is defined by the property "sampleresult.default.encoding"
     * with a default of DEFAULT_HTTP_ENCODING if that is not defined.
     */
    protected static final String DEFAULT_ENCODING
            = JMeterUtils.getPropDefault("sampleresult.default.encoding", // $NON-NLS-1$
            DEFAULT_HTTP_ENCODING);

    /**
     * The default used by {@link #setResponseData(String, String)}
     */
    private static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

    /**
     * Data type value ({@value}) indicating that the response data is text.
     *
     * @see #getDataType
     * @see #setDataType(String)
     */
    public static final String TEXT = "text"; // $NON-NLS-1$

    /**
     * Data type value ({@value}) indicating that the response data is binary.
     *
     * @see #getDataType
     * @see #setDataType(String)
     */
    public static final String BINARY = "bin"; // $NON-NLS-1$

    private static final boolean DISABLE_SUBRESULTS_RENAMING = JMeterUtils.getPropDefault("subresults.disable_renaming", false);

    // List of types that are known to be binary
    private static final String[] BINARY_TYPES = {
            "image/",       //$NON-NLS-1$
            "audio/",       //$NON-NLS-1$
            "video/",       //$NON-NLS-1$
    };

    // List of types that are known to be ascii, although they may appear to be binary
    private static final String[] NON_BINARY_TYPES = {
            "audio/x-mpegurl",  //$NON-NLS-1$ (HLS Media Manifest)
            "audio/mpegurl",    //$NON-NLS-1$ (HLS Media Manifest)
            "video/f4m",        //$NON-NLS-1$ (Flash Media Manifest)
            "image/svg+xml"     //$NON-NLS-1$ (SVG is xml)
    };


    /**
     * empty array which can be returned instead of null
     */
    private static final byte[] EMPTY_BA = new byte[0];

    private static final SampleResult[] EMPTY_SR = new SampleResult[0];

    private static final AssertionResult[] EMPTY_AR = new AssertionResult[0];

    private static final boolean START_TIMESTAMP =
            JMeterUtils.getPropDefault("sampleresult.timestamp.start", false);  // $NON-NLS-1$

    /**
     * Allow read-only access from test code
     */
    private static final boolean USE_NANO_TIME =
            JMeterUtils.getPropDefault("sampleresult.useNanoTime", true);  // $NON-NLS-1$

    /**
     * How long between checks of nanotime; default 5000ms; set to <=0 to disable the thread
     */
    private static final long NANOTHREAD_SLEEP =
            JMeterUtils.getPropDefault("sampleresult.nanoThreadSleep", 5000);  // $NON-NLS-1$

    private static final String NULL_FILENAME = "NULL";

    static {
        if (START_TIMESTAMP) {
            log.info("Note: Sample TimeStamps are START times");
        } else {
            log.info("Note: Sample TimeStamps are END times");
        }
        log.info("sampleresult.default.encoding is set to {}", DEFAULT_ENCODING);
        log.info("sampleresult.useNanoTime={}", USE_NANO_TIME);
        log.info("sampleresult.nanoThreadSleep={}", NANOTHREAD_SLEEP);

        if (USE_NANO_TIME && NANOTHREAD_SLEEP > 0) {
            // Make sure we start with a reasonable value
            NanoOffset.nanoOffset = System.currentTimeMillis() - SampleResult.sampleNsClockInMs();
            NanoOffset nanoOffset = new NanoOffset();
            nanoOffset.setDaemon(true);
            nanoOffset.setName("NanoOffset");
            nanoOffset.start();
        }
    }

    /**
     * 定制自定义添加 =================
     */
    private String samplerId;

    private String resourceId;

    public String getSamplerId() {
        return this.samplerId;
    }

    public String getResourceId() {
        return this.resourceId;
    }

    // 数据格式 List<id_name> 多层父级按照同级统计
    private String scenario;

    public String getScenario() {
        return this.scenario;
    }

    /**
     * 定制自定义添加 =================
     */
    private SampleSaveConfiguration saveConfig;

    private SampleResult parent;

    private byte[] responseData = EMPTY_BA;

    private String responseCode = "";// Never return null

    private String label = "";// Never return null

    /**
     * Filename used by ResultSaver
     */
    private String resultFileName = "";

    /**
     * The data used by the sampler
     */
    private String samplerData;

    private String threadName = ""; // Never return null

    private String responseMessage = "";

    private String responseHeaders = ""; // Never return null

    private String requestHeaders = "";

    /**
     * timeStamp == 0 means either not yet initialised or no stamp available (e.g. when loading a results file)
     * the time stamp - can be start or end
     */
    private long timeStamp = 0;

    private long startTime = 0;

    private long endTime = 0;

    private long idleTime = 0;// Allow for non-sample time

    /**
     * Start of pause (if any)
     */
    private long pauseTime = 0;

    private List<AssertionResult> assertionResults;

    private List<SampleResult> subResults;

    /**
     * The data type of the sample
     *
     * @see #getDataType()
     * @see #setDataType(String)
     * @see #TEXT
     * @see #BINARY
     */
    private String dataType = ""; // Don't return null if not set

    private boolean success;

    /**
     * Files that this sample has been saved in.
     * In Non GUI mode and when best config is used, size never exceeds 1,
     * but as a compromise set it to 2
     */
    private final Set<String> files = ConcurrentHashMap.newKeySet(2);

    // TODO do contentType and/or dataEncoding belong in HTTPSampleResult instead?
    private String dataEncoding;// (is this really the character set?) e.g.
    // ISO-8895-1, UTF-8

    private String contentType = ""; // e.g. text/html; charset=utf-8

    /**
     * elapsed time
     */
    private long elapsedTime = 0;

    /**
     * time to first response
     */
    private long latency = 0;

    /**
     * time to end connecting
     */
    private long connectTime = 0;

    /**
     * Way to signal what to do on Test
     */
    private TestLogicalAction testLogicalAction = TestLogicalAction.CONTINUE;

    /**
     * Should thread terminate?
     */
    private boolean stopThread = false;

    /**
     * Should test terminate?
     */
    private boolean stopTest = false;

    /**
     * Should test terminate abruptly?
     */
    private boolean stopTestNow = false;

    private int sampleCount = 1;

    private long bytes = 0; // Allows override of sample size in case sampler does not want to store all the data

    private int headersSize = 0;

    private long bodySize = 0;

    /**
     * Currently active threads in this thread group
     */
    private volatile int groupThreads = 0;

    /**
     * Currently active threads in all thread groups
     */
    private volatile int allThreads = 0;

    private final long nanoTimeOffset;

    // Allow testcode access to the settings
    final boolean useNanoTime;

    final long nanoThreadSleep;

    private long sentBytes;

    private URL location;

    private transient boolean ignore;

    private transient int subResultIndex;

    /**
     * Cache for responseData as string to avoid multiple computations
     */
    private transient volatile String responseDataAsString;

    public SampleResult() {
        this(USE_NANO_TIME, NANOTHREAD_SLEEP);
    }

    // Allow test code to change the default useNanoTime setting
    SampleResult(boolean nanoTime) {
        this(nanoTime, NANOTHREAD_SLEEP);
    }

    // Allow test code to change the default useNanoTime and nanoThreadSleep settings
    SampleResult(boolean nanoTime, long nanoThreadSleep) {
        this.elapsedTime = 0;
        this.useNanoTime = nanoTime;
        this.nanoThreadSleep = nanoThreadSleep;
        this.nanoTimeOffset = initOffset();
        // 增加请求ID的获取
        Sampler sampler = JMeterContextService.getContext().getCurrentSampler();
        if (sampler != null) {
            this.samplerId = sampler.getPropertyAsString("MS-ID");
            this.resourceId = sampler.getPropertyAsString("MS-RESOURCE-ID");
            this.scenario = sampler.getPropertyAsString("MS-SCENARIO");
        }

    }

    /**
     * Copy constructor.
     *
     * @param res existing sample result
     */
    public SampleResult(SampleResult res) {
        this();
        allThreads = res.allThreads;//OK
        assertionResults = res.assertionResults;
        bytes = res.bytes;
        headersSize = res.headersSize;
        bodySize = res.bodySize;
        contentType = res.contentType;//OK
        dataEncoding = res.dataEncoding;//OK
        dataType = res.dataType;//OK
        endTime = res.endTime;//OK
        // files is created automatically, and applies per instance
        groupThreads = res.groupThreads;//OK
        idleTime = res.idleTime;
        label = res.label;//OK
        latency = res.latency;
        connectTime = res.connectTime;
        location = res.location;//OK
        parent = res.parent;
        pauseTime = res.pauseTime;
        requestHeaders = res.requestHeaders;//OK
        responseCode = res.responseCode;//OK
        responseData = res.responseData;//OK
        responseDataAsString = null;
        responseHeaders = res.responseHeaders;//OK
        responseMessage = res.responseMessage;//OK

        // Don't copy this; it is per instance resultFileName = res.resultFileName;

        sampleCount = res.sampleCount;
        samplerData = res.samplerData;
        saveConfig = res.saveConfig;
        sentBytes = res.sentBytes;
        startTime = res.startTime;//OK
        stopTest = res.stopTest;
        stopTestNow = res.stopTestNow;
        stopThread = res.stopThread;
        testLogicalAction = res.testLogicalAction;
        subResults = res.subResults;
        success = res.success;//OK
        threadName = res.threadName;//OK
        elapsedTime = res.elapsedTime;
        timeStamp = res.timeStamp;
    }

    /**
     * Create a sample with a specific elapsed time but don't allow the times to
     * be changed later
     * <p>
     * (only used by HTTPSampleResult)
     *
     * @param elapsed time
     * @param atend   create the sample finishing now, else starting now
     */
    protected SampleResult(long elapsed, boolean atend) {
        this();
        long now = currentTimeInMillis();
        if (atend) {
            setTimes(now - elapsed, now);
        } else {
            setTimes(now, now + elapsed);
        }
    }

    /**
     * Allow users to create a sample with specific timestamp and elapsed times
     * for cloning purposes, but don't allow the times to be changed later
     * <p>
     * Currently used by CSVSaveService and
     * StatisticalSampleResult
     *
     * @param stamp   this may be a start time or an end time (both in
     *                milliseconds)
     * @param elapsed time in milliseconds
     */
    public SampleResult(long stamp, long elapsed) {
        this();
        stampAndTime(stamp, elapsed);
    }

    private long initOffset() {
        if (useNanoTime) {
            return nanoThreadSleep > 0 ? NanoOffset.getNanoOffset() : System.currentTimeMillis() - sampleNsClockInMs();
        } else {
            return Long.MIN_VALUE;
        }
    }

    /**
     * @param propertiesToSave The propertiesToSave to set.
     */
    public void setSaveConfig(SampleSaveConfiguration propertiesToSave) {
        this.saveConfig = propertiesToSave;
    }

    public SampleSaveConfiguration getSaveConfig() {
        return saveConfig;
    }

    public boolean isStampedAtStart() {
        return START_TIMESTAMP;
    }

    /**
     * Create a sample with specific start and end times for test purposes, but
     * don't allow the times to be changed later
     * <p>
     * (used by StatVisualizerModel.Test)
     *
     * @param start start time in milliseconds since unix epoch
     * @param end   end time in milliseconds since unix epoch
     * @return sample with given start and end time
     */
    public static SampleResult createTestSample(long start, long end) {
        SampleResult res = new SampleResult();
        res.setStartTime(start);
        res.setEndTime(end);
        return res;
    }

    /**
     * Create a sample with a specific elapsed time for test purposes, but don't
     * allow the times to be changed later
     *
     * @param elapsed - desired elapsed time in milliseconds
     * @return sample that starts 'now' and ends <code>elapsed</code> milliseconds later
     */
    public static SampleResult createTestSample(long elapsed) {
        long now = System.currentTimeMillis();
        return createTestSample(now, now + elapsed);
    }

    private static long sampleNsClockInMs() {
        return System.nanoTime() / 1000000;
    }

    /**
     * Helper method to get 1 ms resolution timing.
     *
     * @return the current time in milliseconds
     * @throws RuntimeException when <code>useNanoTime</code> is <code>true</code> but
     *                          <code>nanoTimeOffset</code> is not set
     */
    public long currentTimeInMillis() {
        if (useNanoTime) {
            if (nanoTimeOffset == Long.MIN_VALUE) {
                throw new IllegalStateException("Invalid call; nanoTimeOffset has not been set");
            }
            return sampleNsClockInMs() + nanoTimeOffset;
        }
        return System.currentTimeMillis();
    }

    // Helper method to maintain timestamp relationships
    private void stampAndTime(long stamp, long elapsed) {
        if (START_TIMESTAMP) {
            startTime = stamp;
            endTime = stamp + elapsed;
        } else {
            startTime = stamp - elapsed;
            endTime = stamp;
        }
        timeStamp = stamp;
        elapsedTime = elapsed;
    }

    /**
     * For use by SaveService only.
     *
     * @param stamp   this may be a start time or an end time (both in milliseconds)
     * @param elapsed time in milliseconds
     * @throws RuntimeException when <code>startTime</code> or <code>endTime</code> has been
     *                          set already
     */
    public void setStampAndTime(long stamp, long elapsed) {
        if (startTime != 0 || endTime != 0) {
            throw new IllegalStateException("Calling setStampAndTime() after start/end times have been set");
        }
        stampAndTime(stamp, elapsed);
    }

    /**
     * Set the "marked" flag to show that the result has been written to the file.
     *
     * @param filename the name of the file
     * @return <code>true</code> if the result was previously marked
     */
    public boolean markFile(String filename) {
        return !files.add(filename != null ? filename : NULL_FILENAME);
    }

    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Set response code to OK, i.e. "200"
     */
    public void setResponseCodeOK() {
        responseCode = OK_CODE;
    }

    public void setResponseCode(String code) {
        responseCode = code;
    }

    public boolean isResponseCodeOK() {
        return responseCode.equals(OK_CODE);
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String msg) {
        responseMessage = msg;
    }

    public void setResponseMessageOK() {
        responseMessage = OK_MSG;
    }

    /**
     * Set result statuses OK - shorthand method to set:
     * <ul>
     * <li>ResponseCode</li>
     * <li>ResponseMessage</li>
     * <li>Successful status</li>
     * </ul>
     */
    public void setResponseOK() {
        setResponseCodeOK();
        setResponseMessageOK();
        setSuccessful(true);
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * Get the sample timestamp, which may be either the start time or the end time.
     *
     * @return timeStamp in milliseconds
     * @see #getStartTime()
     * @see #getEndTime()
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    public String getSampleLabel() {
        return label;
    }

    /**
     * Get the sample label for use in summary reports etc.
     *
     * @param includeGroup whether to include the thread group name
     * @return the label
     */
    public String getSampleLabel(boolean includeGroup) {
        if (includeGroup) {
            return threadName.substring(0, threadName.lastIndexOf(' ')) + ":" + label;
        }
        return label;
    }

    public void setSampleLabel(String label) {
        this.label = label;
    }

    public void addAssertionResult(AssertionResult assertResult) {
        if (assertionResults == null) {
            assertionResults = new ArrayList<>();
        }
        assertionResults.add(assertResult);
    }

    /**
     * Gets the assertion results associated with this sample.
     *
     * @return an array containing the assertion results for this sample.
     * Returns empty array if there are no assertion results.
     */
    public AssertionResult[] getAssertionResults() {
        if (assertionResults == null) {
            return EMPTY_AR;
        }
        return assertionResults.toArray(new AssertionResult[assertionResults.size()]);
    }

    /**
     * Add a subresult and adjust the parent byte count and end-time.
     *
     * @param subResult the {@link SampleResult} to be added
     */
    public void addSubResult(SampleResult subResult) {
        addSubResult(subResult, isRenameSampleLabel());
    }

    /**
     * see https://bz.apache.org/bugzilla/show_bug.cgi?id=63055
     *
     * @return true if TestPlan is in functional mode or property subresults.disable_renaming is true
     */
    public static boolean isRenameSampleLabel() {
        return !(TestPlan.getFunctionalMode() || DISABLE_SUBRESULTS_RENAMING);
    }

    /**
     * Add a subresult and adjust the parent byte count and end-time.
     *
     * @param subResult        the {@link SampleResult} to be added
     * @param renameSubResults boolean do we rename subResults based on position
     */
    public void addSubResult(SampleResult subResult, boolean renameSubResults) {
        if (subResult == null) {
            // see https://bz.apache.org/bugzilla/show_bug.cgi?id=54778
            return;
        }
        String tn = getThreadName();
        if (tn.length() == 0) {
            tn = Thread.currentThread().getName();
            this.setThreadName(tn);
        }
        subResult.setThreadName(tn);

        // Extend the time to the end of the added sample
        setEndTime(Math.max(getEndTime(), subResult.getEndTime() + nanoTimeOffset - subResult.nanoTimeOffset)); // Bug 51855
        // Include the byte count for the added sample
        setBytes(getBytesAsLong() + subResult.getBytesAsLong());
        setSentBytes(getSentBytes() + subResult.getSentBytes());
        setHeadersSize(getHeadersSize() + subResult.getHeadersSize());
        setBodySize(getBodySizeAsLong() + subResult.getBodySizeAsLong());
        addRawSubResult(subResult, renameSubResults);
    }

    /**
     * Add a subresult to the collection without updating any parent fields.
     *
     * @param subResult the {@link SampleResult} to be added
     */
    public void addRawSubResult(SampleResult subResult) {
        storeSubResult(subResult, isRenameSampleLabel());
    }

    /**
     * Add a subresult to the collection without updating any parent fields.
     *
     * @param subResult the {@link SampleResult} to be added
     */
    private void addRawSubResult(SampleResult subResult, boolean renameSubResults) {
        storeSubResult(subResult, renameSubResults);
    }

    /**
     * Add a subresult read from a results file.
     * <p>
     * As for {@link SampleResult#addSubResult(SampleResult)
     * addSubResult(SampleResult)}, except that the fields don't need to be
     * accumulated
     *
     * @param subResult the {@link SampleResult} to be added
     */
    public void storeSubResult(SampleResult subResult) {
        storeSubResult(subResult, isRenameSampleLabel());
    }

    /**
     * Add a subresult read from a results file.
     * <p>
     * As for {@link SampleResult#addSubResult(SampleResult)
     * addSubResult(SampleResult)}, except that the fields don't need to be
     * accumulated
     *
     * @param subResult        the {@link SampleResult} to be added
     * @param renameSubResults boolean do we rename subResults based on position
     */
    private void storeSubResult(SampleResult subResult, boolean renameSubResults) {
        if (subResults == null) {
            subResults = new ArrayList<>();
        }
        if (renameSubResults) {
            subResult.setSampleLabel(getSampleLabel() + "-" + subResultIndex++);
        }
        subResults.add(subResult);
        subResult.setParent(this);
    }

    /**
     * Gets the subresults associated with this sample.
     *
     * @return an array containing the subresults for this sample. Returns an
     * empty array if there are no subresults.
     */
    public SampleResult[] getSubResults() {
        if (subResults == null) {
            return EMPTY_SR;
        }
        return subResults.toArray(new SampleResult[subResults.size()]);
    }

    /**
     * Sets the responseData attribute of the SampleResult object.
     * <p>
     * If the parameter is null, then the responseData is set to an empty byte array.
     * This ensures that getResponseData() can never be null.
     *
     * @param response the new responseData value
     */
    public void setResponseData(byte[] response) {
        responseDataAsString = null;
        responseData = response == null ? EMPTY_BA : response;
    }

    /**
     * Sets the responseData attribute of the SampleResult object.
     * Should only be called after setting the dataEncoding (if necessary)
     *
     * @param response the new responseData value (String)
     * @deprecated - only intended for use from BeanShell code
     */
    @Deprecated
    public void setResponseData(String response) {
        responseDataAsString = null;
        try {
            responseData = response.getBytes(getDataEncodingWithDefault());
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not convert string, using default encoding. " + e.getLocalizedMessage());
            responseData = response.getBytes(Charset.defaultCharset()); // N.B. default charset is used deliberately here
        }
    }

    /**
     * Sets the encoding and responseData attributes of the SampleResult object.
     *
     * @param response the new responseData value (String)
     * @param encoding the encoding to set and then use (if null, use platform default)
     */
    public void setResponseData(final String response, final String encoding) {
        responseDataAsString = null;
        String encodeUsing = encoding != null ? encoding : DEFAULT_CHARSET;
        try {
            responseData = response.getBytes(encodeUsing);
            setDataEncoding(encodeUsing);
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not convert string using '" + encodeUsing +
                    "', using default encoding: " + DEFAULT_CHARSET, e);
            responseData = response.getBytes(Charset.defaultCharset()); // N.B. default charset is used deliberately here
            setDataEncoding(DEFAULT_CHARSET);
        }
    }

    /**
     * Gets the responseData attribute of the SampleResult object.
     * <p>
     * Note that some samplers may not store all the data, in which case
     * getResponseData().length will be incorrect.
     * <p>
     * Instead, always use {@link #getBytes()} to obtain the sample result byte count.
     * </p>
     *
     * @return the responseData value (cannot be null)
     */
    public byte[] getResponseData() {
        return responseData;
    }

    /**
     * Gets the responseData of the SampleResult object as a String
     *
     * @return the responseData value as a String, converted according to the encoding
     */
    public String getResponseDataAsString() {
        try {
            if (responseDataAsString == null) {
                responseDataAsString = new String(responseData, getDataEncodingWithDefault());
            }
            return responseDataAsString;
        } catch (UnsupportedEncodingException e) {
            log.warn("Using platform default as " + getDataEncodingWithDefault() + " caused " + e);
            return new String(responseData, Charset.defaultCharset()); // N.B. default charset is used deliberately here
        }
    }

    public void setSamplerData(String s) {
        samplerData = s;
    }

    public String getSamplerData() {
        return samplerData;
    }

    /**
     * Get the time it took this sample to occur.
     *
     * @return elapsed time in milliseconds
     */
    public long getTime() {
        return elapsedTime;
    }

    public boolean isSuccessful() {
        return success;
    }

    /**
     * Sets the data type of the sample.
     *
     * @param dataType String containing {@link #BINARY} or {@link #TEXT}
     * @see #BINARY
     * @see #TEXT
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Returns the data type of the sample.
     *
     * @return String containing {@link #BINARY} or {@link #TEXT} or the empty string
     * @see #BINARY
     * @see #TEXT
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Extract and save the DataEncoding and DataType from the parameter provided.
     * Does not save the full content Type.
     *
     * @param ct - content type (may be null)
     * @see #setContentType(String) which should be used to save the full content-type string
     */
    public void setEncodingAndType(String ct) {
        if (ct != null) {
            // Extract charset and store as DataEncoding
            // N.B. The meta tag:
            // <META http-equiv="content-type" content="text/html; charset=foobar">
            // is now processed by HTTPSampleResult#getDataEncodingWithDefault
            final String charsetPrefix = "charset="; // $NON-NLS-1$
            int cset = ct.toLowerCase(java.util.Locale.ENGLISH).indexOf(charsetPrefix);
            if (cset >= 0) {
                String charSet = ct.substring(cset + charsetPrefix.length());
                // handle: ContentType: text/plain; charset=ISO-8859-1; format=flowed
                int semiColon = charSet.indexOf(';');
                if (semiColon >= 0) {
                    charSet = charSet.substring(0, semiColon);
                }
                // Check for quoted string
                if (charSet.startsWith("\"") || charSet.startsWith("\'")) { // $NON-NLS-1$
                    setDataEncoding(charSet.substring(1, charSet.length() - 1)); // remove quotes
                } else {
                    setDataEncoding(charSet);
                }
            }
            if (isBinaryType(ct)) {
                setDataType(BINARY);
            } else {
                setDataType(TEXT);
            }
        }
    }

    /*
     * Determine if content-type is known to be binary, i.e. not displayable as text.
     *
     * @param ct content type
     * @return true if content-type is of type binary.
     */
    public static boolean isBinaryType(String ct) {
        for (String entry : NON_BINARY_TYPES) {
            if (ct.startsWith(entry)) {
                return false;
            }
        }
        for (String binaryType : BINARY_TYPES) {
            if (ct.startsWith(binaryType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the successful attribute of the SampleResult object.
     *
     * @param success the new successful value
     */
    public void setSuccessful(boolean success) {
        this.success = success;
    }

    /**
     * Returns the display name.
     *
     * @return display name of this sample result
     */
    @Override
    public String toString() {
        return getSampleLabel();
    }

    /**
     * Returns the dataEncoding or the default if no dataEncoding was provided.
     *
     * @return the value of the dataEncoding or DEFAULT_ENCODING
     */
    public String getDataEncodingWithDefault() {
        return getDataEncodingWithDefault(DEFAULT_ENCODING);
    }

    /**
     * Returns the dataEncoding or the default if no dataEncoding was provided.
     *
     * @param defaultEncoding the default to be applied
     * @return the value of the dataEncoding or the provided default
     */
    protected String getDataEncodingWithDefault(String defaultEncoding) {
        if (dataEncoding != null && dataEncoding.length() > 0) {
            return dataEncoding;
        }
        return defaultEncoding;
    }

    /**
     * Returns the dataEncoding. May be null or the empty String.
     *
     * @return the value of the dataEncoding
     */
    public String getDataEncodingNoDefault() {
        return dataEncoding;
    }

    /**
     * Sets the dataEncoding.
     *
     * @param dataEncoding the dataEncoding to set, e.g. ISO-8895-1, UTF-8
     */
    public void setDataEncoding(String dataEncoding) {
        this.dataEncoding = dataEncoding;
    }

    /**
     * @return whether to stop the test waiting for current running Sampler to end
     */
    public boolean isStopTest() {
        return stopTest;
    }

    /**
     * @return whether to stop the test now interrupting current running samplers
     */
    public boolean isStopTestNow() {
        return stopTestNow;
    }

    /**
     * @return whether to stop this thread
     */
    public boolean isStopThread() {
        return stopThread;
    }

    public void setStopTest(boolean b) {
        stopTest = b;
    }

    public void setStopTestNow(boolean b) {
        stopTestNow = b;
    }

    public void setStopThread(boolean b) {
        stopThread = b;
    }

    /**
     * @return the request headers
     */
    public String getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * @return the response headers
     */
    public String getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * @param string -
     *               request headers
     */
    public void setRequestHeaders(String string) {
        requestHeaders = string;
    }

    /**
     * @param string -
     *               response headers
     */
    public void setResponseHeaders(String string) {
        responseHeaders = string;
    }

    /**
     * @return the full content type - e.g. text/html [;charset=utf-8 ]
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the media type from the Content Type
     *
     * @return the media type - e.g. text/html (without charset, if any)
     */
    public String getMediaType() {
        return JOrphanUtils.trim(contentType, " ;").toLowerCase(java.util.Locale.ENGLISH);
    }

    /**
     * Stores the content-type string, e.g. <code>text/xml; charset=utf-8</code>
     *
     * @param string the content-type to be set
     * @see #setEncodingAndType(String) which can be used to extract the charset.
     */
    public void setContentType(String string) {
        contentType = string;
    }

    /**
     * @return idleTime
     */
    public long getIdleTime() {
        return idleTime;
    }

    /**
     * @return the end time
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @return the start time
     */
    public long getStartTime() {
        return startTime;
    }

    /*
     * Helper methods N.B. setStartTime must be called before setEndTime
     *
     * setStartTime is used by HTTPSampleResult to clone the parent sampler and
     * allow the original start time to be kept
     */
    protected final void setStartTime(long start) {
        startTime = start;
        if (START_TIMESTAMP) {
            timeStamp = startTime;
        }
    }

    public void setEndTime(long end) {
        endTime = end;
        if (!START_TIMESTAMP) {
            timeStamp = endTime;
        }
        if (startTime == 0) {
            log.error("setEndTime must be called after setStartTime", new Throwable(INVALID_CALL_SEQUENCE_MSG));
        } else {
            elapsedTime = endTime - startTime - idleTime;
        }
    }

    /**
     * Set idle time pause.
     * For use by SampleResultConverter/CSVSaveService.
     *
     * @param idle long
     */
    public void setIdleTime(long idle) {
        idleTime = idle;
    }

    private void setTimes(long start, long end) {
        setStartTime(start);
        setEndTime(end);
    }

    /**
     * Record the start time of a sample
     */
    public void sampleStart() {
        if (startTime == 0) {
            setStartTime(currentTimeInMillis());
        } else {
            log.error("sampleStart called twice", new Throwable(INVALID_CALL_SEQUENCE_MSG));
        }
    }

    /**
     * Record the end time of a sample and calculate the elapsed time
     */
    public void sampleEnd() {
        if (endTime == 0) {
            setEndTime(currentTimeInMillis());
        } else {
            log.error("sampleEnd called twice", new Throwable(INVALID_CALL_SEQUENCE_MSG));
        }
    }

    /**
     * Pause a sample
     */
    public void samplePause() {
        if (pauseTime != 0) {
            log.error("samplePause called twice", new Throwable(INVALID_CALL_SEQUENCE_MSG));
        }
        pauseTime = currentTimeInMillis();
    }

    /**
     * Resume a sample
     */
    public void sampleResume() {
        if (pauseTime == 0) {
            log.error("sampleResume without samplePause", new Throwable(INVALID_CALL_SEQUENCE_MSG));
        }
        idleTime += currentTimeInMillis() - pauseTime;
        pauseTime = 0;
    }

    /**
     * When a Sampler is working as a monitor
     *
     * @param monitor flag whether this sampler is working as a monitor
     * @deprecated since 3.2 NOOP
     */
    @Deprecated
    public void setMonitor(boolean monitor) {
        // NOOP
    }

    /**
     * If the sampler is a monitor, method will return true.
     *
     * @return true if the sampler is a monitor
     * @deprecated since 3.2 always return false
     */
    @Deprecated
    public boolean isMonitor() {
        return false;
    }

    /**
     * The statistical sample sender aggregates several samples to save on
     * transmission costs.
     *
     * @param count number of samples represented by this instance
     */
    public void setSampleCount(int count) {
        sampleCount = count;
    }

    /**
     * return the sample count. by default, the value is 1.
     *
     * @return the sample count
     */
    public int getSampleCount() {
        return sampleCount;
    }

    /**
     * Returns the count of errors.
     *
     * @return 0 - or 1 if the sample failed
     * <p>
     * TODO do we need allow for nested samples?
     */
    public int getErrorCount() {
        return success ? 0 : 1;
    }

    public void setErrorCount(int i) {// for reading from CSV files
        // ignored currently
    }

    /*
     * TODO: error counting needs to be sorted out.
     *
     * At present the Statistical Sampler tracks errors separately
     * It would make sense to move the error count here, but this would
     * mean lots of changes.
     * It's also tricky maintaining the count - it can't just be incremented/decremented
     * when the success flag is set as this may be done multiple times.
     * The work-round for now is to do the work in the StatisticalSampleResult,
     * which overrides this method.
     * Note that some JMS samplers also create samples with > 1 sample count
     * Also the Transaction Controller probably needs to be changed to do
     * proper sample and error accounting.
     * The purpose of this work-round is to allow at least minimal support for
     * errors in remote statistical batch mode.
     *
     */

    /**
     * In the event the sampler does want to pass back the actual contents, we
     * still want to calculate the throughput. The bytes are the bytes of the
     * response data.
     *
     * @param length the number of bytes of the response data for this sample
     */
    public void setBytes(long length) {
        bytes = length;
    }

    /**
     * In the event the sampler does want to pass back the actual contents, we
     * still want to calculate the throughput. The bytes are the bytes of the
     * response data.
     *
     * @param length the number of bytes of the response data for this sample
     * @deprecated use setBytes(long)
     */
    @Deprecated
    public void setBytes(int length) {
        setBytes((long) length);
    }

    /**
     * @param sentBytesCount long sent bytes
     */
    public void setSentBytes(long sentBytesCount) {
        sentBytes = sentBytesCount;
    }

    /**
     * @return the sentBytes
     */
    public long getSentBytes() {
        return sentBytes;
    }

    /**
     * return the bytes returned by the response.
     *
     * @return byte count
     * @deprecated use getBytesAsLong
     */
    @Deprecated
    public int getBytes() {
        return (int) getBytesAsLong();
    }

    /**
     * return the bytes returned by the response.
     *
     * @return byte count
     */
    public long getBytesAsLong() {
        long tmpSum = this.getHeadersSize() + this.getBodySizeAsLong();
        return tmpSum == 0 ? bytes : tmpSum;
    }

    /**
     * @return Returns the latency.
     */
    public long getLatency() {
        return latency;
    }

    /**
     * Set the time to the first response
     */
    public void latencyEnd() {
        latency = currentTimeInMillis() - startTime - idleTime;
    }

    /**
     * This is only intended for use by SampleResultConverter!
     *
     * @param latency The latency to set.
     */
    public void setLatency(long latency) {
        this.latency = latency;
    }

    /**
     * @return Returns the connect time.
     */
    public long getConnectTime() {
        return connectTime;
    }

    /**
     * Set the time to the end of connecting
     */
    public void connectEnd() {
        connectTime = currentTimeInMillis() - startTime - idleTime;
    }

    /**
     * This is only intended for use by SampleResultConverter!
     *
     * @param time The connect time to set.
     */
    public void setConnectTime(long time) {
        this.connectTime = time;
    }

    /**
     * This is only intended for use by SampleResultConverter!
     *
     * @param timeStamp The timeStamp to set.
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }


    public void setURL(URL location) {
        this.location = location;
    }

    public URL getURL() {
        return location;
    }

    /**
     * Get a String representation of the URL (if defined).
     *
     * @return ExternalForm of URL, or empty string if url is null
     */
    public String getUrlAsString() {
        return location == null ? "" : location.toExternalForm();
    }

    /**
     * @return Returns the parent.
     */
    public SampleResult getParent() {
        return parent;
    }

    /**
     * @param parent The parent to set.
     */
    public void setParent(SampleResult parent) {
        this.parent = parent;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public int getGroupThreads() {
        return groupThreads;
    }

    public void setGroupThreads(int n) {
        this.groupThreads = n;
    }

    public int getAllThreads() {
        return allThreads;
    }

    public void setAllThreads(int n) {
        this.allThreads = n;
    }

    // Bug 47394

    /**
     * Allow custom SampleSenders to drop unwanted assertionResults
     */
    public void removeAssertionResults() {
        this.assertionResults = null;
    }

    /**
     * Allow custom SampleSenders to drop unwanted subResults
     */
    public void removeSubResults() {
        this.subResults = null;
    }

    /**
     * Set the headers size in bytes
     *
     * @param size the number of bytes of the header
     */
    public void setHeadersSize(int size) {
        this.headersSize = size;
    }

    /**
     * Get the headers size in bytes
     *
     * @return the headers size
     */
    public int getHeadersSize() {
        return headersSize;
    }

    /**
     * @return the body size in bytes
     * @deprecated replaced by getBodySizeAsLong()
     */
    @Deprecated
    public int getBodySize() {
        return (int) getBodySizeAsLong();
    }

    /**
     * @return the body size in bytes
     */
    public long getBodySizeAsLong() {
        return bodySize == 0 ? responseData.length : bodySize;
    }

    /**
     * @param bodySize the body size to set
     */
    public void setBodySize(long bodySize) {
        this.bodySize = bodySize;
    }

    /**
     * @param bodySize the body size to set
     * @deprecated use setBodySize(long)
     */
    @Deprecated
    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    private static class NanoOffset extends Thread {

        private static volatile long nanoOffset;

        static long getNanoOffset() {
            return nanoOffset;
        }

        @Override
        public void run() {
            // Wait longer than a clock pulse (generally 10-15ms)
            getOffset(30L); // Catch an early clock pulse to reduce slop.
            while (true) {
                getOffset(NANOTHREAD_SLEEP); // Can now afford to wait a bit longer between checks
            }
        }

        private static void getOffset(long wait) {
            try {
                TimeUnit.MILLISECONDS.sleep(wait);
                long clock = System.currentTimeMillis();
                long nano = SampleResult.sampleNsClockInMs();
                nanoOffset = clock - nano;
            } catch (InterruptedException ignore) {
                // ignored
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * @return the startNextThreadLoop
     * @deprecated use {@link SampleResult#getTestLogicalAction()}
     */
    @Deprecated
    public boolean isStartNextThreadLoop() {
        return testLogicalAction == TestLogicalAction.START_NEXT_ITERATION_OF_THREAD;
    }

    /**
     * @param startNextThreadLoop the startNextLoop to set
     * @deprecated use SampleResult#setTestLogicalAction(TestLogicalAction)
     */
    @Deprecated
    public void setStartNextThreadLoop(boolean startNextThreadLoop) {
        if (startNextThreadLoop) {
            testLogicalAction = TestLogicalAction.START_NEXT_ITERATION_OF_THREAD;
        } else {
            testLogicalAction = TestLogicalAction.CONTINUE;
        }
    }

    /**
     * Clean up cached data
     */
    public void cleanAfterSample() {
        this.responseDataAsString = null;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("This should not happen");
        }
    }

    @Override
    public List<String> getSearchableTokens() throws Exception {
        List<String> datasToSearch = new ArrayList<>(4);
        datasToSearch.add(getSampleLabel());
        datasToSearch.add(getResponseDataAsString());
        datasToSearch.add(getRequestHeaders());
        datasToSearch.add(getResponseHeaders());
        return datasToSearch;
    }

    /**
     * @return boolean true if this SampleResult should not be sent to Listeners
     */
    public boolean isIgnore() {
        return ignore;
    }

    /**
     * Call this method to tell JMeter to ignore this SampleResult by Listeners
     */
    public void setIgnore() {
        this.ignore = true;
    }

    /**
     * @return String first non null assertion failure message if assertionResults is not null, null otherwise
     */
    public String getFirstAssertionFailureMessage() {
        String message = null;
        AssertionResult[] results = getAssertionResults();

        if (results != null) {
            // Find the first non-null message
            for (AssertionResult result : results) {
                message = result.getFailureMessage();
                if (message != null) {
                    break;
                }
            }
        }
        return message;
    }

    /**
     * @return the testLogicalAction
     */
    public TestLogicalAction getTestLogicalAction() {
        return testLogicalAction;
    }

    /**
     * @param testLogicalAction the testLogicalAction to set
     */
    public void setTestLogicalAction(TestLogicalAction testLogicalAction) {
        this.testLogicalAction = testLogicalAction;
    }
}
