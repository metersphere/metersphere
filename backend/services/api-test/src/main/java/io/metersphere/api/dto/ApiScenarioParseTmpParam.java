package io.metersphere.api.dto;

import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.controller.MsScriptElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 执行场景解析参数时的临时参数
 *
 * @Author: jianxing
 * @CreateTime: 2024-02-22  11:27
 */
@Data
public class ApiScenarioParseTmpParam {
    /**
     * 步骤详情 Map
     * key 为步骤ID
     * value 为步骤详情字符串
     */
    private Map<String, String> stepDetailMap;
    /**
     * 资源的请求内容 Map
     * key 为资源ID
     * value 为请求详情字符串
     */
    private Map<String, String> resourceDetailMap;
    /**
     * MsHTTPElement Map
     * key 为 stepType
     * value 为 MsHTTPElement
     */
    private Map<String, List<MsHTTPElement>> stepTypeHttpElementMap = new HashMap<>();
    /**
     * 步骤 Map
     * key 为 uniqueId
     * value 为 步骤
     */
    private Map<String, ApiScenarioStepCommonDTO> uniqueIdStepMap = new HashMap<>();
    /**
     * 场景中所有的 MsCommonElement 列表
     */
    private List<MsCommonElement> commonElements = new ArrayList<>();
    /**
     * 场景中所有的脚本步骤列表
     */
    private List<MsScriptElement> scriptElements = new ArrayList<>();
    /**
     * 执行时包含文件的资源 ID 列表
     * 场景执行时，包含 用例、场景、步骤ID
     */
    private Set<String> fileResourceIds = HashSet.newHashSet(0);
    /**
     * 包含文件文件的步骤ID和场景ID的映射
     */
    private Map<String, String> fileStepScenarioMap = new HashMap<>(0);
    /**
     * 执行的资源所属项目的ID列表
     * 场景执行时，为引用的资源的项目ID列表
     */
    private Set<String> refProjectIds = HashSet.newHashSet(0);
    /**
     * 环境相关信息
     */
    private ApiScenarioParseEnvInfo scenarioParseEnvInfo;
    /**
     * 场景中的待执行的请求总数
     */
    private AtomicLong requestCount = new AtomicLong(0);
}
