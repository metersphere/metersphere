package io.metersphere.controller;

import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.dto.CustomFieldTemplateDao;
import io.metersphere.service.CustomFieldTemplateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("custom/field/template")
public class CustomFieldTemplateController {

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    @PostMapping("/list")
    public List<CustomFieldTemplateDao> list(@RequestBody CustomFieldTemplate request) {
        return customFieldTemplateService.list(request);
    }

    @PostMapping("/update")
    public void update(@RequestBody CustomFieldTemplate request) {
        customFieldTemplateService.update(request);
    }
}
