package io.metersphere.controller;

import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.service.BaseCustomFieldTemplateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/custom/field/template")
public class BaseCustomFieldTemplateController {

    @Resource
    private BaseCustomFieldTemplateService baseCustomFieldTemplateService;

    @PostMapping("/update")
    public void update(@RequestBody CustomFieldTemplate request) {
        baseCustomFieldTemplateService.update(request);
    }
}
