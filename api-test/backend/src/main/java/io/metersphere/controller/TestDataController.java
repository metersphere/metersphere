package io.metersphere.controller;

import io.metersphere.api.exec.generator.TestDataGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/test/data")
public class TestDataController {
    @PostMapping("/generator")
    public String preview(@RequestBody String jsonSchema) {
        return TestDataGenerator.generator(jsonSchema);
    }
}
