/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.metersphere.api.dubbo.utils;

import io.metersphere.api.dubbo.MethodArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Constants
 */
public class Constants {

    //Registry Protocol
    public static final String REGISTRY_NONE = "none";
    public static final String REGISTRY_ZOOKEEPER = "zookeeper";
    public static final String REGISTRY_NACOS = "nacos";
    public static final String APOLLO = "apollo";
    public static final String REGISTRY_MULTICAST = "multicast";
    public static final String REGISTRY_REDIS = "redis";
    public static final String REGISTRY_SIMPLE = "simple";

    //RPC Protocol
    public static final String RPC_PROTOCOL_DUBBO = "dubbo";
    public static final String RPC_PROTOCOL_RMI = "rmi";
    public static final String RPC_PROTOCOL_HESSIAN = "hessian";
    public static final String RPC_PROTOCOL_HTTP = "http";
    public static final String RPC_PROTOCOL_WEBSERVICE = "webservice";
    public static final String RPC_PROTOCOL_THRIFT = "thrift";
    public static final String RPC_PROTOCOL_MEMCACHED = "memcached";
    public static final String RPC_PROTOCOL_REDIS = "redis";

    public static final String ASYNC = "async";
    public static final String SYMBOL = "://";

    public static final int INT_DEFAULT = 0;
    public static final double DOUBLE_DEFAULT = 0.0d;
    public static final boolean BOOLEAN_DEFAULT = false;
    public static final char CHAR_DEFAULT = '\u0000';
    public static final float FLOAT_DEFAULT = 0.0f;
    public static final byte BYTE_DEFAULT = 0;
    public static final long LONG_DEFAULT = 0l;
    public static final short SHORT_DEFAULT = 0;
    public static final int[] INT_ARRAY_DEFAULT = null;
    public static final double[] DOUBLE_ARRAY_DEFAULT = null;
    public static final boolean[] BOOLEAN_ARRAY_DEFAULT = null;
    public static final char[] CHAT_ARRAY_DEFAULT = null;
    public static final float[] FLOAT_ARRAY_DEFAULT = null;
    public static final byte[] BYTE_ARRAY_DEFAULT = null;
    public static final long[] LONG_ARRAY_DEFAULT = null;
    public static final short[] SHORT_ARRAY_DEFAULT = null;

