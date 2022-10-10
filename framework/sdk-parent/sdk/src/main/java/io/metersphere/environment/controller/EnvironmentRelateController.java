package io.metersphere.environment.controller;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.MicroService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/environment/relate")
public class EnvironmentRelateController {

    @Resource
    private MicroService microService;

    @GetMapping("/api/testcase/get/{id}")
    public Object getApiCaseById(String id) {
        return microService.getForData(MicroServiceName.API_TEST, "/api/testcase/get/" + id);
    }

    @GetMapping("/api/definition/document/{id}/{type}")
    public Object getApiDocument(String id, String type) {
        return microService.getForData(MicroServiceName.API_TEST, "/api/definition/document/" + id + "/" + type);
    }

    @PostMapping("/api/definition/generator")
    public Object jsonGenerator(@RequestBody Object obj) {
        return microService.postForData(MicroServiceName.API_TEST, "/api/definition/generator", obj);
    }

    @GetMapping("/api/module/getUserDefaultApiType")
    public Object getUserDefaultApiType() {
        return microService.getForData(MicroServiceName.API_TEST, "/api/module/getUserDefaultApiType/");
    }

    @PostMapping("/api/definition/list/{goPage}/{pageSize}")
    public Object list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Object obj) {
        return microService.postForData(MicroServiceName.API_TEST, String.format("/api/definition/list/%s/%s", goPage, pageSize), obj);
    }

    @GetMapping("/api/definition/get/{id}")
    public Object getApiDefinitionResult(@PathVariable String id) {
        return microService.getForData(MicroServiceName.API_TEST, "/api/definition/get/" + id);
    }

    @PostMapping("/api/testcase/list/{goPage}/{pageSize}")
    public Object listSimple(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Object request) {
        return microService.postForData(MicroServiceName.API_TEST, String.format("/api/testcase/list/%s/%s", goPage, pageSize), request);
    }

    @GetMapping("/api/module/list/{projectId}/{protocol}")
    public Object getNodeByProjectId(@PathVariable String projectId, @PathVariable String protocol) {
        return microService.getForData(MicroServiceName.API_TEST, "/api/module/list/" + projectId + "/" + protocol);
    }

    @PostMapping("/custom/func/list/{goPage}/{pageSize}")
    public Object query(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Object request) {
        return microService.postForData(MicroServiceName.PROJECT_MANAGEMENT, String.format("/custom/func/list/%s/%s", goPage, pageSize), request);
    }

    @GetMapping("/custom/func/get/{id}")
    public Object get(@PathVariable String id) {
        return microService.getForData(MicroServiceName.PROJECT_MANAGEMENT, "/custom/func/get/" + id);
    }

}
