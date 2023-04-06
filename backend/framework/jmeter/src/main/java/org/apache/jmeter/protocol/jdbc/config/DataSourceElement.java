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

package org.apache.jmeter.protocol.jdbc.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@TestElementMetadata(labelResource = "displayName")
public class DataSourceElement extends AbstractTestElement
        implements ConfigElement, TestStateListener, TestBean {
    private static final Logger log = LoggerFactory.getLogger(DataSourceElement.class);

    private static final long serialVersionUID = 235L;

    private transient String dataSource;
    private transient String driver;
    private transient String dbUrl;
    private transient String username;
    private transient String password;
    private transient String checkQuery;
    private transient String connectionProperties;
    private transient String initQuery;
    private transient String poolMax;
    private transient String connectionAge;
    private transient String timeout;
    private transient String trimInterval;
    private transient String transactionIsolation;
    private transient String poolPreparedStatements;

    private transient boolean keepAlive;
    private transient boolean autocommit;
    private transient boolean preinit;

    /*
     *  The datasource is set up by testStarted and cleared by testEnded.
     *  These are called from different threads, so access must be synchronized.
     *  The same instance is called in each case.
     */
    private transient BasicDataSource dbcpDataSource;

    // Keep a record of the pre-thread pools so that they can be disposed of at the end of a test
    private transient Set<BasicDataSource> perThreadPoolSet;

    public DataSourceElement() {
        log.info("DataSourceElement init");
    }

    @Override
    public void testEnded() {
        synchronized (this) {
            if (dbcpDataSource != null) {
                try {
                    dbcpDataSource.close();
                } catch (SQLException ex) {
                    log.error("Error closing pool: {}", getName(), ex);
                }
            }
            dbcpDataSource = null;
        }
        if (perThreadPoolSet != null) {// in case
            for (BasicDataSource dsc : perThreadPoolSet) {
                log.debug("Closing pool: {}@{}", getDataSourceName(), System.identityHashCode(dsc));
                try {
                    dsc.close();
                } catch (SQLException ex) {
                    log.error("Error closing pool:{}", getName(), ex);
                }
            }
            perThreadPoolSet = null;
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();
        String poolName = getDataSource();
        if (JOrphanUtils.isBlank(poolName)) {
            throw new IllegalArgumentException("Name for DataSource must not be empty in " + getName());
        } else if (ObjectUtils.isEmpty(variables.getObject(poolName))) {
            String maxPool = getPoolMax();
            perThreadPoolSet = Collections.synchronizedSet(new HashSet<BasicDataSource>());
            if (maxPool.equals("0")) {
                variables.putObject(poolName, new DataSourceComponentImpl()); // pool will be created later
            } else {
                BasicDataSource src = initPool(maxPool);
                synchronized (this) {
                    dbcpDataSource = src;
                    variables.putObject(poolName, new DataSourceComponentImpl(dbcpDataSource));
                }
            }
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public Object clone() {
        DataSourceElement el = (DataSourceElement) super.clone();
        synchronized (this) {
            el.dbcpDataSource = dbcpDataSource;
            el.perThreadPoolSet = perThreadPoolSet;
        }
        return el;
    }

    /**
     * Gets a textual description about the pools configuration.
     *
     * @param poolName Pool name
     * @return Connection information on {@code poolName} or a short message,
     * when the JMeter object specified by {@code poolName} is not a
     * pool
     * @throws SQLException when an error occurs, while gathering information about the
     *                      connection
     */
    public static String getConnectionInfo(String poolName) throws SQLException {
        Object poolObject = JMeterContextService.getContext().getVariables().getObject(poolName);
        if (poolObject instanceof DataSourceComponentImpl) {
            DataSourceComponentImpl pool = (DataSourceComponentImpl) poolObject;
            return pool.getConnectionInfo();
        } else {
            return "Object:" + poolName + " is not of expected type '" + DataSourceComponentImpl.class.getName() + "'";
        }
    }

    /**
     * Utility routine to get the connection from the pool.<br>
     * Purpose:
     * <ul>
     * <li>allows JDBCSampler to be entirely independent of the pooling classes
     * </li>
     * <li>allows the pool storage mechanism to be changed if necessary</li>
     * </ul>
     *
     * @param poolName name of the pool to get a connection from
     * @return a possible cached connection from the pool
     * @throws SQLException when an error occurs while getting the connection from the
     *                      pool
     */
    public static Connection getConnection(String poolName) throws SQLException {
        Object poolObject =
                JMeterContextService.getContext().getVariables().getObject(poolName);
        if (poolObject == null) {
            throw new SQLException("No pool found named: '" + poolName + "', ensure Variable Name matches Variable Name of JDBC Connection Configuration");
        } else {
            if (poolObject instanceof DataSourceComponentImpl) {
                DataSourceComponentImpl pool = (DataSourceComponentImpl) poolObject;
                return pool.getConnection();
            } else {
                String errorMsg = "Found object stored under variable:'" + poolName + "' with class:"
                        + poolObject.getClass().getName() + " and value: '" + poolObject
                        + " but it's not a DataSourceComponent, check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new SQLException(errorMsg);
            }
        }
    }

    /*
     * Set up the DataSource - maxPool is a parameter, so the same code can
     * also be used for setting up the per-thread pools.
     */
    private BasicDataSource initPool(String maxPool) {
        BasicDataSource dataSource = new BasicDataSource();

        if (log.isDebugEnabled()) {
            log.debug("MaxPool: {} Timeout: {} TrimInt: {} Auto-Commit: {} Preinit: {} poolPreparedStatements: {}",
                    maxPool, getTimeout(), getTrimInterval(), isAutocommit(), isPreinit(), poolPreparedStatements);
        }
        int poolSize = Integer.parseInt(maxPool);
        dataSource.setMinIdle(0);
        dataSource.setInitialSize(poolSize);
        dataSource.setAutoCommitOnReturn(false);
        if (StringUtils.isNotEmpty(initQuery)) {
            String[] sqlArray = initQuery.split("\n");
            dataSource.setConnectionInitSqls(Arrays.asList(sqlArray));
        } else {
            dataSource.setConnectionInitSqls(Collections.emptyList());
        }
        if (StringUtils.isNotEmpty(connectionProperties)) {
            dataSource.setConnectionProperties(connectionProperties);
        }
        if (StringUtils.isNotEmpty(poolPreparedStatements)) {
            int maxPreparedStatements = Integer.parseInt(poolPreparedStatements);
            if (maxPreparedStatements < 0) {
                dataSource.setPoolPreparedStatements(false);
            } else {
                dataSource.setPoolPreparedStatements(true);
                dataSource.setMaxOpenPreparedStatements(10);
            }
        }
        dataSource.setRollbackOnReturn(false);
        dataSource.setMaxIdle(poolSize);
        dataSource.setMaxTotal(poolSize);
        dataSource.setMaxWaitMillis(Long.parseLong(getTimeout()));

        dataSource.setDefaultAutoCommit(isAutocommit());

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(40);
            sb.append("KeepAlive: ");
            sb.append(isKeepAlive());
            sb.append(" Age: ");
            sb.append(getConnectionAge());
            sb.append(" CheckQuery: ");
            sb.append(getCheckQuery());
            log.debug(sb.toString());
        }
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTestOnCreate(false);
        dataSource.setTestWhileIdle(false);

        if (isKeepAlive()) {
            dataSource.setTestWhileIdle(true);
            String validationQuery = getCheckQuery();
            if (StringUtils.isBlank(validationQuery)) {
                dataSource.setValidationQuery(null);
            } else {
                dataSource.setValidationQuery(validationQuery);
            }
            dataSource.setSoftMinEvictableIdleTimeMillis(Long.parseLong(getConnectionAge()));
            dataSource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(getTrimInterval()));
        }

        int transactionIsolation = DataSourceElementBeanInfo.getTransactionIsolationMode(getTransactionIsolation());
        if (transactionIsolation >= 0) {
            dataSource.setDefaultTransactionIsolation(transactionIsolation);
        }

        String userName = getUsername();
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder(40);
            sb.append("Driver: ");
            sb.append(getDriver());
            sb.append(" DbUrl: ");
            sb.append(getDbUrl());
            sb.append(" User: ");
            sb.append(userName);
            log.debug(sb.toString());
        }
        dataSource.setDriverClassName(getDriver());
        dataSource.setUrl(getDbUrl());

        if (userName.length() > 0) {
            dataSource.setUsername(userName);
            dataSource.setPassword(getPassword());
        }

        if (isPreinit()) {
            // side effect - connection pool init - that is what we want
            // see also https://commons.apache.org/proper/commons-dbcp/apidocs/org/apache/commons/dbcp2/BasicDataSource.html#setInitialSize-int-
            // it says: "The pool is initialized the first time one of the following methods is invoked:
            // getConnection, setLogwriter, setLoginTimeout, getLoginTimeout, getLogWriter."
            // so we get a connection and close it - which releases it back to the pool (but stays open)
            try {
                dataSource.getConnection().close();
                if (log.isDebugEnabled()) {
                    log.debug("Reinitializing the connection pool: {}@{}", getDataSourceName(), System.identityHashCode(dataSource));
                }
            } catch (SQLException ex) {
                if (log.isErrorEnabled()) {
                    log.error("Error reinitializing the connection pool: {}@{}", getDataSourceName(), System.identityHashCode(dataSource), ex);
                }
            }
        }

        log.debug("PoolConfiguration:{}", this.dataSource);
        return dataSource;
    }

    // used to hold per-thread singleton connection pools
    private static final ThreadLocal<Map<String, BasicDataSource>> perThreadPoolMap =
            ThreadLocal.withInitial(HashMap::new);

    /**
     * Wrapper class to allow {@link DataSourceElement#getConnection(String)} to be implemented for both shared
     * and per-thread pools.
     */
    private class DataSourceComponentImpl {

        private final BasicDataSource sharedDSC;

        DataSourceComponentImpl() {
            sharedDSC = null;
        }

        DataSourceComponentImpl(BasicDataSource dsc) {
            sharedDSC = dsc;
        }

        /**
         * @return String connection information
         */
        public String getConnectionInfo() {
            BasicDataSource dsc = getConfiguredDataSource();
            StringBuilder builder = new StringBuilder(100);
            builder.append("shared:").append(sharedDSC != null)
                    .append(", driver:").append(dsc.getDriverClassName())
                    .append(", url:").append(dsc.getUrl())
                    .append(", user:").append(dsc.getUsername());
            return builder.toString();
        }

        /**
         * @return Connection
         * @throws SQLException if database access error occurred
         */
        public Connection getConnection() throws SQLException {
            BasicDataSource dsc = getConfiguredDataSource();
            Connection conn = dsc.getConnection();
            int isolation = DataSourceElementBeanInfo.getTransactionIsolationMode(getTransactionIsolation());
            if (isolation >= 0 && conn.getTransactionIsolation() != isolation) {
                try {
                    // make sure setting the new isolation mode is done in an auto committed transaction
                    conn.setTransactionIsolation(isolation);
                    log.debug("Setting transaction isolation: {}@{}",
                            isolation, System.identityHashCode(dsc));
                } catch (SQLException ex) {
                    log.error("Could not set transaction isolation: {}@{}",
                            isolation, System.identityHashCode(dsc), ex);
                }
            }
            return conn;
        }

        private BasicDataSource getConfiguredDataSource() {
            BasicDataSource dsc;
            if (sharedDSC != null) { // i.e. shared pool
                dsc = sharedDSC;
            } else {
                Map<String, BasicDataSource> poolMap = perThreadPoolMap.get();
                dsc = poolMap.get(getDataSourceName());
                if (dsc == null) {
                    dsc = initPool("1");
                    poolMap.put(getDataSourceName(), dsc);
                    log.debug("Storing pool: {}@{}", getName(), System.identityHashCode(dsc));
                    perThreadPoolSet.add(dsc);
                }
            }
            return dsc;
        }
    }

    @Override
    public void addConfigElement(ConfigElement config) {
    }

    @Override
    public boolean expectsModification() {
        return false;
    }

    /**
     * @return Returns the checkQuery.
     */
    public String getCheckQuery() {
        return checkQuery;
    }

    /**
     * @param checkQuery The checkQuery to set.
     */
    public void setCheckQuery(String checkQuery) {
        this.checkQuery = checkQuery;
    }

    /**
     * @return Returns the connectionAge.
     */
    public String getConnectionAge() {
        return connectionAge;
    }

    /**
     * @param connectionAge The connectionAge to set.
     */
    public void setConnectionAge(String connectionAge) {
        this.connectionAge = connectionAge;
    }

    /**
     * @return Returns the poolname.
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource The poolname to set.
     */
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    private String getDataSourceName() {
        return getDataSource();
    }

    /**
     * @return Returns the dbUrl.
     */
    public String getDbUrl() {
        return dbUrl;
    }

    /**
     * @param dbUrl The dbUrl to set.
     */
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * @return Returns the driver.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @param driver The driver to set.
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the poolMax.
     */
    public String getPoolMax() {
        return poolMax;
    }

    /**
     * @param poolMax The poolMax to set.
     */
    public void setPoolMax(String poolMax) {
        this.poolMax = poolMax;
    }

    /**
     * @return Returns the timeout.
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * @return Returns the trimInterval.
     */
    public String getTrimInterval() {
        return trimInterval;
    }

    /**
     * @param trimInterval The trimInterval to set.
     */
    public void setTrimInterval(String trimInterval) {
        this.trimInterval = trimInterval;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Returns the autocommit.
     */
    public boolean isAutocommit() {
        return autocommit;
    }

    /**
     * @param autocommit The autocommit to set.
     */
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    /**
     * @return Returns the preinit.
     */
    public boolean isPreinit() {
        return preinit;
    }

    /**
     * @param preinit The preinit to set.
     */
    public void setPreinit(boolean preinit) {
        this.preinit = preinit;
    }

    /**
     * @return Returns the keepAlive.
     */
    public boolean isKeepAlive() {
        return keepAlive;
    }

    /**
     * @param keepAlive The keepAlive to set.
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * @return the transaction isolation level
     */
    public String getTransactionIsolation() {
        return transactionIsolation;
    }

    /**
     * @param transactionIsolation The transaction isolation level to set. <code>NULL</code> to
     *                             use the default of the driver.
     */
    public void setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    /**
     * @return the initQuery
     */
    public String getInitQuery() {
        return initQuery;
    }

    /**
     * @param initQuery the initQuery to set
     */
    public void setInitQuery(String initQuery) {
        this.initQuery = initQuery;
    }

    /**
     * @return the connectionProperties
     */
    public String getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * @param connectionProperties the connectionProperties to set
     */
    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    /**
     * Return the max number of pooled prepared statements. "0" means no limit
     * on prepared statements to pool and "-1" disables pooling.
     *
     * @return the max number of pooled prepared statements
     */
    public String getPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    /**
     * Set the max number of pooled prepared statements. "0" means no limit
     * on prepared statements to pool and "-1" disables pooling.
     *
     * @param poolPreparedStatements max number of prepared statements
     */
    public void setPoolPreparedStatements(String poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }
}
