package io.metersphere.metadata.repository;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.metadata.vo.FileRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocalFileRepository implements FileRepository {
    @Override
    public String saveFile(MultipartFile multipartFile, FileRequest request) throws Exception {
        if (multipartFile == null || request == null || StringUtils.isEmpty(request.getFileName()) || StringUtils.isEmpty(request.getProjectId())) {
            return null;
        }
        File file = createFile(request);
        try (InputStream in = multipartFile.getInputStream(); OutputStream out = new FileOutputStream(file)) {
            file.createNewFile();
            final int MAX = 4096;
            byte[] buf = new byte[MAX];
            for (int bytesRead = in.read(buf, 0, MAX); bytesRead != -1; bytesRead = in.read(buf, 0, MAX)) {
                out.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            MSException.throwException(Translator.get("upload_fail"));
        }
        return file.getPath();
    }

    @Override
    public String saveFile(byte[] bytes, FileRequest request) throws IOException {
        File file = createFile(request);
        try (OutputStream ops = new FileOutputStream(file);) {
            ops.write(bytes);
            return file.getPath();
        } catch (Exception e) {
            LogUtil.info(e);
        }
        return null;
    }

    @Override
    public void delete(FileRequest request) throws Exception {
        String path = StringUtils.join(FileUtils.BODY_FILE_DIR + "/", request.getProjectId(), "/", request.getFileName());
        File file = new File(path);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }

    }

    @Override
    public byte[] getFile(FileRequest request) throws Exception {
        byte[] buffer = new byte[0];
        File file = null;
        // 兼容历史数据
        if (StringUtils.isNotEmpty(request.getResourceType()) && StringUtils.isNotEmpty(request.getPath())) {
            file = new File(request.getPath());
        } else {
            file = new File(StringUtils.join(FileUtils.BODY_FILE_DIR, "/", request.getProjectId(), "/", request.getFileName()));
        }
        if (file != null && file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
                byte[] b = new byte[1024];
                int n;
                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                buffer = bos.toByteArray();
            } catch (Exception e) {
                MSException.throwException(e);
            }
        }
        return buffer;
    }

    @Override
    public InputStream getFileAsStream(FileRequest request) throws Exception {
        // 兼容历史数据
        if (StringUtils.isNotEmpty(request.getResourceType()) && StringUtils.isNotEmpty(request.getPath())) {
            return new FileInputStream(request.getPath());
        } else {
            return new FileInputStream(StringUtils.join(FileUtils.BODY_FILE_DIR, "/", request.getProjectId(), "/", request.getFileName()));
        }
    }

    @Override
    public boolean reName(FileRequest request) throws Exception {
        File file = new File(StringUtils.join(FileUtils.BODY_FILE_DIR, "/", request.getProjectId(), "/", request.getBeforeName()));
        File newFile = new File(StringUtils.join(FileUtils.BODY_FILE_DIR, "/", request.getProjectId(), "/", request.getFileName()));
        if (file.exists()) {
            return file.renameTo(newFile);
        }
        return false;
    }

    @Override
    public List<FileInfoDTO> getFileBatch(List<FileRequest> requestList) throws Exception {
        if (CollectionUtils.isNotEmpty(requestList)) {
            return requestList.stream().map(fileRequest -> {
                byte[] content = new byte[0];
                try {
                    content = this.getFile(fileRequest);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return new FileInfoDTO(
                        fileRequest.getResourceId(), fileRequest.getFileName(),
                        fileRequest.getStorage(), fileRequest.getPath(), content);
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    private File createFile(FileRequest request) {
        String path = StringUtils.join(FileUtils.BODY_FILE_DIR, "/", request.getProjectId());
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File file = new File(StringUtils.join(path, "/", request.getFileName()));
        return file;
    }
}
