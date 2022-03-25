package io.metersphere.service;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.MdUploadRequest;
import io.metersphere.i18n.Translator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceService {

    public void mdUpload(MdUploadRequest request, MultipartFile file) {
        FileUtils.uploadFile(file, FileUtils.MD_IMAGE_DIR, request.getId() + "_" + request.getFileName());
    }

    public ResponseEntity<FileSystemResource> getMdImage(String name) {
        if (name.contains("/")) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        File file = new File(FileUtils.MD_IMAGE_DIR + "/" + name);
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
            return URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e);
            return fileName;
        }
    }

    public String decodeFileName(String fileName) {
        try {
            return URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e);
            return fileName;
        }
    }

    public void mdDelete(String fileName) {
        if (fileName.contains("/")) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        FileUtils.deleteFile(FileUtils.MD_IMAGE_DIR + "/" + fileName);
    }
}
