package io.metersphere.provider;

import java.util.Map;

/**
 * @author guoyuqi
 */
public interface BaseCaseProvider {
    /**
     * 更新用例评审数据
     * @param paramMap 更新用例评审所需参数
     */
    void updateCaseReview(Map<String, Object> paramMap);
}
