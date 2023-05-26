package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiDefinitionDTO;
import io.metersphere.api.service.ApiDefinitionService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping(value = "/api/definition")
public class ApiDefinitionController {
    @Resource
    private ApiDefinitionService apiDefinitionService;

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiDefinitionDTO create(@RequestPart("request") ApiDefinitionDTO request,
                                   @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiDefinitionService.create(request, bodyFiles);
    }
}
