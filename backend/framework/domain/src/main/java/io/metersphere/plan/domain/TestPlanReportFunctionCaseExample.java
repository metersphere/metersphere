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

        public Criteria andFunctionCaseNumIsNull() {
            addCriterion("function_case_num is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumIsNotNull() {
            addCriterion("function_case_num is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumEqualTo(Long value) {
            addCriterion("function_case_num =", value, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumNotEqualTo(Long value) {
            addCriterion("function_case_num <>", value, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumGreaterThan(Long value) {
            addCriterion("function_case_num >", value, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumGreaterThanOrEqualTo(Long value) {
            addCriterion("function_case_num >=", value, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumLessThan(Long value) {
            addCriterion("function_case_num <", value, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumLessThanOrEqualTo(Long value) {
            addCriterion("function_case_num <=", value, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumIn(List<Long> values) {
            addCriterion("function_case_num in", values, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumNotIn(List<Long> values) {
            addCriterion("function_case_num not in", values, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumBetween(Long value1, Long value2) {
            addCriterion("function_case_num between", value1, value2, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNumNotBetween(Long value1, Long value2) {
            addCriterion("function_case_num not between", value1, value2, "functionCaseNum");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameIsNull() {
            addCriterion("function_case_name is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameIsNotNull() {
            addCriterion("function_case_name is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameEqualTo(String value) {
            addCriterion("function_case_name =", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameNotEqualTo(String value) {
            addCriterion("function_case_name <>", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameGreaterThan(String value) {
            addCriterion("function_case_name >", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameGreaterThanOrEqualTo(String value) {
            addCriterion("function_case_name >=", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameLessThan(String value) {
            addCriterion("function_case_name <", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameLessThanOrEqualTo(String value) {
            addCriterion("function_case_name <=", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameLike(String value) {
            addCriterion("function_case_name like", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameNotLike(String value) {
            addCriterion("function_case_name not like", value, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameIn(List<String> values) {
            addCriterion("function_case_name in", values, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameNotIn(List<String> values) {
            addCriterion("function_case_name not in", values, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameBetween(String value1, String value2) {
            addCriterion("function_case_name between", value1, value2, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseNameNotBetween(String value1, String value2) {
            addCriterion("function_case_name not between", value1, value2, "functionCaseName");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleIsNull() {
            addCriterion("function_case_module is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleIsNotNull() {
            addCriterion("function_case_module is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleEqualTo(String value) {
            addCriterion("function_case_module =", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleNotEqualTo(String value) {
            addCriterion("function_case_module <>", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleGreaterThan(String value) {
            addCriterion("function_case_module >", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleGreaterThanOrEqualTo(String value) {
            addCriterion("function_case_module >=", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleLessThan(String value) {
            addCriterion("function_case_module <", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleLessThanOrEqualTo(String value) {
            addCriterion("function_case_module <=", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleLike(String value) {
            addCriterion("function_case_module like", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleNotLike(String value) {
            addCriterion("function_case_module not like", value, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleIn(List<String> values) {
            addCriterion("function_case_module in", values, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleNotIn(List<String> values) {
            addCriterion("function_case_module not in", values, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleBetween(String value1, String value2) {
            addCriterion("function_case_module between", value1, value2, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseModuleNotBetween(String value1, String value2) {
            addCriterion("function_case_module not between", value1, value2, "functionCaseModule");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityIsNull() {
            addCriterion("function_case_priority is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityIsNotNull() {
            addCriterion("function_case_priority is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityEqualTo(String value) {
            addCriterion("function_case_priority =", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityNotEqualTo(String value) {
            addCriterion("function_case_priority <>", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityGreaterThan(String value) {
            addCriterion("function_case_priority >", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityGreaterThanOrEqualTo(String value) {
            addCriterion("function_case_priority >=", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityLessThan(String value) {
            addCriterion("function_case_priority <", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityLessThanOrEqualTo(String value) {
            addCriterion("function_case_priority <=", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityLike(String value) {
            addCriterion("function_case_priority like", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityNotLike(String value) {
            addCriterion("function_case_priority not like", value, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityIn(List<String> values) {
            addCriterion("function_case_priority in", values, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityNotIn(List<String> values) {
            addCriterion("function_case_priority not in", values, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityBetween(String value1, String value2) {
            addCriterion("function_case_priority between", value1, value2, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCasePriorityNotBetween(String value1, String value2) {
            addCriterion("function_case_priority not between", value1, value2, "functionCasePriority");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserIsNull() {
            addCriterion("function_case_execute_user is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserIsNotNull() {
            addCriterion("function_case_execute_user is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserEqualTo(String value) {
            addCriterion("function_case_execute_user =", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserNotEqualTo(String value) {
            addCriterion("function_case_execute_user <>", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserGreaterThan(String value) {
            addCriterion("function_case_execute_user >", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserGreaterThanOrEqualTo(String value) {
            addCriterion("function_case_execute_user >=", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserLessThan(String value) {
            addCriterion("function_case_execute_user <", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserLessThanOrEqualTo(String value) {
            addCriterion("function_case_execute_user <=", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserLike(String value) {
            addCriterion("function_case_execute_user like", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserNotLike(String value) {
            addCriterion("function_case_execute_user not like", value, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserIn(List<String> values) {
            addCriterion("function_case_execute_user in", values, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserNotIn(List<String> values) {
            addCriterion("function_case_execute_user not in", values, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserBetween(String value1, String value2) {
            addCriterion("function_case_execute_user between", value1, value2, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteUserNotBetween(String value1, String value2) {
            addCriterion("function_case_execute_user not between", value1, value2, "functionCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountIsNull() {
            addCriterion("function_case_bug_count is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountIsNotNull() {
            addCriterion("function_case_bug_count is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountEqualTo(Long value) {
            addCriterion("function_case_bug_count =", value, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountNotEqualTo(Long value) {
            addCriterion("function_case_bug_count <>", value, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountGreaterThan(Long value) {
            addCriterion("function_case_bug_count >", value, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountGreaterThanOrEqualTo(Long value) {
            addCriterion("function_case_bug_count >=", value, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountLessThan(Long value) {
            addCriterion("function_case_bug_count <", value, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountLessThanOrEqualTo(Long value) {
            addCriterion("function_case_bug_count <=", value, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountIn(List<Long> values) {
            addCriterion("function_case_bug_count in", values, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountNotIn(List<Long> values) {
            addCriterion("function_case_bug_count not in", values, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountBetween(Long value1, Long value2) {
            addCriterion("function_case_bug_count between", value1, value2, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseBugCountNotBetween(Long value1, Long value2) {
            addCriterion("function_case_bug_count not between", value1, value2, "functionCaseBugCount");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultIsNull() {
            addCriterion("function_case_execute_result is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultIsNotNull() {
            addCriterion("function_case_execute_result is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultEqualTo(String value) {
            addCriterion("function_case_execute_result =", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultNotEqualTo(String value) {
            addCriterion("function_case_execute_result <>", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultGreaterThan(String value) {
            addCriterion("function_case_execute_result >", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultGreaterThanOrEqualTo(String value) {
            addCriterion("function_case_execute_result >=", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultLessThan(String value) {
            addCriterion("function_case_execute_result <", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultLessThanOrEqualTo(String value) {
            addCriterion("function_case_execute_result <=", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultLike(String value) {
            addCriterion("function_case_execute_result like", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultNotLike(String value) {
            addCriterion("function_case_execute_result not like", value, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultIn(List<String> values) {
            addCriterion("function_case_execute_result in", values, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultNotIn(List<String> values) {
            addCriterion("function_case_execute_result not in", values, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultBetween(String value1, String value2) {
            addCriterion("function_case_execute_result between", value1, value2, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteResultNotBetween(String value1, String value2) {
            addCriterion("function_case_execute_result not between", value1, value2, "functionCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdIsNull() {
            addCriterion("test_plan_collection_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdIsNotNull() {
            addCriterion("test_plan_collection_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdEqualTo(String value) {
            addCriterion("test_plan_collection_id =", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdNotEqualTo(String value) {
            addCriterion("test_plan_collection_id <>", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdGreaterThan(String value) {
            addCriterion("test_plan_collection_id >", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_collection_id >=", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdLessThan(String value) {
            addCriterion("test_plan_collection_id <", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_collection_id <=", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdLike(String value) {
            addCriterion("test_plan_collection_id like", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdNotLike(String value) {
            addCriterion("test_plan_collection_id not like", value, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdIn(List<String> values) {
            addCriterion("test_plan_collection_id in", values, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdNotIn(List<String> values) {
            addCriterion("test_plan_collection_id not in", values, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdBetween(String value1, String value2) {
            addCriterion("test_plan_collection_id between", value1, value2, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andTestPlanCollectionIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_collection_id not between", value1, value2, "testPlanCollectionId");
            return (Criteria) this;
        }

        public Criteria andPosIsNull() {
            addCriterion("pos is null");
            return (Criteria) this;
        }

        public Criteria andPosIsNotNull() {
            addCriterion("pos is not null");
            return (Criteria) this;
        }

        public Criteria andPosEqualTo(Long value) {
            addCriterion("pos =", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotEqualTo(Long value) {
            addCriterion("pos <>", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosGreaterThan(Long value) {
            addCriterion("pos >", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosGreaterThanOrEqualTo(Long value) {
            addCriterion("pos >=", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosLessThan(Long value) {
            addCriterion("pos <", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosLessThanOrEqualTo(Long value) {
            addCriterion("pos <=", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosIn(List<Long> values) {
            addCriterion("pos in", values, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotIn(List<Long> values) {
            addCriterion("pos not in", values, "pos");
            return (Criteria) this;
        }

        public Criteria andPosBetween(Long value1, Long value2) {
            addCriterion("pos between", value1, value2, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotBetween(Long value1, Long value2) {
            addCriterion("pos not between", value1, value2, "pos");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdIsNull() {
            addCriterion("function_case_execute_report_id is null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdIsNotNull() {
            addCriterion("function_case_execute_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdEqualTo(String value) {
            addCriterion("function_case_execute_report_id =", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdNotEqualTo(String value) {
            addCriterion("function_case_execute_report_id <>", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdGreaterThan(String value) {
            addCriterion("function_case_execute_report_id >", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("function_case_execute_report_id >=", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdLessThan(String value) {
            addCriterion("function_case_execute_report_id <", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdLessThanOrEqualTo(String value) {
            addCriterion("function_case_execute_report_id <=", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdLike(String value) {
            addCriterion("function_case_execute_report_id like", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdNotLike(String value) {
            addCriterion("function_case_execute_report_id not like", value, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdIn(List<String> values) {
            addCriterion("function_case_execute_report_id in", values, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdNotIn(List<String> values) {
            addCriterion("function_case_execute_report_id not in", values, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdBetween(String value1, String value2) {
            addCriterion("function_case_execute_report_id between", value1, value2, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andFunctionCaseExecuteReportIdNotBetween(String value1, String value2) {
            addCriterion("function_case_execute_report_id not between", value1, value2, "functionCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameIsNull() {
            addCriterion("test_plan_name is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameIsNotNull() {
            addCriterion("test_plan_name is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameEqualTo(String value) {
            addCriterion("test_plan_name =", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameNotEqualTo(String value) {
            addCriterion("test_plan_name <>", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameGreaterThan(String value) {
            addCriterion("test_plan_name >", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_name >=", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameLessThan(String value) {
            addCriterion("test_plan_name <", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameLessThanOrEqualTo(String value) {
            addCriterion("test_plan_name <=", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameLike(String value) {
            addCriterion("test_plan_name like", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameNotLike(String value) {
            addCriterion("test_plan_name not like", value, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameIn(List<String> values) {
            addCriterion("test_plan_name in", values, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameNotIn(List<String> values) {
            addCriterion("test_plan_name not in", values, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameBetween(String value1, String value2) {
            addCriterion("test_plan_name between", value1, value2, "testPlanName");
            return (Criteria) this;
        }

        public Criteria andTestPlanNameNotBetween(String value1, String value2) {
            addCriterion("test_plan_name not between", value1, value2, "testPlanName");
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