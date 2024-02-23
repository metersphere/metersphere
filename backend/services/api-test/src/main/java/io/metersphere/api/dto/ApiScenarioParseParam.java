package io.metersphere.api.dto;

import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行场景解析参数时的临时参数
 *
 * @Author: jianxing
 * @CreateTime: 2024-02-22  11:27
 */
@Data
public class ApiScenarioParseParam {
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
     * 场景中所有的 MsCommonElement 列表
     */
    private List<MsCommonElement> commonElements = new ArrayList<>();
}
