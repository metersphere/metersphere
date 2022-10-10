package io.metersphere.commons.utils;

import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.BodyFile;
import io.metersphere.base.domain.FileMetadata;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jorphan.collections.HashTree;
import org.aspectj.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApiFileUtil extends FileUtils {
    public static void deleteBodyFiles(MsTestElement request) {
        if (request != null && StringUtils.isNotBlank(request.getId())) {
            String path = BODY_FILE_DIR + File.separator + request.getId();
            File testDir = new File(path);
            if (!testDir.exists()) {
                return;
            }
            List<String> files = new ArrayList<>();
            if (request != null && StringUtils.equalsIgnoreCase(request.getType(), HTTPSamplerProxy.class.getSimpleName())) {
                MsHTTPSamplerProxy samplerProxy = (MsHTTPSamplerProxy) request;
                Body body = samplerProxy.getBody();
                if (body != null && !CollectionUtils.isEmpty(body.getKvs())) {
                    body.getKvs().stream().filter(KeyValue::isFile).forEach(keyValue -> {
                        files.addAll(keyValue.getFiles().stream().map(BodyFile::getName).collect(Collectors.toList()));
                    });
                }
                if (body != null && !CollectionUtils.isEmpty(body.getBinary())) {
                    body.getBinary().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
                        files.addAll(keyValue.getFiles().stream().map(BodyFile::getName).collect(Collectors.toList()));
                    });
                }
            }

            File[] optFilesName = testDir.listFiles();
            if (CollectionUtils.isNotEmpty(files)) {
                for (File f : optFilesName) {
                    if (!files.contains(f.getName())) {
                        f.delete();
                    }
                }
            } else {
                FileUtil.deleteContents(testDir);
                testDir.delete();
            }
        }
    }

    public static String getFilePath(BodyFile file) {
        String type = StringUtils.isNotEmpty(file.getFileType()) ? file.getFileType().toLowerCase() : null;
        String name = file.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(ApiFileUtil.BODY_FILE_DIR, File.separator, file.getProjectId(), File.separator, name);
    }

    public static String getFilePath(FileMetadata fileMetadata) {
        String type = StringUtils.isNotEmpty(fileMetadata.getType()) ? fileMetadata.getType().toLowerCase() : null;
        String name = fileMetadata.getName();
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return StringUtils.join(ApiFileUtil.BODY_FILE_DIR, File.separator, fileMetadata.getProjectId(), File.separator, name);
    }

    /**
     * 获取当前jmx 涉及到的文件
     *
     * @param tree
     */
    public static void getFiles(HashTree tree, List<BodyFile> files) {
        for (Object key : tree.keySet()) {
            HashTree node = tree.get(key);
            if (key instanceof HTTPSamplerProxy) {
                HTTPSamplerProxy source = (HTTPSamplerProxy) key;
                if (source != null && source.getHTTPFiles().length > 0) {
                    for (HTTPFileArg arg : source.getHTTPFiles()) {
                        BodyFile file = new BodyFile();
                        file.setId(arg.getParamName());
                        file.setName(arg.getPath());
                        files.add(file);
                    }
                }
            } else if (key instanceof CSVDataSet) {
                CSVDataSet source = (CSVDataSet) key;
                if (source != null && StringUtils.isNotEmpty(source.getPropertyAsString("filename"))) {
                    BodyFile file = new BodyFile();
                    file.setId(source.getPropertyAsString("filename"));
                    file.setName(source.getPropertyAsString("filename"));
                    files.add(file);
                }
            }
            if (node != null) {
                getFiles(node, files);
            }
        }
    }
}
