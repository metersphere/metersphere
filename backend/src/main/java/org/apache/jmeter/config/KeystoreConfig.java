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

package org.apache.jmeter.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.util.JMeterStopTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configure Keystore
 */
@TestElementMetadata(labelResource = "displayName")
public class KeystoreConfig extends ConfigTestElement implements TestBean, TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(KeystoreConfig.class);

    private static final String KEY_STORE_START_INDEX = "https.keyStoreStartIndex"; // $NON-NLS-1$
    private static final String KEY_STORE_END_INDEX = "https.keyStoreEndIndex"; // $NON-NLS-1$

    private String startIndex;
    private String endIndex;
    private String preload;
    private String clientCertAliasVarName;

    public KeystoreConfig() {
        super();
    }

    @Override
    public void testEnded() {
        testEnded(null);
    }

    @Override
    public void testEnded(String host) {
        log.info("Destroying Keystore");
        SSLManager.getInstance().destroyKeystore();
    }

    @Override
    public void testStarted() {
        testStarted(null);
    }

    @Override
    public void testStarted(String host) {
        String reuseSSLContext = JMeterUtils.getProperty("https.use.cached.ssl.context");
        if (StringUtils.isEmpty(reuseSSLContext) || "true".equals(reuseSSLContext)) {
            log.warn("https.use.cached.ssl.context property must be set to false to ensure Multiple Certificates are used");
        }
        int startIndexAsInt = JMeterUtils.getPropDefault(KEY_STORE_START_INDEX, 0);
        int endIndexAsInt = JMeterUtils.getPropDefault(KEY_STORE_END_INDEX, -1);

        if (!StringUtils.isEmpty(this.startIndex)) {
            try {
                startIndexAsInt = Integer.parseInt(this.startIndex);
            } catch (NumberFormatException e) {
                log.warn("Failed parsing startIndex: {}, will default to: {}, error message: {}", this.startIndex,
                        startIndexAsInt, e, e);
            }
        }

        if (!StringUtils.isEmpty(this.endIndex)) {
            try {
                endIndexAsInt = Integer.parseInt(this.endIndex);
            } catch (NumberFormatException e) {
                log.warn("Failed parsing endIndex: {}, will default to: {}, error message: {}", this.endIndex,
                        endIndexAsInt, e, e);
            }
        }
        if (endIndexAsInt != -1 && startIndexAsInt > endIndexAsInt) {
            throw new JMeterStopTestException("Keystore Config error : Alias start index must be lower than Alias end index");
        }
        log.info(
                "Configuring Keystore with (preload: '{}', startIndex: {}, endIndex: {}, clientCertAliasVarName: '{}')",
                preload, startIndexAsInt, endIndexAsInt, clientCertAliasVarName);
        // 加载认证文件
        String path = this.getPropertyAsString("MS-KEYSTORE-FILE-PATH");
        String password = this.getPropertyAsString("MS-KEYSTORE-FILE-PASSWORD");
        InputStream in = null;
        try {
            in = new FileInputStream(new File(path));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        SSLManager.getInstance().configureKeystore(Boolean.parseBoolean(preload),
                startIndexAsInt,
                endIndexAsInt,
                clientCertAliasVarName, in, password);
    }

    /**
     * @return the endIndex
     */
    public String getEndIndex() {
        return endIndex;
    }

    /**
     * @param endIndex the endIndex to set
     */
    public void setEndIndex(String endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * @return the startIndex
     */
    public String getStartIndex() {
        return startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setStartIndex(String startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * @return the preload
     */
    public String getPreload() {
        return preload;
    }

    /**
     * @param preload the preload to set
     */
    public void setPreload(String preload) {
        this.preload = preload;
    }

    /**
     * @return the clientCertAliasVarName
     */
    public String getClientCertAliasVarName() {
        return clientCertAliasVarName;
    }

    /**
     * @param clientCertAliasVarName the clientCertAliasVarName to set
     */
    public void setClientCertAliasVarName(String clientCertAliasVarName) {
        this.clientCertAliasVarName = clientCertAliasVarName;
    }
}
