package io.metersphere.reportstatistics.service;

import io.metersphere.base.domain.ReportStatistics;
import io.metersphere.base.domain.ReportStatisticsExample;
import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import io.metersphere.base.mapper.ReportStatisticsMapper;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.reportstatistics.dto.*;
import io.metersphere.reportstatistics.dto.charts.Series;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableItemDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableRowDTO;
import io.metersphere.reportstatistics.utils.ChromeUtil;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author song.tianyang
 * @Date 2021/9/14 4:50 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportStatisticsService {
    @Resource
    private ReportStatisticsMapper reportStatisticsMapper;
    @Resource
    private TestCaseCountService testCaseCountService;

    public ReportStatisticsWithBLOBs saveByRequest(ReportStatisticsSaveRequest request) {
        ReportStatisticsWithBLOBs model = new ReportStatisticsWithBLOBs();
        model.setId(UUID.randomUUID().toString());

        String name = "用例分析报表";
        if (StringUtils.equalsIgnoreCase(ReportStatisticsType.TEST_CASE_COUNT.name(), request.getReportType())) {
            name = "用例统计报表";
            model.setReportType(ReportStatisticsType.TEST_CASE_COUNT.name());
        } else {
            model.setReportType(ReportStatisticsType.TEST_CASE_ANALYSIS.name());
        }
        if (StringUtils.isEmpty(request.getName())) {
            request.setName(name);
        }
        model.setName(request.getName());
        model.setDataOption(request.getDataOption());
        model.setSelectOption(request.getSelectOption());
        model.setCreateTime(System.currentTimeMillis());
        model.setUpdateTime(System.currentTimeMillis());
        model.setProjectId(request.getProjectId());
        String userId = SessionUtils.getUserId();
        model.setCreateUser(userId);
        model.setUpdateUser(userId);

        reportStatisticsMapper.insert(model);

        return model;
    }

    public ReportStatisticsWithBLOBs update(ReportStatisticsSaveRequest request) {
        reportStatisticsMapper.updateByPrimaryKeySelective(request);
        return request;
    }

    public int deleteById(String id) {
        return reportStatisticsMapper.deleteByPrimaryKey(id);
    }

    public ReportStatisticsWithBLOBs selectById(String id, boolean isRemoteRequest) {
        ReportStatisticsWithBLOBs blob = reportStatisticsMapper.selectByPrimaryKey(id);
        if (blob == null) {
            return null;
        }
        if (isRemoteRequest) {
            //通过接口进行的请求时没有session的
            HttpHeaderUtils.runAsUser(blob.getCreateUser());
        }

        Map<String, Object> selectOption = JSON.parseObject(blob.getSelectOption(), Map.class);
        Map<String, Object> dataOption = JSON.parseObject(blob.getDataOption(), Map.class);
        boolean isReportNeedUpdate = this.isReportNeedUpdate(blob);
        if (isReportNeedUpdate) {
            TestCaseCountRequest countRequest = JSON.parseObject(blob.getSelectOption(), TestCaseCountRequest.class);
            Map<String, Object> returnDataOption = this.reloadData(countRequest, dataOption.get("chartType").toString());
            if (returnDataOption != null) {
                dataOption = returnDataOption;
            }
        }

        if (isRemoteRequest) {
            HttpHeaderUtils.clearUser();
        }
        if (!dataOption.containsKey("showTable")) {
            if (dataOption.get("tableData") != null) {
                String tableDataJsonStr = JSON.toJSONString(dataOption.get("tableData"));
                List<TestCaseCountTableDTO> dtos = JSON.parseArray(tableDataJsonStr, TestCaseCountTableDTO.class);
                List<String> yaxis = new ArrayList<>();
                try {
                    yaxis = (List<String>) selectOption.get("yaxis");
                } catch (Exception e) {
                    String yaxisStr = JSON.toJSONString(selectOption.get("yaxis"));
                    yaxis = JSON.parseArray(yaxisStr, String.class);
                }
                TestCaseCountTableDataDTO showTable = this.countShowTable(String.valueOf(selectOption.get("xaxis")), yaxis, dtos);
                dataOption.put("showTable", showTable);
                blob.setDataOption(JSON.toJSONString(dataOption));
            }
        }
        return blob;
    }

    private Map<String, Object> reloadData(TestCaseCountRequest request, String chartType) {
        if (StringUtils.isEmpty(chartType)) {
            chartType = "bar";
        }
        Map<String, Object> returnObject = new HashMap<>();
        TestCaseCountResponse testCaseCountResponse = testCaseCountService.getReport(request);
        if (testCaseCountResponse.getBarChartDTO() != null) {
            Map<String, String> loadOptionObject = new HashMap<>();
            loadOptionObject.put("legend", JSON.toJSONString(testCaseCountResponse.getBarChartDTO().getLegend()));
            loadOptionObject.put("xAxis", JSON.toJSONString(testCaseCountResponse.getBarChartDTO().getXAxis()));
            loadOptionObject.put("xaxis", JSON.toJSONString(testCaseCountResponse.getBarChartDTO().getXAxis()));
            loadOptionObject.put("yAxis", JSON.toJSONString(testCaseCountResponse.getBarChartDTO().getYAxis()));
            loadOptionObject.put("tooltip", "{}");
            loadOptionObject.put("label", "{}");
            if (!CollectionUtils.isEmpty(testCaseCountResponse.getBarChartDTO().getSeries())) {
                List<Series> list = testCaseCountResponse.getBarChartDTO().getSeries();
                for (Series model : list) {
                    model.setType(chartType);
                }
                loadOptionObject.put("series", JSON.toJSONString(list));
            }
            loadOptionObject.put("grid", new HashMap<String, String>().put("bottom", "75px"));
            returnObject.put("loadOption", loadOptionObject);
        }
        if (testCaseCountResponse.getPieChartDTO() != null) {
            Map<String, Object> pieOptionObject = new HashMap<>();
            pieOptionObject.put("title", JSON.toJSONString(testCaseCountResponse.getPieChartDTO().getTitle()));
            pieOptionObject.put("xAxis", JSON.toJSONString(testCaseCountResponse.getPieChartDTO().getXAxis()));
            if (!CollectionUtils.isEmpty(testCaseCountResponse.getPieChartDTO().getSeries())) {
                List<Series> list = testCaseCountResponse.getPieChartDTO().getSeries();
                for (Series model : list) {
                    model.setType(chartType);
                }
                pieOptionObject.put("series", JSON.toJSONString(list));
            }
            pieOptionObject.put("grid", new HashMap<String, String>().put("bottom", "75px"));
            if (testCaseCountResponse.getPieChartDTO().getWidth() > 0) {
                pieOptionObject.put("width", testCaseCountResponse.getPieChartDTO().getWidth());
            }
            returnObject.put("pieOption", pieOptionObject);
        }
        if (testCaseCountResponse.getTableDTOs() != null) {
            returnObject.put("tableData", testCaseCountResponse.getTableDTOs());
        }
        returnObject.put("chartType", chartType);
        return returnObject;
    }

    private TestCaseCountTableDataDTO countShowTable(String groupName, List<String> yaxis, List<TestCaseCountTableDTO> dtos) {
        if (yaxis == null) {
            yaxis = new ArrayList<>();
        }
        TestCaseCountTableDataDTO returnDTO = new TestCaseCountTableDataDTO();
        String[] headers = new String[]{groupName, "Count", "testCase", "apiCase", "scenarioCase", "loadCaseCount"};

        List<TestCaseCountTableItemDataDTO> heads = new ArrayList<>();
        boolean showTestCase = true;
        boolean showApi = true;
        boolean showScenario = true;
        boolean showLoad = true;

        for (String head : headers) {
            if (StringUtils.equalsAnyIgnoreCase(head, groupName, "Count") || yaxis.contains(head)) {
                TestCaseCountTableItemDataDTO headData = new TestCaseCountTableItemDataDTO();
                headData.setId(UUID.randomUUID().toString());
                headData.setValue(head);
                heads.add(headData);
            } else {
                if (StringUtils.equalsIgnoreCase(head, "testCase")) {
                    showTestCase = false;
                } else if (StringUtils.equalsIgnoreCase(head, "apiCase")) {
                    showApi = false;
                } else if (StringUtils.equalsIgnoreCase(head, "scenarioCase")) {
                    showScenario = false;
                } else if (StringUtils.equalsIgnoreCase(head, "loadCaseCount")) {
                    showLoad = false;
                }
            }
        }
        List<TestCaseCountTableRowDTO> tableRows = new ArrayList<>();
        for (TestCaseCountTableDTO data : dtos) {
            TestCaseCountTableRowDTO row = new TestCaseCountTableRowDTO();
            List<TestCaseCountTableItemDataDTO> rowDataList = new ArrayList<>();

            TestCaseCountTableItemDataDTO nameData = new TestCaseCountTableItemDataDTO();
            nameData.setId(UUID.randomUUID().toString());
            nameData.setValue(data.getName());
            rowDataList.add(nameData);

            TestCaseCountTableItemDataDTO countData = new TestCaseCountTableItemDataDTO();
            countData.setId(UUID.randomUUID().toString());
            countData.setValue(data.getAllCount());
            rowDataList.add(countData);

            if (showTestCase) {
                TestCaseCountTableItemDataDTO itemData = new TestCaseCountTableItemDataDTO();
                itemData.setId(UUID.randomUUID().toString());
                itemData.setValue(data.getTestCaseCount());
                rowDataList.add(itemData);
            }

            if (showApi) {
                TestCaseCountTableItemDataDTO itemData = new TestCaseCountTableItemDataDTO();
                itemData.setId(UUID.randomUUID().toString());
                itemData.setValue(data.getApiCaseCount());
                rowDataList.add(itemData);
            }

            if (showScenario) {
                TestCaseCountTableItemDataDTO itemData = new TestCaseCountTableItemDataDTO();
                itemData.setId(UUID.randomUUID().toString());
                itemData.setValue(data.getScenarioCaseCount());
                rowDataList.add(itemData);
            }

            if (showLoad) {
                TestCaseCountTableItemDataDTO itemData = new TestCaseCountTableItemDataDTO();
                itemData.setId(UUID.randomUUID().toString());
                itemData.setValue(data.getLoadCaseCount());
                rowDataList.add(itemData);
            }
            row.setTableDatas(rowDataList);
            tableRows.add(row);
        }
        returnDTO.setHeads(heads);
        returnDTO.setData(tableRows);


        return returnDTO;
    }

    public ReportStatisticsWithBLOBs updateByRequest(ReportStatisticsSaveRequest request) {
        ReportStatisticsWithBLOBs updateModel = new ReportStatisticsWithBLOBs();
        updateModel.setId(request.getId());
        updateModel.setName(request.getName());
        updateModel.setUpdateTime(request.getUpdateTime());
        updateModel.setUpdateUser(SessionUtils.getUserId());
        reportStatisticsMapper.updateByPrimaryKeySelective(updateModel);
        return updateModel;
    }

    public List<ReportStatistics> selectByRequest(ReportStatisticsSaveRequest request) {
        ReportStatisticsExample example = new ReportStatisticsExample();
        ReportStatisticsExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(request.getProjectId())) {
            criteria.andProjectIdEqualTo(request.getProjectId());
        }
        if (StringUtils.isNotEmpty(request.getReportType())) {
            criteria.andReportTypeEqualTo(request.getReportType());
        }
        if (StringUtils.isNotEmpty(request.getName())) {
            criteria.andNameLike(request.getName());
        }
        example.setOrderByClause("create_time DESC");
        return reportStatisticsMapper.selectByExample(example);
    }

    public Map<String, String> getImageContentById(List<ReportStatisticsWithBLOBs> reportRecordIdList, String language) {
        if (CollectionUtils.isEmpty(reportRecordIdList)) {
            return new HashMap<>();
        }
        ChromeUtil chromeUtils = ChromeUtil.getInstance();
        HeadlessRequest headlessRequest = new HeadlessRequest();
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        // 占位符
        String platformUrl = "http://localhost:8081";
        String remoteDriverUrl = "http://localhost:4444";
        if (baseInfo != null) {
            platformUrl = baseInfo.getUrl();
            remoteDriverUrl = baseInfo.getSeleniumDockerUrl();
        }

        Map<String, String> urlMap = new HashMap<>();
        for (ReportStatisticsWithBLOBs blob : reportRecordIdList) {
            String url = platformUrl + "/report/chart-pic?shareId=" + blob.getId();
            urlMap.put(blob.getId(), url);
        }
        headlessRequest.setUrlMap(urlMap);
        headlessRequest.setRemoteDriverUrl(remoteDriverUrl);
        LogUtil.info("使用ChromeUtil来获取图片信息。url:" + JSON.toJSONString(urlMap) + "; language：" + language + "，headlessRequest：" + JSON.toJSONString(headlessRequest));
        Map<String, String> returnMap = chromeUtils.getImageInfo(headlessRequest, language);
        return returnMap;
    }

    public boolean isReportNeedUpdate(ReportStatisticsWithBLOBs model) {
        Map<String, Object> selectOption = JSON.parseObject(model.getSelectOption(), Map.class);
        return selectOption.containsKey("timeType") && StringUtils.equalsIgnoreCase("dynamicTime", String.valueOf(selectOption.get("timeType")));
    }
}
