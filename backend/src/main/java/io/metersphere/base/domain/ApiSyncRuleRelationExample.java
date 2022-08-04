package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiSyncRuleRelationExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiSyncRuleRelationExample() {
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

        public Criteria andShowUpdateRuleIsNull() {
            addCriterion("show_update_rule is null");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleIsNotNull() {
            addCriterion("show_update_rule is not null");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleEqualTo(Boolean value) {
            addCriterion("show_update_rule =", value, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleNotEqualTo(Boolean value) {
            addCriterion("show_update_rule <>", value, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleGreaterThan(Boolean value) {
            addCriterion("show_update_rule >", value, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleGreaterThanOrEqualTo(Boolean value) {
            addCriterion("show_update_rule >=", value, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleLessThan(Boolean value) {
            addCriterion("show_update_rule <", value, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleLessThanOrEqualTo(Boolean value) {
            addCriterion("show_update_rule <=", value, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleIn(List<Boolean> values) {
            addCriterion("show_update_rule in", values, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleNotIn(List<Boolean> values) {
            addCriterion("show_update_rule not in", values, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleBetween(Boolean value1, Boolean value2) {
            addCriterion("show_update_rule between", value1, value2, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andShowUpdateRuleNotBetween(Boolean value1, Boolean value2) {
            addCriterion("show_update_rule not between", value1, value2, "showUpdateRule");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorIsNull() {
            addCriterion("case_creator is null");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorIsNotNull() {
            addCriterion("case_creator is not null");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorEqualTo(Boolean value) {
            addCriterion("case_creator =", value, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorNotEqualTo(Boolean value) {
            addCriterion("case_creator <>", value, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorGreaterThan(Boolean value) {
            addCriterion("case_creator >", value, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorGreaterThanOrEqualTo(Boolean value) {
            addCriterion("case_creator >=", value, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorLessThan(Boolean value) {
            addCriterion("case_creator <", value, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorLessThanOrEqualTo(Boolean value) {
            addCriterion("case_creator <=", value, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorIn(List<Boolean> values) {
            addCriterion("case_creator in", values, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorNotIn(List<Boolean> values) {
            addCriterion("case_creator not in", values, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorBetween(Boolean value1, Boolean value2) {
            addCriterion("case_creator between", value1, value2, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andCaseCreatorNotBetween(Boolean value1, Boolean value2) {
            addCriterion("case_creator not between", value1, value2, "caseCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorIsNull() {
            addCriterion("scenario_creator is null");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorIsNotNull() {
            addCriterion("scenario_creator is not null");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorEqualTo(Boolean value) {
            addCriterion("scenario_creator =", value, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorNotEqualTo(Boolean value) {
            addCriterion("scenario_creator <>", value, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorGreaterThan(Boolean value) {
            addCriterion("scenario_creator >", value, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorGreaterThanOrEqualTo(Boolean value) {
            addCriterion("scenario_creator >=", value, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorLessThan(Boolean value) {
            addCriterion("scenario_creator <", value, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorLessThanOrEqualTo(Boolean value) {
            addCriterion("scenario_creator <=", value, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorIn(List<Boolean> values) {
            addCriterion("scenario_creator in", values, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorNotIn(List<Boolean> values) {
            addCriterion("scenario_creator not in", values, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorBetween(Boolean value1, Boolean value2) {
            addCriterion("scenario_creator between", value1, value2, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andScenarioCreatorNotBetween(Boolean value1, Boolean value2) {
            addCriterion("scenario_creator not between", value1, value2, "scenarioCreator");
            return (Criteria) this;
        }

        public Criteria andSyncCaseIsNull() {
            addCriterion("sync_case is null");
            return (Criteria) this;
        }

        public Criteria andSyncCaseIsNotNull() {
            addCriterion("sync_case is not null");
            return (Criteria) this;
        }

        public Criteria andSyncCaseEqualTo(Boolean value) {
            addCriterion("sync_case =", value, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseNotEqualTo(Boolean value) {
            addCriterion("sync_case <>", value, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseGreaterThan(Boolean value) {
            addCriterion("sync_case >", value, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseGreaterThanOrEqualTo(Boolean value) {
            addCriterion("sync_case >=", value, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseLessThan(Boolean value) {
            addCriterion("sync_case <", value, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseLessThanOrEqualTo(Boolean value) {
            addCriterion("sync_case <=", value, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseIn(List<Boolean> values) {
            addCriterion("sync_case in", values, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseNotIn(List<Boolean> values) {
            addCriterion("sync_case not in", values, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseBetween(Boolean value1, Boolean value2) {
            addCriterion("sync_case between", value1, value2, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSyncCaseNotBetween(Boolean value1, Boolean value2) {
            addCriterion("sync_case not between", value1, value2, "syncCase");
            return (Criteria) this;
        }

        public Criteria andSendNoticeIsNull() {
            addCriterion("send_notice is null");
            return (Criteria) this;
        }

        public Criteria andSendNoticeIsNotNull() {
            addCriterion("send_notice is not null");
            return (Criteria) this;
        }

        public Criteria andSendNoticeEqualTo(Boolean value) {
            addCriterion("send_notice =", value, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeNotEqualTo(Boolean value) {
            addCriterion("send_notice <>", value, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeGreaterThan(Boolean value) {
            addCriterion("send_notice >", value, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeGreaterThanOrEqualTo(Boolean value) {
            addCriterion("send_notice >=", value, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeLessThan(Boolean value) {
            addCriterion("send_notice <", value, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeLessThanOrEqualTo(Boolean value) {
            addCriterion("send_notice <=", value, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeIn(List<Boolean> values) {
            addCriterion("send_notice in", values, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeNotIn(List<Boolean> values) {
            addCriterion("send_notice not in", values, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeBetween(Boolean value1, Boolean value2) {
            addCriterion("send_notice between", value1, value2, "sendNotice");
            return (Criteria) this;
        }

        public Criteria andSendNoticeNotBetween(Boolean value1, Boolean value2) {
            addCriterion("send_notice not between", value1, value2, "sendNotice");
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