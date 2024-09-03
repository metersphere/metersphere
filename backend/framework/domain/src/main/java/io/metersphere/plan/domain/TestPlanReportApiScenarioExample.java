package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanReportApiScenarioExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanReportApiScenarioExample() {
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

        public Criteria andGroupedIsNull() {
            addCriterion("grouped is null");
            return (Criteria) this;
        }

        public Criteria andGroupedIsNotNull() {
            addCriterion("grouped is not null");
            return (Criteria) this;
        }

        public Criteria andGroupedEqualTo(Boolean value) {
            addCriterion("grouped =", value, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedNotEqualTo(Boolean value) {
            addCriterion("grouped <>", value, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedGreaterThan(Boolean value) {
            addCriterion("grouped >", value, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("grouped >=", value, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedLessThan(Boolean value) {
            addCriterion("grouped <", value, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedLessThanOrEqualTo(Boolean value) {
            addCriterion("grouped <=", value, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedIn(List<Boolean> values) {
            addCriterion("grouped in", values, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedNotIn(List<Boolean> values) {
            addCriterion("grouped not in", values, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedBetween(Boolean value1, Boolean value2) {
            addCriterion("grouped between", value1, value2, "grouped");
            return (Criteria) this;
        }

        public Criteria andGroupedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("grouped not between", value1, value2, "grouped");
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

        public Criteria andTestPlanApiScenarioIdIsNull() {
            addCriterion("test_plan_api_scenario_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdIsNotNull() {
            addCriterion("test_plan_api_scenario_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdEqualTo(String value) {
            addCriterion("test_plan_api_scenario_id =", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdNotEqualTo(String value) {
            addCriterion("test_plan_api_scenario_id <>", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdGreaterThan(String value) {
            addCriterion("test_plan_api_scenario_id >", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_api_scenario_id >=", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdLessThan(String value) {
            addCriterion("test_plan_api_scenario_id <", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_api_scenario_id <=", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdLike(String value) {
            addCriterion("test_plan_api_scenario_id like", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdNotLike(String value) {
            addCriterion("test_plan_api_scenario_id not like", value, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdIn(List<String> values) {
            addCriterion("test_plan_api_scenario_id in", values, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdNotIn(List<String> values) {
            addCriterion("test_plan_api_scenario_id not in", values, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdBetween(String value1, String value2) {
            addCriterion("test_plan_api_scenario_id between", value1, value2, "testPlanApiScenarioId");
            return (Criteria) this;
        }

        public Criteria andTestPlanApiScenarioIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_api_scenario_id not between", value1, value2, "testPlanApiScenarioId");
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

        public Criteria andApiScenarioNumIsNull() {
            addCriterion("api_scenario_num is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumIsNotNull() {
            addCriterion("api_scenario_num is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumEqualTo(Long value) {
            addCriterion("api_scenario_num =", value, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumNotEqualTo(Long value) {
            addCriterion("api_scenario_num <>", value, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumGreaterThan(Long value) {
            addCriterion("api_scenario_num >", value, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumGreaterThanOrEqualTo(Long value) {
            addCriterion("api_scenario_num >=", value, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumLessThan(Long value) {
            addCriterion("api_scenario_num <", value, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumLessThanOrEqualTo(Long value) {
            addCriterion("api_scenario_num <=", value, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumIn(List<Long> values) {
            addCriterion("api_scenario_num in", values, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumNotIn(List<Long> values) {
            addCriterion("api_scenario_num not in", values, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumBetween(Long value1, Long value2) {
            addCriterion("api_scenario_num between", value1, value2, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNumNotBetween(Long value1, Long value2) {
            addCriterion("api_scenario_num not between", value1, value2, "apiScenarioNum");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameIsNull() {
            addCriterion("api_scenario_name is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameIsNotNull() {
            addCriterion("api_scenario_name is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameEqualTo(String value) {
            addCriterion("api_scenario_name =", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameNotEqualTo(String value) {
            addCriterion("api_scenario_name <>", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameGreaterThan(String value) {
            addCriterion("api_scenario_name >", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_name >=", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameLessThan(String value) {
            addCriterion("api_scenario_name <", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_name <=", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameLike(String value) {
            addCriterion("api_scenario_name like", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameNotLike(String value) {
            addCriterion("api_scenario_name not like", value, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameIn(List<String> values) {
            addCriterion("api_scenario_name in", values, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameNotIn(List<String> values) {
            addCriterion("api_scenario_name not in", values, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameBetween(String value1, String value2) {
            addCriterion("api_scenario_name between", value1, value2, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioNameNotBetween(String value1, String value2) {
            addCriterion("api_scenario_name not between", value1, value2, "apiScenarioName");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIsNull() {
            addCriterion("api_scenario_module is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIsNotNull() {
            addCriterion("api_scenario_module is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleEqualTo(String value) {
            addCriterion("api_scenario_module =", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleNotEqualTo(String value) {
            addCriterion("api_scenario_module <>", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleGreaterThan(String value) {
            addCriterion("api_scenario_module >", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_module >=", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleLessThan(String value) {
            addCriterion("api_scenario_module <", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_module <=", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleLike(String value) {
            addCriterion("api_scenario_module like", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleNotLike(String value) {
            addCriterion("api_scenario_module not like", value, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIn(List<String> values) {
            addCriterion("api_scenario_module in", values, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleNotIn(List<String> values) {
            addCriterion("api_scenario_module not in", values, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleBetween(String value1, String value2) {
            addCriterion("api_scenario_module between", value1, value2, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleNotBetween(String value1, String value2) {
            addCriterion("api_scenario_module not between", value1, value2, "apiScenarioModule");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityIsNull() {
            addCriterion("api_scenario_priority is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityIsNotNull() {
            addCriterion("api_scenario_priority is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityEqualTo(String value) {
            addCriterion("api_scenario_priority =", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityNotEqualTo(String value) {
            addCriterion("api_scenario_priority <>", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityGreaterThan(String value) {
            addCriterion("api_scenario_priority >", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_priority >=", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityLessThan(String value) {
            addCriterion("api_scenario_priority <", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_priority <=", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityLike(String value) {
            addCriterion("api_scenario_priority like", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityNotLike(String value) {
            addCriterion("api_scenario_priority not like", value, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityIn(List<String> values) {
            addCriterion("api_scenario_priority in", values, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityNotIn(List<String> values) {
            addCriterion("api_scenario_priority not in", values, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityBetween(String value1, String value2) {
            addCriterion("api_scenario_priority between", value1, value2, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioPriorityNotBetween(String value1, String value2) {
            addCriterion("api_scenario_priority not between", value1, value2, "apiScenarioPriority");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserIsNull() {
            addCriterion("api_scenario_execute_user is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserIsNotNull() {
            addCriterion("api_scenario_execute_user is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserEqualTo(String value) {
            addCriterion("api_scenario_execute_user =", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserNotEqualTo(String value) {
            addCriterion("api_scenario_execute_user <>", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserGreaterThan(String value) {
            addCriterion("api_scenario_execute_user >", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_execute_user >=", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserLessThan(String value) {
            addCriterion("api_scenario_execute_user <", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_execute_user <=", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserLike(String value) {
            addCriterion("api_scenario_execute_user like", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserNotLike(String value) {
            addCriterion("api_scenario_execute_user not like", value, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserIn(List<String> values) {
            addCriterion("api_scenario_execute_user in", values, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserNotIn(List<String> values) {
            addCriterion("api_scenario_execute_user not in", values, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserBetween(String value1, String value2) {
            addCriterion("api_scenario_execute_user between", value1, value2, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteUserNotBetween(String value1, String value2) {
            addCriterion("api_scenario_execute_user not between", value1, value2, "apiScenarioExecuteUser");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultIsNull() {
            addCriterion("api_scenario_execute_result is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultIsNotNull() {
            addCriterion("api_scenario_execute_result is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultEqualTo(String value) {
            addCriterion("api_scenario_execute_result =", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultNotEqualTo(String value) {
            addCriterion("api_scenario_execute_result <>", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultGreaterThan(String value) {
            addCriterion("api_scenario_execute_result >", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_execute_result >=", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultLessThan(String value) {
            addCriterion("api_scenario_execute_result <", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_execute_result <=", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultLike(String value) {
            addCriterion("api_scenario_execute_result like", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultNotLike(String value) {
            addCriterion("api_scenario_execute_result not like", value, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultIn(List<String> values) {
            addCriterion("api_scenario_execute_result in", values, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultNotIn(List<String> values) {
            addCriterion("api_scenario_execute_result not in", values, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultBetween(String value1, String value2) {
            addCriterion("api_scenario_execute_result between", value1, value2, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteResultNotBetween(String value1, String value2) {
            addCriterion("api_scenario_execute_result not between", value1, value2, "apiScenarioExecuteResult");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdIsNull() {
            addCriterion("api_scenario_execute_report_id is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdIsNotNull() {
            addCriterion("api_scenario_execute_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdEqualTo(String value) {
            addCriterion("api_scenario_execute_report_id =", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdNotEqualTo(String value) {
            addCriterion("api_scenario_execute_report_id <>", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdGreaterThan(String value) {
            addCriterion("api_scenario_execute_report_id >", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_execute_report_id >=", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdLessThan(String value) {
            addCriterion("api_scenario_execute_report_id <", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_execute_report_id <=", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdLike(String value) {
            addCriterion("api_scenario_execute_report_id like", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdNotLike(String value) {
            addCriterion("api_scenario_execute_report_id not like", value, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdIn(List<String> values) {
            addCriterion("api_scenario_execute_report_id in", values, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdNotIn(List<String> values) {
            addCriterion("api_scenario_execute_report_id not in", values, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdBetween(String value1, String value2) {
            addCriterion("api_scenario_execute_report_id between", value1, value2, "apiScenarioExecuteReportId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioExecuteReportIdNotBetween(String value1, String value2) {
            addCriterion("api_scenario_execute_report_id not between", value1, value2, "apiScenarioExecuteReportId");
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

        public Criteria andApiScenarioBugCountIsNull() {
            addCriterion("api_scenario_bug_count is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountIsNotNull() {
            addCriterion("api_scenario_bug_count is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountEqualTo(Long value) {
            addCriterion("api_scenario_bug_count =", value, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountNotEqualTo(Long value) {
            addCriterion("api_scenario_bug_count <>", value, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountGreaterThan(Long value) {
            addCriterion("api_scenario_bug_count >", value, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountGreaterThanOrEqualTo(Long value) {
            addCriterion("api_scenario_bug_count >=", value, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountLessThan(Long value) {
            addCriterion("api_scenario_bug_count <", value, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountLessThanOrEqualTo(Long value) {
            addCriterion("api_scenario_bug_count <=", value, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountIn(List<Long> values) {
            addCriterion("api_scenario_bug_count in", values, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountNotIn(List<Long> values) {
            addCriterion("api_scenario_bug_count not in", values, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountBetween(Long value1, Long value2) {
            addCriterion("api_scenario_bug_count between", value1, value2, "apiScenarioBugCount");
            return (Criteria) this;
        }

        public Criteria andApiScenarioBugCountNotBetween(Long value1, Long value2) {
            addCriterion("api_scenario_bug_count not between", value1, value2, "apiScenarioBugCount");
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