    public static final String FIELD_DUBBO_REGISTRY_PROTOCOL = "FIELD_DUBBO_REGISTRY_PROTOCOL";
    public static final String FIELD_DUBBO_REGISTRY_GROUP = "FIELD_DUBBO_REGISTRY_GROUP";
    public static final String FIELD_DUBBO_REGISTRY_USER_NAME = "FIELD_DUBBO_REGISTRY_USER_NAME";
    public static final String FIELD_DUBBO_REGISTRY_PASSWORD = "FIELD_DUBBO_REGISTRY_PASSWORD";
    public static final String FIELD_DUBBO_REGISTRY_TIMEOUT = "FIELD_DUBBO_REGISTRY_TIMEOUT";
    public static final String FIELD_DUBBO_CONFIG_CENTER_PROTOCOL = "FIELD_DUBBO_CONFIG_CENTER_PROTOCOL";
    public static final String FIELD_DUBBO_CONFIG_CENTER_GROUP = "FIELD_DUBBO_CONFIG_CENTER_GROUP";
    public static final String FIELD_DUBBO_CONFIG_CENTER_NAMESPACE = "FIELD_DUBBO_CONFIG_CENTER_NAMESPACE";
    public static final String FIELD_DUBBO_CONFIG_CENTER_USER_NAME = "FIELD_DUBBO_CONFIG_CENTER_USER_NAME";
    public static final String FIELD_DUBBO_CONFIG_CENTER_PASSWORD = "FIELD_DUBBO_CONFIG_CENTER_PASSWORD";
    public static final String FIELD_DUBBO_CONFIG_CENTER_TIMEOUT = "FIELD_DUBBO_CONFIG_CENTER_TIMEOUT";
    public static final String FIELD_DUBBO_CONFIG_CENTER_ADDRESS = "FIELD_DUBBO_CONFIG_CENTER_ADDRESS";
    public static final String FIELD_DUBBO_RPC_PROTOCOL = "FIELD_DUBBO_RPC_PROTOCOL";
    public static final String FIELD_DUBBO_ADDRESS = "FIELD_DUBBO_ADDRESS";
    public static final String FIELD_DUBBO_TIMEOUT = "FIELD_DUBBO_TIMEOUT";
    public static final String FIELD_DUBBO_VERSION = "FIELD_DUBBO_VERSION";
    public static final String FIELD_DUBBO_RETRIES = "FIELD_DUBBO_RETRIES";
    public static final String FIELD_DUBBO_CLUSTER = "FIELD_DUBBO_CLUSTER";
    public static final String FIELD_DUBBO_GROUP = "FIELD_DUBBO_GROUP";
    public static final String FIELD_DUBBO_CONNECTIONS = "FIELD_DUBBO_CONNECTIONS";
    public static final String FIELD_DUBBO_LOADBALANCE = "FIELD_DUBBO_LOADBALANCE";
    public static final String FIELD_DUBBO_ASYNC = "FIELD_DUBBO_ASYNC";
    public static final String FIELD_DUBBO_INTERFACE = "FIELD_DUBBO_INTERFACE";
    public static final String FIELD_DUBBO_METHOD = "FIELD_DUBBO_METHOD";
    public static final String FIELD_DUBBO_METHOD_ARGS = "FIELD_DUBBO_METHOD_ARGS";
    public static final String FIELD_DUBBO_METHOD_ARGS_SIZE = "FIELD_DUBBO_METHOD_ARGS_SIZE";
    public static final String FIELD_DUBBO_ATTACHMENT_ARGS = "FIELD_DUBBO_ATTACHMENT_ARGS";
    public static final String FIELD_DUBBO_ATTACHMENT_ARGS_SIZE = "FIELD_DUBBO_ATTACHMENT_ARGS_SIZE";
    public static final String DEFAULT_TIMEOUT = "1000";
    public static final String DEFAULT_VERSION = "1.0";
    public static final String DEFAULT_RETRIES = "0";
    public static final String DEFAULT_CLUSTER = "failfast";
    public static final String DEFAULT_CONNECTIONS = "100";

    //冗余配置元件中的address、protocols、group,用于在sample gui获取配置元件中的默认值
    public static String DEFAULT_PANEL_ADDRESS = "";
    public static String DEFAULT_PANEL_PROTOCOLS = "";
    public static String DEFAULT_PANEL_GROUP = "";

    public static final void redundancy(TestElement element) {
        DEFAULT_PANEL_ADDRESS = Constants.getAddress(element);
        DEFAULT_PANEL_PROTOCOLS = Constants.getRegistryProtocol(element);
        DEFAULT_PANEL_GROUP = Constants.getRegistryGroup(element);
    }

