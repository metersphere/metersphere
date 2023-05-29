package io.metersphere.ui.controller;

import io.metersphere.ui.domain.UiElement;
import io.metersphere.ui.service.UiElementService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ui_element")
public class UiElementController {

    @Resource
    private UiElementService uiElementService;

    @GetMapping("list")
    public List<UiElement> getList() {
        return uiElementService.list();
    }

    @PostMapping("add")
    public UiElement add(@Validated({Created.class}) @RequestBody UiElement uiElement) {
        return uiElementService.add(uiElement);
    }

    @PostMapping("update")
    public UiElement update(@Validated({Updated.class}) @RequestBody UiElement uiElement) {
        return uiElementService.update(uiElement);
    }
}
