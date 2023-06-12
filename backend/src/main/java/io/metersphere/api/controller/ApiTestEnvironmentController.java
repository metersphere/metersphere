package io.metersphere.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiTestEnvironmentDTO;
import io.metersphere.api.dto.ssl.KeyStoreEntry;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.CommandService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.EnvironmentRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

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
    public Pager<List<ApiTestEnvironmentWithBLOBs>> listByCondition(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EnvironmentRequest environmentRequest) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiTestEnvironmentService.listByConditions(environmentRequest));
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
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENVIRONMENT_READ_CREATE, PermissionConstants.PROJECT_ENVIRONMENT_READ_COPY, PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_READ_CREATE, PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_READ_COPY}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#apiTestEnvironmentWithBLOBs.id)", msClass = ApiTestEnvironmentService.class)
    public String create(@RequestPart("request") ApiTestEnvironmentDTO apiTestEnvironmentWithBLOBs, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles) {
        checkParams(apiTestEnvironmentWithBLOBs);
        return apiTestEnvironmentService.add(apiTestEnvironmentWithBLOBs, sslFiles);
    }

    @PostMapping(value = "/update")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENVIRONMENT_READ_EDIT, PermissionConstants.WORKSPACE_PROJECT_ENVIRONMENT_READ_EDIT}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#apiTestEnvironment.id)", content = "#msClass.getLogDetails(#apiTestEnvironment.id)", msClass = ApiTestEnvironmentService.class)
    public void update(@RequestPart("request") ApiTestEnvironmentDTO apiTestEnvironment, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles) {
        checkParams(apiTestEnvironment);
        apiTestEnvironmentService.update(apiTestEnvironment, sslFiles);
    }

    private void checkParams(ApiTestEnvironmentDTO apiTestEnvironment) {
        try {
            JSONObject json = JSONObject.parseObject(apiTestEnvironment.getConfig());
            JSONObject commonConfig = json.getJSONObject("commonConfig");
            JSONArray databaseConfigs = json.getJSONArray("databaseConfigs");

            Object requestTimeout = commonConfig.get("requestTimeout");
            Object responseTimeout = commonConfig.get("responseTimeout");
            if (commonConfig != null && (requestTimeout != null || responseTimeout != null) && ((int) requestTimeout < 1 ||
                    (int) responseTimeout < 1)) {
                MSException.throwException(Translator.get("invalid_parameter"));
            }
            if (databaseConfigs.size() > 0) {
                for (Object databaseConfig : databaseConfigs) {
                    JSONObject database = (JSONObject) databaseConfig;
                    Object poolMax = database.get("poolMax");
                    Object timeout = database.get("timeout");
                    if (database != null && (poolMax != null || timeout != null) && (int) database.get("poolMax") < 1 || (int) database.get("timeout") < 1) {
                        MSException.throwException(Translator.get("invalid_parameter"));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE)
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestEnvironmentService.class)
    public void delete(@PathVariable String id) {
        apiTestEnvironmentService.delete(id);
    }

    @GetMapping("/getTcpMockInfo/{projectId}")
    public String getMockInfo(@PathVariable(value = "projectId") String projectId) {
        return apiTestEnvironmentService.getMockInfo(projectId);
    }
}
