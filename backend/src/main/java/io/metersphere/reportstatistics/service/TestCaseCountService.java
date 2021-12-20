package io.metersphere.reportstatistics.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.ext.ExtTestCaseCountMapper;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.dto.TestCaseTemplateDao;
import io.metersphere.i18n.Translator;
import io.metersphere.reportstatistics.dto.*;
import io.metersphere.reportstatistics.dto.charts.*;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableItemDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableRowDTO;
import io.metersphere.service.TestCaseTemplateService;
import io.metersphere.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseCountService {
    @Resource
    private ExtTestCaseCountMapper extTestCaseCountMapper;
    @Resource
    UserService userService;

    public TestCaseCountResponse getReport(TestCaseCountRequest request) {
        request.setFilterType(request.getFilterType().toUpperCase(Locale.ROOT));

        TestAnalysisChartDTO dto = new TestAnalysisChartDTO();
        PieChartDTO pieChartDTO = new PieChartDTO();
        List<TestCaseCountTableDTO> dtos = new LinkedList<>();

        List<Series> seriesList = new LinkedList<>();
        XAxis xAxis = new XAxis();
        xAxis.setAxisLabel(new HashMap<String, Integer>() {
            {
                this.put("interval", 0);
                this.put("rotate", 0);
            }
        });

        Legend legend = new Legend();
        formatLegend(legend, request.getYaxis(), request);
        dto.setLegend(legend);

        //根据X轴（分组计算字段）来整理不同表对应的字段 注意：x轴维护人/查询条件有维护人时 不查接口和性能； x轴为用例等级/查询条件有用例等级的，不查性能
        boolean yAxisSelectTestCase = false;
        boolean yAxisSelectApi = false;
        boolean yAxisSelectScenarioCase = false;
        boolean yAxisSelectLoad = false;
        boolean selectApi = true;
        boolean selectLoad = true;
        boolean parseUser = false;
        boolean parseStatus = false;

        switch (request.getXaxis()) {
            case "creator":
                request.setTestCaseGroupColumn("create_user");
                request.setApiCaseGroupColumn("create_user_id");
                request.setScenarioCaseGroupColumn("create_user");
                request.setLoadCaseGroupColumn("create_user");
                parseUser = true;
                break;
            case "maintainer":
                request.setTestCaseGroupColumn("maintainer");
                request.setApiCaseGroupColumn("'无维护人'");
                request.setScenarioCaseGroupColumn("principal");
                request.setLoadCaseGroupColumn("'无维护人'");
                selectApi = false;
                selectLoad = false;
                parseUser = true;
                break;
            case "casetype":
                Map<String, String> caseDescMap = this.getCaseDescMap();
                request.setTestCaseGroupColumn("'" + caseDescMap.get("testCaseDesc") + "'");
                request.setApiCaseGroupColumn("'" + caseDescMap.get("apiCaseDesc") + "'");
                request.setScenarioCaseGroupColumn("'" + caseDescMap.get("scenarioCaseDesc") + "'");
                request.setLoadCaseGroupColumn("'" + caseDescMap.get("loadCaseDesc") + "'");
                break;
            case "casestatus":
                request.setTestCaseGroupColumn("status");
                request.setApiCaseGroupColumn("status");
                request.setScenarioCaseGroupColumn("status");
                request.setLoadCaseGroupColumn("status");
                selectApi = false;
                parseStatus = true;
                break;
            case "caselevel":
                request.setTestCaseGroupColumn("priority");
                request.setApiCaseGroupColumn("priority");
                request.setScenarioCaseGroupColumn("level");
                request.setLoadCaseGroupColumn("'无用例等级'");
                selectLoad = false;
                break;
            default:
                return new TestCaseCountResponse();
        }

        //计算时间
        if (StringUtils.equalsIgnoreCase(request.getTimeType(), "dynamicTime")) {
            int dateCountType = 0;
            if (StringUtils.equalsIgnoreCase(request.getTimeRangeUnit(), "day")) {
                dateCountType = Calendar.DAY_OF_MONTH;
            } else if (StringUtils.equalsIgnoreCase(request.getTimeRangeUnit(), "month")) {
                dateCountType = Calendar.MONTH;
            } else if (StringUtils.equalsIgnoreCase(request.getTimeRangeUnit(), "year")) {
                dateCountType = Calendar.YEAR;
            }

            if (dateCountType != 0 && request.getTimeRange() != 0) {
                long startTime = DateUtils.dateSum(new Date(), (0 - request.getTimeRange()), dateCountType).getTime();
                request.setStartTime(startTime);
            }

        } else if (StringUtils.equalsIgnoreCase(request.getTimeType(), "fixedTime")) {
            if (CollectionUtils.isNotEmpty(request.getTimes()) && request.getTimes().size() == 2) {
                request.setStartTime(request.getTimes().get(0));
                request.setEndTime(request.getTimes().get(1));
            }
        }

        //计算更多属性
        if (CollectionUtils.isNotEmpty(request.getFilters())) {
            for (Map<String, Object> filterMap : request.getFilters()) {
                String filterType = String.valueOf(filterMap.get("type"));

                if (StringUtils.equalsAnyIgnoreCase(filterType, "casetype", "caselevel", "creator", "maintainer")) {
                    Object valueObj = filterMap.get("values");
                    if (valueObj instanceof List) {
                        List<String> searchList = (List) valueObj;
                        if (!searchList.isEmpty()) {
                            request.setFilterSearchList(filterType, searchList);
                        }
                    }

                    if (StringUtils.equalsIgnoreCase(filterType, "caselevel")) {
                        selectLoad = false;
                    } else if (StringUtils.equalsIgnoreCase(filterType, "maintainer")) {
                        selectApi = false;
                        selectLoad = false;
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(filterType, "casestatus")) {
                    List<String> searchList = new ArrayList<>();
                    Object valueObj = filterMap.get("values");
                    if (valueObj instanceof List) {
                        for (String statusStr : (List<String>) valueObj) {
                            searchList.add(statusStr.toUpperCase(Locale.ROOT));
                        }
                    }
                    //如果包含Running
                    if (searchList.contains("RUNNING")) {
                        if (!searchList.contains("Starting")) {
                            searchList.add("STARTING");
                        }
                        if (!searchList.contains("Underway")) {
                            searchList.add("UNDERWAY");
                        }
                    }

                    if (searchList.contains("FINISHED")) {
                        if (!searchList.contains("Completed")) {
                            searchList.add("Completed");
                        }
                    }

                    if (!searchList.isEmpty()) {
                        request.setFilterSearchList(filterType, searchList);
                    }
                    selectApi = false;
                }
            }
        }

        //获取测试用例、接口用例、场景用例、性能用例的统计
        List<TestCaseCountChartResult> functionCaseCountResult = new ArrayList<>();
        List<TestCaseCountChartResult> apiCaseCountResult = new ArrayList<>();
        List<TestCaseCountChartResult> scenarioCaseCount = new ArrayList<>();
        List<TestCaseCountChartResult> loadCaseCount = new ArrayList<>();

        List<String> moreOptionsAboutCaseType = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(request.getFilterType(), "And") && MapUtils.isNotEmpty(request.getFilterSearchList())) {
            if (request.getFilterSearchList().containsKey("maintainer")) {
                selectApi = false;
            }
            if (request.getFilterSearchList().containsKey("caselevel")) {
                selectLoad = false;
            }

            if (request.getFilterSearchList().containsKey("casetype")) {
                //如果"且"查询，同时针对案例类型做过筛选，那么则分开批量查询
                List<String> selectCaseTypeList = request.getFilterSearchList().get("casetype");
                request.getFilterSearchList().remove("casetype");
                if (CollectionUtils.isNotEmpty(selectCaseTypeList)) {
                    moreOptionsAboutCaseType.addAll(selectCaseTypeList);
                }
            }
        }
        //没有选择的话默认搜索条件是所有类型的案例
        if (moreOptionsAboutCaseType.isEmpty()) {
            moreOptionsAboutCaseType.add("testCase");
            moreOptionsAboutCaseType.add("apiCase");
            moreOptionsAboutCaseType.add("scenarioCase");
            moreOptionsAboutCaseType.add("loadCase");
        }

        //解析Y轴，判断要查询的案例类型
        if (CollectionUtils.isNotEmpty(request.getYaxis())) {

            for (String selectType : request.getYaxis()) {
                if (moreOptionsAboutCaseType.contains(selectType)) {
                    if (StringUtils.equalsIgnoreCase(selectType, "testCase")) {
                        yAxisSelectTestCase = true;
                    } else if (StringUtils.equalsIgnoreCase(selectType, "apiCase")) {
                        if (selectApi) {
                            yAxisSelectApi = true;
                        }
                    } else if (StringUtils.equalsIgnoreCase(selectType, "scenarioCase")) {
                        yAxisSelectScenarioCase = true;
                    } else if (StringUtils.equalsIgnoreCase(selectType, "loadCase")) {
                        if (selectLoad) {
                            yAxisSelectLoad = true;
                        }
                    }
                }
            }
        }


        if (yAxisSelectTestCase) {
            functionCaseCountResult = extTestCaseCountMapper.getFunctionCaseCount(request);
            if (functionCaseCountResult.isEmpty() && StringUtils.equalsIgnoreCase(request.getXaxis(), "casetype")) {
                TestCaseCountChartResult result = new TestCaseCountChartResult();
                result.setCountNum(0);
                result.setGroupName(request.getTestCaseGroupColumn().substring(1, request.getTestCaseGroupColumn().length() - 1));
                functionCaseCountResult.add(result);
            }
        }
        if (yAxisSelectApi) {
            Map<String, List<String>> apiCaseFilterList = new HashMap<>();
            if (MapUtils.isNotEmpty(request.getFilterSearchList())) {
                for (Map.Entry<String, List<String>> entry : request.getFilterSearchList().entrySet()) {
                    String type = entry.getKey();
                    if (!StringUtils.equalsAnyIgnoreCase(type, "maintainer", "casestatus")) {
                        apiCaseFilterList.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            request.setApiFilterSearchList(apiCaseFilterList);
            apiCaseCountResult = extTestCaseCountMapper.getApiCaseCount(request);
            if (apiCaseCountResult.isEmpty() && StringUtils.equalsIgnoreCase(request.getXaxis(), "casetype")) {
                TestCaseCountChartResult result = new TestCaseCountChartResult();
                result.setCountNum(0);
                result.setGroupName(request.getApiCaseGroupColumn().substring(1, request.getApiCaseGroupColumn().length() - 1));
                apiCaseCountResult.add(result);
            }
        }
        if (yAxisSelectScenarioCase) {
            scenarioCaseCount = extTestCaseCountMapper.getScenarioCaseCount(request);
            if (scenarioCaseCount.isEmpty() && StringUtils.equalsIgnoreCase(request.getXaxis(), "casetype")) {
                TestCaseCountChartResult result = new TestCaseCountChartResult();
                result.setCountNum(0);
                result.setGroupName(request.getScenarioCaseGroupColumn().substring(1, request.getScenarioCaseGroupColumn().length() - 1));
                scenarioCaseCount.add(result);
            }
        }
        if (yAxisSelectLoad) {
            Map<String, List<String>> loadCaseFilterMap = new HashMap<>();
            if (MapUtils.isNotEmpty(request.getFilterSearchList())) {
                for (Map.Entry<String, List<String>> entry : request.getFilterSearchList().entrySet()) {
                    String type = entry.getKey();
                    if (!StringUtils.equalsAnyIgnoreCase(type, "maintainer", "caselevel")) {
                        loadCaseFilterMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            request.setLoadFilterSearchList(loadCaseFilterMap);
            loadCaseCount = extTestCaseCountMapper.getLoadCaseCount(request);
            if (loadCaseCount.isEmpty() && StringUtils.equalsIgnoreCase(request.getXaxis(), "casetype")) {
                TestCaseCountChartResult result = new TestCaseCountChartResult();
                result.setCountNum(0);
                result.setGroupName(request.getLoadCaseGroupColumn().substring(1, request.getLoadCaseGroupColumn().length() - 1));
                loadCaseCount.add(result);
            }
        }

        //默认值判断。 如：用例类型为 接口、功能、性能， 但是只查出了接口用例，那么功能、性能
        if (request.getFilterSearchList() != null) {
            List<String> xaxisColumnsList = new ArrayList<>();
            if (request.getFilterSearchList().containsKey(request.getXaxis())) {
                xaxisColumnsList = request.getFilterSearchList().get(request.getXaxis());

                if (CollectionUtils.isNotEmpty(xaxisColumnsList)) {
                    for (String xcolum : xaxisColumnsList) {
                        functionCaseCountResult = this.checkCountChartResultHasColumn(xcolum, functionCaseCountResult);
                        apiCaseCountResult = this.checkCountChartResultHasColumn(xcolum, apiCaseCountResult);
                        scenarioCaseCount = this.checkCountChartResultHasColumn(xcolum, scenarioCaseCount);
                        loadCaseCount = this.checkCountChartResultHasColumn(xcolum, loadCaseCount);
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(request.getXaxis(), "caseType")) {
                functionCaseCountResult = this.checkCountChartResultHasColumn("功能用例", functionCaseCountResult);
                apiCaseCountResult = this.checkCountChartResultHasColumn("接口用例", apiCaseCountResult);
                scenarioCaseCount = this.checkCountChartResultHasColumn("场景用例", scenarioCaseCount);
                loadCaseCount = this.checkCountChartResultHasColumn("性能用例", loadCaseCount);
            }
        }


        Map<String, TestCaseCountSummary> summaryMap = this.summaryCountResult(parseUser, parseStatus, request.getProjectId(), request.getOrder(),
                functionCaseCountResult, apiCaseCountResult, scenarioCaseCount, loadCaseCount);

        formatXaxisSeries(xAxis, seriesList, dto, summaryMap, yAxisSelectTestCase, yAxisSelectApi, yAxisSelectScenarioCase, yAxisSelectLoad);
        formatTable(dtos, summaryMap);

        formatPieChart(pieChartDTO, request.getXaxis(), summaryMap, yAxisSelectTestCase, yAxisSelectApi, yAxisSelectScenarioCase, yAxisSelectLoad);

        TestCaseCountTableDataDTO showTable = this.countShowTable(request.getXaxis(), request.getYaxis(), dtos);

        TestCaseCountResponse testCaseCountResult = new TestCaseCountResponse();
        testCaseCountResult.setBarChartDTO(dto);
        testCaseCountResult.setTableDTOs(dtos);
        testCaseCountResult.setPieChartDTO(pieChartDTO);
        testCaseCountResult.setShowTable(showTable);

        return testCaseCountResult;
    }

    private TestCaseCountTableDataDTO countShowTable(String groupName, List<String> yaxis, List<TestCaseCountTableDTO> dtos) {
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

    private List<TestCaseCountChartResult> checkCountChartResultHasColumn(String xcolumn, List<TestCaseCountChartResult> resultList) {
        boolean hasResult = false;
        for (TestCaseCountChartResult result : resultList) {
            if (StringUtils.equals(result.getGroupName(), xcolumn)) {
                hasResult = true;
                break;
            }
        }
        if (!hasResult) {
            TestCaseCountChartResult result = new TestCaseCountChartResult();
            result.setCountNum(0);
            result.setGroupName(xcolumn);
            resultList.add(result);
        }
        return resultList;
    }

    private void formatPieChart(PieChartDTO pieChartDTO, String groupName, Map<String, TestCaseCountSummary> summaryMap,
                                boolean selectTestCase, boolean selectApi, boolean selectScenarioCase, boolean selectLoad) {
        JSONArray titleArray = new JSONArray();
        titleArray.add("type");
        titleArray.add("count");
        titleArray.add(groupName);

        List<Series> seriesArr = new ArrayList<>();
        List<Title> titles = new ArrayList<>();

        int leftPx = 200;
        Map<String, String> caseDescMap = this.getCaseDescMap();
        for (TestCaseCountSummary summary : summaryMap.values()) {
            String leftPxStr = String.valueOf(leftPx);

            List<Object> dataList = new ArrayList<>();

            if (selectTestCase && summary.testCaseCount > 0) {
                PieData pieData = new PieData();
                pieData.setName(caseDescMap.get("testCaseDesc"));
                pieData.setValue(summary.testCaseCount);
                pieData.setColor("#F38F1F");
                dataList.add(pieData);
            }

            if (selectApi && summary.apiCaseCount > 0) {
                PieData apicasePieData = new PieData();
                apicasePieData.setName(caseDescMap.get("apiCaseDesc"));
                apicasePieData.setValue(summary.apiCaseCount);
                apicasePieData.setColor("#6FD999");
                dataList.add(apicasePieData);
            }

            if (selectScenarioCase && summary.scenarioCaseCount > 0) {
                PieData scenarioPieData = new PieData();
                scenarioPieData.setName(caseDescMap.get("scenarioCaseDesc"));
                scenarioPieData.setValue(summary.scenarioCaseCount);
                scenarioPieData.setColor("#2884F3");
                dataList.add(scenarioPieData);
            }

            if (selectLoad && summary.loadCaseCount > 0) {
                PieData loadCasePieData = new PieData();
                loadCasePieData.setName(caseDescMap.get("loadCaseDesc"));
                loadCasePieData.setValue(summary.loadCaseCount);
                loadCasePieData.setColor("#F45E53");
                dataList.add(loadCasePieData);
            }

            Series series = new Series();
            series.setType("pie");
            series.setRadius("50");
            series.setEncode(new JSONObject() {{
                this.put("itemName", "groupname");
                this.put("value", summary.groupName);
            }});
            seriesArr.add(series);

            series.setData(dataList);
            series.setCenter(new ArrayList<String>() {{
                this.add(leftPxStr);
                this.add("50%");
            }});

            Map<String,Object> labelMap = new HashMap<>();
            Map<String,Object> normalMap = new HashMap<>();
            normalMap.put("show",true);
            normalMap.put("formatter","{b}: {c}({d}%)");
            labelMap.put("normal",normalMap);
            series.setLabel(labelMap);

            Title title = new Title();
            title.setSubtext(summary.groupName);
            title.setLeft(leftPxStr);
            titles.add(title);

            leftPx = leftPx + 350;
        }

        pieChartDTO.setSeries(seriesArr);
        pieChartDTO.setTitle(titles);
        pieChartDTO.setWidth(leftPx);
    }

    private Map<String, TestCaseCountSummary> summaryCountResult(boolean parseGroupNameToUserName, boolean parseGrouNameToCaseStatus, String projectId, String order,
                                                                 List<TestCaseCountChartResult> functionCaseCountResult, List<TestCaseCountChartResult> apiCaseCountResult, List<TestCaseCountChartResult> scenarioCaseCount, List<TestCaseCountChartResult> loadCaseCount) {
        Map<String, TestCaseCountSummary> summaryMap = new LinkedHashMap<>();

        //groupName 解析对象
        Map<String, String> groupNameParseMap = new HashMap<>();
        if (parseGroupNameToUserName) {
            groupNameParseMap.putAll(this.getUserIdMap());
        }
        if (parseGrouNameToCaseStatus) {
            groupNameParseMap.putAll(this.getCaseStatusMap(projectId));
        }

        if (CollectionUtils.isNotEmpty(functionCaseCountResult)) {
            for (TestCaseCountChartResult result : functionCaseCountResult) {
                if (result.getGroupName() == null) {
                    result.setGroupName(groupNameParseMap.get("running"));
                } else {
                    if (groupNameParseMap.containsKey(result.getGroupName().toLowerCase(Locale.ROOT))) {
                        result.setGroupName(groupNameParseMap.get(result.getGroupName().toLowerCase(Locale.ROOT)));
                    }
                }

                String groupName = result.getGroupName();
                if (StringUtils.isNotEmpty(groupName)) {
                    TestCaseCountSummary summary = summaryMap.get(groupName);
                    if (summary == null) {
                        summary = new TestCaseCountSummary(groupName);
                    }
                    summary.testCaseCount = result.getCountNum();
                    summaryMap.put(groupName, summary);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(apiCaseCountResult)) {
            for (TestCaseCountChartResult result : apiCaseCountResult) {
                if (result.getGroupName() == null) {
                    result.setGroupName(groupNameParseMap.get("running"));
                } else {
                    if (groupNameParseMap.containsKey(result.getGroupName().toLowerCase(Locale.ROOT))) {
                        result.setGroupName(groupNameParseMap.get(result.getGroupName().toLowerCase(Locale.ROOT)));
                    }
                }
                String groupName = result.getGroupName();
                if (StringUtils.isNotEmpty(groupName)) {
                    TestCaseCountSummary summary = summaryMap.get(groupName);
                    if (summary == null) {
                        summary = new TestCaseCountSummary(groupName);
                    }
                    summary.apiCaseCount = result.getCountNum();
                    summaryMap.put(groupName, summary);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(scenarioCaseCount)) {
            for (TestCaseCountChartResult result : scenarioCaseCount) {
                if (result.getGroupName() == null) {
                    result.setGroupName(groupNameParseMap.get("running"));
                } else {
                    if (groupNameParseMap.containsKey(result.getGroupName().toLowerCase(Locale.ROOT))) {
                        result.setGroupName(groupNameParseMap.get(result.getGroupName().toLowerCase(Locale.ROOT)));
                    }
                }
                String groupName = result.getGroupName();
                if (StringUtils.isNotEmpty(groupName)) {
                    TestCaseCountSummary summary = summaryMap.get(groupName);
                    if (summary == null) {
                        summary = new TestCaseCountSummary(groupName);
                    }
                    summary.scenarioCaseCount = result.getCountNum();
                    summaryMap.put(groupName, summary);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(loadCaseCount)) {
            for (TestCaseCountChartResult result : loadCaseCount) {
                if (result.getGroupName() == null) {
                    result.setGroupName(groupNameParseMap.get("running"));
                } else {
                    if (groupNameParseMap.containsKey(result.getGroupName().toLowerCase(Locale.ROOT))) {
                        result.setGroupName(groupNameParseMap.get(result.getGroupName().toLowerCase(Locale.ROOT)));
                    }
                }
                String groupName = result.getGroupName();
                if (StringUtils.isNotEmpty(groupName)) {
                    TestCaseCountSummary summary = summaryMap.get(groupName);
                    if (summary == null) {
                        summary = new TestCaseCountSummary(groupName);
                    }
                    summary.loadCaseCount = result.getCountNum();
                    summaryMap.put(groupName, summary);
                }
            }
        }

        Map<String, TestCaseCountSummary> returmMap = new LinkedHashMap<>();

        if (StringUtils.equalsIgnoreCase(order, "desc")) {
            TreeMap<Long, List<TestCaseCountSummary>> treeMap = new TreeMap<>();
            for (TestCaseCountSummary model : summaryMap.values()) {
                if (treeMap.containsKey(model.getAllCount())) {
                    treeMap.get(model.getAllCount()).add(model);
                } else {
                    List<TestCaseCountSummary> list = new ArrayList<>();
                    list.add(model);
                    treeMap.put(model.getAllCount(), list);
                }
            }
            ArrayList<TestCaseCountSummary> sortedList = new ArrayList<>();
            for (List<TestCaseCountSummary> list : treeMap.values()) {
                sortedList.addAll(list);
            }

            for (int i = sortedList.size(); i > 0; i--) {
                TestCaseCountSummary model = sortedList.get(i - 1);
                returmMap.put(model.groupName, model);
            }
        } else if (StringUtils.equalsIgnoreCase(order, "asc")) {
            TreeMap<Long, List<TestCaseCountSummary>> treeMap = new TreeMap<>();
            for (TestCaseCountSummary model : summaryMap.values()) {
                if (treeMap.containsKey(model.getAllCount())) {
                    treeMap.get(model.getAllCount()).add(model);
                } else {
                    List<TestCaseCountSummary> list = new ArrayList<>();
                    list.add(model);
                    treeMap.put(model.getAllCount(), list);
                }
            }
            for (List<TestCaseCountSummary> list : treeMap.values()) {
                for (TestCaseCountSummary model : list) {
                    returmMap.put(model.groupName, model);
                }
            }
        } else {
            returmMap = summaryMap;
        }


        return returmMap;
    }

    private Map<String, String> getUserIdMap() {
        List<User> userList = userService.getUserList();
        Map<String, String> userIdMap = new HashMap<>();
        for (User model : userList) {
            userIdMap.put(model.getId(), model.getName() + "\n(" + model.getId() + ")");
        }
        return userIdMap;
    }

    private Map<String, String> getCaseStatusMap(String projectId) {

        Map<String, String> caseStatusMap = new HashMap<>();

        TestCaseTemplateService testCaseTemplateService = CommonBeanFactory.getBean(TestCaseTemplateService.class);
        TestCaseTemplateDao testCaseTemplate = testCaseTemplateService.getTemplate(projectId);

        caseStatusMap.put("prepare", Translator.get("test_case_status_prepare"));
        caseStatusMap.put("error", Translator.get("test_case_status_error"));
        caseStatusMap.put("success", Translator.get("test_case_status_success"));
        caseStatusMap.put("trash", Translator.get("test_case_status_trash"));
        caseStatusMap.put("underway", Translator.get("test_case_status_running"));
        caseStatusMap.put("starting", Translator.get("test_case_status_running"));
        caseStatusMap.put("saved", Translator.get("test_case_status_saved"));
        caseStatusMap.put("running", Translator.get("test_case_status_running"));
        caseStatusMap.put("finished", Translator.get("test_case_status_finished"));
        caseStatusMap.put("completed", Translator.get("test_case_status_finished"));

        if (testCaseTemplate != null && CollectionUtils.isNotEmpty(testCaseTemplate.getCustomFields())) {
            for (CustomField customField : testCaseTemplate.getCustomFields()) {
                if (StringUtils.equals(customField.getName(), "用例状态")) {
                    JSONArray optionsArr = JSONArray.parseArray(customField.getOptions());
                    for (int i = 0; i < optionsArr.size(); i++) {
                        JSONObject jsonObject = optionsArr.getJSONObject(i);
                        if (jsonObject.containsKey("value") && jsonObject.containsKey("text") &&
                                !StringUtils.equalsAnyIgnoreCase(jsonObject.getString("value"), "Prepare", "Error", "Success", "Trash", "Underway", "Starting", "Saved")) {
                            caseStatusMap.put(jsonObject.getString("value"), jsonObject.getString("text"));
                        }
                    }
                }
            }
        }

        return caseStatusMap;
    }


    private void formatXaxisSeries(XAxis xAxis, List<Series> seriesList, TestAnalysisChartDTO dto, Map<String, TestCaseCountSummary> summaryMap,
                                   boolean selectTestCase, boolean selectApi, boolean selectScenarioCase, boolean selectLoad) {
        List<String> xAxisDataList = new ArrayList<>();

        List<Object> testCaseCountList = new ArrayList<>();
        List<Object> apiCaseCountList = new ArrayList<>();
        List<Object> scenarioCaseCountList = new ArrayList<>();
        List<Object> loadCaseCountList = new ArrayList<>();
        for (TestCaseCountSummary summary : summaryMap.values()) {
            xAxisDataList.add(summary.groupName);
            if (summary.testCaseCount > 0) {
                testCaseCountList.add(String.valueOf(summary.testCaseCount));
            } else {
                testCaseCountList.add(null);
            }

            if (summary.apiCaseCount > 0) {
                apiCaseCountList.add(String.valueOf(summary.apiCaseCount));
            } else {
                apiCaseCountList.add(null);
            }

            if (summary.scenarioCaseCount > 0) {
                scenarioCaseCountList.add(String.valueOf(summary.scenarioCaseCount));
            } else {
                scenarioCaseCountList.add(null);
            }

            if (summary.loadCaseCount > 0) {
                loadCaseCountList.add(String.valueOf(summary.loadCaseCount));
            } else {
                loadCaseCountList.add(null);
            }

        }
        xAxis.setData(xAxisDataList);

        Map<String, String> caseDescMap = this.getCaseDescMap();

        //柱状图上显示数字
        Map<String, Object> labelMap = new HashMap<>();
        labelMap.put("show", true);
        labelMap.put("position", "inside");

        if (selectTestCase) {
            Series tetcaseSeries = new Series();
            tetcaseSeries.setName(caseDescMap.get("testCaseDesc"));
            tetcaseSeries.setColor("#F38F1F");
            tetcaseSeries.setRadius("20%");
            tetcaseSeries.setType("bar");
            tetcaseSeries.setStack("total");
            tetcaseSeries.setData(testCaseCountList);
            tetcaseSeries.setBarWidth("50");
            tetcaseSeries.setLabel(labelMap);
            seriesList.add(tetcaseSeries);
        }
        if (selectApi) {
            Series apiSeries = new Series();
            apiSeries.setName(caseDescMap.get("apiCaseDesc"));
            apiSeries.setColor("#6FD999");
            apiSeries.setType("bar");
            apiSeries.setStack("total");
            apiSeries.setData(apiCaseCountList);
            apiSeries.setLabel(labelMap);
            seriesList.add(apiSeries);
        }
        if (selectScenarioCase) {
            Series scenarioSeries = new Series();
            scenarioSeries.setName(caseDescMap.get("scenarioCaseDesc"));
            scenarioSeries.setColor("#2884F3");
            scenarioSeries.setType("bar");
            scenarioSeries.setStack("total");
            scenarioSeries.setData(scenarioCaseCountList);
            scenarioSeries.setLabel(labelMap);
            seriesList.add(scenarioSeries);
        }
        if (selectLoad) {
            Series loadSeries = new Series();
            loadSeries.setName(caseDescMap.get("loadCaseDesc"));
            loadSeries.setColor("#F45E53");
            loadSeries.setType("bar");
            loadSeries.setStack("total");
            loadSeries.setData(loadCaseCountList);
            loadSeries.setLabel(labelMap);
            seriesList.add(loadSeries);
        }

        dto.setXAxis(xAxis);
        dto.setYAxis(new YAxis());
        dto.setSeries(seriesList);

        Legend legend = new Legend();
        legend.setY("top");
        dto.setLegend(legend);
    }

    private void formatLegend(Legend legend, List<String> datas, TestCaseCountRequest yrequest) {
        Map<String, Boolean> selected = new LinkedHashMap<>();
        List<String> list = new LinkedList<>();
        legend.setSelected(selected);
        legend.setData(datas);
    }

    private void formatTable(List<TestCaseCountTableDTO> dtos, Map<String, TestCaseCountSummary> summaryMap) {
        for (TestCaseCountSummary summary : summaryMap.values()) {
            TestCaseCountTableDTO dto = new TestCaseCountTableDTO(summary.groupName, summary.testCaseCount, summary.apiCaseCount, summary.scenarioCaseCount, summary.loadCaseCount);
            dtos.add(dto);
        }
    }

    private Map<String, String> getCaseDescMap() {
        Map<String, String> map = new HashMap<>();
        map.put("testCaseDesc", Translator.get("test_case"));
        map.put("apiCaseDesc", Translator.get("api_case"));
        map.put("scenarioCaseDesc", Translator.get("scenario_case"));
        map.put("loadCaseDesc", Translator.get("performance_case"));
        return map;
    }

    public Map<String, List<Map<String, String>>> getSelectFilterDatas(String projectId) {
        Map<String, List<Map<String, String>>> returnMap = new HashMap<>();

        //组装用户
        QueryMemberRequest memberRequest = new QueryMemberRequest();
        memberRequest.setProjectId(projectId);
        List<User> userList = userService.getUserList();

        List<Map<String, String>> returnUserList = new ArrayList<>();
        for (User user : userList) {
            Map<String, String> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("label", user.getName() + "(" + user.getId() + ")");
            returnUserList.add(map);
        }

        //组装用例等级和用例状态
        TestCaseTemplateService testCaseTemplateService = CommonBeanFactory.getBean(TestCaseTemplateService.class);
        TestCaseTemplateDao testCaseTemplate = testCaseTemplateService.getTemplate(projectId);

        List<Map<String, String>> caseLevelList = new ArrayList<>();
        List<Map<String, String>> caseStatusList = new ArrayList<>();

        Map<String, String> statusPrepareMap = new HashMap<>();
        statusPrepareMap.put("id", "Prepare");
        statusPrepareMap.put("label", Translator.get("test_case_status_prepare"));

        Map<String, String> statusSavedMap = new HashMap<>();
        statusSavedMap.put("id", "Saved");
        statusSavedMap.put("label", Translator.get("test_case_status_saved"));

        Map<String, String> statusRunningMap = new HashMap<>();
        statusRunningMap.put("id", "Running");
        statusRunningMap.put("label", Translator.get("test_case_status_running"));

        Map<String, String> statusFinishedMap = new HashMap<>();
        statusFinishedMap.put("id", "Finished");
        statusFinishedMap.put("label", Translator.get("test_case_status_finished"));

        caseStatusList.add(statusPrepareMap);
        caseStatusList.add(statusSavedMap);
        caseStatusList.add(statusRunningMap);
        caseStatusList.add(statusFinishedMap);

        Map<String, String> levelMap1 = new HashMap<>();
        levelMap1.put("id", "P0");
        levelMap1.put("label", "P0");
        Map<String, String> levelMap2 = new HashMap<>();
        levelMap2.put("id", "P1");
        levelMap2.put("label", "P1");
        Map<String, String> levelMap3 = new HashMap<>();
        levelMap3.put("id", "P2");
        levelMap3.put("label", "P2");
        Map<String, String> levelMap4 = new HashMap<>();
        levelMap4.put("id", "P3");
        levelMap4.put("label", "P3");
        caseLevelList.add(levelMap1);
        caseLevelList.add(levelMap2);
        caseLevelList.add(levelMap3);
        caseLevelList.add(levelMap4);


        if (testCaseTemplate != null && CollectionUtils.isNotEmpty(testCaseTemplate.getCustomFields())) {
            for (CustomField customField : testCaseTemplate.getCustomFields()) {
                if (StringUtils.equals(customField.getName(), "用例状态")) {
                    JSONArray optionsArr = JSONArray.parseArray(customField.getOptions());
                    for (int i = 0; i < optionsArr.size(); i++) {
                        JSONObject jsonObject = optionsArr.getJSONObject(i);
                        if (jsonObject.containsKey("value") && jsonObject.containsKey("text")) {
                            String value = jsonObject.getString("value");
                            if (!StringUtils.equalsAnyIgnoreCase(value, "Prepare", "Error", "Success", "Trash", "Underway", "Starting", "Saved",
                                    "Completed", "test_track.case.status_finished")) {
                                Map<String, String> statusMap = new HashMap<>();
                                statusMap.put("id", jsonObject.getString("value"));
                                statusMap.put("label", jsonObject.getString("text"));
                                caseStatusList.add(statusMap);
                            }
                        }
                    }
                } else if (StringUtils.equals(customField.getName(), "用例等级")) {
                    JSONArray optionsArr = JSONArray.parseArray(customField.getOptions());
                    for (int i = 0; i < optionsArr.size(); i++) {
                        JSONObject jsonObject = optionsArr.getJSONObject(i);
                        if (jsonObject.containsKey("value") && jsonObject.containsKey("text") &&
                                !StringUtils.equalsAnyIgnoreCase(jsonObject.getString("value"), "P0", "P1", "P2", "P3")) {
                            Map<String, String> levelMap = new HashMap<>();
                            levelMap.put("id", jsonObject.getString("value"));
                            levelMap.put("label", jsonObject.getString("text"));
                            caseLevelList.add(levelMap);
                        }
                    }
                }
            }
        }
        Map<String, String> caseDescMap = this.getCaseDescMap();
        // 组装用例类型
        List<Map<String, String>> caseTypeList = new ArrayList<>();
        Map<String, String> caseTypeMap1 = new HashMap<>();
        caseTypeMap1.put("id", "testCase");
        caseTypeMap1.put("label", caseDescMap.get("testCaseDesc"));
        Map<String, String> caseTypeMap2 = new HashMap<>();
        caseTypeMap2.put("id", "apiCase");
        caseTypeMap2.put("label", caseDescMap.get("apiCaseDesc"));
        Map<String, String> caseTypeMap3 = new HashMap<>();
        caseTypeMap3.put("id", "scenarioCase");
        caseTypeMap3.put("label", caseDescMap.get("scenarioCaseDesc"));
        Map<String, String> caseTypeMap4 = new HashMap<>();
        caseTypeMap4.put("id", "loadCase");
        caseTypeMap4.put("label", caseDescMap.get("loadCaseDesc"));
        caseTypeList.add(caseTypeMap1);
        caseTypeList.add(caseTypeMap2);
        caseTypeList.add(caseTypeMap3);
        caseTypeList.add(caseTypeMap4);

        returnMap.put("casetype", caseTypeList);
        returnMap.put("caselevel", caseLevelList);
        returnMap.put("casestatus", caseStatusList);
        returnMap.put("creator", returnUserList);
        returnMap.put("maintainer", returnUserList);
        return returnMap;
    }
}
