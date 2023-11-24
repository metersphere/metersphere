package io.metersphere.functional.constants;

import io.metersphere.sdk.util.Translator;

public enum CaseReviewStatus {


    PREPARED("case_review.prepared"),
    UNDERWAY("case_review.underway"),
    COMPLETED("case_review.completed"),
    ARCHIVED("case_review.archived");

    private final String name;

    CaseReviewStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return getTranslationName(this.name);
    }

    /**
     * 返回国际化后的状态信息
     * 如果没有匹配则返回原文
     */
    String getTranslationName(String name) {
        return Translator.get(name, name);
    }
}
