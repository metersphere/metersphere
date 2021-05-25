package io.metersphere.performance.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.utils.StringUtils;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.NodeDTO;
import io.metersphere.performance.base.GranularityData;
import io.metersphere.performance.base.ReportTimeInfo;
import io.metersphere.performance.controller.request.MetricDataRequest;
import io.metersphere.performance.controller.request.MetricQuery;
import io.metersphere.performance.controller.request.MetricRequest;
import io.metersphere.performance.dto.MetricData;
import io.metersphere.performance.dto.Monitor;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.TestResourceService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class MetricQueryService {

    private String prometheusHost;

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private PerformanceReportService performanceReportService;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private TestResourceService testResourceService;
    @Resource
    private SystemParameterService systemParameterService;


    public List<MetricData> queryMetricData(MetricRequest metricRequest) {
        String host = systemParameterService.getValue(ParamConstants.BASE.PROMETHEUS_HOST.getValue());
        prometheusHost = StringUtils.isNotBlank(host) ? host : "http://ms-prometheus:9090";
        List<MetricData> metricDataList = new ArrayList<>();
        long endTime = metricRequest.getEndTime();
        long startTime = metricRequest.getStartTime();
        long reliableEndTime;
        if (endTime > System.currentTimeMillis()) {
            reliableEndTime = System.currentTimeMillis();
        } else {
            reliableEndTime = endTime;
        }
        int step = getGranularity(startTime, reliableEndTime);

        Optional.ofNullable(metricRequest.getMetricDataQueries()).ifPresent(metricDataQueries -> metricDataQueries.forEach(query -> {
            String promQL = query.getPromQL();
            promQL = String.format(promQL, query.getInstance());
            if (StringUtils.isEmpty(promQL)) {
                MSException.throwException("promQL is null");
            } else {
                Optional.of(queryPrometheusMetric(promQL, query.getSeriesName(), startTime, reliableEndTime, step, query.getInstance())).ifPresent(metricDataList::addAll);
            }
        }));

        return metricDataList;
    }


    private List<MetricData> queryPrometheusMetric(String promQL, String seriesName, long startTime, long endTime, int step, String instance) {
        List<MetricData> metricData = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.###");
        String start = df.format(startTime / 1000.0);
        String end = df.format(endTime / 1000.0);
        try {
            JSONObject response = restTemplate.getForObject(prometheusHost + "/api/v1/query_range?query={promQL}&start={start}&end={end}&step={step}", JSONObject.class, promQL, start, end, step);
            LogUtil.info(prometheusHost + "/api/v1/query_range?query={" + promQL + "}&start={" + start + "}&end{" + end + "}&step={" + step + "}");
            metricData = handleResult(seriesName, response, instance);
        } catch (Exception e) {
            LogUtil.error("query prometheus metric fail.");
            LogUtil.error(e.getMessage(), e);
        }
        return metricData;
    }

    private List<MetricData> handleResult(String seriesName, JSONObject response, String instance) {
        List<MetricData> list = new ArrayList<>();

        Map<String, Set<String>> labelMap = new HashMap<>();

        if (response != null && StringUtils.equals(response.getString("status"), "success")) {
            JSONObject data = response.getJSONObject("data");
            JSONArray result = data.getJSONArray("result");

            if (result.size() > 1) {
                result.forEach(rObject -> {
                    JSONObject resultObject = new JSONObject((Map) rObject);
                    JSONObject metrics = resultObject.getJSONObject("metric");

                    if (metrics != null && metrics.size() > 0) {
                        for (Map.Entry<String, Object> entry : metrics.entrySet())
                            labelMap.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(entry.getValue().toString());
                    }
                });
            }

            Optional<String> uniqueLabelKey = labelMap.entrySet().stream().filter(entry -> entry.getValue().size() == result.size()).map(Map.Entry::getKey).findFirst();

            result.forEach(rObject -> {
                MetricData metricData = new MetricData();
                List<String> timestamps = new ArrayList<>();
                List<Double> values = new ArrayList<>();

                JSONObject resultObject = new JSONObject((Map) rObject);
                JSONObject metrics = resultObject.getJSONObject("metric");
                JSONArray jsonArray = resultObject.getJSONArray("values");
                jsonArray.forEach(value -> {
                    JSONArray ja = JSONObject.parseArray(value.toString());
                    Double timestamp = ja.getDouble(0);
                    try {
                        timestamps.add(DateUtils.getTimeString((long) (timestamp * 1000)));
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage(), e);
                        e.printStackTrace();
                    }
                    values.add(ja.getDouble(1));
                });

                if (CollectionUtils.isNotEmpty(values)) {
                    metricData.setValues(values);
                    metricData.setTimestamps(timestamps);
                    metricData.setSeriesName(seriesName);
                    metricData.setInstance(instance);
                    uniqueLabelKey.ifPresent(s -> metricData.setUniqueLabel(metrics.getString(s)));
                    list.add(metricData);
                }
            });


        }

        return list;
    }

    public List<MetricData> queryMetric(String reportId) {
        List<String> instances = new ArrayList<>();
        LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(reportId);
        String poolId = report.getTestResourcePoolId();
        List<TestResource> resourceList = testResourceService.getTestResourceList(poolId);
        // 默认监控资源池下的节点
        if (CollectionUtils.isNotEmpty(resourceList)) {
            resourceList.forEach(resource -> {
                NodeDTO dto = JSON.parseObject(resource.getConfiguration(), NodeDTO.class);
                if (StringUtils.isNotBlank(dto.getIp())) {
                    int port = dto.getMonitorPort() == null ? 9100 : dto.getMonitorPort();
                    instances.add(dto.getIp() + ":" + port);
                }
            });
        }
        String advancedConfiguration = report.getAdvancedConfiguration();
        JSONObject jsonObject = JSON.parseObject(advancedConfiguration);
        JSONArray monitorParams = jsonObject.getJSONArray("monitorParams");
        if (monitorParams == null) {
            return new ArrayList<>();
        }
        List<MetricDataRequest> list = new ArrayList<>();
        // 加入高级设置中的监控配置
        for (int i = 0; i < monitorParams.size(); i++) {
            Monitor monitor = monitorParams.getObject(i, Monitor.class);
            String instance = monitor.getIp() + ":" + monitor.getPort();
            if (!instances.contains(instance)) {
                instances.add(instance);
            }
        }

        instances.forEach(instance -> {
            getRequest(instance, list);
        });

        ReportTimeInfo reportTimeInfo = performanceReportService.getReportTimeInfo(reportId);
        MetricRequest metricRequest = new MetricRequest();
        metricRequest.setMetricDataQueries(list);
        try {
            metricRequest.setStartTime(reportTimeInfo.getStartTime());
            metricRequest.setEndTime(reportTimeInfo.getEndTime());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

        return queryMetricData(metricRequest);
    }

    private void getRequest(String instance, List<MetricDataRequest> list) {
        Map<String, String> map = MetricQuery.getMetricQueryMap();
        Set<String> set = map.keySet();
        set.forEach(s -> {
            MetricDataRequest request = new MetricDataRequest();
            String promQL = map.get(s);
            request.setPromQL(promQL);
            request.setSeriesName(s);
            request.setInstance(instance);
            list.add(request);
        });
    }

    public List<String> queryReportResource(String reportId) {
        List<String> result = new ArrayList<>();
        List<String> resourceIdAndIndexes = extLoadTestReportMapper.selectResourceId(reportId);
        resourceIdAndIndexes.forEach(resourceIdAndIndex -> {
            String[] split = org.apache.commons.lang3.StringUtils.split(resourceIdAndIndex, "_");
            String resourceId = split[0];
            TestResource testResource = testResourceService.getTestResource(resourceId);
            if (testResource == null) {
                return;
            }
            String configuration = testResource.getConfiguration();
            if (org.apache.commons.lang3.StringUtils.isBlank(configuration)) {
                return;
            }
            NodeDTO dto = JSON.parseObject(configuration, NodeDTO.class);
            if (StringUtils.isNotBlank(dto.getIp())) {
                Integer monitorPort = dto.getMonitorPort();
                int port = monitorPort == null ? 9100 : monitorPort;
                result.add(dto.getIp() + ":" + port);
            }
        });

        LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(reportId);
        String advancedConfiguration = report.getAdvancedConfiguration();
        JSONObject jsonObject = JSON.parseObject(advancedConfiguration);
        JSONArray monitorParams = jsonObject.getJSONArray("monitorParams");
        if (monitorParams == null) {
            return result;
        }

        for (int i = 0; i < monitorParams.size(); i++) {
            Monitor monitor = monitorParams.getObject(i, Monitor.class);
            String instance = monitor.getIp() + ":" + monitor.getPort();
            if (!result.contains(instance)) {
                result.add(instance);
            }
        }

        return result;
    }

    private Integer getGranularity(long start, long end) {
        Integer granularity = 15;
        try {
            int duration = (int) (end - start) / 1000;
            Optional<GranularityData.Data> dataOptional = GranularityData.dataList.stream()
                    .filter(data -> duration >= data.getStart() && duration <= data.getEnd())
                    .findFirst();
            if (dataOptional.isPresent()) {
                GranularityData.Data data = dataOptional.get();
                granularity = data.getGranularity();
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return granularity;
    }
}
