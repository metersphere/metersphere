package io.metersphere.log.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.log.utils.diff.json.JacksonDiff;
import io.metersphere.log.utils.diff.json.JsonDiff;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.DefinitionReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiDefinitionDiffUtil {
    public static final String JSON_START = "{\"root\":";
    public static final String JSON_END = "}";

    public static String diffResponse(String newValue, String oldValue) {
        Map<String, String> diffMap = new LinkedHashMap<>();
        JSONObject bloBsNew = JSON.parseObject(newValue);
        JSONObject bloBsOld = JSON.parseObject(oldValue);
        if (bloBsNew == null || StringUtils.isEmpty(bloBsNew.getString("type"))) {
            return null;
        }
        JsonDiff jsonDiff = new JacksonDiff();
        if (bloBsNew.getString("type").equals("HTTP")) {
            diffHttpResponse(bloBsNew, bloBsOld, jsonDiff, diffMap);
            if (diffMap.size() > 0) {
                diffMap.put("type", bloBsNew.getString("type"));
            }
        }
        return JSON.toJSONString(diffMap);
    }

    public static String diff(String newValue, String oldValue) {
        try {
            JSONObject bloBsNew = JSON.parseObject(newValue);
            JSONObject bloBsOld = JSON.parseObject(oldValue);
            if (bloBsNew == null || StringUtils.isEmpty(bloBsNew.getString("type"))) {
                return null;
            }
            Map<String, String> diffMap = new LinkedHashMap<>();
            diffMap.put("type", bloBsNew.getString("type"));
            JsonDiff jsonDiff = new JacksonDiff();
            if (bloBsNew.getString("type").equals("TCPSampler")) {
                MsTCPSampler tcpSamplerNew = bloBsNew.toJavaObject(MsTCPSampler.class);
                MsTCPSampler tcpSamplerOld = bloBsOld.toJavaObject(MsTCPSampler.class);
                diffTcp(tcpSamplerNew, tcpSamplerOld, jsonDiff, diffMap);
            } else if (bloBsNew.getString("type").equals("HTTPSamplerProxy")) {
                MsHTTPSamplerProxy httpSamplerProxyNew = bloBsNew.toJavaObject(MsHTTPSamplerProxy.class);
                MsHTTPSamplerProxy httpSamplerProxyOld = bloBsOld.toJavaObject(MsHTTPSamplerProxy.class);
                diffHttp(httpSamplerProxyNew, httpSamplerProxyOld, jsonDiff, diffMap);
            } else if (bloBsNew.getString("type").equals("JDBCSampler")) {
                MsJDBCSampler jdbcSamplerNew = bloBsNew.toJavaObject(MsJDBCSampler.class);
                MsJDBCSampler jdbcSamplerOld = bloBsOld.toJavaObject(MsJDBCSampler.class);
                diffJdbc(jdbcSamplerNew, jdbcSamplerOld, jsonDiff, diffMap);
            } else {
                MsDubboSampler dubboSamplerNew = bloBsNew.toJavaObject(MsDubboSampler.class);
                MsDubboSampler dubboSamplerOld = bloBsOld.toJavaObject(MsDubboSampler.class);
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
        if (CollectionUtils.isNotEmpty(httpNew.getHeaders()) && CollectionUtils.isNotEmpty(httpOld.getHeaders())) {
            httpNew.getHeaders().remove(httpNew.getHeaders().size() - 1);
            httpOld.getHeaders().remove(httpOld.getHeaders().size() - 1);
        }
        String headerNew = JSON_START + JSON.toJSONString(httpNew.getHeaders()) + JSON_END;
        String headerOld = JSON_START + JSON.toJSONString(httpOld.getHeaders()) + JSON_END;
        if (!StringUtils.equals(headerNew, headerOld)) {
            String patch = jsonDiff.diff(headerOld, headerNew);
            String diffPatch = jsonDiff.apply(headerNew, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("header", diffPatch);
            }
        }
        // 对比QUERY参数
        if (CollectionUtils.isNotEmpty(httpNew.getArguments()) && CollectionUtils.isNotEmpty(httpOld.getArguments())) {
            httpNew.getArguments().remove(httpNew.getArguments().size() - 1);
            httpOld.getArguments().remove(httpOld.getArguments().size() - 1);
        }
        String queryNew = JSON_START + JSON.toJSONString(httpNew.getArguments()) + JSON_END;
        String queryOld = JSON_START + JSON.toJSONString(httpOld.getArguments()) + JSON_END;
        if (!StringUtils.equals(queryNew, queryOld)) {
            String patch = jsonDiff.diff(queryOld, queryNew);
            String diff = jsonDiff.apply(queryNew, patch);
            if (StringUtils.isNotEmpty(diff)) {
                diffMap.put("query", diff);
            }
        }
        // 对比REST参数
        if (CollectionUtils.isNotEmpty(httpNew.getRest()) && CollectionUtils.isNotEmpty(httpOld.getRest())) {
            httpNew.getRest().remove(httpNew.getRest().size() - 1);
            httpOld.getRest().remove(httpOld.getRest().size() - 1);
        }
        String restNew = JSON_START + JSON.toJSONString(httpNew.getRest()) + JSON_END;
        String restOld = JSON_START + JSON.toJSONString(httpOld.getRest()) + JSON_END;
        if (!StringUtils.equals(restNew, restOld)) {
            String patch = jsonDiff.diff(restOld, restNew);
            String diff = jsonDiff.apply(restNew, patch);
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
                String diff = jsonDiff.apply(bodyNew, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put("body", diff);
                }
            }
            // 对比BODY-FORM参数
            if (CollectionUtils.isNotEmpty(httpNew.getBody().getKvs()) && CollectionUtils.isNotEmpty(httpOld.getBody().getKvs())) {
                httpNew.getBody().getKvs().remove(httpNew.getBody().getKvs().size() - 1);
                httpOld.getBody().getKvs().remove(httpOld.getBody().getKvs().size() - 1);
            }
            String bodyFormNew = JSON_START + JSON.toJSONString(httpNew.getBody().getKvs()) + JSON_END;
            String bodyFormOld = JSON_START + JSON.toJSONString(httpOld.getBody().getKvs()) + JSON_END;
            if (!StringUtils.equals(bodyFormNew, bodyFormOld)) {
                String patch = jsonDiff.diff(bodyFormOld, bodyFormNew);
                String diff = jsonDiff.apply(bodyFormNew, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put("body_form", diff);
                }
            }
            // 对比BODY-XML参数
            if (!StringUtils.equals(httpNew.getBody().getRaw(), httpOld.getBody().getRaw())) {
                diffMap.put("body_raw_1", httpNew.getBody().getRaw());
                diffMap.put("body_raw_2", httpOld.getBody().getRaw());
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
                        item.setOriginalValue("");
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

    private static void diffHttpResponse(JSONObject httpNew, JSONObject httpOld, JsonDiff jsonDiff, Map<String, String> diffMap) {
        // 请求头对比 old/new
        if (httpNew.get("headers") != null && httpOld.get("headers") != null) {
            String headerNew = JSON_START + httpNew.get("headers").toString() + JSON_END;
            String headerOld = JSON_START + httpOld.get("headers").toString() + JSON_END;
            if (!StringUtils.equals(headerNew, headerOld)) {
                String patch = jsonDiff.diff(headerOld, headerNew);
                String diffPatch = jsonDiff.apply(headerNew, patch);
                if (StringUtils.isNotEmpty(diffPatch)) {
                    diffMap.put("header", diffPatch);
                }
            }
        }
        // 对比statusCode参数
        if (httpNew.get("statusCode") != null && httpOld.get("statusCode") != null) {
            String statusCodeNew = JSON_START + httpNew.get("statusCode").toString() + JSON_END;
            String statusCodeOld = JSON_START + httpOld.get("statusCode").toString() + JSON_END;
            if (!StringUtils.equals(statusCodeNew, statusCodeOld)) {
                String patch = jsonDiff.diff(statusCodeOld, statusCodeNew);
                String diff = jsonDiff.apply(statusCodeNew, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put("statusCode", diff);
                }
            }
        }
        // 对比BODY-JSON参数
        if (httpNew.get("body") != null && httpOld.get("body") != null) {
            String bodyStrNew = httpNew.get("body").toString();
            String bodyStrOld = httpOld.get("body").toString();
            if (!StringUtils.equals(bodyStrNew, bodyStrOld)) {
                String patch = jsonDiff.diff(bodyStrOld, bodyStrNew);
                String diff = jsonDiff.apply(bodyStrNew, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put("body", diff);
                }
            }
            // 对比BODY-FORM参数
            Body bodyNew = JSON.parseObject(bodyStrNew, Body.class);
            Body bodyOld = JSON.parseObject(bodyStrOld, Body.class);

            if (CollectionUtils.isNotEmpty(bodyNew.getKvs()) && CollectionUtils.isNotEmpty(bodyOld.getKvs())) {
                bodyNew.getKvs().remove(bodyNew.getKvs().size() - 1);
                bodyOld.getKvs().remove(bodyOld.getKvs().size() - 1);
            }
            String bodyFormNew = JSON_START + JSON.toJSONString(bodyNew.getKvs()) + JSON_END;
            String bodyFormOld = JSON_START + JSON.toJSONString(bodyOld.getKvs()) + JSON_END;
            if (!StringUtils.equals(bodyFormNew, bodyFormOld)) {
                String patch = jsonDiff.diff(bodyFormOld, bodyFormNew);
                String diff = jsonDiff.apply(bodyFormNew, patch);
                if (StringUtils.isNotEmpty(diff)) {
                    diffMap.put("body_form", diff);
                }
            }
            // 对比BODY-XML参数
            if (!StringUtils.equals(bodyNew.getRaw(), bodyOld.getRaw())) {
                diffMap.put("body_raw_1", bodyNew.getRaw());
                diffMap.put("body_raw_2", bodyOld.getRaw());
            }
        }
    }

    private static void diffTcp(MsTCPSampler tcpNew, MsTCPSampler tcpOld, JsonDiff jsonDiff, Map<String, String> diffMap) {
        // 对比请求参数
        if (CollectionUtils.isNotEmpty(tcpNew.getParameters())) {
            tcpNew.getParameters().remove(tcpNew.getParameters().size() - 1);
            tcpOld.getParameters().remove(tcpOld.getParameters().size() - 1);
        }
        String queryNew = JSON_START + JSON.toJSONString(tcpNew.getParameters()) + JSON_END;
        String queryOld = JSON_START + JSON.toJSONString(tcpOld.getParameters()) + JSON_END;
        if (!StringUtils.equals(queryNew, queryOld)) {
            String patch = jsonDiff.diff(queryOld, queryNew);
            String diff = jsonDiff.apply(queryNew, patch);
            if (StringUtils.isNotEmpty(diff)) {
                diffMap.put("query", diff);
            }
        }
        // 对比BODY-JSON参数
        if (!StringUtils.equals(tcpNew.getJsonDataStruct(), tcpOld.getJsonDataStruct())) {
            String patch = jsonDiff.diff(tcpOld.getJsonDataStruct(), tcpNew.getJsonDataStruct());
            String diff = jsonDiff.apply(tcpNew.getJsonDataStruct(), patch);
            if (StringUtils.isNotEmpty(diff)) {
                diffMap.put("body_json", diff);
            }
        }
        // 对比BODY-XML参数
        String xmlNew = JSON_START + JSON.toJSONString(tcpNew.getXmlDataStruct()) + JSON_END;
        String xmlOld = JSON_START + JSON.toJSONString(tcpOld.getXmlDataStruct()) + JSON_END;
        if (!StringUtils.equals(xmlNew, xmlOld)) {
            diffMap.put("body_xml_1", JSON.toJSONString(tcpNew.getXmlDataStruct()));
            diffMap.put("body_xml_2", JSON.toJSONString(tcpOld.getXmlDataStruct()));
            String patch = jsonDiff.diff(xmlOld, xmlNew);
            String diffPatch = jsonDiff.apply(xmlNew, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("body_xml", diffPatch);
            }
        }
        // 对比BODY-RAW参数
        if (!StringUtils.equals(tcpNew.getRawDataStruct(), tcpOld.getRawDataStruct())) {
            diffMap.put("body_raw_1", tcpNew.getRawDataStruct());
            diffMap.put("body_raw_2", tcpOld.getRawDataStruct());
        }
        // 对比pre参数
        if (tcpNew.getTcpPreProcessor() != null && !StringUtils.equals(tcpNew.getTcpPreProcessor().getScript(), tcpOld.getTcpPreProcessor().getScript())) {
            diffMap.put("script_1", tcpNew.getTcpPreProcessor().getScript());
            diffMap.put("script_2", tcpOld.getTcpPreProcessor().getScript());
        }
    }

    private static List<DetailColumn> getColumn(List<DetailColumn> columnsNew, List<DetailColumn> columnsOld) {
        OperatingLogDetails detailsNew = new OperatingLogDetails();
        detailsNew.setColumns(columnsNew);
        OperatingLogDetails detailsOld = new OperatingLogDetails();
        detailsOld.setColumns(columnsOld);
        List<DetailColumn> diffColumns = ReflexObjectUtil.compared(detailsOld, detailsNew, "");
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
        String variablesNew = JSON_START + JSON.toJSONString(jdbcNew.getVariables()) + JSON_END;
        String variablesOld = JSON_START + JSON.toJSONString(jdbcOld.getVariables()) + JSON_END;
        if (!StringUtils.equals(variablesNew, variablesOld)) {
            String patch = jsonDiff.diff(variablesOld, variablesNew);
            String diffPatch = jsonDiff.apply(variablesNew, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("variables", diffPatch);
            }
        }
        if (!StringUtils.equals(jdbcNew.getQuery(), jdbcOld.getQuery())) {
            diffMap.put("query_1", jdbcNew.getQuery());
            diffMap.put("query_2", jdbcOld.getQuery());
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
        String argsNew = JSON_START + JSON.toJSONString(dubboNew.getArgs()) + JSON_END;
        String argsOld = JSON_START + JSON.toJSONString(dubboOld.getArgs()) + JSON_END;
        if (!StringUtils.equals(argsNew, argsOld)) {
            String patch = jsonDiff.diff(argsOld, argsNew);
            String diffPatch = jsonDiff.apply(argsNew, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("args", diffPatch);
            }
        }
        // 对比Attachment参数
        if (CollectionUtils.isNotEmpty(dubboNew.getAttachmentArgs())) {
            dubboNew.getAttachmentArgs().remove(dubboNew.getAttachmentArgs().size() - 1);
            dubboOld.getAttachmentArgs().remove(dubboOld.getAttachmentArgs().size() - 1);
        }
        String attachmentNew = JSON_START + JSON.toJSONString(dubboNew.getAttachmentArgs()) + JSON_END;
        String attachmentOld = JSON_START + JSON.toJSONString(dubboOld.getAttachmentArgs()) + JSON_END;
        if (!StringUtils.equals(attachmentNew, attachmentOld)) {
            String patch = jsonDiff.diff(attachmentOld, attachmentNew);
            String diffPatch = jsonDiff.apply(attachmentNew, patch);
            if (StringUtils.isNotEmpty(diffPatch)) {
                diffMap.put("attachment", diffPatch);
            }
        }
    }
}
