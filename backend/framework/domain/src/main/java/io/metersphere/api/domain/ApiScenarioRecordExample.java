package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiScenarioRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiScenarioRecordExample() {
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

        public Criteria andApiScenarioReportIdIsNull() {
            addCriterion("api_scenario_report_id is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdIsNotNull() {
            addCriterion("api_scenario_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdEqualTo(String value) {
            addCriterion("api_scenario_report_id =", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdNotEqualTo(String value) {
            addCriterion("api_scenario_report_id <>", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdGreaterThan(String value) {
            addCriterion("api_scenario_report_id >", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_report_id >=", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdLessThan(String value) {
            addCriterion("api_scenario_report_id <", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_report_id <=", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdLike(String value) {
            addCriterion("api_scenario_report_id like", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdNotLike(String value) {
            addCriterion("api_scenario_report_id not like", value, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdIn(List<String> values) {
            addCriterion("api_scenario_report_id in", values, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdNotIn(List<String> values) {
            addCriterion("api_scenario_report_id not in", values, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdBetween(String value1, String value2) {
            addCriterion("api_scenario_report_id between", value1, value2, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioReportIdNotBetween(String value1, String value2) {
            addCriterion("api_scenario_report_id not between", value1, value2, "apiScenarioReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdIsNull() {
            addCriterion("api_scenario_id is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdIsNotNull() {
            addCriterion("api_scenario_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdEqualTo(String value) {
            addCriterion("api_scenario_id =", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdNotEqualTo(String value) {
            addCriterion("api_scenario_id <>", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdGreaterThan(String value) {
            addCriterion("api_scenario_id >", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_id >=", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdLessThan(String value) {
            addCriterion("api_scenario_id <", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_id <=", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdLike(String value) {
            addCriterion("api_scenario_id like", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdNotLike(String value) {
            addCriterion("api_scenario_id not like", value, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdIn(List<String> values) {
            addCriterion("api_scenario_id in", values, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdNotIn(List<String> values) {
            addCriterion("api_scenario_id not in", values, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdBetween(String value1, String value2) {
            addCriterion("api_scenario_id between", value1, value2, "apiScenarioId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioIdNotBetween(String value1, String value2) {
            addCriterion("api_scenario_id not between", value1, value2, "apiScenarioId");
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