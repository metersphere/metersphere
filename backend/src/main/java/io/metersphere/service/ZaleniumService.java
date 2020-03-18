package io.metersphere.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FucTestMapper;
import io.metersphere.base.mapper.FucTestReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ZaleniumService {

    @Resource
    RestTemplate restTemplate;

    @Resource
    FucTestReportMapper fucTestReportMapper;

    @Resource
    FucTestMapper fucTestMapper;

    public void syncTestResult(){

        List<ZaleniumTest> zaleniumTests = getZaleniumTest();

        List<String> fucTestIds = zaleniumTests.stream().map(test -> test.getTestName())
                .collect(Collectors.toList());

        FucTestExample fucTestExample = new FucTestExample();
        fucTestExample.createCriteria().andIdIn(fucTestIds);
        List<FucTest> fucTests = fucTestMapper.selectByExample(fucTestExample);

        List<String> reportIds = fucTestReportMapper.selectByExample(new FucTestReportExample()).stream().map(report -> report.getId()).collect(Collectors.toList());

        Map<String, FucTest> fucTestMaps = fucTests.stream()
                .collect(Collectors.toMap(FucTest::getId, Function.identity()));

        zaleniumTests.forEach(item -> {
            if(!reportIds.contains(item.getTestName())){
                saveFucTestReport(item, fucTestMaps.get(item.getTestName()));
            }
        });
    }

    private void saveFucTestReport(ZaleniumTest item, FucTest fucTest) {
        FucTestReport fucTestReport = new FucTestReport();
        fucTestReport.setCreateTime(System.currentTimeMillis());
        fucTestReport.setUpdateTime(System.currentTimeMillis());
        fucTestReport.setTestId(item.getTestName());
        fucTestReport.setStatus("1");
        fucTestReport.setId(item.getTestName());
        JSONObject content = new JSONObject();
        content.put("videoUrl", "dashboard/" + item.getFileName());
        content.put("seleniumLog", "dashboard/logs/" + item.getTestNameNoExtension() + "/selenium-multinode-stderr.log");
        content.put("browserDriverLog", "dashboard/" + item.getBrowserDriverLogFileName());
        fucTestReport.setContent(content.toJSONString());
        if(fucTest != null){
            fucTestReport.setDescription(fucTest.getDescription());
            fucTestReport.setName(fucTest.getName());
        } else {
            fucTestReport.setDescription("ZaleniumTest");
            fucTestReport.setName(item.getTestName());
        }
        fucTestReportMapper.insert(fucTestReport);
    }

    private List<ZaleniumTest> getZaleniumTest() {
        List<ZaleniumTest> tests = new ArrayList<>();
        String url = "http://localhost:4444/dashboard/information?lastDateAddedToDashboard=0";
        tests.addAll(Arrays.asList(restTemplate.getForObject(url, ZaleniumTest[].class)));
        return tests;
    }

    public FucTestLog getFucTestLog(String endpoint, FucTestLog logPaths) {
        FucTestLog testLog = new FucTestLog();
        testLog.setSeleniumLog(getZaleniumTestLog(endpoint, logPaths.getSeleniumLog()));
        testLog.setBrowserDriverLog(getZaleniumTestLog(endpoint, logPaths.getBrowserDriverLog()));
        return testLog;
    }

    private String getZaleniumTestLog(String endpoint, String logPath) {
        return restTemplate.getForObject(endpoint + "/" + logPath, String.class);
    }

}
