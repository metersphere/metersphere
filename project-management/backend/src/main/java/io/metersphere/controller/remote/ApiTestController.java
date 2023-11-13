package io.metersphere.controller.remote;

import io.metersphere.dto.FileOperationRequest;
import io.metersphere.service.BaseProjectService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = {
        "/api/automation",
})
public class ApiTestController {

    @Resource
    private BaseProjectService baseProjectService;
    @PostMapping("/file/download")
    public ResponseEntity<byte[]> download(@RequestBody FileOperationRequest request){
        byte[] bytes = baseProjectService.loadFileAsBytes(request);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + request.getName() + "\"").body(bytes);
    }
}
