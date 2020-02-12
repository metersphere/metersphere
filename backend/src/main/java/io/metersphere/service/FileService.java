package io.metersphere.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FileService {
    // 将上传的文件保存在内存，方便测试
    private Map<String, MultipartFile> fileMap = new ConcurrentHashMap<>();

    public void upload(String name, MultipartFile file) throws IOException {
        String result = new BufferedReader(new InputStreamReader(file.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        System.out.println(String.format("upload file: %s, content: \n%s", name, result));

        fileMap.put(name, file);
    }

    public Resource loadFileAsResource(String name) {
        final MultipartFile file = fileMap.get(name);

        if (file != null) {
            try {
                return new InputStreamResource(file.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}