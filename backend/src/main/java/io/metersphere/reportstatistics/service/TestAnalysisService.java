package io.metersphere.reportstatistics.service;

import io.metersphere.base.mapper.ext.ExtTestAnalysisMapper;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.reportstatistics.dto.*;
import io.metersphere.reportstatistics.dto.charts.Legend;
import io.metersphere.reportstatistics.dto.charts.Series;
import io.metersphere.reportstatistics.dto.charts.XAxis;
import io.metersphere.reportstatistics.dto.charts.YAxis;
import io.metersphere.service.ProjectService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestAnalysisService {
    @Resource
    private ExtTestAnalysisMapper extTestAnalysisMapper;
    @Resource
    private ProjectService projectService;

    private final String ADD = "新增用例";
    private final String UPDATE = "修改用例";

    public TestAnalysisResult getReport(TestAnalysisChartRequest request) {
        if (CollectionUtils.isEmpty(request.getTimes())) {
            // 最近七天
            request.setTimes(Arrays.asList(System.currentTimeMillis() - 7 * 24 * 3600 * 1000L, System.currentTimeMillis()));
        }
        request.setStartTime(DateUtils.getDataStr(request.getTimes().get(0)));
        request.setEndTime(DateUtils.getDataStr(request.getTimes().get(1)));
        if (CollectionUtils.isEmpty(request.getProjects())) {
            // 获取当前组织空间下所有项目
            String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
            ProjectRequest projectRequest = new ProjectRequest();
            projectRequest.setWorkspaceId(currentWorkspaceId);
            List<ProjectDTO> projectDTOS = projectService.getProjectList(projectRequest);
            if (CollectionUtils.isNotEmpty(projectDTOS)) {
                request.setProjects(projectDTOS.stream().map(ProjectDTO::getId).collect(Collectors.toList()));
            } else {
                request.setProjects(new LinkedList<String>(){{this.add(UUID.randomUUID().toString());}});
            }
        }
        TestAnalysisChartDTO dto = new TestAnalysisChartDTO();
        List<TestAnalysisTableDTO> dtos = new LinkedList<>();

        List<Series> seriesList = new LinkedList<>();
        XAxis xAxis = new XAxis();

        if (CollectionUtils.isEmpty(request.getUsers())) {
            // 组织charts格式数据
            Legend legend = new Legend();
            formatLegend(legend, null, request);
            dto.setLegend(legend);
            List<TestAnalysisChartResult> createResults = extTestAnalysisMapper.getCraeteCaseReport(request);
            // 获取修改的用例统计报表
            List<TestAnalysisChartResult> updateResults = extTestAnalysisMapper.getUpdateCaseReport(request);
            formatXaxisSeries(xAxis, seriesList, "", dto, createResults, updateResults);
            formatTable(dtos, createResults, updateResults);
        } else {
            List<String> users = request.getUsers();
            Legend legend = new Legend();
            formatLegend(legend, users, request);
            dto.setLegend(legend);

            // 按用户展示
            boolean isFlag = true;
            for (String item : users) {
                request.setUsers(Arrays.asList(item));
                List<TestAnalysisChartResult> createResults = extTestAnalysisMapper.getCraeteCaseReport(request);
                // 获取修改的用例统计报表
                List<TestAnalysisChartResult> updateResults = extTestAnalysisMapper.getUpdateCaseReport(request);
                formatXaxisSeries(xAxis, seriesList, item + "-", dto, createResults, updateResults);

                // 初始化列表总量，按天统计总量
                if (isFlag) {
                    formatTable(dtos, createResults, updateResults);
                    isFlag = false;
                }
                // 增加子项
                for (int j = 0; j < dtos.size(); j++) {
                    TestAnalysisTableDTO childItem = new TestAnalysisTableDTO(item, createResults.get(j).getCountNum(), updateResults.get(j).getCountNum(), null);
                    dtos.get(j).getChildren().add(childItem);
                }
            }
        }
        // 每行总计
        dtos.forEach(item -> {
            if (CollectionUtils.isNotEmpty(item.getChildren())) {
                // table 总和计算
                List<Integer> collect = item.getChildren().stream().map(childItem -> Integer.valueOf(childItem.getCreateCount())).collect(Collectors.toList());
                // reduce求和
                Optional<Integer> createCount = collect.stream().reduce(Integer::sum);
                List<Integer> upCollect = item.getChildren().stream().map(childItem -> Integer.valueOf(childItem.getUpdateCount())).collect(Collectors.toList());
                // reduce求和
                Optional<Integer> updateCount = upCollect.stream().reduce(Integer::sum);
                item.setCreateCount(createCount.get().toString());
                item.setUpdateCount(updateCount.get().toString());
            }
        });
        // table 总和计算
        List<Integer> collect = dtos.stream().map(item -> Integer.valueOf(item.getCreateCount())).collect(Collectors.toList());
        // reduce求和
        Optional<Integer> createCount = collect.stream().reduce(Integer::sum);
        List<Integer> upCollect = dtos.stream().map(item -> Integer.valueOf(item.getUpdateCount())).collect(Collectors.toList());
        // reduce求和
        Optional<Integer> updateCount = upCollect.stream().reduce(Integer::sum);
        dtos.add(new TestAnalysisTableDTO("Count", createCount.get().toString(), updateCount.get().toString(), new LinkedList<>()));

        TestAnalysisResult testAnalysisResult = new TestAnalysisResult();
        testAnalysisResult.setChartDTO(dto);
        testAnalysisResult.setTableDTOs(dtos);
        return testAnalysisResult;
    }

    private void formatXaxisSeries(XAxis xAxis, List<Series> seriesList, String name, TestAnalysisChartDTO dto, List<TestAnalysisChartResult> createResults, List<TestAnalysisChartResult> updateResults) {
        if (CollectionUtils.isNotEmpty(createResults)) {
            xAxis.setData(createResults.stream().map(TestAnalysisChartResult::getDateStr).collect(Collectors.toList()));
            Series series = new Series();
            series.setName(name + ADD);
            series.setData(createResults.stream().map(TestAnalysisChartResult::getCountNum).collect(Collectors.toList()));
            seriesList.add(series);
        }
        if (CollectionUtils.isNotEmpty(updateResults)) {
            xAxis.setData(updateResults.stream().map(TestAnalysisChartResult::getDateStr).collect(Collectors.toList()));
            Series series = new Series();
            series.setName(name + UPDATE);
            series.setColor("#B8741A");
            series.setData(updateResults.stream().map(TestAnalysisChartResult::getCountNum).collect(Collectors.toList()));
            seriesList.add(series);
        }
        dto.setXAxis(xAxis);
        dto.setYAxis(new YAxis());
        dto.setSeries(seriesList);
    }

    private void formatLegend(Legend legend, List<String> datas, TestAnalysisChartRequest request) {
        Map<String, Boolean> selected = new LinkedHashMap<>();
        List<String> list = new LinkedList<>();
        if (CollectionUtils.isEmpty(datas)) {
            selected.put(ADD, request.isCreateCase());
            selected.put(UPDATE, request.isUpdateCase());
            list.add(ADD);
            list.add(UPDATE);
        } else {
            datas.forEach(item -> {
                selected.put(item + "-" + ADD, request.isCreateCase());
                selected.put(item + "-" + UPDATE, request.isUpdateCase());
                list.add(item + "-" + ADD);
                list.add(item + "-" + UPDATE);
            });
        }
        legend.setSelected(selected);
        legend.setData(list);
    }

    private void formatTable(List<TestAnalysisTableDTO> dtos, List<TestAnalysisChartResult> createResults, List<TestAnalysisChartResult> updateResults) {
        for (int i = 0; i < createResults.size(); i++) {
            TestAnalysisTableDTO dto = new TestAnalysisTableDTO(createResults.get(i).getDateStr(), createResults.get(i).getCountNum(), updateResults.get(i).getCountNum(), new LinkedList<>());
            dtos.add(dto);
        }
    }
}
