package io.metersphere.service;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.EngineFactory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public void downloadZip(String reportId, double[] ratios, int resourceIndex, HttpServletResponse response) {
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
            EngineContext context = EngineFactory.createContext(loadTestReport, ratios, resourceIndex);
            zipFilesToByteArray(context, response);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e);
        }
    }

    private void zipFilesToByteArray(EngineContext context, HttpServletResponse response) throws IOException {
        String testId = context.getTestId();
        String fileName = testId + ".jmx";


        //  每个测试生成一个文件夹
        // 保存jmx
        LoadTestReportWithBLOBs record = new LoadTestReportWithBLOBs();
        record.setId(context.getReportId());
        record.setJmxContent(new String(context.getContent()));
        extLoadTestReportMapper.updateJmxContentIfAbsent(record);
        // 关联文件
        Map<String, InputStream> testResourceFiles = context.getTestResourceFiles();
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            // jmx 本身
            zos.putNextEntry(new ZipEntry(fileName));
            zos.write(context.getContent());
            zos.closeEntry();
            // 关联文件
            if (!MapUtils.isEmpty(testResourceFiles)) {
                for (String name : testResourceFiles.keySet()) {
                    InputStream in = testResourceFiles.get(name);
                    zos.putNextEntry(new ZipEntry(name));
                    byte[] bytes = new byte[4096];
                    int len;
                    while ((len = in.read(bytes)) != -1) {
                        zos.write(bytes, 0, len);
                    }
                    in.close();
                    zos.closeEntry();
                }
            }
            response.setHeader("Content-Disposition", "attachment;filename=" + testId + ".zip");
            response.flushBuffer();
        }
    }
}
