package io.metersphere.performance.service;


import com.alibaba.excel.util.CollectionUtils;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.engine.EngineContext;
import io.metersphere.performance.engine.EngineFactory;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional(rollbackFor = Exception.class)
public class JmeterFileService {

    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;

    public byte[] downloadZip(String testId, double[] ratios, String reportId, int resourceIndex) {
        try {
            LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(testId);
            TestPlanLoadCase testPlanLoadCase = null;
            if (loadTest == null) {
                // 通过测试计划执行性能用例时，testId为测试计划性能用例表的主键ID，根据ID查询用例自身的压力配置
                testPlanLoadCase = testPlanLoadCaseMapper.selectByPrimaryKey(testId);
                if (testPlanLoadCase != null) {
                    loadTest = loadTestMapper.selectByPrimaryKey(testPlanLoadCase.getLoadCaseId());
                    if (loadTest != null) {
                        // 用例自身设置了资源池ID
                        if (StringUtils.isNotBlank(testPlanLoadCase.getTestResourcePoolId())) {
                            loadTest.setTestResourcePoolId(testPlanLoadCase.getTestResourcePoolId());
                        }
                        // 用例自身设置了压力配置
                        if (StringUtils.isNotBlank(testPlanLoadCase.getLoadConfiguration())) {
                            loadTest.setLoadConfiguration(testPlanLoadCase.getLoadConfiguration());
                        }
                    }
                }
            }
            EngineContext context = EngineFactory.createContext(loadTest, ratios, reportId, resourceIndex);
            if (testPlanLoadCase != null) {
                // ID
                context.setTestId(testPlanLoadCase.getId());
            }
            return zipFilesToByteArray(context);
        } catch (MSException e) {
            LogUtil.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }
        return null;
    }

    private byte[] zipFilesToByteArray(EngineContext context) throws IOException {
        String testId = context.getTestId();
        String fileName = testId + ".jmx";

        Map<String, byte[]> files = new HashMap<>();

        //  每个测试生成一个文件夹
        files.put(fileName, context.getContent().getBytes(StandardCharsets.UTF_8));
        // 保存jmx
        LoadTestReportWithBLOBs record = new LoadTestReportWithBLOBs();
        record.setId(context.getReportId());
        record.setJmxContent(context.getContent());
        extLoadTestReportMapper.updateJmxContentIfAbsent(record);
        // 保存 byte[]
        Map<String, byte[]> jarFiles = context.getTestResourceFiles();
        if (!CollectionUtils.isEmpty(jarFiles)) {
            for (String k : jarFiles.keySet()) {
                byte[] v = jarFiles.get(k);
                files.put(k, v);
            }
        }

        return listBytesToZip(files);
    }


    private byte[] listBytesToZip(Map<String, byte[]> mapReport) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (Map.Entry<String, byte[]> report : mapReport.entrySet()) {
            ZipEntry entry = new ZipEntry(report.getKey());
            entry.setSize(report.getValue().length);
            zos.putNextEntry(entry);
            zos.write(report.getValue());
        }
        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }
}
