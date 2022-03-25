package io.metersphere.performance.service;

import com.alibaba.excel.util.CollectionUtils;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.engine.EngineContext;
import io.metersphere.performance.engine.EngineFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


// 非事务运行
@Service
public class JmeterFileService {
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    public byte[] downloadZip(String reportId, double[] ratios, int resourceIndex) {
        try {
            LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
            int wait = 0;
            while (loadTestReport == null) {
                if (wait > 120_000) {
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(200);
                wait += 200;
                loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
            }
            if (loadTestReport == null) {
                MSException.throwException("测试报告不存在或还没产生");
            }
            EngineContext context = EngineFactory.createContext(loadTestReport, ratios, reportId, resourceIndex);
            return zipFilesToByteArray(context);
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
        files.put(fileName, context.getContent());
        // 保存jmx
        LoadTestReportWithBLOBs record = new LoadTestReportWithBLOBs();
        record.setId(context.getReportId());
        record.setJmxContent(new String(context.getContent()));
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
