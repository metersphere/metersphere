package io.metersphere.functional.excel.converter;

/**
 * @author wx
 */
public enum FunctionalCaseExecuteStatus {

    PENDING("case.execute.status.pending", 1),
    SUCCESS("case.minder.status.success", 2),
    BLOCKED("case.minder.status.blocked", 3),
    ERROR("case.minder.status.error", 4);

    private String i18nKey;
    private Integer order;

    FunctionalCaseExecuteStatus(String i18nKey, int order) {
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
