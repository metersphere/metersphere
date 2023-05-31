package io.metersphere.environment.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.EnvironmentGroup;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.environment.dto.*;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.environment.service.CommandService;
import io.metersphere.environment.ssl.KeyStoreEntry;
import io.metersphere.environment.utils.TcpTreeTableDataParser;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.EnvironmentRequest;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/environment")
public class TestEnvironmentController {
    @Resource
    BaseEnvironmentService baseEnvironmentService;
    @Resource
    private CommandService commandService;
    @Resource
    private BaseEnvGroupProjectService baseEnvGroupProjectService;

    @GetMapping("/list/{projectId}")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ")
    public List<ApiTestEnvironmentWithBLOBs> list(@PathVariable String projectId) {
        return baseEnvironmentService.list(projectId);
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
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ")
    public Pager<List<ApiTestEnvironmentWithBLOBs>> listByCondition(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EnvironmentRequest environmentRequest) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, baseEnvironmentService.listByConditions(environmentRequest));
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ")
    public ApiTestEnvironmentWithBLOBs get(@PathVariable String id) {
        return baseEnvironmentService.get(id);
    }


    @PostMapping(value = "/get/entry")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ")
    public List<KeyStoreEntry> getEntry(@RequestPart("request") String password, @RequestPart(value = "file") MultipartFile sslFiles) {
        return commandService.get(password, sslFiles);
    }

    @PostMapping("/add")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ+CREATE")
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.CREATE, title = "#apiTestEnvironmentWithBLOBs.name", project = "#apiTestEnvironmentWithBLOBs.projectId", msClass = BaseEnvironmentService.class)
    public String create(@RequestPart("request") TestEnvironmentDTO apiTestEnvironmentWithBLOBs, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles, @RequestPart(value = "variablesFiles", required = false) List<MultipartFile> variableFile) {
        checkParams(apiTestEnvironmentWithBLOBs);
        return baseEnvironmentService.add(apiTestEnvironmentWithBLOBs, sslFiles, variableFile);
    }

    @PostMapping("/import")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ+IMPORT")
    public String create(@RequestBody List<TestEnvironmentDTO> environments) {
        environments.forEach(this::checkParams);
        return baseEnvironmentService.importEnvironment(environments);
    }

    @PostMapping(value = "/update")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ+EDIT")
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#apiTestEnvironment.id)", content = "#msClass.getLogDetails(#apiTestEnvironment.id)", msClass = BaseEnvironmentService.class)
    public void update(@RequestPart("request") TestEnvironmentDTO apiTestEnvironment, @RequestPart(value = "files", required = false) List<MultipartFile> sslFiles, @RequestPart(value = "variablesFiles", required = false) List<MultipartFile> variableFile) {
        checkParams(apiTestEnvironment);
        baseEnvironmentService.update(apiTestEnvironment, sslFiles, variableFile);
    }

    private void checkParams(TestEnvironmentDTO apiTestEnvironment) {
        try {
            Map<Object, Object> map = JSON.parseObject(apiTestEnvironment.getConfig(), Map.class);
            JSONObject json = new JSONObject(map);
            JSONObject commonConfig = json.getJSONObject("commonConfig");
            JSONArray databaseConfigs = json.getJSONArray("databaseConfigs");

            Object requestTimeout = commonConfig.get("requestTimeout");
            Object responseTimeout = commonConfig.get("responseTimeout");
            if (commonConfig != null && (requestTimeout != null || responseTimeout != null) && ((int) requestTimeout < 1 || (int) responseTimeout < 1)) {
                MSException.throwException(Translator.get("invalid_parameter"));
            }
            if (databaseConfigs.length() > 0) {
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
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ+DELETE")
    @MsAuditLog(module = OperLogModule.PROJECT_ENVIRONMENT_SETTING, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = BaseEnvironmentService.class)
    public void delete(@PathVariable String id) {
        baseEnvironmentService.delete(id);
    }


    @GetMapping("/group/map/{groupId}")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ")
    public Map<String, String> getEnvMap(@PathVariable String groupId) {
        return baseEnvGroupProjectService.getEnvMap(groupId);
    }

    @GetMapping("/module/list/{projectId}/{protocol}")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ")
    public List<ApiModuleDTO> getNodeByProjectId(@PathVariable String projectId, @PathVariable String protocol) {
        return baseEnvironmentService.getNodeTreeByProjectId(projectId, protocol);
    }

    @PostMapping("/raw-to-xml")
    public List<TcpTreeTableDataStruct> rawToXml(@RequestBody ParseTreeDataDTO parseTreeDataDTO) {
        return TcpTreeTableDataParser.xml2TreeTableData(parseTreeDataDTO.getStringData());
    }

    @PostMapping("/database/validate")
    @RequiresPermissions("PROJECT_ENVIRONMENT:READ")
    public void validate(@RequestBody DatabaseConfig databaseConfig) {
        try {
            DriverManager.getConnection(databaseConfig.getDbUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    /* 环境组部分*/

    @PostMapping("/group/list/{goPage}/{pageSize}")
    public Pager<List<EnvironmentGroup>> get(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EnvironmentGroupRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, baseEnvironmentService.getList(request));
    }

    @GetMapping("/group/get/{id}")
    public List<EnvironmentGroup> getRelateProject(@PathVariable("id") String projectId) {
        return baseEnvironmentService.getRelateProjectGroup(projectId);
    }

    @PostMapping("/group/get/all")
    public List<EnvironmentGroup> getAll(@RequestBody EnvironmentGroupRequest request) {
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return baseEnvironmentService.getList(request);
    }

    @PostMapping("/group/get/option")
    public List<EnvironmentGroupDTO> getEnvOptionGroup(@RequestBody EnvironmentGroupRequest request) {
        return baseEnvironmentService.getEnvOptionGroup(request.getProjectIds());
    }

    @GetMapping("/group/project/list/{groupId}")
    public List<EnvironmentGroupProjectDTO> getList(@PathVariable String groupId) {
        return baseEnvGroupProjectService.getList(groupId);
    }
}
