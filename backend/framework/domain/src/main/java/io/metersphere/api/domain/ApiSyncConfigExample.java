package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiSyncConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiSyncConfigExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andResourceIdIsNull() {
            addCriterion("resource_id is null");
            return (Criteria) this;
        }

        public Criteria andResourceIdIsNotNull() {
            addCriterion("resource_id is not null");
            return (Criteria) this;
        }

        public Criteria andResourceIdEqualTo(String value) {
            addCriterion("resource_id =", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotEqualTo(String value) {
            addCriterion("resource_id <>", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdGreaterThan(String value) {
            addCriterion("resource_id >", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdGreaterThanOrEqualTo(String value) {
            addCriterion("resource_id >=", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdLessThan(String value) {
            addCriterion("resource_id <", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdLessThanOrEqualTo(String value) {
            addCriterion("resource_id <=", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdLike(String value) {
            addCriterion("resource_id like", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotLike(String value) {
            addCriterion("resource_id not like", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdIn(List<String> values) {
            addCriterion("resource_id in", values, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotIn(List<String> values) {
            addCriterion("resource_id not in", values, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdBetween(String value1, String value2) {
            addCriterion("resource_id between", value1, value2, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotBetween(String value1, String value2) {
            addCriterion("resource_id not between", value1, value2, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIsNull() {
            addCriterion("resource_type is null");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIsNotNull() {
            addCriterion("resource_type is not null");
            return (Criteria) this;
        }

        public Criteria andResourceTypeEqualTo(String value) {
            addCriterion("resource_type =", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotEqualTo(String value) {
            addCriterion("resource_type <>", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeGreaterThan(String value) {
            addCriterion("resource_type >", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeGreaterThanOrEqualTo(String value) {
            addCriterion("resource_type >=", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeLessThan(String value) {
            addCriterion("resource_type <", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeLessThanOrEqualTo(String value) {
            addCriterion("resource_type <=", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeLike(String value) {
            addCriterion("resource_type like", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotLike(String value) {
            addCriterion("resource_type not like", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIn(List<String> values) {
            addCriterion("resource_type in", values, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotIn(List<String> values) {
            addCriterion("resource_type not in", values, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeBetween(String value1, String value2) {
            addCriterion("resource_type between", value1, value2, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotBetween(String value1, String value2) {
            addCriterion("resource_type not between", value1, value2, "resourceType");
            return (Criteria) this;
        }

        public Criteria andHideIsNull() {
            addCriterion("hide is null");
            return (Criteria) this;
        }

        public Criteria andHideIsNotNull() {
            addCriterion("hide is not null");
            return (Criteria) this;
        }

        public Criteria andHideEqualTo(Boolean value) {
            addCriterion("hide =", value, "hide");
            return (Criteria) this;
        }

        public Criteria andHideNotEqualTo(Boolean value) {
            addCriterion("hide <>", value, "hide");
            return (Criteria) this;
        }

        public Criteria andHideGreaterThan(Boolean value) {
            addCriterion("hide >", value, "hide");
            return (Criteria) this;
        }

        public Criteria andHideGreaterThanOrEqualTo(Boolean value) {
            addCriterion("hide >=", value, "hide");
            return (Criteria) this;
        }

        public Criteria andHideLessThan(Boolean value) {
            addCriterion("hide <", value, "hide");
            return (Criteria) this;
        }

        public Criteria andHideLessThanOrEqualTo(Boolean value) {
            addCriterion("hide <=", value, "hide");
            return (Criteria) this;
        }

        public Criteria andHideIn(List<Boolean> values) {
            addCriterion("hide in", values, "hide");
            return (Criteria) this;
        }

        public Criteria andHideNotIn(List<Boolean> values) {
            addCriterion("hide not in", values, "hide");
            return (Criteria) this;
        }

        public Criteria andHideBetween(Boolean value1, Boolean value2) {
            addCriterion("hide between", value1, value2, "hide");
            return (Criteria) this;
        }

        public Criteria andHideNotBetween(Boolean value1, Boolean value2) {
            addCriterion("hide not between", value1, value2, "hide");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorIsNull() {
            addCriterion("notify_case_creator is null");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorIsNotNull() {
            addCriterion("notify_case_creator is not null");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorEqualTo(Boolean value) {
            addCriterion("notify_case_creator =", value, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorNotEqualTo(Boolean value) {
            addCriterion("notify_case_creator <>", value, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorGreaterThan(Boolean value) {
            addCriterion("notify_case_creator >", value, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorGreaterThanOrEqualTo(Boolean value) {
            addCriterion("notify_case_creator >=", value, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorLessThan(Boolean value) {
            addCriterion("notify_case_creator <", value, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorLessThanOrEqualTo(Boolean value) {
            addCriterion("notify_case_creator <=", value, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorIn(List<Boolean> values) {
            addCriterion("notify_case_creator in", values, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorNotIn(List<Boolean> values) {
            addCriterion("notify_case_creator not in", values, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorBetween(Boolean value1, Boolean value2) {
            addCriterion("notify_case_creator between", value1, value2, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyCaseCreatorNotBetween(Boolean value1, Boolean value2) {
            addCriterion("notify_case_creator not between", value1, value2, "notifyCaseCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorIsNull() {
            addCriterion("notify_scenario_creator is null");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorIsNotNull() {
            addCriterion("notify_scenario_creator is not null");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorEqualTo(Boolean value) {
            addCriterion("notify_scenario_creator =", value, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorNotEqualTo(Boolean value) {
            addCriterion("notify_scenario_creator <>", value, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorGreaterThan(Boolean value) {
            addCriterion("notify_scenario_creator >", value, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorGreaterThanOrEqualTo(Boolean value) {
            addCriterion("notify_scenario_creator >=", value, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorLessThan(Boolean value) {
            addCriterion("notify_scenario_creator <", value, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorLessThanOrEqualTo(Boolean value) {
            addCriterion("notify_scenario_creator <=", value, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorIn(List<Boolean> values) {
            addCriterion("notify_scenario_creator in", values, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorNotIn(List<Boolean> values) {
            addCriterion("notify_scenario_creator not in", values, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorBetween(Boolean value1, Boolean value2) {
            addCriterion("notify_scenario_creator between", value1, value2, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andNotifyScenarioCreatorNotBetween(Boolean value1, Boolean value2) {
            addCriterion("notify_scenario_creator not between", value1, value2, "notifyScenarioCreator");
            return (Criteria) this;
        }

        public Criteria andSyncEnableIsNull() {
            addCriterion("sync_enable is null");
            return (Criteria) this;
        }

        public Criteria andSyncEnableIsNotNull() {
            addCriterion("sync_enable is not null");
            return (Criteria) this;
        }

        public Criteria andSyncEnableEqualTo(Boolean value) {
            addCriterion("sync_enable =", value, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableNotEqualTo(Boolean value) {
            addCriterion("sync_enable <>", value, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableGreaterThan(Boolean value) {
            addCriterion("sync_enable >", value, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("sync_enable >=", value, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableLessThan(Boolean value) {
            addCriterion("sync_enable <", value, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableLessThanOrEqualTo(Boolean value) {
            addCriterion("sync_enable <=", value, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableIn(List<Boolean> values) {
            addCriterion("sync_enable in", values, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableNotIn(List<Boolean> values) {
            addCriterion("sync_enable not in", values, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableBetween(Boolean value1, Boolean value2) {
            addCriterion("sync_enable between", value1, value2, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andSyncEnableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("sync_enable not between", value1, value2, "syncEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableIsNull() {
            addCriterion("notice_enable is null");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableIsNotNull() {
            addCriterion("notice_enable is not null");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableEqualTo(Boolean value) {
            addCriterion("notice_enable =", value, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableNotEqualTo(Boolean value) {
            addCriterion("notice_enable <>", value, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableGreaterThan(Boolean value) {
            addCriterion("notice_enable >", value, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("notice_enable >=", value, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableLessThan(Boolean value) {
            addCriterion("notice_enable <", value, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableLessThanOrEqualTo(Boolean value) {
            addCriterion("notice_enable <=", value, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableIn(List<Boolean> values) {
            addCriterion("notice_enable in", values, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableNotIn(List<Boolean> values) {
            addCriterion("notice_enable not in", values, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableBetween(Boolean value1, Boolean value2) {
            addCriterion("notice_enable between", value1, value2, "noticeEnable");
            return (Criteria) this;
        }

        public Criteria andNoticeEnableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("notice_enable not between", value1, value2, "noticeEnable");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}