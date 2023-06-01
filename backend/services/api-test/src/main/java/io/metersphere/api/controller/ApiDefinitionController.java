package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiDefinitionDTO;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.validation.groups.Created;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping(value = "/api/definition")
public class ApiDefinitionController {
    @Resource
    private ApiDefinitionService apiDefinitionService;

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiDefinitionDTO create(@Validated({Created.class}) @RequestBody ApiDefinitionDTO request,
                                   @RequestParam(value = "files") List<MultipartFile> bodyFiles) {
        return apiDefinitionService.create(request, bodyFiles);
    }
}
