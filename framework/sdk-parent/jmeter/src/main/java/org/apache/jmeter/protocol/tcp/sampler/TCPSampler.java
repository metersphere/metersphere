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

package org.apache.jmeter.protocol.tcp.sampler;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A sampler which understands Tcp requests.
 *
 */
public class TCPSampler extends AbstractSampler implements ThreadListener, Interruptible {
    private static final long serialVersionUID = 280L;

    private static final Logger log = LoggerFactory.getLogger(TCPSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList(
                    "org.apache.jmeter.config.gui.LoginConfigGui",
                    "org.apache.jmeter.protocol.tcp.config.gui.TCPConfigGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui"
            ));

    public static final String SERVER = "TCPSampler.server"; //$NON-NLS-1$

    public static final String PORT = "TCPSampler.port"; //$NON-NLS-1$

    public static final String FILENAME = "TCPSampler.filename"; //$NON-NLS-1$

    public static final String CLASSNAME = "TCPSampler.classname";//$NON-NLS-1$

    public static final String NODELAY = "TCPSampler.nodelay"; //$NON-NLS-1$

    public static final String TIMEOUT = "TCPSampler.timeout"; //$NON-NLS-1$

    public static final String TIMEOUT_CONNECT = "TCPSampler.ctimeout"; //$NON-NLS-1$

    public static final String REQUEST = "TCPSampler.request"; //$NON-NLS-1$

    public static final String RE_USE_CONNECTION = "TCPSampler.reUseConnection"; //$NON-NLS-1$
    public static final boolean RE_USE_CONNECTION_DEFAULT = true;

    public static final String CLOSE_CONNECTION = "TCPSampler.closeConnection"; //$NON-NLS-1$
    public static final boolean CLOSE_CONNECTION_DEFAULT = false;

    public static final String SO_LINGER = "TCPSampler.soLinger"; //$NON-NLS-1$

    public static final String EOL_BYTE = "TCPSampler.EolByte"; //$NON-NLS-1$

    private static final String TCPKEY = "TCP"; //$NON-NLS-1$ key for HashMap

    private static final String ERRKEY = "ERR"; //$NON-NLS-1$ key for HashMap

    // the response is scanned for these strings
    private static final String STATUS_PREFIX = JMeterUtils.getPropDefault("tcp.status.prefix", ""); //$NON-NLS-1$

    private static final String STATUS_SUFFIX = JMeterUtils.getPropDefault("tcp.status.suffix", ""); //$NON-NLS-1$

    private static final String STATUS_PROPERTIES = JMeterUtils.getPropDefault("tcp.status.properties", ""); //$NON-NLS-1$

    private static final Properties STATUS_PROPS = new Properties();

    private static final String PROTO_PREFIX = "org.apache.jmeter.protocol.tcp.sampler."; //$NON-NLS-1$

    private static final boolean HAVE_STATUS_PROPS;

    //设置tcp编码
    public static String charset = "UTF-8";
    public void setCharset(String charset) {
        this.setProperty("CHARSET", charset);
    }
    public String getCharset() {
        return getPropertyAsString("CHARSET");
    }
    static {
        boolean hsp = false;
        log.debug("Status prefix={}, suffix={}, properties={}",
                STATUS_PREFIX, STATUS_SUFFIX, STATUS_PROPERTIES); //$NON-NLS-1$
        if (STATUS_PROPERTIES.length() > 0) {
            File f = new File(STATUS_PROPERTIES);
            try (FileInputStream fis = new FileInputStream(f)){
                STATUS_PROPS.load(fis);
                log.debug("Successfully loaded properties"); //$NON-NLS-1$
                hsp = true;
            } catch (FileNotFoundException e) {
                log.debug("Property file {} not found", STATUS_PROPERTIES); //$NON-NLS-1$
            } catch (IOException e) {
                log.debug("Error reading property file {} error {}", STATUS_PROPERTIES, e.toString()); //$NON-NLS-1$
            }
        }
        HAVE_STATUS_PROPS = hsp;
    }

    /** the cache of TCP Connections */
    // KEY = TCPKEY or ERRKEY, Entry= Socket or String
    private static final ThreadLocal<Map<String, Object>> tp =
            ThreadLocal.withInitial(HashMap::new);

    private transient TCPClient protocolHandler;

    private transient boolean firstSample; // Are we processing the first sample?

    private transient volatile Socket currentSocket; // used for handling interrupt

