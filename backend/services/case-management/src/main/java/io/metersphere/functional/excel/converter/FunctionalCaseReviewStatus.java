package io.metersphere.functional.excel.converter;

/**
 * @author wx
 */
public enum FunctionalCaseReviewStatus {

    UN_REVIEWED("case.review.status.un_reviewed", 1),
    UNDER_REVIEWED("case.review.status.under_reviewed", 2),
    PASS("case.review.status.pass", 3),
    UN_PASS("case.review.status.un_pass", 4),
    RE_REVIEWED("case.review.status.re_reviewed", 5);

    private String i18nKey;
    private Integer order;

    FunctionalCaseReviewStatus(String i18nKey, int order) {
        this.i18nKey = i18nKey;
        this.order = order;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public Integer getOrder() {
        return order;
    }
}
