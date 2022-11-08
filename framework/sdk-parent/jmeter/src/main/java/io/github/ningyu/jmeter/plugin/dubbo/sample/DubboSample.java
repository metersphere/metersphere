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
package io.github.ningyu.jmeter.plugin.dubbo.sample;

import io.github.ningyu.jmeter.plugin.util.*;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.dubbo.common.constants.CommonConstants.GENERIC_SERIALIZATION_DEFAULT;

/**
 * DubboSample
 */
public class DubboSample extends AbstractSampler implements Interruptible {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final long serialVersionUID = -6794913295411458705L;
    public static ApplicationConfig application = new ApplicationConfig("DubboSample");

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        //构造请求数据
        res.setSamplerData(getSampleData());
        //调用dubbo
        res.setResponseData(JsonUtils.toJson(callDubbo(res)), StandardCharsets.UTF_8.name());
        //构造响应数据
        res.setDataType(SampleResult.TEXT);
        //dubbo协议执行没有时间，现在默认执行时间为当前时间
        res.setStartTime(System.currentTimeMillis());
        res.setEndTime(System.currentTimeMillis());
        return res;
    }

    /**
     * Construct request data
     */
    private String getSampleData() {
        log.info("sample中的实例id" + this.toString() + ",element名称" + this.getName());
        StringBuilder sb = new StringBuilder();
        sb.append("Registry Protocol: ").append(Constants.getRegistryProtocol(this)).append("\n");
        sb.append("Address: ").append(Constants.getAddress(this)).append("\n");
        sb.append("RPC Protocol: ").append(Constants.getRpcProtocol(this)).append("\n");
        sb.append("Timeout: ").append(Constants.getTimeout(this)).append("\n");
        sb.append("Version: ").append(Constants.getVersion(this)).append("\n");
        sb.append("Retries: ").append(Constants.getRetries(this)).append("\n");
        sb.append("Cluster: ").append(Constants.getCluster(this)).append("\n");
        sb.append("Group: ").append(Constants.getGroup(this)).append("\n");
        sb.append("Connections: ").append(Constants.getConnections(this)).append("\n");
        sb.append("LoadBalance: ").append(Constants.getLoadbalance(this)).append("\n");
        sb.append("Async: ").append(Constants.getAsync(this)).append("\n");
        sb.append("Interface: ").append(Constants.getInterface(this)).append("\n");
        sb.append("Method: ").append(Constants.getMethod(this)).append("\n");
        sb.append("Method Args: ").append(Constants.getMethodArgs(this).toString());
        sb.append("Attachment Args: ").append(Constants.getAttachmentArgs(this).toString());
        return sb.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object callDubbo(SampleResult res) {
        // This instance is heavy, encapsulating the connection to the registry and the connection to the provider,
        // so please cache yourself, otherwise memory and connection leaks may occur.
        ReferenceConfig reference = new ReferenceConfig();
        // set application
        reference.setApplication(application);
        /* registry center */
        String address = Constants.getAddress(this);
        if (StringUtils.isBlank(address)) {
            setResponseError(res, ErrorCode.MISS_ADDRESS);
            return ErrorCode.MISS_ADDRESS.getMessage();
        }
        RegistryConfig registry;
        String rpcProtocol = Constants.getRpcProtocol(this).replaceAll("://", "");
        String protocol = Constants.getRegistryProtocol(this);
        String registryGroup = Constants.getRegistryGroup(this);
        Integer registryTimeout = null;
        try {
            if (!StringUtils.isBlank(Constants.getRegistryTimeout(this))) {
                registryTimeout = Integer.valueOf(Constants.getRegistryTimeout(this));
            }
        } catch (NumberFormatException e) {
            setResponseError(res, ErrorCode.TIMEOUT_ERROR);
            return ErrorCode.TIMEOUT_ERROR.getMessage();
        }
        if (StringUtils.isBlank(protocol) || Constants.REGISTRY_NONE.equals(protocol)) {
            //direct connection
            StringBuilder sb = new StringBuilder();
            sb.append(Constants.getRpcProtocol(this))
                    .append(Constants.getAddress(this))
                    .append("/").append(Constants.getInterface(this));
            log.debug("rpc invoker url : " + sb.toString());
            reference.setUrl(sb.toString());
        } else if (Constants.REGISTRY_SIMPLE.equals(protocol)) {
            registry = new RegistryConfig();
            registry.setAddress(address);
            reference.setProtocol(rpcProtocol);
            reference.setRegistry(registry);
        } else {
            registry = new RegistryConfig();
            registry.setProtocol(protocol);
            registry.setGroup(registryGroup);
            registry.setAddress(address);
            if (registryTimeout != null) {
                registry.setTimeout(registryTimeout);
            }
            reference.setProtocol(rpcProtocol);
            reference.setRegistry(registry);
        }
        /* config center */
        try {
            String configCenterProtocol = Constants.getConfigCenterProtocol(this);
            if (!StringUtils.isBlank(configCenterProtocol)) {
                String configCenterGroup = Constants.getConfigCenterGroup(this);
                String configCenterNamespace = Constants.getConfigCenterNamespace(this);
                String configCenterAddress = Constants.getConfigCenterAddress(this);
                if (StringUtils.isBlank(configCenterAddress)) {
                    setResponseError(res, ErrorCode.MISS_ADDRESS);
                    return ErrorCode.MISS_ADDRESS.getMessage();
                }
                Long configCenterTimeout = null;
                try {
                    if (!StringUtils.isBlank(Constants.getConfigCenterTimeout(this))) {
                        configCenterTimeout = Long.valueOf(Constants.getConfigCenterTimeout(this));
                    }
                } catch (NumberFormatException e) {
                    setResponseError(res, ErrorCode.TIMEOUT_ERROR);
                    return ErrorCode.TIMEOUT_ERROR.getMessage();
                }
                ConfigCenterConfig configCenter = new ConfigCenterConfig();
                configCenter.setProtocol(configCenterProtocol);
                configCenter.setGroup(configCenterGroup);
                configCenter.setNamespace(configCenterNamespace);
                configCenter.setAddress(configCenterAddress);
                if (configCenterTimeout != null) {
                    configCenter.setTimeout(configCenterTimeout);
                }
                reference.setConfigCenter(configCenter);
            }
        } catch (IllegalStateException e) {
            /* Duplicate Config */
            setResponseError(res, ErrorCode.DUPLICATE_CONFIGCENTERCONFIG);
            return ErrorCode.DUPLICATE_CONFIGCENTERCONFIG.getMessage();
        }
        try {
            // set interface
            String interfaceName = Constants.getInterface(this);
            if (StringUtils.isBlank(interfaceName)) {
                setResponseError(res, ErrorCode.MISS_INTERFACE);
                return ErrorCode.MISS_INTERFACE.getMessage();
            }
            reference.setInterface(interfaceName);

            // set retries
            Integer retries = null;
            try {
                if (!StringUtils.isBlank(Constants.getRetries(this))) {
                    retries = Integer.valueOf(Constants.getRetries(this));
                }
            } catch (NumberFormatException e) {
                setResponseError(res, ErrorCode.RETRIES_ERROR);
                return ErrorCode.RETRIES_ERROR.getMessage();
            }
            if (retries != null) {
                reference.setRetries(retries);
            }

            // set cluster
            String cluster = Constants.getCluster(this);
            if (!StringUtils.isBlank(cluster)) {
                reference.setCluster(Constants.getCluster(this));
            }

            // set version
            String version = Constants.getVersion(this);
            if (!StringUtils.isBlank(version)) {
                reference.setVersion(version);
            }

            // set timeout
            Integer timeout = null;
            try {
                if (!StringUtils.isBlank(Constants.getTimeout(this))) {
                    timeout = Integer.valueOf(Constants.getTimeout(this));
                }
            } catch (NumberFormatException e) {
                setResponseError(res, ErrorCode.TIMEOUT_ERROR);
                return ErrorCode.TIMEOUT_ERROR.getMessage();
            }
            if (timeout != null) {
                reference.setTimeout(timeout);
            }

            // set group
            String group = Constants.getGroup(this);
            if (!StringUtils.isBlank(group)) {
                reference.setGroup(group);
            }

            // set connections
            Integer connections = null;
            try {
                if (!StringUtils.isBlank(Constants.getConnections(this))) {
                    connections = Integer.valueOf(Constants.getConnections(this));
                }
            } catch (NumberFormatException e) {
                setResponseError(res, ErrorCode.CONNECTIONS_ERROR);
                return ErrorCode.CONNECTIONS_ERROR.getMessage();
            }
            if (connections != null) {
                reference.setConnections(connections);
            }

            // set loadBalance
            String loadBalance = Constants.getLoadbalance(this);
            if (!StringUtils.isBlank(loadBalance)) {
                reference.setLoadbalance(loadBalance);
            }

            // set async
            String async = Constants.getAsync(this);
            if (!StringUtils.isBlank(async)) {
                reference.setAsync(Constants.ASYNC.equals(async));
            }

            // set generic
            reference.setGeneric(GENERIC_SERIALIZATION_DEFAULT);

            CheckUtils.checkZookeeper(reference);

            String methodName = Constants.getMethod(this);
            if (StringUtils.isBlank(methodName)) {
                setResponseError(res, ErrorCode.MISS_METHOD);
                return ErrorCode.MISS_METHOD.getMessage();
            }

            // The registry's address is to generate the ReferenceConfigCache key
            ReferenceConfigCache cache = ReferenceConfigCache.getCache(Constants.getAddress(this));
            GenericService genericService = (GenericService) cache.get(reference);
            if (genericService == null) {
                setResponseError(res, ErrorCode.GENERIC_SERVICE_IS_NULL);
                return MessageFormat.format(ErrorCode.GENERIC_SERVICE_IS_NULL.getMessage(), interfaceName);
            }
            String[] parameterTypes;
            Object[] parameterValues;
            List<MethodArgument> args = Constants.getMethodArgs(this);
            List<String> parameterTypeList = new ArrayList<>();
            List<Object> parameterValuesList = new ArrayList<>();
            for (MethodArgument arg : args) {
                ClassUtils.parseParameter(parameterTypeList, parameterValuesList, arg);
            }
            parameterTypes = parameterTypeList.toArray(new String[0]);
            parameterValues = parameterValuesList.toArray(new Object[0]);

            List<MethodArgument> attachmentArgs = Constants.getAttachmentArgs(this);
            if (!attachmentArgs.isEmpty()) {
                RpcContext.getContext().setAttachments(attachmentArgs.stream().collect(Collectors.toMap(MethodArgument::getParamType, MethodArgument::getParamValue)));
            }

            res.sampleStart();
            Object result;
            try {
                result = genericService.$invoke(methodName, parameterTypes, parameterValues);
                res.setResponseOK();
            } catch (Exception e) {
                log.error("Exception：", e);
                if (e instanceof RpcException) {
                    RpcException rpcException = (RpcException) e;
                    setResponseError(res, String.valueOf(rpcException.getCode()), rpcException.getMessage());
                } else {
                    setResponseError(res, ErrorCode.UNKNOWN_EXCEPTION);
                }
                result = e;
            }
            res.sampleEnd();
            cache.destroy(reference); // 清除缓存
            return result;
        } catch (Exception e) {
            log.error("UnknownException：", e);
            setResponseError(res, ErrorCode.UNKNOWN_EXCEPTION);
            return e;
        }
    }

    public void setResponseError(SampleResult res, ErrorCode errorCode) {
        setResponseError(res, errorCode.getCode(), errorCode.getMessage());
    }

    public void setResponseError(SampleResult res, String code, String message) {
        res.setSuccessful(false);
        res.setResponseCode(code);
        res.setResponseMessage(message);
    }

    @Override
    public boolean interrupt() {
        Thread.currentThread().interrupt();
        return true;
    }
}