    public TCPSampler() {
        log.debug("Created {}", this); //$NON-NLS-1$
    }

    private String getError() {
        Map<String, Object> cp = tp.get();
        return (String) cp.get(ERRKEY);
    }

    private Socket getSocket(String socketKey) {
        Map<String, Object> cp = tp.get();
        Socket con = null;
        if (isReUseConnection()) {
            con = (Socket) cp.get(socketKey);
            if (con != null) {
                log.debug("{} Reusing connection {}", this, con); //$NON-NLS-1$
            }
        }
        if (con == null) {
            // Not in cache, so create new one and cache it
            try {
                closeSocket(socketKey); // Bug 44910 - close previous socket (if any)
                SocketAddress sockaddr = new InetSocketAddress(getServer(), getPort());
                con = new Socket(); // NOSONAR socket is either cache in ThreadLocal for reuse and closed at end of thread or closed here
                if (getPropertyAsString(SO_LINGER,"").length() > 0){
                    con.setSoLinger(true, getSoLinger());
                }
                con.connect(sockaddr, getConnectTimeout());
                if(log.isDebugEnabled()) {
                    log.debug("Created new connection {}", con); //$NON-NLS-1$
                }
                cp.put(socketKey, con);
            } catch (UnknownHostException e) {
                log.warn("Unknown host for {}", getLabel(), e);//$NON-NLS-1$
                cp.put(ERRKEY, e.toString());
                return null;
            } catch (IOException e) {
                log.warn("Could not create socket for {}", getLabel(), e); //$NON-NLS-1$
                cp.put(ERRKEY, e.toString());
                return null;
            }
        }
        // (re-)Define connection params - Bug 50977
        try {
            con.setSoTimeout(getTimeout());
            con.setTcpNoDelay(getNoDelay());
            if(log.isDebugEnabled()) {
                log.debug("{} Timeout={}, NoDelay={}", this, getTimeout(), getNoDelay()); //$NON-NLS-1$
            }
        } catch (SocketException se) {
            log.warn("Could not set timeout or nodelay for {}", getLabel(), se); //$NON-NLS-1$
            cp.put(ERRKEY, se.toString());
        }
        return con;
    }

    /**
     * @return String socket key in cache Map
     */
    private String getSocketKey() {
        return TCPKEY+"#"+getServer()+"#"+getPort()+"#"+getUsername()+"#"+getPassword();
    }

    public String getUsername() {
        return getPropertyAsString(ConfigTestElement.USERNAME);
    }

    public String getPassword() {
        return getPropertyAsString(ConfigTestElement.PASSWORD);
    }

    public void setServer(String newServer) {
        this.setProperty(SERVER, newServer);
    }

    public String getServer() {
        return getPropertyAsString(SERVER);
    }

    public boolean isReUseConnection() {
        return getPropertyAsBoolean(RE_USE_CONNECTION, RE_USE_CONNECTION_DEFAULT);
    }

    public void setCloseConnection(String close) {
        this.setProperty(CLOSE_CONNECTION, close, "");
    }

    public boolean isCloseConnection() {
        return getPropertyAsBoolean(CLOSE_CONNECTION, CLOSE_CONNECTION_DEFAULT);
    }

    public void setSoLinger(String soLinger) {
        this.setProperty(SO_LINGER, soLinger, "");
    }

    public int getSoLinger() {
        return getPropertyAsInt(SO_LINGER);
    }

    public void setEolByte(String eol) {
        this.setProperty(EOL_BYTE, eol, "");
    }

    public int getEolByte() {
        return getPropertyAsInt(EOL_BYTE);
    }


    public void setPort(String newFilename) {
        this.setProperty(PORT, newFilename);
    }

    public int getPort() {
        return getPropertyAsInt(PORT);
    }

    public void setFilename(String newFilename) {
        this.setProperty(FILENAME, newFilename);
    }

    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    public void setRequestData(String newRequestData) {
        this.setProperty(REQUEST, newRequestData);
    }

    public String getRequestData() {
        return getPropertyAsString(REQUEST);
    }

    public void setTimeout(String newTimeout) {
        this.setProperty(TIMEOUT, newTimeout);
    }

    public int getTimeout() {
        return getPropertyAsInt(TIMEOUT);
    }

    public void setConnectTimeout(String newTimeout) {
        this.setProperty(TIMEOUT_CONNECT, newTimeout, "");
    }

    public int getConnectTimeout() {
        return getPropertyAsInt(TIMEOUT_CONNECT, 0);
    }

