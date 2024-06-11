package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanReportSummaryExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanReportSummaryExample() {
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

        public Criteria andFunctionalCaseCountIsNull() {
            addCriterion("functional_case_count is null");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountIsNotNull() {
            addCriterion("functional_case_count is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountEqualTo(Long value) {
            addCriterion("functional_case_count =", value, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountNotEqualTo(Long value) {
            addCriterion("functional_case_count <>", value, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountGreaterThan(Long value) {
            addCriterion("functional_case_count >", value, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountGreaterThanOrEqualTo(Long value) {
            addCriterion("functional_case_count >=", value, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountLessThan(Long value) {
            addCriterion("functional_case_count <", value, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountLessThanOrEqualTo(Long value) {
            addCriterion("functional_case_count <=", value, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountIn(List<Long> values) {
            addCriterion("functional_case_count in", values, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountNotIn(List<Long> values) {
            addCriterion("functional_case_count not in", values, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountBetween(Long value1, Long value2) {
            addCriterion("functional_case_count between", value1, value2, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseCountNotBetween(Long value1, Long value2) {
            addCriterion("functional_case_count not between", value1, value2, "functionalCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountIsNull() {
            addCriterion("api_case_count is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountIsNotNull() {
            addCriterion("api_case_count is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountEqualTo(Long value) {
            addCriterion("api_case_count =", value, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountNotEqualTo(Long value) {
            addCriterion("api_case_count <>", value, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountGreaterThan(Long value) {
            addCriterion("api_case_count >", value, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountGreaterThanOrEqualTo(Long value) {
            addCriterion("api_case_count >=", value, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountLessThan(Long value) {
            addCriterion("api_case_count <", value, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountLessThanOrEqualTo(Long value) {
            addCriterion("api_case_count <=", value, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountIn(List<Long> values) {
            addCriterion("api_case_count in", values, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountNotIn(List<Long> values) {
            addCriterion("api_case_count not in", values, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountBetween(Long value1, Long value2) {
            addCriterion("api_case_count between", value1, value2, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiCaseCountNotBetween(Long value1, Long value2) {
            addCriterion("api_case_count not between", value1, value2, "apiCaseCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountIsNull() {
            addCriterion("api_scenario_count is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountIsNotNull() {
            addCriterion("api_scenario_count is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountEqualTo(Long value) {
            addCriterion("api_scenario_count =", value, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountNotEqualTo(Long value) {
            addCriterion("api_scenario_count <>", value, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountGreaterThan(Long value) {
            addCriterion("api_scenario_count >", value, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountGreaterThanOrEqualTo(Long value) {
            addCriterion("api_scenario_count >=", value, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountLessThan(Long value) {
            addCriterion("api_scenario_count <", value, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountLessThanOrEqualTo(Long value) {
            addCriterion("api_scenario_count <=", value, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountIn(List<Long> values) {
            addCriterion("api_scenario_count in", values, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountNotIn(List<Long> values) {
            addCriterion("api_scenario_count not in", values, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountBetween(Long value1, Long value2) {
            addCriterion("api_scenario_count between", value1, value2, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioCountNotBetween(Long value1, Long value2) {
            addCriterion("api_scenario_count not between", value1, value2, "apiScenarioCount");
            return (Criteria) this;
        }

        public Criteria andBugCountIsNull() {
            addCriterion("bug_count is null");
            return (Criteria) this;
        }

        public Criteria andBugCountIsNotNull() {
            addCriterion("bug_count is not null");
            return (Criteria) this;
        }

        public Criteria andBugCountEqualTo(Long value) {
            addCriterion("bug_count =", value, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountNotEqualTo(Long value) {
            addCriterion("bug_count <>", value, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountGreaterThan(Long value) {
            addCriterion("bug_count >", value, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountGreaterThanOrEqualTo(Long value) {
            addCriterion("bug_count >=", value, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountLessThan(Long value) {
            addCriterion("bug_count <", value, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountLessThanOrEqualTo(Long value) {
            addCriterion("bug_count <=", value, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountIn(List<Long> values) {
            addCriterion("bug_count in", values, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountNotIn(List<Long> values) {
            addCriterion("bug_count not in", values, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountBetween(Long value1, Long value2) {
            addCriterion("bug_count between", value1, value2, "bugCount");
            return (Criteria) this;
        }

        public Criteria andBugCountNotBetween(Long value1, Long value2) {
            addCriterion("bug_count not between", value1, value2, "bugCount");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdIsNull() {
            addCriterion("test_plan_report_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdIsNotNull() {
            addCriterion("test_plan_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdEqualTo(String value) {
            addCriterion("test_plan_report_id =", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotEqualTo(String value) {
            addCriterion("test_plan_report_id <>", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdGreaterThan(String value) {
            addCriterion("test_plan_report_id >", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_report_id >=", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdLessThan(String value) {
            addCriterion("test_plan_report_id <", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_report_id <=", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdLike(String value) {
            addCriterion("test_plan_report_id like", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotLike(String value) {
            addCriterion("test_plan_report_id not like", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdIn(List<String> values) {
            addCriterion("test_plan_report_id in", values, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotIn(List<String> values) {
            addCriterion("test_plan_report_id not in", values, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdBetween(String value1, String value2) {
            addCriterion("test_plan_report_id between", value1, value2, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_report_id not between", value1, value2, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andPlanCountIsNull() {
            addCriterion("plan_count is null");
            return (Criteria) this;
        }

        public Criteria andPlanCountIsNotNull() {
            addCriterion("plan_count is not null");
            return (Criteria) this;
        }

        public Criteria andPlanCountEqualTo(Long value) {
            addCriterion("plan_count =", value, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountNotEqualTo(Long value) {
            addCriterion("plan_count <>", value, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountGreaterThan(Long value) {
            addCriterion("plan_count >", value, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountGreaterThanOrEqualTo(Long value) {
            addCriterion("plan_count >=", value, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountLessThan(Long value) {
            addCriterion("plan_count <", value, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountLessThanOrEqualTo(Long value) {
            addCriterion("plan_count <=", value, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountIn(List<Long> values) {
            addCriterion("plan_count in", values, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountNotIn(List<Long> values) {
            addCriterion("plan_count not in", values, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountBetween(Long value1, Long value2) {
            addCriterion("plan_count between", value1, value2, "planCount");
            return (Criteria) this;
        }

        public Criteria andPlanCountNotBetween(Long value1, Long value2) {
            addCriterion("plan_count not between", value1, value2, "planCount");
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