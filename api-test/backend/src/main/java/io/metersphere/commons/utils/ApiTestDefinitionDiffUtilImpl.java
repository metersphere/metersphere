package io.metersphere.commons.utils;

import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.log.utils.ApiDefinitionDiffUtil;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.utils.diff.json.JacksonDiff;
import io.metersphere.log.utils.diff.json.JsonDiff;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.DefinitionReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiTestDefinitionDiffUtilImpl implements ApiDefinitionDiffUtil {
    public static final String JSON_START = "{\"root\":";
    public static final String JSON_END = "}";
    public static final String TYPE = "type";
    public static final String HTTP = "HTTP";
    public static final String BACK_SCRIPT = "backScript";
    public static final String HEADS = "headers";
    public static final String STATUS_CODE = "statusCode";
    public static final String BODY = "body";
    public static final String QUERY = "query";
    public static final String BODY_FORM = "body_form";
    public static final String BODY_RAW = "body_raw";
    public static final String REQUEST = "request";
    public static final String BODY_XML = "body_xml";
    public static final String SCRIPT = ElementConstants.SCRIPT;

    @Override
    public String diffResponse(String newValue, String oldValue) {
        Map<String, String> diffMap = new LinkedHashMap<>();
        JSONObject bloBsIsNew = JSONUtil.parseObject(newValue);
        JSONObject bloBsIsOld = JSONUtil.parseObject(oldValue);
        if (bloBsIsNew == null || StringUtils.isEmpty(bloBsIsNew.getString(TYPE))) {
            return null;
        }
        JsonDiff jsonDiff = new JacksonDiff();
        if (bloBsIsNew.getString(TYPE).equals(HTTP)) {
            diffHttpResponse(bloBsIsNew, bloBsIsOld, jsonDiff, diffMap);
            if (diffMap.size() > 0) {
                diffMap.put(TYPE, bloBsIsNew.getString(TYPE));
            }
        }
        return JSON.toJSONString(diffMap);
    }

    @Override
    public String diff(String newValue, String oldValue) {
        try {
            JSONObject bloBsIsNew = JSONUtil.parseObject(newValue);
            JSONObject bloBsIsOld = JSONUtil.parseObject(oldValue);
            if (bloBsIsNew == null || StringUtils.isEmpty(bloBsIsNew.getString(TYPE))) {
                return null;
            }
            Map<String, String> diffMap = new LinkedHashMap<>();
            diffMap.put(TYPE, bloBsIsNew.getString(TYPE));
            JsonDiff jsonDiff = new JacksonDiff();
            if (bloBsIsNew.getString(TYPE).equals(ElementConstants.TCP_SAMPLER)) {
                MsTCPSampler tcpSamplerNew = JSON.parseObject(bloBsIsNew.toString(), MsTCPSampler.class);
                MsTCPSampler tcpSamplerOld = JSON.parseObject(bloBsIsOld.toString(), MsTCPSampler.class);
                diffTcp(tcpSamplerNew, tcpSamplerOld, jsonDiff, diffMap);
            } else if (bloBsIsNew.getString(TYPE).equals(ElementConstants.HTTP_SAMPLER)) {
                MsHTTPSamplerProxy httpSamplerProxyNew = JSON.parseObject(bloBsIsNew.toString(), MsHTTPSamplerProxy.class);
                MsHTTPSamplerProxy httpSamplerProxyOld = JSON.parseObject(bloBsIsOld.toString(), MsHTTPSamplerProxy.class);
                diffHttp(httpSamplerProxyNew, httpSamplerProxyOld, jsonDiff, diffMap);
            } else if (bloBsIsNew.getString(TYPE).equals(ElementConstants.JDBC_SAMPLER)) {
                MsJDBCSampler jdbcSamplerNew = JSON.parseObject(bloBsIsNew.toString(), MsJDBCSampler.class);
                MsJDBCSampler jdbcSamplerOld = JSON.parseObject(bloBsIsOld.toString(), MsJDBCSampler.class);
                diffJdbc(jdbcSamplerNew, jdbcSamplerOld, jsonDiff, diffMap);
            } else {
                MsDubboSampler dubboSamplerNew = JSON.parseObject(bloBsIsNew.toString(), MsDubboSampler.class);
                MsDubboSampler dubboSamplerOld = JSON.parseObject(bloBsIsOld.toString(), MsDubboSampler.class);
                diffDubbo(dubboSamplerNew, dubboSamplerOld, jsonDiff, diffMap);
            }
            if (diffMap.size() > 1) {
                return JSON.toJSONString(diffMap);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    private static void diffHttp(MsHTTPSamplerProxy httpNew, MsHTTPSamplerProxy httpOld, JsonDiff jsonDiff, Map<String, String> diffMap) {
        // 请求头对比 old/new
        if (CollectionUtils.isNotEmpty(httpNew.getHeaders())) {
            removeSpaceName(httpNew.getHeaders());
        }
        if (CollectionUtils.isNotEmpty(httpOld.getHeaders())) {
            removeSpaceName(httpOld.getHeaders());
        }

        String headerNew = StringUtils.join(JSON_START, JSON.toJSONString(httpNew.getHeaders()), JSON_END);
        String headerOld = StringUtils.join(JSON_START, JSON.toJSONString(httpOld.getHeaders()), JSON_END);
        if (!StringUtils.equals(headerNew, headerOld)) {
            String patch = jsonDiff.diff(headerOld, headerNew);
            String diffPatch = jsonDiff.apply(headerOld, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("header", diffPatch);
            }
        }

        // 对比QUERY参数
        if (CollectionUtils.isNotEmpty(httpNew.getArguments())) {
            removeSpaceName(httpNew.getArguments());
        }
        if (CollectionUtils.isNotEmpty(httpOld.getArguments())) {
            removeSpaceName(httpOld.getArguments());
        }

        String queryNew = StringUtils.join(JSON_START, JSON.toJSONString(httpNew.getArguments()), JSON_END);
        String queryOld = StringUtils.join(JSON_START, JSON.toJSONString(httpOld.getArguments()), JSON_END);
        if (!StringUtils.equals(queryNew, queryOld)) {
            String patch = jsonDiff.diff(queryOld, queryNew);
            String diff = jsonDiff.apply(queryOld, patch);
            if (StringUtils.isNotEmpty(diff)) {
                diffMap.put(QUERY, diff);
            }
        }
        // 对比REST参数
        if (CollectionUtils.isNotEmpty(httpNew.getRest())) {
            removeSpaceName(httpNew.getRest());
        }
        if (CollectionUtils.isNotEmpty(httpOld.getRest())) {
            removeSpaceName(httpOld.getRest());
        }
        String restNew = StringUtils.join(JSON_START, JSON.toJSONString(httpNew.getRest()), JSON_END);
        String restOld = StringUtils.join(JSON_START, JSON.toJSONString(httpOld.getRest()), JSON_END);
        if (!StringUtils.equals(restNew, restOld)) {
            String patch = jsonDiff.diff(restOld, restNew);
            String diff = jsonDiff.apply(restOld, patch);
            if (StringUtils.isNotEmpty(diff)) {
                diffMap.put("rest", diff);
            }
        }
        // 对比BODY-JSON参数
        if (httpNew.getBody() != null) {
            String bodyNew = JSON.toJSONString(httpNew.getBody());
            String bodyOld = JSON.toJSONString(httpOld.getBody());
            if (!StringUtils.equals(bodyNew, bodyOld)) {
                String patch = jsonDiff.diff(bodyOld, bodyNew);
                String diff = jsonDiff.apply(bodyOld, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put(BODY, diff);
                }
            }
            // 对比BODY-FORM参数
            if (CollectionUtils.isNotEmpty(httpNew.getBody().getKvs())) {
                removeSpaceName(httpNew.getBody().getKvs());
            }
            if (CollectionUtils.isNotEmpty(httpOld.getBody().getKvs())) {
                removeSpaceName(httpOld.getBody().getKvs());
            }
            String bodyFormNew = StringUtils.join(JSON_START, JSON.toJSONString(httpNew.getBody().getKvs()), JSON_END);
            String bodyFormOld = StringUtils.join(JSON_START, JSON.toJSONString(httpOld.getBody().getKvs()), JSON_END);
            if (!StringUtils.equals(bodyFormNew, bodyFormOld)) {
                String patch = jsonDiff.diff(bodyFormOld, bodyFormNew);
                String diff = jsonDiff.apply(bodyFormOld, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put(BODY_FORM, diff);
                }
            }
            // 对比BODY-XML参数
            if (!StringUtils.equals(httpNew.getBody().getRaw(), httpOld.getBody().getRaw())) {
                diffMap.put(StringUtils.join(BODY_RAW, "_1"), httpNew.getBody().getRaw());
                diffMap.put(StringUtils.join(BODY_RAW, "_2"), httpOld.getBody().getRaw());
            }

            // 认证配置
            if (httpNew.getAuthManager() != null || httpOld.getAuthManager() != null) {
                List<DetailColumn> authColumns = ReflexObjectUtil.getColumns(httpNew.getAuthManager(), DefinitionReference.authColumns);
                List<DetailColumn> authColumnsOld = ReflexObjectUtil.getColumns(httpOld.getAuthManager(), DefinitionReference.authColumns);
                List<DetailColumn> authDiffColumns = getColumn(authColumns, authColumnsOld);
                if (CollectionUtils.isNotEmpty(authDiffColumns)) {
                    diffMap.put("body_auth", JSON.toJSONString(authDiffColumns));
                } else if (CollectionUtils.isEmpty(authDiffColumns) && CollectionUtils.isEmpty(authColumnsOld) && CollectionUtils.isNotEmpty(authColumns)) {
                    authColumns.forEach(item -> {
                        Object value = item.getOriginalValue();
                        item.setNewValue(value);
                        item.setOriginalValue(StringUtils.EMPTY);
                    });
                    diffMap.put("body_auth", JSON.toJSONString(authColumns));
                }
            }

            // 其他设置
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(httpNew, DefinitionReference.httpColumns);
            List<DetailColumn> columnsOld = ReflexObjectUtil.getColumns(httpOld, DefinitionReference.httpColumns);
            List<DetailColumn> diffColumns = getColumn(columns, columnsOld);
            if (CollectionUtils.isNotEmpty(diffColumns)) {
                diffMap.put("body_config", JSON.toJSONString(diffColumns));
            }
        }
    }

    private static void removeSpaceName(List<KeyValue> keyValues) {
        Iterator<KeyValue> iterator = keyValues.iterator();
        while (iterator.hasNext()) {
            KeyValue next = iterator.next();
            if (StringUtils.isBlank(next.getName())) {
                iterator.remove();
            }
        }
    }

    private static void diffHttpResponse(JSONObject httpNew, JSONObject httpOld, JsonDiff jsonDiff, Map<String, String> diffMap) {
        // 请求头对比 old/new
        if (httpNew.get(HEADS) != null && httpOld.get(HEADS) != null) {
            String headerNew = StringUtils.join(JSON_START, httpNew.get(HEADS).toString(), JSON_END);
            String headerOld = StringUtils.join(JSON_START, httpOld.get(HEADS).toString(), JSON_END);
            if (!StringUtils.equals(headerNew, headerOld)) {
                String patch = jsonDiff.diff(headerOld, headerNew);
                String diffPatch = jsonDiff.apply(headerOld, patch);
                if (StringUtils.isNotEmpty(diffPatch)) {
                    diffMap.put("header", diffPatch);
                }
            }
        }
        // 对比statusCode参数
        if (httpNew.get(STATUS_CODE) != null && httpOld.get(STATUS_CODE) != null) {
            String statusCodeNew = StringUtils.join(JSON_START, httpNew.get(STATUS_CODE).toString(), JSON_END);
            String statusCodeOld = StringUtils.join(JSON_START, httpOld.get(STATUS_CODE).toString(), JSON_END);
            if (!StringUtils.equals(statusCodeNew, statusCodeOld)) {
                String patch = jsonDiff.diff(statusCodeOld, statusCodeNew);
                String diff = jsonDiff.apply(statusCodeOld, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put(STATUS_CODE, diff);
                }
            }
        }
        // 对比BODY-JSON参数
        if (httpNew.get(BODY) != null && httpOld.get(BODY) != null) {
            String bodyStrNew = httpNew.get(BODY).toString();
            String bodyStrOld = httpOld.get(BODY).toString();
            if (!StringUtils.equals(bodyStrNew, bodyStrOld)) {
                String patch = jsonDiff.diff(bodyStrOld, bodyStrNew);
                String diff = jsonDiff.apply(bodyStrOld, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put(BODY, diff);
                }
            }
            // 对比BODY-FORM参数
            Body bodyNew = JSON.parseObject(bodyStrNew, Body.class);
            Body bodyOld = JSON.parseObject(bodyStrOld, Body.class);

            if (CollectionUtils.isNotEmpty(bodyNew.getKvs()) && CollectionUtils.isNotEmpty(bodyOld.getKvs())) {
                bodyNew.getKvs().remove(bodyNew.getKvs().size() - 1);
                bodyOld.getKvs().remove(bodyOld.getKvs().size() - 1);
            }
            String bodyFormNew = StringUtils.join(JSON_START, JSON.toJSONString(bodyNew.getKvs()), JSON_END);
            String bodyFormOld = StringUtils.join(JSON_START, JSON.toJSONString(bodyOld.getKvs()), JSON_END);
            if (!StringUtils.equals(bodyFormNew, bodyFormOld)) {
                String patch = jsonDiff.diff(bodyFormOld, bodyFormNew);
                String diff = jsonDiff.apply(bodyFormOld, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put(BODY_FORM, diff);
                }
            }
            // 对比BODY-XML参数
            if (!StringUtils.equals(bodyNew.getRaw(), bodyOld.getRaw())) {
                diffMap.put(StringUtils.join(BODY_RAW, "_1"), bodyNew.getRaw());
                diffMap.put(StringUtils.join(BODY_RAW, "_2"), bodyOld.getRaw());
            }
        }
    }

    private static void diffTcp(MsTCPSampler tcpNew, MsTCPSampler tcpOld, JsonDiff jsonDiff, Map<String, String> diffMap) {
        // 对比请求参数
        if (CollectionUtils.isNotEmpty(tcpNew.getParameters())) {
            tcpNew.getParameters().remove(tcpNew.getParameters().size() - 1);
            tcpOld.getParameters().remove(tcpOld.getParameters().size() - 1);
        }
        String queryNew = StringUtils.join(JSON_START, JSON.toJSONString(tcpNew.getParameters()), JSON_END);
        String queryOld = StringUtils.join(JSON_START, JSON.toJSONString(tcpOld.getParameters()), JSON_END);
        if (!StringUtils.equals(queryNew, queryOld)) {
            String patch = jsonDiff.diff(queryOld, queryNew);
            String diff = jsonDiff.apply(queryOld, patch);
            if (StringUtils.isNotEmpty(diff)) {
                diffMap.put(QUERY, diff);
            }
        }
        // 对比BODY-JSON参数
        if (!StringUtils.equals(tcpNew.getJsonDataStruct(), tcpOld.getJsonDataStruct())) {
            String patch = jsonDiff.diff(tcpOld.getJsonDataStruct(), tcpNew.getJsonDataStruct());
            String diff = jsonDiff.apply(tcpNew.getJsonDataStruct(), patch);
            if (StringUtils.isNotEmpty(diff) && !StringUtils.equals(patch, "{}")) {
                diffMap.put("body_json", diff);
            }
        }
        // 对比BODY-XML参数
        String xmlNew = StringUtils.join(JSON_START, JSON.toJSONString(tcpNew.getXmlDataStruct()), JSON_END);
        String xmlOld = StringUtils.join(JSON_START, JSON.toJSONString(tcpOld.getXmlDataStruct()), JSON_END);
        if (!StringUtils.equals(xmlNew, xmlOld)) {
            diffMap.put(StringUtils.join(BODY_XML, "_1"), JSON.toJSONString(tcpNew.getXmlDataStruct()));
            diffMap.put(StringUtils.join(BODY_XML, "_2"), JSON.toJSONString(tcpOld.getXmlDataStruct()));
            String patch = jsonDiff.diff(xmlOld, xmlNew);
            String diffPatch = jsonDiff.apply(xmlOld, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put(BODY_XML, diffPatch);
            }
        }
        // 对比BODY-RAW参数
        if (!StringUtils.equals(tcpNew.getRawDataStruct(), tcpOld.getRawDataStruct())) {
            diffMap.put(StringUtils.join(BODY_RAW, "_1"), tcpNew.getRawDataStruct());
            diffMap.put(StringUtils.join(BODY_RAW, "_2"), tcpOld.getRawDataStruct());
        }
        // 对比pre参数
        if (tcpNew.getTcpPreProcessor() != null && !StringUtils.equals(tcpNew.getTcpPreProcessor().getScript(), tcpOld.getTcpPreProcessor().getScript())) {
            diffMap.put(StringUtils.join(SCRIPT, "_1"), tcpNew.getTcpPreProcessor().getScript());
            diffMap.put(StringUtils.join(SCRIPT, "_2"), tcpOld.getTcpPreProcessor().getScript());
        }
    }


    private static List<DetailColumn> getColumn(List<DetailColumn> columnsNew, List<DetailColumn> columnsOld) {
        OperatingLogDetails detailsNew = new OperatingLogDetails();
        detailsNew.setColumns(columnsNew);
        OperatingLogDetails detailsOld = new OperatingLogDetails();
        detailsOld.setColumns(columnsOld);

        List<DetailColumn> diffColumns = ReflexObjectUtil.compared(detailsOld, detailsNew, StringUtils.EMPTY);
        return diffColumns;
    }

    private static void diffJdbc(MsJDBCSampler jdbcNew, MsJDBCSampler jdbcOld, JsonDiff jsonDiff, Map<String, String> diffMap) {
        // 基础参数对比
        List<DetailColumn> columns = ReflexObjectUtil.getColumns(jdbcNew, DefinitionReference.jdbcColumns);
        List<DetailColumn> columnsOld = ReflexObjectUtil.getColumns(jdbcOld, DefinitionReference.jdbcColumns);
        List<DetailColumn> diffColumns = getColumn(columns, columnsOld);
        if (CollectionUtils.isNotEmpty(diffColumns)) {
            diffMap.put("base", JSON.toJSONString(diffColumns));
        }
        // 自定义变量对比
        if (CollectionUtils.isNotEmpty(jdbcNew.getVariables())) {
            jdbcNew.getVariables().remove(jdbcNew.getVariables().size() - 1);
            jdbcOld.getVariables().remove(jdbcOld.getVariables().size() - 1);
        }
        String variablesNew = StringUtils.join(JSON_START, JSON.toJSONString(jdbcNew.getVariables()), JSON_END);
        String variablesOld = StringUtils.join(JSON_START, JSON.toJSONString(jdbcOld.getVariables()), JSON_END);
        if (!StringUtils.equals(variablesNew, variablesOld)) {
            String patch = jsonDiff.diff(variablesOld, variablesNew);
            String diffPatch = jsonDiff.apply(variablesOld, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("variables", diffPatch);
            }
        }
        if (!StringUtils.equals(jdbcNew.getQuery(), jdbcOld.getQuery())) {
            diffMap.put(StringUtils.join(QUERY, "_1"), jdbcNew.getQuery());
            diffMap.put(StringUtils.join(QUERY, "_2"), jdbcOld.getQuery());
        }
    }

    private static void diffDubbo(MsDubboSampler dubboNew, MsDubboSampler dubboOld, JsonDiff jsonDiff, Map<String, String> diffMap) {
        // Config对比
        List<DetailColumn> interfaceColumns = getColumn(ReflexObjectUtil.getColumns(dubboNew.get_interface()), ReflexObjectUtil.getColumns(dubboOld.get_interface()));
        if (CollectionUtils.isNotEmpty(interfaceColumns)) {
            diffMap.put("interface", JSON.toJSONString(interfaceColumns));
        }
        // Config对比
        List<DetailColumn> diffColumns = getColumn(ReflexObjectUtil.getColumns(dubboNew.getConfigCenter()), ReflexObjectUtil.getColumns(dubboOld.getConfigCenter()));
        if (CollectionUtils.isNotEmpty(diffColumns)) {
            diffMap.put("config", JSON.toJSONString(diffColumns));
        }
        // Registry对比
        List<DetailColumn> registryColumns = getColumn(ReflexObjectUtil.getColumns(dubboNew.getRegistryCenter()), ReflexObjectUtil.getColumns(dubboOld.getRegistryCenter()));
        if (CollectionUtils.isNotEmpty(registryColumns)) {
            diffMap.put("registry", JSON.toJSONString(registryColumns));
        }
        // service对比
        List<DetailColumn> serviceColumns = getColumn(ReflexObjectUtil.getColumns(dubboNew.getConsumerAndService()), ReflexObjectUtil.getColumns(dubboOld.getConsumerAndService()));
        if (CollectionUtils.isNotEmpty(serviceColumns)) {
            diffMap.put("service", JSON.toJSONString(serviceColumns));
        }
        // 对比Args参数
        if (CollectionUtils.isNotEmpty(dubboNew.getArgs())) {
            dubboNew.getArgs().remove(dubboNew.getArgs().size() - 1);
            dubboOld.getArgs().remove(dubboOld.getArgs().size() - 1);
        }
        String argsNew = StringUtils.join(JSON_START, JSON.toJSONString(dubboNew.getArgs()), JSON_END);
        String argsOld = StringUtils.join(JSON_START, JSON.toJSONString(dubboOld.getArgs()), JSON_END);
        if (!StringUtils.equals(argsNew, argsOld)) {
            String patch = jsonDiff.diff(argsOld, argsNew);
            String diffPatch = jsonDiff.apply(argsOld, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("args", diffPatch);
            }
        }
        // 对比Attachment参数
        if (CollectionUtils.isNotEmpty(dubboNew.getAttachmentArgs())) {
            dubboNew.getAttachmentArgs().remove(dubboNew.getAttachmentArgs().size() - 1);
            dubboOld.getAttachmentArgs().remove(dubboOld.getAttachmentArgs().size() - 1);
        }
        String attachmentNew = StringUtils.join(JSON_START, JSON.toJSONString(dubboNew.getAttachmentArgs()), JSON_END);
        String attachmentOld = StringUtils.join(JSON_START, JSON.toJSONString(dubboOld.getAttachmentArgs()), JSON_END);
        if (!StringUtils.equals(attachmentNew, attachmentOld)) {
            String patch = jsonDiff.diff(attachmentOld, attachmentNew);
            String diffPatch = jsonDiff.apply(attachmentOld, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("attachment", diffPatch);
            }
        }
    }
}