    public boolean getNoDelay() {
        return getPropertyAsBoolean(NODELAY);
    }

    public void setClassname(String classname) {
        this.setProperty(CLASSNAME, classname, ""); //$NON-NLS-1$
    }

    public String getClassname() {
        String clazz = getPropertyAsString(CLASSNAME,"");
        if (clazz==null || clazz.length()==0){
            clazz = JMeterUtils.getPropDefault("tcp.handler", "TCPClientImpl"); //$NON-NLS-1$ $NON-NLS-2$
        }
        return clazz;
    }

    /**
     * Returns a formatted string label describing this sampler Example output:
     * Tcp://Tcp.nowhere.com/pub/README.txt
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        return "tcp://" + this.getServer() + ":" + this.getPort();//$NON-NLS-1$ $NON-NLS-2$
    }

    private Class<?> getClass(String className) {
        Class<?> c = null;
        try {
            c = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                c = Class.forName(PROTO_PREFIX + className, false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e1) {
                log.error("Could not find protocol class '{}'", className); //$NON-NLS-1$
            }
        }
        return c;

    }

    private TCPClient getProtocol() {
        TCPClient tcpClient = null;
        Class<?> javaClass = getClass(getClassname());
        if (javaClass == null){
            return null;
        }
        try {
            tcpClient = (TCPClient) javaClass.getDeclaredConstructor().newInstance();
            if (getPropertyAsString(EOL_BYTE, "").length()>0){
                tcpClient.setEolByte(getEolByte());
                log.info("Using eolByte={}", getEolByte());
            }

            if (log.isDebugEnabled()) {
                log.debug("{} Created: {}@{}", this, getClassname(), Integer.toHexString(tcpClient.hashCode())); //$NON-NLS-1$
            }
        } catch (Exception e) {
            log.error("{} Exception creating: {} ", this, getClassname(), e); //$NON-NLS-1$
        }
        return tcpClient;
    }

    @Override
    public SampleResult sample(Entry e)// Entry tends to be ignored ...
    {
        if (firstSample) { // Do stuff we cannot do as part of threadStarted()
            initSampling();
            firstSample=false;
        }
        final boolean reUseConnection = isReUseConnection();
        final boolean closeConnection = isCloseConnection();
        String socketKey = getSocketKey();
        if (log.isDebugEnabled()){
            log.debug(getLabel() + " " + getFilename() + " " + getUsername() + " " + getPassword());
        }
        SampleResult res = new SampleResult();
        boolean isSuccessful = false;
        res.setSampleLabel(getName());// Use the test element name for the label
        String sb = "Host: " + getServer() +
                " Port: " + getPort() + "\n" +
                "Reuse: " + reUseConnection +
                " Close: " + closeConnection + "\n[" +
                "SOLINGER: " + getSoLinger() +
                " EOL: " + getEolByte() +
                " noDelay: " + getNoDelay() +
                "]";
        res.setSamplerData(sb);
        res.sampleStart();
        try {
            Socket sock;
            try {
                sock = getSocket(socketKey);
            } finally {
                res.connectEnd();
            }
            if (sock == null) {
                res.setResponseCode("500"); //$NON-NLS-1$
                res.setResponseMessage(getError());
            } else if (protocolHandler == null){
                res.setResponseCode("500"); //$NON-NLS-1$
                res.setResponseMessage("Protocol handler not found");
            } else {
                currentSocket = sock;
                InputStream is = sock.getInputStream();
                OutputStream os = sock.getOutputStream();
                String req = getRequestData();
                // TODO handle filenames
                res.setSamplerData(req);
                String in = null;
                //替换原来的编码
                if (protocolHandler instanceof MsTCPClientImpl) {
                    ((MsTCPClientImpl) protocolHandler).write(os, req , getCharset());
                     in = ((MsTCPClientImpl) protocolHandler).read(is, res , getCharset());
                } else {
                    protocolHandler.write(os, req);
                     in = protocolHandler.read(is, res);
                }
                isSuccessful = setupSampleResult(res, in, null, protocolHandler);
            }
        } catch (ReadException ex) {
            log.error("", ex);
            isSuccessful=setupSampleResult(res, ex.getPartialResponse(), ex,protocolHandler);
            closeSocket(socketKey);
        } catch (Exception ex) {
            log.error("", ex);
            isSuccessful=setupSampleResult(res, "", ex, protocolHandler);
            closeSocket(socketKey);
        } finally {
            currentSocket = null;
            // Calculate response time
            res.sampleEnd();

            // Set if we were successful or not
            res.setSuccessful(isSuccessful);

            if (!reUseConnection || closeConnection) {
                closeSocket(socketKey);
            }
        }
        return res;
    }

    /**
     * Fills SampleResult object
     * @param sampleResult {@link SampleResult}
     * @param readResponse Response read until error occurred
     * @param exception Source exception
     * @param protocolHandler {@link TCPClient}
     * @return boolean if sample is considered as successful
     */
    private boolean setupSampleResult(SampleResult sampleResult,
                                      String readResponse,
                                      Exception exception,
                                      TCPClient protocolHandler) {
        //替换原来的编码
        sampleResult.setResponseData(readResponse,
                protocolHandler != null ? this.getCharset() : null);
        sampleResult.setDataType(SampleResult.TEXT);
        if(exception==null) {
            sampleResult.setResponseCodeOK();
            sampleResult.setResponseMessage("OK"); //$NON-NLS-1$
        } else {
            sampleResult.setResponseCode("500"); //$NON-NLS-1$
            sampleResult.setResponseMessage(exception.toString()); //$NON-NLS-1$
        }
        boolean isSuccessful = exception == null;
        // Reset the status code if the message contains one
        if (!StringUtils.isEmpty(readResponse) && STATUS_PREFIX.length() > 0) {
            int i = readResponse.indexOf(STATUS_PREFIX);
            int j = readResponse.indexOf(STATUS_SUFFIX, i + STATUS_PREFIX.length());
            if (i != -1 && j > i) {
                String rc = readResponse.substring(i + STATUS_PREFIX.length(), j);
                sampleResult.setResponseCode(rc);
                isSuccessful = isSuccessful && checkResponseCode(rc);
                if (HAVE_STATUS_PROPS) {
                    sampleResult.setResponseMessage(STATUS_PROPS.getProperty(rc, "Status code not found in properties")); //$NON-NLS-1$
                } else {
                    sampleResult.setResponseMessage("No status property file");
                }
            } else {
                sampleResult.setResponseCode("999"); //$NON-NLS-1$
                sampleResult.setResponseMessage("Status value not found");
                isSuccessful = false;
            }
        }
        return isSuccessful;
    }

