package io.metersphere.plugin.platform.dto.reponse;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DemandRelatePageResponse {

    /**
     * 自定义表头字段
     */
    private Map<String, String> customHeaderMap;
    /**
     * 需求列表数据
     */
    private List<Demand> demandList;

    @Data
    public static class Demand {
        /**
         * 需求ID
         */
        private String demandId;
        /**
         * 父需求ID
         */
        private String parent;
        /**
         * 需求名称/标题
         */
        private String demandName;
        /**
         * 需求地址
         */
        private String demandUrl;
        /**
         * 自定义字段
         */
        private Map<String, Object> customFields;
    }
}
