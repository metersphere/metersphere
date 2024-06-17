package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanCollectionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanCollectionExample() {
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

        public Criteria andTestPlanIdIsNull() {
            addCriterion("test_plan_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdIsNotNull() {
            addCriterion("test_plan_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdEqualTo(String value) {
            addCriterion("test_plan_id =", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotEqualTo(String value) {
            addCriterion("test_plan_id <>", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdGreaterThan(String value) {
            addCriterion("test_plan_id >", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_id >=", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLessThan(String value) {
            addCriterion("test_plan_id <", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_id <=", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdLike(String value) {
            addCriterion("test_plan_id like", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotLike(String value) {
            addCriterion("test_plan_id not like", value, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdIn(List<String> values) {
            addCriterion("test_plan_id in", values, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotIn(List<String> values) {
            addCriterion("test_plan_id not in", values, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdBetween(String value1, String value2) {
            addCriterion("test_plan_id between", value1, value2, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andTestPlanIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_id not between", value1, value2, "testPlanId");
            return (Criteria) this;
        }

        public Criteria andParentIdIsNull() {
            addCriterion("parent_id is null");
            return (Criteria) this;
        }

        public Criteria andParentIdIsNotNull() {
            addCriterion("parent_id is not null");
            return (Criteria) this;
        }

        public Criteria andParentIdEqualTo(String value) {
            addCriterion("parent_id =", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdNotEqualTo(String value) {
            addCriterion("parent_id <>", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdGreaterThan(String value) {
            addCriterion("parent_id >", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdGreaterThanOrEqualTo(String value) {
            addCriterion("parent_id >=", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdLessThan(String value) {
            addCriterion("parent_id <", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdLessThanOrEqualTo(String value) {
            addCriterion("parent_id <=", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdLike(String value) {
            addCriterion("parent_id like", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdNotLike(String value) {
            addCriterion("parent_id not like", value, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdIn(List<String> values) {
            addCriterion("parent_id in", values, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdNotIn(List<String> values) {
            addCriterion("parent_id not in", values, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdBetween(String value1, String value2) {
            addCriterion("parent_id between", value1, value2, "parentId");
            return (Criteria) this;
        }

        public Criteria andParentIdNotBetween(String value1, String value2) {
            addCriterion("parent_id not between", value1, value2, "parentId");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("`type` like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("`type` not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodIsNull() {
            addCriterion("execute_method is null");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodIsNotNull() {
            addCriterion("execute_method is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodEqualTo(String value) {
            addCriterion("execute_method =", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodNotEqualTo(String value) {
            addCriterion("execute_method <>", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodGreaterThan(String value) {
            addCriterion("execute_method >", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodGreaterThanOrEqualTo(String value) {
            addCriterion("execute_method >=", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodLessThan(String value) {
            addCriterion("execute_method <", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodLessThanOrEqualTo(String value) {
            addCriterion("execute_method <=", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodLike(String value) {
            addCriterion("execute_method like", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodNotLike(String value) {
            addCriterion("execute_method not like", value, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodIn(List<String> values) {
            addCriterion("execute_method in", values, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodNotIn(List<String> values) {
            addCriterion("execute_method not in", values, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodBetween(String value1, String value2) {
            addCriterion("execute_method between", value1, value2, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExecuteMethodNotBetween(String value1, String value2) {
            addCriterion("execute_method not between", value1, value2, "executeMethod");
            return (Criteria) this;
        }

        public Criteria andExtendedIsNull() {
            addCriterion("extended is null");
            return (Criteria) this;
        }

        public Criteria andExtendedIsNotNull() {
            addCriterion("extended is not null");
            return (Criteria) this;
        }

        public Criteria andExtendedEqualTo(Boolean value) {
            addCriterion("extended =", value, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedNotEqualTo(Boolean value) {
            addCriterion("extended <>", value, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedGreaterThan(Boolean value) {
            addCriterion("extended >", value, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("extended >=", value, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedLessThan(Boolean value) {
            addCriterion("extended <", value, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedLessThanOrEqualTo(Boolean value) {
            addCriterion("extended <=", value, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedIn(List<Boolean> values) {
            addCriterion("extended in", values, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedNotIn(List<Boolean> values) {
            addCriterion("extended not in", values, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedBetween(Boolean value1, Boolean value2) {
            addCriterion("extended between", value1, value2, "extended");
            return (Criteria) this;
        }

        public Criteria andExtendedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("extended not between", value1, value2, "extended");
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

        public Criteria andTestResourcePoolIdIsNull() {
            addCriterion("test_resource_pool_id is null");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdIsNotNull() {
            addCriterion("test_resource_pool_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdEqualTo(String value) {
            addCriterion("test_resource_pool_id =", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotEqualTo(String value) {
            addCriterion("test_resource_pool_id <>", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdGreaterThan(String value) {
            addCriterion("test_resource_pool_id >", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_resource_pool_id >=", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdLessThan(String value) {
            addCriterion("test_resource_pool_id <", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdLessThanOrEqualTo(String value) {
            addCriterion("test_resource_pool_id <=", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdLike(String value) {
            addCriterion("test_resource_pool_id like", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotLike(String value) {
            addCriterion("test_resource_pool_id not like", value, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdIn(List<String> values) {
            addCriterion("test_resource_pool_id in", values, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotIn(List<String> values) {
            addCriterion("test_resource_pool_id not in", values, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdBetween(String value1, String value2) {
            addCriterion("test_resource_pool_id between", value1, value2, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andTestResourcePoolIdNotBetween(String value1, String value2) {
            addCriterion("test_resource_pool_id not between", value1, value2, "testResourcePoolId");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailIsNull() {
            addCriterion("retry_on_fail is null");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailIsNotNull() {
            addCriterion("retry_on_fail is not null");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailEqualTo(Boolean value) {
            addCriterion("retry_on_fail =", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailNotEqualTo(Boolean value) {
            addCriterion("retry_on_fail <>", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailGreaterThan(Boolean value) {
            addCriterion("retry_on_fail >", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailGreaterThanOrEqualTo(Boolean value) {
            addCriterion("retry_on_fail >=", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailLessThan(Boolean value) {
            addCriterion("retry_on_fail <", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailLessThanOrEqualTo(Boolean value) {
            addCriterion("retry_on_fail <=", value, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailIn(List<Boolean> values) {
            addCriterion("retry_on_fail in", values, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailNotIn(List<Boolean> values) {
            addCriterion("retry_on_fail not in", values, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailBetween(Boolean value1, Boolean value2) {
            addCriterion("retry_on_fail between", value1, value2, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryOnFailNotBetween(Boolean value1, Boolean value2) {
            addCriterion("retry_on_fail not between", value1, value2, "retryOnFail");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIsNull() {
            addCriterion("retry_times is null");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIsNotNull() {
            addCriterion("retry_times is not null");
            return (Criteria) this;
        }

        public Criteria andRetryTimesEqualTo(Integer value) {
            addCriterion("retry_times =", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotEqualTo(Integer value) {
            addCriterion("retry_times <>", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesGreaterThan(Integer value) {
            addCriterion("retry_times >", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_times >=", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesLessThan(Integer value) {
            addCriterion("retry_times <", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesLessThanOrEqualTo(Integer value) {
            addCriterion("retry_times <=", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIn(List<Integer> values) {
            addCriterion("retry_times in", values, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotIn(List<Integer> values) {
            addCriterion("retry_times not in", values, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesBetween(Integer value1, Integer value2) {
            addCriterion("retry_times between", value1, value2, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_times not between", value1, value2, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIsNull() {
            addCriterion("retry_interval is null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIsNotNull() {
            addCriterion("retry_interval is not null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalEqualTo(Integer value) {
            addCriterion("retry_interval =", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotEqualTo(Integer value) {
            addCriterion("retry_interval <>", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalGreaterThan(Integer value) {
            addCriterion("retry_interval >", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_interval >=", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalLessThan(Integer value) {
            addCriterion("retry_interval <", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalLessThanOrEqualTo(Integer value) {
            addCriterion("retry_interval <=", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIn(List<Integer> values) {
            addCriterion("retry_interval in", values, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotIn(List<Integer> values) {
            addCriterion("retry_interval not in", values, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalBetween(Integer value1, Integer value2) {
            addCriterion("retry_interval between", value1, value2, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_interval not between", value1, value2, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andStopOnFailIsNull() {
            addCriterion("stop_on_fail is null");
            return (Criteria) this;
        }

        public Criteria andStopOnFailIsNotNull() {
            addCriterion("stop_on_fail is not null");
            return (Criteria) this;
        }

        public Criteria andStopOnFailEqualTo(Boolean value) {
            addCriterion("stop_on_fail =", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailNotEqualTo(Boolean value) {
            addCriterion("stop_on_fail <>", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailGreaterThan(Boolean value) {
            addCriterion("stop_on_fail >", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailGreaterThanOrEqualTo(Boolean value) {
            addCriterion("stop_on_fail >=", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailLessThan(Boolean value) {
            addCriterion("stop_on_fail <", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailLessThanOrEqualTo(Boolean value) {
            addCriterion("stop_on_fail <=", value, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailIn(List<Boolean> values) {
            addCriterion("stop_on_fail in", values, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailNotIn(List<Boolean> values) {
            addCriterion("stop_on_fail not in", values, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailBetween(Boolean value1, Boolean value2) {
            addCriterion("stop_on_fail between", value1, value2, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andStopOnFailNotBetween(Boolean value1, Boolean value2) {
            addCriterion("stop_on_fail not between", value1, value2, "stopOnFail");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNull() {
            addCriterion("create_user is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNotNull() {
            addCriterion("create_user is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserEqualTo(String value) {
            addCriterion("create_user =", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotEqualTo(String value) {
            addCriterion("create_user <>", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThan(String value) {
            addCriterion("create_user >", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThanOrEqualTo(String value) {
            addCriterion("create_user >=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThan(String value) {
            addCriterion("create_user <", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThanOrEqualTo(String value) {
            addCriterion("create_user <=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLike(String value) {
            addCriterion("create_user like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotLike(String value) {
            addCriterion("create_user not like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserIn(List<String> values) {
            addCriterion("create_user in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotIn(List<String> values) {
            addCriterion("create_user not in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserBetween(String value1, String value2) {
            addCriterion("create_user between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotBetween(String value1, String value2) {
            addCriterion("create_user not between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Long value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Long value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Long value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Long value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Long value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Long> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Long> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Long value1, Long value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Long value1, Long value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
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