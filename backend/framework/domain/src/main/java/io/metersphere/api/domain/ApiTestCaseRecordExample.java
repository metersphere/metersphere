package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiTestCaseRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiTestCaseRecordExample() {
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

        public Criteria andApiReportIdIsNull() {
            addCriterion("api_report_id is null");
            return (Criteria) this;
        }

        public Criteria andApiReportIdIsNotNull() {
            addCriterion("api_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiReportIdEqualTo(String value) {
            addCriterion("api_report_id =", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdNotEqualTo(String value) {
            addCriterion("api_report_id <>", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdGreaterThan(String value) {
            addCriterion("api_report_id >", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_report_id >=", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdLessThan(String value) {
            addCriterion("api_report_id <", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdLessThanOrEqualTo(String value) {
            addCriterion("api_report_id <=", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdLike(String value) {
            addCriterion("api_report_id like", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdNotLike(String value) {
            addCriterion("api_report_id not like", value, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdIn(List<String> values) {
            addCriterion("api_report_id in", values, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdNotIn(List<String> values) {
            addCriterion("api_report_id not in", values, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdBetween(String value1, String value2) {
            addCriterion("api_report_id between", value1, value2, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiReportIdNotBetween(String value1, String value2) {
            addCriterion("api_report_id not between", value1, value2, "apiReportId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdIsNull() {
            addCriterion("api_test_case_id is null");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdIsNotNull() {
            addCriterion("api_test_case_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdEqualTo(String value) {
            addCriterion("api_test_case_id =", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdNotEqualTo(String value) {
            addCriterion("api_test_case_id <>", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdGreaterThan(String value) {
            addCriterion("api_test_case_id >", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_test_case_id >=", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdLessThan(String value) {
            addCriterion("api_test_case_id <", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdLessThanOrEqualTo(String value) {
            addCriterion("api_test_case_id <=", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdLike(String value) {
            addCriterion("api_test_case_id like", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdNotLike(String value) {
            addCriterion("api_test_case_id not like", value, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdIn(List<String> values) {
            addCriterion("api_test_case_id in", values, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdNotIn(List<String> values) {
            addCriterion("api_test_case_id not in", values, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdBetween(String value1, String value2) {
            addCriterion("api_test_case_id between", value1, value2, "apiTestCaseId");
            return (Criteria) this;
        }

        public Criteria andApiTestCaseIdNotBetween(String value1, String value2) {
            addCriterion("api_test_case_id not between", value1, value2, "apiTestCaseId");
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