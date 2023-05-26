package io.metersphere.ui.domain;

import java.util.ArrayList;
import java.util.List;

public class UiElementScenarioReferenceExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public UiElementScenarioReferenceExample() {
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

        public Criteria andElementIdIsNull() {
            addCriterion("element_id is null");
            return (Criteria) this;
        }

        public Criteria andElementIdIsNotNull() {
            addCriterion("element_id is not null");
            return (Criteria) this;
        }

        public Criteria andElementIdEqualTo(String value) {
            addCriterion("element_id =", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdNotEqualTo(String value) {
            addCriterion("element_id <>", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdGreaterThan(String value) {
            addCriterion("element_id >", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdGreaterThanOrEqualTo(String value) {
            addCriterion("element_id >=", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdLessThan(String value) {
            addCriterion("element_id <", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdLessThanOrEqualTo(String value) {
            addCriterion("element_id <=", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdLike(String value) {
            addCriterion("element_id like", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdNotLike(String value) {
            addCriterion("element_id not like", value, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdIn(List<String> values) {
            addCriterion("element_id in", values, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdNotIn(List<String> values) {
            addCriterion("element_id not in", values, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdBetween(String value1, String value2) {
            addCriterion("element_id between", value1, value2, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementIdNotBetween(String value1, String value2) {
            addCriterion("element_id not between", value1, value2, "elementId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdIsNull() {
            addCriterion("element_module_id is null");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdIsNotNull() {
            addCriterion("element_module_id is not null");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdEqualTo(String value) {
            addCriterion("element_module_id =", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdNotEqualTo(String value) {
            addCriterion("element_module_id <>", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdGreaterThan(String value) {
            addCriterion("element_module_id >", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdGreaterThanOrEqualTo(String value) {
            addCriterion("element_module_id >=", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdLessThan(String value) {
            addCriterion("element_module_id <", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdLessThanOrEqualTo(String value) {
            addCriterion("element_module_id <=", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdLike(String value) {
            addCriterion("element_module_id like", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdNotLike(String value) {
            addCriterion("element_module_id not like", value, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdIn(List<String> values) {
            addCriterion("element_module_id in", values, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdNotIn(List<String> values) {
            addCriterion("element_module_id not in", values, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdBetween(String value1, String value2) {
            addCriterion("element_module_id between", value1, value2, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andElementModuleIdNotBetween(String value1, String value2) {
            addCriterion("element_module_id not between", value1, value2, "elementModuleId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdIsNull() {
            addCriterion("scenario_id is null");
            return (Criteria) this;
        }

        public Criteria andScenarioIdIsNotNull() {
            addCriterion("scenario_id is not null");
            return (Criteria) this;
        }

        public Criteria andScenarioIdEqualTo(String value) {
            addCriterion("scenario_id =", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotEqualTo(String value) {
            addCriterion("scenario_id <>", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdGreaterThan(String value) {
            addCriterion("scenario_id >", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdGreaterThanOrEqualTo(String value) {
            addCriterion("scenario_id >=", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdLessThan(String value) {
            addCriterion("scenario_id <", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdLessThanOrEqualTo(String value) {
            addCriterion("scenario_id <=", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdLike(String value) {
            addCriterion("scenario_id like", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotLike(String value) {
            addCriterion("scenario_id not like", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdIn(List<String> values) {
            addCriterion("scenario_id in", values, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotIn(List<String> values) {
            addCriterion("scenario_id not in", values, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdBetween(String value1, String value2) {
            addCriterion("scenario_id between", value1, value2, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotBetween(String value1, String value2) {
            addCriterion("scenario_id not between", value1, value2, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNull() {
            addCriterion("project_id is null");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNotNull() {
            addCriterion("project_id is not null");
            return (Criteria) this;
        }

        public Criteria andProjectIdEqualTo(String value) {
            addCriterion("project_id =", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotEqualTo(String value) {
            addCriterion("project_id <>", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThan(String value) {
            addCriterion("project_id >", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThanOrEqualTo(String value) {
            addCriterion("project_id >=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThan(String value) {
            addCriterion("project_id <", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThanOrEqualTo(String value) {
            addCriterion("project_id <=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLike(String value) {
            addCriterion("project_id like", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotLike(String value) {
            addCriterion("project_id not like", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdIn(List<String> values) {
            addCriterion("project_id in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotIn(List<String> values) {
            addCriterion("project_id not in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdBetween(String value1, String value2) {
            addCriterion("project_id between", value1, value2, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotBetween(String value1, String value2) {
            addCriterion("project_id not between", value1, value2, "projectId");
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