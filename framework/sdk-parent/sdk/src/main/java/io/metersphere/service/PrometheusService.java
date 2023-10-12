package io.metersphere.service;


import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.ResourcePoolOperationInfo;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.request.NodeOperationSelectRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class PrometheusService {

    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;

    public List<ResourcePoolOperationInfo> queryNodeOperationInfo(NodeOperationSelectRequest request) {
        if (CollectionUtils.isEmpty(request.getNodeIds())) {
            return new ArrayList<>();
        }
        String host = systemParameterService.getValue(ParamConstants.BASE.PROMETHEUS_HOST.getValue());
        String prometheusHost = StringUtils.isNotBlank(host) ? host : "http://ms-prometheus:9090";
        List<TestResourcePoolDTO> testResourcePoolDTOS = baseTestResourcePoolService.listResourcePoolById(request.getNodeIds());
        return this.queryNodeOperationInfoByPrometheus(prometheusHost, testResourcePoolDTOS);
    }

    private List<ResourcePoolOperationInfo> queryNodeOperationInfoByPrometheus(String host, List<TestResourcePoolDTO> testResourcePoolDTOS) {

        List<ResourcePoolOperationInfo> returnList = new ArrayList<>();

        HttpHeaders headers;
        try {
            headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            // 如果prometheus开启了认证，需要在请求头中添加认证信息
            if (host.contains("@")) {
                URL url = new URL(host);
                // 获取认证信息部分
                String userInfo = url.getUserInfo();
                headers.add("Authorization", "Basic " + CodingUtil.base64Encoding(userInfo));
                host = host.replace(userInfo + "@", "");
            }
        } catch (Exception e) {
            LogUtil.error("Get prometheus header fail.");
            LogUtil.error(e.getMessage(), e);
            return returnList;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);

        for (TestResourcePoolDTO testResourcePoolDTO : testResourcePoolDTOS) {
            ResourcePoolOperationInfo nodeOperationInfo = new ResourcePoolOperationInfo();
            nodeOperationInfo.setId(testResourcePoolDTO.getId());

            boolean queryCpuUsage = true;
            if (testResourcePoolDTO.getResources().size() > 1) {
                queryCpuUsage = false;
            }

            String cpuUsage = null;
            int runningTask = -1;

            for (TestResource testResource : testResourcePoolDTO.getResources()) {
                String config = testResource.getConfiguration();
                if (StringUtils.isNotBlank(config)) {
                    Map<String, Object> configMap = JSON.parseObject(config, Map.class);
                    String ip = String.valueOf(configMap.get("ip"));
                    String port = String.valueOf(configMap.get("port"));
                    String nodeId = ip + ":" + port;
                    if (queryCpuUsage) {
                        String cpuUsageQL = this.generatePromQL(new String[]{"system_cpu_usage"}, nodeId);
                        LogUtil.debug(host + "/api/v1/query?query=" + cpuUsageQL);
                        String cpuUsageDouble = this.runPromQL(headers, host, cpuUsageQL);
                        if(StringUtils.isNotBlank(cpuUsageDouble)){
                            cpuUsage = decimalFormat.format(Double.parseDouble(cpuUsageDouble) * 100) + "%";
                        }
                    }

                    // 查询任务数
                    List<String> taskSeriesNames = new ArrayList<>() {{
                        this.add("running_tasks_load_count");
                        this.add("running_tasks_api_count");
                    }};
                    String taskCountQL = this.generatePromQL(taskSeriesNames.toArray(new String[0]), nodeId);
                    String result = this.runPromQL(headers, host, taskCountQL);
                    if (StringUtils.isNotBlank(result)) {
                        runningTask += Integer.parseInt(result);
                    }
                    nodeOperationInfo.addNodeOperationInfo(String.valueOf(configMap.get("id")), ip, port, cpuUsage, runningTask);
                }
            }
            if (MapUtils.isNotEmpty(nodeOperationInfo.getNodeOperationInfos())) {
                returnList.add(nodeOperationInfo);
            }
        }

        return returnList;
    }

    private String generatePromQL(String[] series, String nodeId) {
        StringBuilder promQL = new StringBuilder();
        for (int i = 0; i < series.length; i++) {
            String seriesName = series[i];
            if (i > 0) {
                promQL.append("+");
            }
            promQL.append(seriesName);
            promQL.append("{");
            promQL.append("instance=\"");
            promQL.append(nodeId);
            promQL.append("\"}");
        }
        return promQL.toString();
    }

    private String runPromQL(HttpHeaders headers, String host, String promQL) {
        try {
            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            postParameters.add("query", promQL);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(postParameters, headers);
            return this.parseResultValue(restTemplate.postForObject(host + "/api/v1/query", httpEntity, Map.class));
        } catch (Exception e) {
            LogUtil.error("query prometheus metric fail.");
            LogUtil.error(e.getMessage(), e);
        }
        return null;
    }

    private String parseResultValue(Map response) {
        String value = null;
        if (response != null && StringUtils.equals((String) response.get("status"), "success")) {
            Map data = (Map) response.get("data");
            List result = (List) data.get("result");

            if (CollectionUtils.isNotEmpty(result)) {
                Map resultObject = (Map) result.get(0);
                List valueMetrics = (List) resultObject.get("value");
                if (CollectionUtils.isNotEmpty(valueMetrics)) {
                    value = valueMetrics.get(1).toString();
                }
            }

        }
        return value;
    }
}
