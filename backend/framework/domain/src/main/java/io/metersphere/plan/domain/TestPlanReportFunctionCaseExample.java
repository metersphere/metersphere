package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanReportFunctionCaseExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanReportFunctionCaseExample() {
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

        public Criteria andTestPlanFunctionCaseIdIsNull() {
            addCriterion("test_plan_function_case_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdIsNotNull() {
            addCriterion("test_plan_function_case_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdEqualTo(String value) {
            addCriterion("test_plan_function_case_id =", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdNotEqualTo(String value) {
            addCriterion("test_plan_function_case_id <>", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdGreaterThan(String value) {
            addCriterion("test_plan_function_case_id >", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_function_case_id >=", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdLessThan(String value) {
            addCriterion("test_plan_function_case_id <", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_function_case_id <=", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdLike(String value) {
            addCriterion("test_plan_function_case_id like", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdNotLike(String value) {
            addCriterion("test_plan_function_case_id not like", value, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdIn(List<String> values) {
            addCriterion("test_plan_function_case_id in", values, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdNotIn(List<String> values) {
            addCriterion("test_plan_function_case_id not in", values, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdBetween(String value1, String value2) {
            addCriterion("test_plan_function_case_id between", value1, value2, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanFunctionCaseIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_function_case_id not between", value1, value2, "testPlanFunctionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdIsNull() {
            addCriterion("function_case_id is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdIsNotNull() {
            addCriterion("function_case_id is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdEqualTo(String value) {
            addCriterion("function_case_id =", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdNotEqualTo(String value) {
            addCriterion("function_case_id <>", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdGreaterThan(String value) {
            addCriterion("function_case_id >", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdGreaterThanOrEqualTo(String value) {
            addCriterion("function_case_id >=", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdLessThan(String value) {
            addCriterion("function_case_id <", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdLessThanOrEqualTo(String value) {
            addCriterion("function_case_id <=", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdLike(String value) {
            addCriterion("function_case_id like", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdNotLike(String value) {
            addCriterion("function_case_id not like", value, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdIn(List<String> values) {
            addCriterion("function_case_id in", values, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdNotIn(List<String> values) {
            addCriterion("function_case_id not in", values, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdBetween(String value1, String value2) {
            addCriterion("function_case_id between", value1, value2, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseIdNotBetween(String value1, String value2) {
            addCriterion("function_case_id not between", value1, value2, "functionCaseId");
            return (Criteria) this;
        }

        public Criteria andExecuteResultIsNull() {
            addCriterion("execute_result is null");
            return (Criteria) this;
        }

        public Criteria andExecuteResultIsNotNull() {
            addCriterion("execute_result is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteResultEqualTo(String value) {
            addCriterion("execute_result =", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultNotEqualTo(String value) {
            addCriterion("execute_result <>", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultGreaterThan(String value) {
            addCriterion("execute_result >", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultGreaterThanOrEqualTo(String value) {
            addCriterion("execute_result >=", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultLessThan(String value) {
            addCriterion("execute_result <", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultLessThanOrEqualTo(String value) {
            addCriterion("execute_result <=", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultLike(String value) {
            addCriterion("execute_result like", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultNotLike(String value) {
            addCriterion("execute_result not like", value, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultIn(List<String> values) {
            addCriterion("execute_result in", values, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultNotIn(List<String> values) {
            addCriterion("execute_result not in", values, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultBetween(String value1, String value2) {
            addCriterion("execute_result between", value1, value2, "executeResult");
            return (Criteria) this;
        }

        public Criteria andExecuteResultNotBetween(String value1, String value2) {
            addCriterion("execute_result not between", value1, value2, "executeResult");
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