    /**
     * get Registry Protocol
     *
     * @return the protocol
     */
    public static final String getRegistryProtocol(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_REGISTRY_PROTOCOL);
    }

    /**
     * set Registry Protocol
     *
     * @param registryProtocol the protocol to set
     */
    public static final void setRegistryProtocol(String registryProtocol, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_REGISTRY_PROTOCOL, StringUtils.trimAllWhitespace(registryProtocol)));
    }

    /**
     * get Registry Group
     *
     * @return the group
     */
    public static final String getRegistryGroup(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_REGISTRY_GROUP);
    }

    /**
     * set Registry Group
     *
     * @param registryGroup the group to set
     */
    public static final void setRegistryGroup(String registryGroup, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_REGISTRY_GROUP, StringUtils.trimAllWhitespace(registryGroup)));
    }

    /**
     * get Registry username
     *
     * @return the username
     */
    public static final String getRegistryUserName(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_REGISTRY_USER_NAME);
    }

    /**
     * set Registry username
     *
     * @param username the username to set
     */
    public static final void setRegistryUserName(String username, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_REGISTRY_USER_NAME, StringUtils.trimAllWhitespace(username)));
    }

    /**
     * get Registry password
     *
     * @return the password
     */
    public static final String getRegistryPassword(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_REGISTRY_PASSWORD);
    }

    /**
     * set Registry password
     *
     * @param password the password to set
     */
    public static final void setRegistryPassword(String password, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_REGISTRY_PASSWORD, StringUtils.trimAllWhitespace(password)));
    }

    /**
     * get Registry timeout
     *
     * @return the timeout
     */
    public static final String getRegistryTimeout(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_REGISTRY_TIMEOUT);
    }

    /**
     * set Registry timeout
     *
     * @param timeout the group to set
     */
    public static final void setRegistryTimeout(String timeout, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_REGISTRY_TIMEOUT, StringUtils.trimAllWhitespace(timeout)));
    }

    /**
     * get ConfigCenter protocol
     *
     * @return the protocol
     */
    public static final String getConfigCenterProtocol(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONFIG_CENTER_PROTOCOL);
    }

    /**
     * set ConfigCenter protocol
     *
     * @param protocol the protocol to set
     */
    public static final void setConfigCenterProtocol(String protocol, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONFIG_CENTER_PROTOCOL, StringUtils.trimAllWhitespace(protocol)));
    }

    /**
     * get ConfigCenter group
     *
     * @return the group
     */
    public static final String getConfigCenterGroup(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONFIG_CENTER_GROUP);
    }

    /**
     * set ConfigCenter group
     *
     * @param group the group to set
     */
    public static final void setConfigCenterGroup(String group, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONFIG_CENTER_GROUP, StringUtils.trimAllWhitespace(group)));
    }

    /**
     * get ConfigCenter namespace
     *
     * @return the namespace
     */
    public static final String getConfigCenterNamespace(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONFIG_CENTER_NAMESPACE);
    }

    /**
     * set ConfigCenter namespace
     *
     * @param namespace the namespace to set
     */
    public static final void setConfigCenterNamespace(String namespace, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONFIG_CENTER_NAMESPACE, StringUtils.trimAllWhitespace(namespace)));
    }

    /**
     * get ConfigCenter username
     *
     * @return the username
     */
    public static final String getConfigCenterUserName(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONFIG_CENTER_USER_NAME);
    }

    /**
     * set ConfigCenter username
     *
     * @param username the username to set
     */
    public static final void setConfigCenterUserName(String username, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONFIG_CENTER_USER_NAME, StringUtils.trimAllWhitespace(username)));
    }

    /**
     * get ConfigCenter password
     *
     * @return the password
     */
    public static final String getConfigCenterPassword(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONFIG_CENTER_PASSWORD);
    }

    /**
     * set ConfigCenter password
     *
     * @param password the password to set
     */
    public static final void setConfigCenterPassword(String password, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONFIG_CENTER_PASSWORD, StringUtils.trimAllWhitespace(password)));
    }

    /**
     * get ConfigCenter address
     *
     * @return the address
     */
    public static final String getConfigCenterAddress(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONFIG_CENTER_ADDRESS);
    }

    /**
     * set ConfigCenter namespace
     *
     * @param address the address to set
     */
    public static final void setConfigCenterAddress(String address, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONFIG_CENTER_ADDRESS, StringUtils.trimAllWhitespace(address)));
    }

    /**
     * get ConfigCenter timeout
     *
     * @return the timeout
     */
    public static final String getConfigCenterTimeout(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONFIG_CENTER_TIMEOUT);
    }

    /**
     * set ConfigCenter namespace
     *
     * @param timeout the timeout to set
     */
    public static final void setConfigCenterTimeout(String timeout, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONFIG_CENTER_TIMEOUT, StringUtils.trimAllWhitespace(timeout)));
    }

    /**
     * get RPC protocol
     *
     * @return the RPC protocol
     */
    public static final String getRpcProtocol(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_RPC_PROTOCOL);
    }

    /**
     * set RPC protocol
     *
     * @param rpcProtocol the protocol to set
     */
    public static final void setRpcProtocol(String rpcProtocol, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_RPC_PROTOCOL, StringUtils.trimAllWhitespace(rpcProtocol)));
    }

    /**
     * get address
     *
     * @return the address
     */
    public static final String getAddress(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_ADDRESS);
    }

    /**
     * set address
     *
     * @param address the address to set
     */
    public static final void setAddress(String address, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_ADDRESS, StringUtils.trimAllWhitespace(address)));
    }

    /**
     * get timeout
     *
     * @return the timeout
     */
    public static final String getTimeout(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_TIMEOUT, DEFAULT_TIMEOUT);
    }

    /**
     * set timeout
     *
     * @param timeout the timeout to set
     */
    public static final void setTimeout(String timeout, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_TIMEOUT, StringUtils.trimAllWhitespace(timeout)));
    }

    /**
     * get version
     *
     * @return the version
     */
    public static final String getVersion(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_VERSION, DEFAULT_VERSION);
    }

    /**
     * set version
     *
     * @param version the version to set
     */
    public static final void setVersion(String version, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_VERSION, StringUtils.trimAllWhitespace(version)));
    }

    /**
     * get retries
     *
     * @return the retries
     */
    public static final String getRetries(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_RETRIES, DEFAULT_RETRIES);
    }

    /**
     * set retries
     *
     * @param retries the retries to set
     */
    public static final void setRetries(String retries, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_RETRIES, StringUtils.trimAllWhitespace(retries)));
    }

    /**
     * get cluster
     *
     * @return the cluster
     */
    public static final String getCluster(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CLUSTER, DEFAULT_CLUSTER);
    }

    /**
     * set cluster
     *
     * @param cluster the cluster to set
     */
    public static final void setCluster(String cluster, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CLUSTER, StringUtils.trimAllWhitespace(cluster)));
    }

    /**
     * get group
     *
     * @return the group
     */
    public static final String getGroup(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_GROUP, null);
    }

    /**
     * set group
     *
     * @param group the group to set
     */
    public static final void setGroup(String group, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_GROUP, StringUtils.trimAllWhitespace(group)));
    }

    /**
     * get connections
     *
     * @return the group
     */
    public static final String getConnections(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_CONNECTIONS, DEFAULT_CONNECTIONS);
    }

    /**
     * set connections
     *
     * @param connections the connections to set
     */
    public static final void setConnections(String connections, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_CONNECTIONS, StringUtils.trimAllWhitespace(connections)));
    }

    /**
     * get loadbalance
     *
     * @return the loadbalance
     */
    public static final String getLoadbalance(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_LOADBALANCE);
    }

    /**
     * set loadbalance
     *
     * @param loadbalance the loadbalance to set
     */
    public static final void setLoadbalance(String loadbalance, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_LOADBALANCE, StringUtils.trimAllWhitespace(loadbalance)));
    }

    /**
     * get async
     *
     * @return the async
     */
    public static final String getAsync(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_ASYNC);
    }

    /**
     * set async
     *
     * @param async the async to set
     */
    public static final void setAsync(String async, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_ASYNC, StringUtils.trimAllWhitespace(async)));
    }

    /**
     * get interfaceName
     *
     * @return the interfaceName
     */
    public static final String getInterface(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_INTERFACE);
    }

    /**
     * set interfaceName
     *
     * @param interfaceName the interfaceName to set
     */
    public static final void setInterfaceName(String interfaceName, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_INTERFACE, StringUtils.trimAllWhitespace(interfaceName)));
    }

    /**
     * get method
     *
     * @return the method
     */
    public static final String getMethod(TestElement element) {
        return element.getPropertyAsString(FIELD_DUBBO_METHOD);
    }

    /**
     * set method
     *
     * @param method the method to set
     */
    public static final void setMethod(String method, TestElement element) {
        element.setProperty(new StringProperty(FIELD_DUBBO_METHOD, StringUtils.trimAllWhitespace(method)));
    }

    /**
     * get methodArgs
     *
     * @return the methodArgs
     */
    public static final List<MethodArgument> getMethodArgs(TestElement element) {
        int paramsSize = element.getPropertyAsInt(FIELD_DUBBO_METHOD_ARGS_SIZE, 0);
        List<MethodArgument> list = new ArrayList<MethodArgument>();
        for (int i = 1; i <= paramsSize; i++) {
            String paramType = element.getPropertyAsString(FIELD_DUBBO_METHOD_ARGS + "_PARAM_TYPE" + i);
            String paramValue = element.getPropertyAsString(FIELD_DUBBO_METHOD_ARGS + "_PARAM_VALUE" + i);
            MethodArgument args = new MethodArgument(paramType, paramValue);
            list.add(args);
        }
        return list;
    }

    /**
     * set methodArgs
     *
     * @param methodArgs the methodArgs to set
     */
    public static final void setMethodArgs(List<MethodArgument> methodArgs, TestElement element) {
        int size = methodArgs == null ? 0 : methodArgs.size();
        element.setProperty(new IntegerProperty(FIELD_DUBBO_METHOD_ARGS_SIZE, size));
        if (size > 0) {
            for (int i = 1; i <= methodArgs.size(); i++) {
                element.setProperty(new StringProperty(FIELD_DUBBO_METHOD_ARGS + "_PARAM_TYPE" + i, methodArgs.get(i - 1).getParamType()));
                element.setProperty(new StringProperty(FIELD_DUBBO_METHOD_ARGS + "_PARAM_VALUE" + i, methodArgs.get(i - 1).getParamValue()));
            }
        }
    }

    /**
     * get attachmentArgs
     *
     * @return the attachmentArgs
     */
    public static final List<MethodArgument> getAttachmentArgs(TestElement element) {
        int paramsSize = element.getPropertyAsInt(FIELD_DUBBO_ATTACHMENT_ARGS_SIZE, 0);
        List<MethodArgument> list = new ArrayList<MethodArgument>();
        for (int i = 1; i <= paramsSize; i++) {
            String paramType = element.getPropertyAsString(FIELD_DUBBO_ATTACHMENT_ARGS + "_KEY" + i);
            String paramValue = element.getPropertyAsString(FIELD_DUBBO_ATTACHMENT_ARGS + "_VALUE" + i);
            MethodArgument args = new MethodArgument(paramType, paramValue);
            list.add(args);
        }
        return list;
    }

    /**
     * set attachmentArgs
     *
     * @param methodArgs the attachmentArgs to set
     */
    public static final void setAttachmentArgs(List<MethodArgument> methodArgs, TestElement element) {
        int size = methodArgs == null ? 0 : methodArgs.size();
        element.setProperty(new IntegerProperty(FIELD_DUBBO_ATTACHMENT_ARGS_SIZE, size));
        if (size > 0) {
            for (int i = 1; i <= methodArgs.size(); i++) {
                element.setProperty(new StringProperty(FIELD_DUBBO_ATTACHMENT_ARGS + "_KEY" + i, methodArgs.get(i - 1).getParamType()));
                element.setProperty(new StringProperty(FIELD_DUBBO_ATTACHMENT_ARGS + "_VALUE" + i, methodArgs.get(i - 1).getParamValue()));
            }
        }
    }

}
