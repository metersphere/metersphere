package io.metersphere.plugin.platform.dto.response;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PlatformDemandDTO {

    /**
     * 自定义表头字段(支持过滤)
     */
    private List<PlatformCustomFieldItemDTO> customHeaders;
    /**
     * 需求列表数据
     */
    private List<Demand> list;

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
         * 已关联的需求要禁用
         */
        private boolean disabled;
        /**
         * 子需求集合
         */
        private List<Demand> children;
        /**
         * 自定义字段
         */
        private Map<String, Object> customFields = new HashMap<>();
    }
}
