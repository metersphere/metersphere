package io.metersphere.functional.constants;

import io.metersphere.sdk.util.Translator;

public enum CaseReviewPassRule {
    SINGLE("case_review.single"),
    MULTIPLE("case_review.multiple");

    private final String name;

    CaseReviewPassRule(String name) {
        this.name = name;
    }

    public String getName() {
        return getTranslationName(this.name);
    }

    /**
     * 返回国际化后的规则信息
     * 如果没有匹配则返回原文
     */
    String getTranslationName(String name) {
        return Translator.get(name, name);
    }
}
