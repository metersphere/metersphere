package io.metersphere.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class FileService {
    public void upload(String name, MultipartFile file) throws IOException {
        String result = new BufferedReader(new InputStreamReader(file.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        System.out.println(String.format("upload file: %s, content: \n%s", name, result));
    }
}