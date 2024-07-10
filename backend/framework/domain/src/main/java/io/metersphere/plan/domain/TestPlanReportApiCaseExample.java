package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanReportApiCaseExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanReportApiCaseExample() {
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

        public Criteria andEnvironmentIdIsNull() {
            addCriterion("environment_id is null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdIsNotNull() {
            addCriterion("environment_id is not null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdEqualTo(String value) {
            addCriterion("environment_id =", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdNotEqualTo(String value) {
            addCriterion("environment_id <>", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdGreaterThan(String value) {
            addCriterion("environment_id >", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdGreaterThanOrEqualTo(String value) {
            addCriterion("environment_id >=", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdLessThan(String value) {
            addCriterion("environment_id <", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdLessThanOrEqualTo(String value) {
            addCriterion("environment_id <=", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdLike(String value) {
            addCriterion("environment_id like", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdNotLike(String value) {
            addCriterion("environment_id not like", value, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdIn(List<String> values) {
            addCriterion("environment_id in", values, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdNotIn(List<String> values) {
            addCriterion("environment_id not in", values, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdBetween(String value1, String value2) {
            addCriterion("environment_id between", value1, value2, "environmentId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentIdNotBetween(String value1, String value2) {
            addCriterion("environment_id not between", value1, value2, "environmentId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdIsNull() {
            addCriterion("test_plan_api_case_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdIsNotNull() {
            addCriterion("test_plan_api_case_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdEqualTo(String value) {
            addCriterion("test_plan_api_case_id =", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdNotEqualTo(String value) {
            addCriterion("test_plan_api_case_id <>", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdGreaterThan(String value) {
            addCriterion("test_plan_api_case_id >", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_api_case_id >=", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdLessThan(String value) {
            addCriterion("test_plan_api_case_id <", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_api_case_id <=", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdLike(String value) {
            addCriterion("test_plan_api_case_id like", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdNotLike(String value) {
            addCriterion("test_plan_api_case_id not like", value, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdIn(List<String> values) {
            addCriterion("test_plan_api_case_id in", values, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdNotIn(List<String> values) {
            addCriterion("test_plan_api_case_id not in", values, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdBetween(String value1, String value2) {
            addCriterion("test_plan_api_case_id between", value1, value2, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiCaseIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_api_case_id not between", value1, value2, "testPlanApiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdIsNull() {
            addCriterion("api_case_id is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdIsNotNull() {
            addCriterion("api_case_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdEqualTo(String value) {
            addCriterion("api_case_id =", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotEqualTo(String value) {
            addCriterion("api_case_id <>", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdGreaterThan(String value) {
            addCriterion("api_case_id >", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_id >=", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdLessThan(String value) {
            addCriterion("api_case_id <", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdLessThanOrEqualTo(String value) {
            addCriterion("api_case_id <=", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdLike(String value) {
            addCriterion("api_case_id like", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotLike(String value) {
            addCriterion("api_case_id not like", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdIn(List<String> values) {
            addCriterion("api_case_id in", values, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotIn(List<String> values) {
            addCriterion("api_case_id not in", values, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdBetween(String value1, String value2) {
            addCriterion("api_case_id between", value1, value2, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotBetween(String value1, String value2) {
            addCriterion("api_case_id not between", value1, value2, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumIsNull() {
            addCriterion("api_case_num is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumIsNotNull() {
            addCriterion("api_case_num is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumEqualTo(Long value) {
            addCriterion("api_case_num =", value, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumNotEqualTo(Long value) {
            addCriterion("api_case_num <>", value, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumGreaterThan(Long value) {
            addCriterion("api_case_num >", value, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumGreaterThanOrEqualTo(Long value) {
            addCriterion("api_case_num >=", value, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumLessThan(Long value) {
            addCriterion("api_case_num <", value, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumLessThanOrEqualTo(Long value) {
            addCriterion("api_case_num <=", value, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumIn(List<Long> values) {
            addCriterion("api_case_num in", values, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumNotIn(List<Long> values) {
            addCriterion("api_case_num not in", values, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumBetween(Long value1, Long value2) {
            addCriterion("api_case_num between", value1, value2, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNumNotBetween(Long value1, Long value2) {
            addCriterion("api_case_num not between", value1, value2, "apiCaseNum");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameIsNull() {
            addCriterion("api_case_name is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameIsNotNull() {
            addCriterion("api_case_name is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameEqualTo(String value) {
            addCriterion("api_case_name =", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameNotEqualTo(String value) {
            addCriterion("api_case_name <>", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameGreaterThan(String value) {
            addCriterion("api_case_name >", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_name >=", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameLessThan(String value) {
            addCriterion("api_case_name <", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameLessThanOrEqualTo(String value) {
            addCriterion("api_case_name <=", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameLike(String value) {
            addCriterion("api_case_name like", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameNotLike(String value) {
            addCriterion("api_case_name not like", value, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameIn(List<String> values) {
            addCriterion("api_case_name in", values, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameNotIn(List<String> values) {
            addCriterion("api_case_name not in", values, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameBetween(String value1, String value2) {
            addCriterion("api_case_name between", value1, value2, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseNameNotBetween(String value1, String value2) {
            addCriterion("api_case_name not between", value1, value2, "apiCaseName");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleIsNull() {
            addCriterion("api_case_module is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleIsNotNull() {
            addCriterion("api_case_module is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleEqualTo(String value) {
            addCriterion("api_case_module =", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleNotEqualTo(String value) {
            addCriterion("api_case_module <>", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleGreaterThan(String value) {
            addCriterion("api_case_module >", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_module >=", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleLessThan(String value) {
            addCriterion("api_case_module <", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleLessThanOrEqualTo(String value) {
            addCriterion("api_case_module <=", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleLike(String value) {
            addCriterion("api_case_module like", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleNotLike(String value) {
            addCriterion("api_case_module not like", value, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleIn(List<String> values) {
            addCriterion("api_case_module in", values, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleNotIn(List<String> values) {
            addCriterion("api_case_module not in", values, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleBetween(String value1, String value2) {
            addCriterion("api_case_module between", value1, value2, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCaseModuleNotBetween(String value1, String value2) {
            addCriterion("api_case_module not between", value1, value2, "apiCaseModule");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityIsNull() {
            addCriterion("api_case_priority is null");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityIsNotNull() {
            addCriterion("api_case_priority is not null");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityEqualTo(String value) {
            addCriterion("api_case_priority =", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityNotEqualTo(String value) {
            addCriterion("api_case_priority <>", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityGreaterThan(String value) {
            addCriterion("api_case_priority >", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_priority >=", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityLessThan(String value) {
            addCriterion("api_case_priority <", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityLessThanOrEqualTo(String value) {
            addCriterion("api_case_priority <=", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityLike(String value) {
            addCriterion("api_case_priority like", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityNotLike(String value) {
            addCriterion("api_case_priority not like", value, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityIn(List<String> values) {
            addCriterion("api_case_priority in", values, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityNotIn(List<String> values) {
            addCriterion("api_case_priority not in", values, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityBetween(String value1, String value2) {
            addCriterion("api_case_priority between", value1, value2, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCasePriorityNotBetween(String value1, String value2) {
            addCriterion("api_case_priority not between", value1, value2, "apiCasePriority");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserIsNull() {
            addCriterion("api_case_execute_user is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserIsNotNull() {
            addCriterion("api_case_execute_user is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserEqualTo(String value) {
            addCriterion("api_case_execute_user =", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserNotEqualTo(String value) {
            addCriterion("api_case_execute_user <>", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserGreaterThan(String value) {
            addCriterion("api_case_execute_user >", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_execute_user >=", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserLessThan(String value) {
            addCriterion("api_case_execute_user <", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserLessThanOrEqualTo(String value) {
            addCriterion("api_case_execute_user <=", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserLike(String value) {
            addCriterion("api_case_execute_user like", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserNotLike(String value) {
            addCriterion("api_case_execute_user not like", value, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserIn(List<String> values) {
            addCriterion("api_case_execute_user in", values, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserNotIn(List<String> values) {
            addCriterion("api_case_execute_user not in", values, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserBetween(String value1, String value2) {
            addCriterion("api_case_execute_user between", value1, value2, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteUserNotBetween(String value1, String value2) {
            addCriterion("api_case_execute_user not between", value1, value2, "apiCaseExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultIsNull() {
            addCriterion("api_case_execute_result is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultIsNotNull() {
            addCriterion("api_case_execute_result is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultEqualTo(String value) {
            addCriterion("api_case_execute_result =", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultNotEqualTo(String value) {
            addCriterion("api_case_execute_result <>", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultGreaterThan(String value) {
            addCriterion("api_case_execute_result >", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_execute_result >=", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultLessThan(String value) {
            addCriterion("api_case_execute_result <", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultLessThanOrEqualTo(String value) {
            addCriterion("api_case_execute_result <=", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultLike(String value) {
            addCriterion("api_case_execute_result like", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultNotLike(String value) {
            addCriterion("api_case_execute_result not like", value, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultIn(List<String> values) {
            addCriterion("api_case_execute_result in", values, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultNotIn(List<String> values) {
            addCriterion("api_case_execute_result not in", values, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultBetween(String value1, String value2) {
            addCriterion("api_case_execute_result between", value1, value2, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteResultNotBetween(String value1, String value2) {
            addCriterion("api_case_execute_result not between", value1, value2, "apiCaseExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdIsNull() {
            addCriterion("api_case_execute_report_id is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdIsNotNull() {
            addCriterion("api_case_execute_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdEqualTo(String value) {
            addCriterion("api_case_execute_report_id =", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdNotEqualTo(String value) {
            addCriterion("api_case_execute_report_id <>", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdGreaterThan(String value) {
            addCriterion("api_case_execute_report_id >", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_execute_report_id >=", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdLessThan(String value) {
            addCriterion("api_case_execute_report_id <", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdLessThanOrEqualTo(String value) {
            addCriterion("api_case_execute_report_id <=", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdLike(String value) {
            addCriterion("api_case_execute_report_id like", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdNotLike(String value) {
            addCriterion("api_case_execute_report_id not like", value, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdIn(List<String> values) {
            addCriterion("api_case_execute_report_id in", values, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdNotIn(List<String> values) {
            addCriterion("api_case_execute_report_id not in", values, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdBetween(String value1, String value2) {
            addCriterion("api_case_execute_report_id between", value1, value2, "apiCaseExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiCaseExecuteReportIdNotBetween(String value1, String value2) {
            addCriterion("api_case_execute_report_id not between", value1, value2, "apiCaseExecuteReportId");
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