package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiTestEnvironmentDTO;
import io.metersphere.api.dto.ssl.KeyStoreEntry;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.CommandService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.EnvironmentRequest;
import io.metersphere.log.annotation.MsAuditLog;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.List;
import com.alibaba.fastjson.*;

@RestController
@RequestMapping(value = "/api/environment")
public class ApiTestEnvironmentController {

    @Resource
    ApiTestEnvironmentService apiTestEnvironmentService;
    @Resource
    private CommandService commandService;

    @GetMapping("/list/{projectId}")
    public List<ApiTestEnvironmentWithBLOBs> list(@PathVariable String projectId) {
        return apiTestEnvironmentService.list(projectId);
    }

    /**
     * 查询指定项目和指定名称的环境
     *
     * @param goPage
     * @param pageSize
     * @param environmentRequest
     * @return
     */
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiTestEnvironmentWithBLOBs>> listByCondition(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EnvironmentRequest environmentRequest) throws UnsupportedEncodingException {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<ApiTestEnvironmentWithBLOBs> environments = apiTestEnvironmentService.listByConditions(environmentRequest);
        for(ApiTestEnvironmentWithBLOBs i: environments) {
            i.setConfig(java.util.Base64.getEncoder().encodeToString(i.getConfig().getBytes("utf-8")));
        }
        return PageUtils.setPageInfo(page, environments);
    }

    @GetMapping("/get/{id}")
    public ApiTestEnvironmentWithBLOBs get(@PathVariable String id) {
        return apiTestEnvironmentService.get(id);
    }


    @PostMapping(value = "/get/entry")
    public List<KeyStoreEntry> getEntry(@RequestPart("request") String password, @RequestPart(value = "file") MultipartFile sslFiles) {
        return commandService.get(password, sslFiles);
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#apiTestEnvironmentWithBLOBs.id)", msClass = ApiTestEnvironmentService.class)
    public String create(@RequestPart("request") ApiTestEnvironmentDTO apiTestEnvironmentWithBLOBs, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles) {
        return apiTestEnvironmentService.add(apiTestEnvironmentWithBLOBs, sslFiles);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#apiTestEnvironment.id)", content = "#msClass.getLogDetails(#apiTestEnvironment.id)", msClass = ApiTestEnvironmentService.class)
    public void update(@RequestPart("request") ApiTestEnvironmentDTO apiTestEnvironment, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles) {
        try {
            JSONObject json = JSONObject.parseObject(apiTestEnvironment.getConfig());
            JSONObject commonConfig = json.getJSONObject("commonConfig");
            JSONArray databaseConfigs = json.getJSONArray("databaseConfigs");

            if ((int) commonConfig.get("requestTimeout") < 1 ||
                    (int) commonConfig.get("responseTimeout") < 1 ) {
                throw new RuntimeException("commonConfig error!");
            }
            for (Object databaseConfig : databaseConfigs) {
                JSONObject database = (JSONObject) databaseConfig;
                if ((int) database.get("poolMax") < 1 || (int) database.get("timeout") < 1) {
                    throw new RuntimeException("databaseConfig error!");
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
            throw e;
        }

        apiTestEnvironmentService.update(apiTestEnvironment, sslFiles);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestEnvironmentService.class)
    public void delete(@PathVariable String id) {
        apiTestEnvironmentService.delete(id);
    }

    @GetMapping("/getTcpMockInfo/{projectId}")
    public String getMockInfo(@PathVariable(value = "projectId") String projectId) {
        return apiTestEnvironmentService.getMockInfo(projectId);
    }
}
