package io.metersphere.api.service;

import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.JarConfigService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ApiJmeterFileService {

    public byte[] downloadJmeterFiles(List<BodyFile> bodyFileList) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        Map<String, byte[]> multipartFiles = this.getMultipartFiles(bodyFileList);
        if (!com.alibaba.excel.util.CollectionUtils.isEmpty(multipartFiles)) {
            for (String k : multipartFiles.keySet()) {
                byte[] v = multipartFiles.get(k);
                files.put(k, v);
            }
        }
        return listBytesToZip(files);
    }

    public byte[] downloadJmeterJar() {
        Map<String, byte[]> files = new HashMap<>();
        // 获取JAR
        Map<String, byte[]> jarFiles = this.getJar();
        if (!com.alibaba.excel.util.CollectionUtils.isEmpty(jarFiles)) {
            for (String k : jarFiles.keySet()) {
                byte[] v = jarFiles.get(k);
                files.put(k, v);
            }
        }
        return listBytesToZip(files);
    }

    private Map<String, byte[]> getJar() {
        Map<String, byte[]> jarFiles = new LinkedHashMap<>();
        // jar 包
        JarConfigService jarConfigService = CommonBeanFactory.getBean(JarConfigService.class);
        List<JarConfig> jars = jarConfigService.list();
        jars.forEach(jarConfig -> {
            try {
                String path = jarConfig.getPath();
                File file = new File(path);
                if (file.isDirectory() && !path.endsWith("/")) {
                    file = new File(path + "/");
                }
                byte[] fileByte = FileUtils.fileToByte(file);
                if (fileByte != null) {
                    jarFiles.put(file.getName(), fileByte);
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
        return jarFiles;
    }

    private Map<String, byte[]> getMultipartFiles(List<BodyFile> files) {
        Map<String, byte[]> multipartFiles = new LinkedHashMap<>();
        // 获取附件
        if (CollectionUtils.isNotEmpty(files)) {
            for (BodyFile bodyFile : files) {
                File file = new File(bodyFile.getName());
                if (file != null && !file.exists()) {
                    byte[] fileByte = FileUtils.fileToByte(file);
                    if (fileByte != null) {
                        multipartFiles.put(file.getName(), fileByte);
                    }
                }
            }
        }
        return multipartFiles;
    }

    private byte[] listBytesToZip(Map<String, byte[]> mapReport) {
        try {
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
        } catch (Exception e) {
            return null;
        }
    }
}