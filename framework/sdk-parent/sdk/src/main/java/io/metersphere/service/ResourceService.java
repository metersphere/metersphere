package io.metersphere.service;

import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.request.MdImageSaveRequest;
import io.metersphere.request.MdUploadRequest;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceService {

    @Resource
    private MdFileService mdFileService;

    public String mdUpload(MdUploadRequest request, MultipartFile file) {
        FileUtils.validateFileName(request.getFileName());
        String fileName = request.getId() + request.getFileName().substring(request.getFileName().lastIndexOf("."));
        FileUtils.uploadFile(file, FileUtils.MD_IMAGE_DIR, fileName);
        return fileName;
    }

    public String mdUpload2Temp(MdUploadRequest request, MultipartFile file) {
        FileUtils.validateFileName(request.getFileName());
        String fileName = request.getId() + request.getFileName().substring(request.getFileName().lastIndexOf("."));
        FileUtils.uploadFile(file, FileUtils.MD_IMAGE_TEMP_DIR, fileName);
        return fileName;
    }

    public ResponseEntity<FileSystemResource> getMdImage(String name) {
        FileUtils.validateFileName(name);
        File file = new File(FileUtils.MD_IMAGE_TEMP_DIR + "/" + name);
        if (file.exists()) {
            // 如果临时目录有该图片则，从临时目录中返回
            return getImage(FileUtils.MD_IMAGE_TEMP_DIR + "/" + name);
        }
        return getImage(FileUtils.MD_IMAGE_DIR + "/" + name);
    }

    public ResponseEntity<FileSystemResource> getUiResultImage(String name, String reportId) {
        FileUtils.validateFileName(name, reportId);
        return getImage(FileUtils.UI_IMAGE_DIR + File.separator + reportId + File.separator + name);
    }

    public ResponseEntity<FileSystemResource> getImage(String path) {
        File file = new File(path);
        HttpHeaders headers = new HttpHeaders();
        String fileName = encodeFileName(file.getName());
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(file));
    }

    public String encodeFileName(String fileName) {
        try {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e);
            return fileName;
        }
    }

    public void mdDelete(String fileName) {
        FileUtils.validateFileName(fileName);
        FileUtils.deleteFile(FileUtils.MD_IMAGE_DIR + "/" + fileName);
    }

    public void saveMdImages(MdImageSaveRequest request) {
        mdFileService.saveFiles(request);
    }

}
