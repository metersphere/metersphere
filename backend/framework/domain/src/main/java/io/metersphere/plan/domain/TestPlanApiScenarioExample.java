package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanApiScenarioExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanApiScenarioExample() {
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

        public Criteria andExecuteUserIsNull() {
            addCriterion("execute_user is null");
            return (Criteria) this;
        }

        public Criteria andExecuteUserIsNotNull() {
            addCriterion("execute_user is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteUserEqualTo(String value) {
            addCriterion("execute_user =", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotEqualTo(String value) {
            addCriterion("execute_user <>", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserGreaterThan(String value) {
            addCriterion("execute_user >", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserGreaterThanOrEqualTo(String value) {
            addCriterion("execute_user >=", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserLessThan(String value) {
            addCriterion("execute_user <", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserLessThanOrEqualTo(String value) {
            addCriterion("execute_user <=", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserLike(String value) {
            addCriterion("execute_user like", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotLike(String value) {
            addCriterion("execute_user not like", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserIn(List<String> values) {
            addCriterion("execute_user in", values, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotIn(List<String> values) {
            addCriterion("execute_user not in", values, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserBetween(String value1, String value2) {
            addCriterion("execute_user between", value1, value2, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotBetween(String value1, String value2) {
            addCriterion("execute_user not between", value1, value2, "executeUser");
            return (Criteria) this;
        }

        public Criteria andLastExecResultIsNull() {
            addCriterion("last_exec_result is null");
            return (Criteria) this;
        }

        public Criteria andLastExecResultIsNotNull() {
            addCriterion("last_exec_result is not null");
            return (Criteria) this;
        }

        public Criteria andLastExecResultEqualTo(String value) {
            addCriterion("last_exec_result =", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultNotEqualTo(String value) {
            addCriterion("last_exec_result <>", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultGreaterThan(String value) {
            addCriterion("last_exec_result >", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultGreaterThanOrEqualTo(String value) {
            addCriterion("last_exec_result >=", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultLessThan(String value) {
            addCriterion("last_exec_result <", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultLessThanOrEqualTo(String value) {
            addCriterion("last_exec_result <=", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultLike(String value) {
            addCriterion("last_exec_result like", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultNotLike(String value) {
            addCriterion("last_exec_result not like", value, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultIn(List<String> values) {
            addCriterion("last_exec_result in", values, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultNotIn(List<String> values) {
            addCriterion("last_exec_result not in", values, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultBetween(String value1, String value2) {
            addCriterion("last_exec_result between", value1, value2, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecResultNotBetween(String value1, String value2) {
            addCriterion("last_exec_result not between", value1, value2, "lastExecResult");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdIsNull() {
            addCriterion("last_exec_report_id is null");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdIsNotNull() {
            addCriterion("last_exec_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdEqualTo(String value) {
            addCriterion("last_exec_report_id =", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdNotEqualTo(String value) {
            addCriterion("last_exec_report_id <>", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdGreaterThan(String value) {
            addCriterion("last_exec_report_id >", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("last_exec_report_id >=", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdLessThan(String value) {
            addCriterion("last_exec_report_id <", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdLessThanOrEqualTo(String value) {
            addCriterion("last_exec_report_id <=", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdLike(String value) {
            addCriterion("last_exec_report_id like", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdNotLike(String value) {
            addCriterion("last_exec_report_id not like", value, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdIn(List<String> values) {
            addCriterion("last_exec_report_id in", values, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdNotIn(List<String> values) {
            addCriterion("last_exec_report_id not in", values, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdBetween(String value1, String value2) {
            addCriterion("last_exec_report_id between", value1, value2, "lastExecReportId");
            return (Criteria) this;
        }

        public Criteria andLastExecReportIdNotBetween(String value1, String value2) {
            addCriterion("last_exec_report_id not between", value1, value2, "lastExecReportId");
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

        public Criteria andLastExecTimeIsNull() {
            addCriterion("last_exec_time is null");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeIsNotNull() {
            addCriterion("last_exec_time is not null");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeEqualTo(Long value) {
            addCriterion("last_exec_time =", value, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeNotEqualTo(Long value) {
            addCriterion("last_exec_time <>", value, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeGreaterThan(Long value) {
            addCriterion("last_exec_time >", value, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("last_exec_time >=", value, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeLessThan(Long value) {
            addCriterion("last_exec_time <", value, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeLessThanOrEqualTo(Long value) {
            addCriterion("last_exec_time <=", value, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeIn(List<Long> values) {
            addCriterion("last_exec_time in", values, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeNotIn(List<Long> values) {
            addCriterion("last_exec_time not in", values, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeBetween(Long value1, Long value2) {
            addCriterion("last_exec_time between", value1, value2, "lastExecTime");
            return (Criteria) this;
        }

        public Criteria andLastExecTimeNotBetween(Long value1, Long value2) {
            addCriterion("last_exec_time not between", value1, value2, "lastExecTime");
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