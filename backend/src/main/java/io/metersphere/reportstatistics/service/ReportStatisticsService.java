package io.metersphere.reportstatistics.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.ReportStatistics;
import io.metersphere.base.domain.ReportStatisticsExample;
import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import io.metersphere.base.mapper.ReportStatisticsMapper;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.reportstatistics.dto.ReportStatisticsSaveRequest;
import io.metersphere.reportstatistics.dto.ReportStatisticsType;
import io.metersphere.reportstatistics.dto.TestCaseCountResponse;
import io.metersphere.reportstatistics.dto.TestCaseCountTableDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableItemDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableRowDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author song.tianyang
 * @Date 2021/9/14 4:50 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportStatisticsService {
    @Resource
    private ReportStatisticsMapper reportStatisticsMapper;

    public ReportStatisticsWithBLOBs saveByRequest(ReportStatisticsSaveRequest request) {
        ReportStatisticsWithBLOBs model = new ReportStatisticsWithBLOBs();
        model.setId(UUID.randomUUID().toString());

        String name = "用例分析报表";
        if(StringUtils.equalsIgnoreCase(ReportStatisticsType.TEST_CASE_COUNT.name(),request.getReportType())){
            name = "用例统计报表";
            model.setReportType(ReportStatisticsType.TEST_CASE_COUNT.name());
        }else {
            model.setReportType(ReportStatisticsType.TEST_CASE_ANALYSIS.name());
        }
        if(StringUtils.isEmpty(request.getName())){
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

    public int deleteById(String id) {
        return reportStatisticsMapper.deleteByPrimaryKey(id);
    }

    public ReportStatisticsWithBLOBs selectById(String id) {
        ReportStatisticsWithBLOBs blob =  reportStatisticsMapper.selectByPrimaryKey(id);
        JSONObject dataOption = JSONObject.parseObject(blob.getDataOption());
        JSONObject selectOption = JSONObject.parseObject(blob.getSelectOption());
        if(!dataOption.containsKey("showTable")){
            List<TestCaseCountTableDTO> dtos = JSONArray.parseArray(dataOption.getString("tableData"),TestCaseCountTableDTO.class);
            TestCaseCountTableDataDTO showTable = this.countShowTable(selectOption.getString("xaxis"), JSONArray.parseArray(selectOption.getString("yaxis"),String.class),dtos);
            dataOption.put("showTable",showTable);
            blob.setDataOption(dataOption.toString());
        }
        return blob;
    }
    private TestCaseCountTableDataDTO countShowTable(String groupName, List<String> yaxis, List<TestCaseCountTableDTO> dtos) {
        TestCaseCountTableDataDTO returnDTO = new TestCaseCountTableDataDTO();
        String [] headers = new String[]{groupName,"总计","testCase","apiCase","scenarioCase","loadCaseCount"};

        List<TestCaseCountTableItemDataDTO> heads = new ArrayList<>();
        boolean showTestCase = true,showApi = true,showScenario = true,showLoad = true;
        for (String head : headers) {
            if(StringUtils.equalsAnyIgnoreCase(head,groupName,"总计") || yaxis.contains(head)){
                TestCaseCountTableItemDataDTO headData = new TestCaseCountTableItemDataDTO();
                headData.setId(UUID.randomUUID().toString());
                headData.setValue(head);
                heads.add(headData);
            }else {
                if(StringUtils.equalsIgnoreCase(head,"testCase")){
                    showTestCase = false;
                }else if(StringUtils.equalsIgnoreCase(head,"apiCase")){
                    showApi = false;
                }else if(StringUtils.equalsIgnoreCase(head,"scenarioCase")){
                    showScenario = false;
                }else if(StringUtils.equalsIgnoreCase(head,"loadCaseCount")){
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

            if(showTestCase){
                TestCaseCountTableItemDataDTO itemData = new TestCaseCountTableItemDataDTO();
                itemData.setId(UUID.randomUUID().toString());
                itemData.setValue(data.getTestCaseCount());
                rowDataList.add(itemData);
            }

            if(showApi){
                TestCaseCountTableItemDataDTO itemData = new TestCaseCountTableItemDataDTO();
                itemData.setId(UUID.randomUUID().toString());
                itemData.setValue(data.getApiCaseCount());
                rowDataList.add(itemData);
            }

            if(showScenario){
                TestCaseCountTableItemDataDTO itemData = new TestCaseCountTableItemDataDTO();
                itemData.setId(UUID.randomUUID().toString());
                itemData.setValue(data.getScenarioCaseCount());
                rowDataList.add(itemData);
            }

            if(showLoad){
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
        if(StringUtils.isNotEmpty(request.getProjectId())){
            criteria.andProjectIdEqualTo(request.getProjectId());
        }
        if(StringUtils.isNotEmpty(request.getReportType())){
            criteria.andReportTypeEqualTo(request.getReportType());
        }
        if(StringUtils.isNotEmpty(request.getName())){
            criteria.andNameLike(request.getName());
        }
        example.setOrderByClause("create_time DESC");
        return  reportStatisticsMapper.selectByExample(example);
    }
}
