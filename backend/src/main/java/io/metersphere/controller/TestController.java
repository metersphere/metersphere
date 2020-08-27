package io.metersphere.controller;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/test")
public class TestController {


    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public Object testUpload(@RequestPart(value = "id") String id, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("file", file.getOriginalFilename());
        jsonObject.put("files", bodyFiles.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.toList()));
        return jsonObject;
    }

    @GetMapping(value = "/{str}")
    public Object getString(@PathVariable String str) throws InterruptedException {
        if (StringUtils.equals("error", str)) {
            throw new RuntimeException("test error");
        }
        if (StringUtils.equals("warning", str)) {
            return ResultHolder.error("test warning");
        }
        if (StringUtils.equals("user", str)) {
            return ResultHolder.success(SessionUtils.getUser());
        }
        if (StringUtils.equals("sleep", str)) {
            Thread.sleep(2000L);
            return ResultHolder.success(str);
        }
        return ResultHolder.success(str);
    }

}