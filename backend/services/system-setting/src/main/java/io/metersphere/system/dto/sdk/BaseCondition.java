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
        //判断之前有没有转义过。转义过就不再转义。耍花活的自己想办法解决
        if (StringUtils.contains(keyword, "%") && !StringUtils.contains(keyword, "\\%")) {
            keyword = StringUtils.replace(keyword, "%", "\\%");
        }
        if (StringUtils.contains(keyword, "_") && !StringUtils.contains(keyword, "\\_")) {
            keyword = StringUtils.replace(keyword, "_", "\\_");
        }
        this.keyword = keyword;
    }

    public void initKeyword(String keyword) {
        //  直接初始化keyword
        this.keyword = keyword;
    }
}