    /**
     * @param rc response code
     * @return whether this represents success or not
     */
    private boolean checkResponseCode(String rc) {
        int responseCode = Integer.parseInt(rc);
        return responseCode >= 400 && responseCode <= 599;
    }

    @Override
    public void threadStarted() {
        log.debug("Thread Started"); //$NON-NLS-1$
        firstSample = true;
    }

    // Cannot do this as part of threadStarted() because the Config elements have not been processed.
    private void initSampling() {
        protocolHandler = getProtocol();
        if (log.isDebugEnabled()) {
            log.debug("Using Protocol Handler: {}",  //$NON-NLS-1$
                    protocolHandler == null ? "NONE" : protocolHandler.getClass().getName()); //$NON-NLS-1$
        }
        if (protocolHandler != null){
            protocolHandler.setupTest();
        }
    }

    /**
     * Close socket of current sampler
     */
    private void closeSocket(String socketKey) {
        Map<String, Object> cp = tp.get();
        Socket con = (Socket) cp.remove(socketKey);
        if (con != null) {
            log.debug("{} Closing connection {}", this, con); //$NON-NLS-1$
            try {
                con.close();
            } catch (IOException e) {
                log.warn("Error closing socket {}", e); //$NON-NLS-1$
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        log.debug("Thread Finished"); //$NON-NLS-1$
        tearDown();
        if (protocolHandler != null){
            protocolHandler.teardownTest();
        }
    }

    /**
     * Closes all connections, clears Map and remove thread local Map
     */
    private void tearDown() {
        Map<String, Object> cp = tp.get();
        cp.forEach((k, v) -> {
            if(k.startsWith(TCPKEY)) {
                try {
                    ((Socket)v).close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        });
        cp.clear();
        tp.remove();
    }

    /**
     * @see AbstractSampler#applies(ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    @Override
    public boolean interrupt() {
        Optional<Socket> sock = Optional.ofNullable(currentSocket); // fetch in case gets nulled later
        if (sock.isPresent()) {
            try {
                sock.get().close();
            } catch (IOException ignored) {
                // NOOP
            }
            return true;
        } else {
            return false;
        }
    }
}
