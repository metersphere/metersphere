package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.ApiDefinitionDTO;
import io.metersphere.api.dto.ApiDefinitionListRequest;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.validation.groups.Created;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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

    @PostMapping(value = "/page")
    public Pager<List<ApiDefinition>> list(@Validated @RequestBody ApiDefinitionListRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getOrderColumn()) ? request.getOrderColumn() : "create_time desc");
        return PageUtils.setPageInfo(page, apiDefinitionService.list(request));
    }

}
