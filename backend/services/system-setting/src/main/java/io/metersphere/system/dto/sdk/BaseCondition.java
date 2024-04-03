package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
public class BaseCondition {
    @Schema(description =  "关键字")
    private String keyword;

    @Schema(description = "匹配模式 所有/任一", allowableValues = {"AND", "OR"})
    private String searchMode = "AND";

    @Schema(description =  "过滤字段")
    private Map<String, List<String>> filter;

    @Schema(description =  "高级搜索")
    private Map<String, Object> combine;

    // 转JSON时会调用。 前台数据传过来时可以顺便处理掉转义字符
    public void setKeyword(String keyword) {
        keyword = StringUtils.replace(keyword, "%", "\\%");
        keyword = StringUtils.replace(keyword, "_", "\\_");
        this.keyword = keyword;
    }

    //  直接初始化keyword
    public void initKeyword(String keyword) {
        this.keyword = keyword;
    }
}
