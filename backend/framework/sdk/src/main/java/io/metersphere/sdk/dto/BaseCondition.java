package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Data
public class BaseCondition {
    @Schema(description =  "关键字")
    private String keyword;

    @Schema(description =  "过滤字段")
    private Map<String, List<String>> filter;

    @Schema(description =  "视图ID")
    private String viewId;

    @Schema(description =  "高级搜索")
    @Valid
    private CombineSearch combineSearch;

    @Deprecated
    private String searchMode = "AND";
    @Deprecated
    private Map<String, Object> combine;

    // 转JSON时会调用。 前台数据传过来时可以顺便处理掉转义字符
    @Deprecated
    public void setKeyword(String keyword) {
        this.keyword = transferKeyword(keyword);
    }

    public static String transferKeyword(String keyword) {
        if (StringUtils.contains(keyword, "\\") && !StringUtils.contains(keyword, "\\\\")) {
            keyword = StringUtils.replace(keyword, "\\", "\\\\");
        }
        //判断之前有没有转义过。转义过就不再转义。耍花活的自己想办法解决
        if (StringUtils.contains(keyword, "%") && !StringUtils.contains(keyword, "\\%")) {
            keyword = StringUtils.replace(keyword, "%", "\\%");
        }
        if (StringUtils.contains(keyword, "_") && !StringUtils.contains(keyword, "\\_")) {
            keyword = StringUtils.replace(keyword, "_", "\\_");
        }
        return keyword;
    }

    public void initKeyword(String keyword) {
        //  直接初始化keyword
        this.keyword = keyword;
    }
}
