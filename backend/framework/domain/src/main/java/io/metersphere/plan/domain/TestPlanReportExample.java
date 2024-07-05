package io.metersphere.plan.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestPlanReportExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanReportExample() {
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

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(Long value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(Long value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(Long value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(Long value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(Long value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<Long> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<Long> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(Long value1, Long value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(Long value1, Long value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return (Criteria) this;
        }

        public Criteria andEndTimeEqualTo(Long value) {
            addCriterion("end_time =", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotEqualTo(Long value) {
            addCriterion("end_time <>", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThan(Long value) {
            addCriterion("end_time >", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("end_time >=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThan(Long value) {
            addCriterion("end_time <", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(Long value) {
            addCriterion("end_time <=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIn(List<Long> values) {
            addCriterion("end_time in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotIn(List<Long> values) {
            addCriterion("end_time not in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeBetween(Long value1, Long value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotBetween(Long value1, Long value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andExecStatusIsNull() {
            addCriterion("exec_status is null");
            return (Criteria) this;
        }

        public Criteria andExecStatusIsNotNull() {
            addCriterion("exec_status is not null");
            return (Criteria) this;
        }

        public Criteria andExecStatusEqualTo(String value) {
            addCriterion("exec_status =", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusNotEqualTo(String value) {
            addCriterion("exec_status <>", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusGreaterThan(String value) {
            addCriterion("exec_status >", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusGreaterThanOrEqualTo(String value) {
            addCriterion("exec_status >=", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusLessThan(String value) {
            addCriterion("exec_status <", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusLessThanOrEqualTo(String value) {
            addCriterion("exec_status <=", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusLike(String value) {
            addCriterion("exec_status like", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusNotLike(String value) {
            addCriterion("exec_status not like", value, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusIn(List<String> values) {
            addCriterion("exec_status in", values, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusNotIn(List<String> values) {
            addCriterion("exec_status not in", values, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusBetween(String value1, String value2) {
            addCriterion("exec_status between", value1, value2, "execStatus");
            return (Criteria) this;
        }

        public Criteria andExecStatusNotBetween(String value1, String value2) {
            addCriterion("exec_status not between", value1, value2, "execStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusIsNull() {
            addCriterion("result_status is null");
            return (Criteria) this;
        }

        public Criteria andResultStatusIsNotNull() {
            addCriterion("result_status is not null");
            return (Criteria) this;
        }

        public Criteria andResultStatusEqualTo(String value) {
            addCriterion("result_status =", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusNotEqualTo(String value) {
            addCriterion("result_status <>", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusGreaterThan(String value) {
            addCriterion("result_status >", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusGreaterThanOrEqualTo(String value) {
            addCriterion("result_status >=", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusLessThan(String value) {
            addCriterion("result_status <", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusLessThanOrEqualTo(String value) {
            addCriterion("result_status <=", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusLike(String value) {
            addCriterion("result_status like", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusNotLike(String value) {
            addCriterion("result_status not like", value, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusIn(List<String> values) {
            addCriterion("result_status in", values, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusNotIn(List<String> values) {
            addCriterion("result_status not in", values, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusBetween(String value1, String value2) {
            addCriterion("result_status between", value1, value2, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andResultStatusNotBetween(String value1, String value2) {
            addCriterion("result_status not between", value1, value2, "resultStatus");
            return (Criteria) this;
        }

        public Criteria andPassRateIsNull() {
            addCriterion("pass_rate is null");
            return (Criteria) this;
        }

        public Criteria andPassRateIsNotNull() {
            addCriterion("pass_rate is not null");
            return (Criteria) this;
        }

        public Criteria andPassRateEqualTo(BigDecimal value) {
            addCriterion("pass_rate =", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotEqualTo(BigDecimal value) {
            addCriterion("pass_rate <>", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThan(BigDecimal value) {
            addCriterion("pass_rate >", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("pass_rate >=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThan(BigDecimal value) {
            addCriterion("pass_rate <", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("pass_rate <=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateIn(List<BigDecimal> values) {
            addCriterion("pass_rate in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotIn(List<BigDecimal> values) {
            addCriterion("pass_rate not in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pass_rate between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pass_rate not between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andTriggerModeIsNull() {
            addCriterion("trigger_mode is null");
            return (Criteria) this;
        }

        public Criteria andTriggerModeIsNotNull() {
            addCriterion("trigger_mode is not null");
            return (Criteria) this;
        }

        public Criteria andTriggerModeEqualTo(String value) {
            addCriterion("trigger_mode =", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeNotEqualTo(String value) {
            addCriterion("trigger_mode <>", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeGreaterThan(String value) {
            addCriterion("trigger_mode >", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeGreaterThanOrEqualTo(String value) {
            addCriterion("trigger_mode >=", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeLessThan(String value) {
            addCriterion("trigger_mode <", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeLessThanOrEqualTo(String value) {
            addCriterion("trigger_mode <=", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeLike(String value) {
            addCriterion("trigger_mode like", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeNotLike(String value) {
            addCriterion("trigger_mode not like", value, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeIn(List<String> values) {
            addCriterion("trigger_mode in", values, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeNotIn(List<String> values) {
            addCriterion("trigger_mode not in", values, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeBetween(String value1, String value2) {
            addCriterion("trigger_mode between", value1, value2, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andTriggerModeNotBetween(String value1, String value2) {
            addCriterion("trigger_mode not between", value1, value2, "triggerMode");
            return (Criteria) this;
        }

        public Criteria andPassThresholdIsNull() {
            addCriterion("pass_threshold is null");
            return (Criteria) this;
        }

        public Criteria andPassThresholdIsNotNull() {
            addCriterion("pass_threshold is not null");
            return (Criteria) this;
        }

        public Criteria andPassThresholdEqualTo(BigDecimal value) {
            addCriterion("pass_threshold =", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdNotEqualTo(BigDecimal value) {
            addCriterion("pass_threshold <>", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdGreaterThan(BigDecimal value) {
            addCriterion("pass_threshold >", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("pass_threshold >=", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdLessThan(BigDecimal value) {
            addCriterion("pass_threshold <", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdLessThanOrEqualTo(BigDecimal value) {
            addCriterion("pass_threshold <=", value, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdIn(List<BigDecimal> values) {
            addCriterion("pass_threshold in", values, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdNotIn(List<BigDecimal> values) {
            addCriterion("pass_threshold not in", values, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pass_threshold between", value1, value2, "passThreshold");
            return (Criteria) this;
        }

        public Criteria andPassThresholdNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pass_threshold not between", value1, value2, "passThreshold");
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

        public Criteria andIntegratedIsNull() {
            addCriterion("integrated is null");
            return (Criteria) this;
        }

        public Criteria andIntegratedIsNotNull() {
            addCriterion("integrated is not null");
            return (Criteria) this;
        }

        public Criteria andIntegratedEqualTo(Boolean value) {
            addCriterion("integrated =", value, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedNotEqualTo(Boolean value) {
            addCriterion("integrated <>", value, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedGreaterThan(Boolean value) {
            addCriterion("integrated >", value, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("integrated >=", value, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedLessThan(Boolean value) {
            addCriterion("integrated <", value, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedLessThanOrEqualTo(Boolean value) {
            addCriterion("integrated <=", value, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedIn(List<Boolean> values) {
            addCriterion("integrated in", values, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedNotIn(List<Boolean> values) {
            addCriterion("integrated not in", values, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedBetween(Boolean value1, Boolean value2) {
            addCriterion("integrated between", value1, value2, "integrated");
            return (Criteria) this;
        }

        public Criteria andIntegratedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("integrated not between", value1, value2, "integrated");
            return (Criteria) this;
        }

        public Criteria andDeletedIsNull() {
            addCriterion("deleted is null");
            return (Criteria) this;
        }

        public Criteria andDeletedIsNotNull() {
            addCriterion("deleted is not null");
            return (Criteria) this;
        }

        public Criteria andDeletedEqualTo(Boolean value) {
            addCriterion("deleted =", value, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedNotEqualTo(Boolean value) {
            addCriterion("deleted <>", value, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedGreaterThan(Boolean value) {
            addCriterion("deleted >", value, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("deleted >=", value, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedLessThan(Boolean value) {
            addCriterion("deleted <", value, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedLessThanOrEqualTo(Boolean value) {
            addCriterion("deleted <=", value, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedIn(List<Boolean> values) {
            addCriterion("deleted in", values, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedNotIn(List<Boolean> values) {
            addCriterion("deleted not in", values, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedBetween(Boolean value1, Boolean value2) {
            addCriterion("deleted between", value1, value2, "deleted");
            return (Criteria) this;
        }

        public Criteria andDeletedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("deleted not between", value1, value2, "deleted");
            return (Criteria) this;
        }

        public Criteria andExecuteRateIsNull() {
            addCriterion("execute_rate is null");
            return (Criteria) this;
        }

        public Criteria andExecuteRateIsNotNull() {
            addCriterion("execute_rate is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteRateEqualTo(BigDecimal value) {
            addCriterion("execute_rate =", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateNotEqualTo(BigDecimal value) {
            addCriterion("execute_rate <>", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateGreaterThan(BigDecimal value) {
            addCriterion("execute_rate >", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("execute_rate >=", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateLessThan(BigDecimal value) {
            addCriterion("execute_rate <", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateLessThanOrEqualTo(BigDecimal value) {
            addCriterion("execute_rate <=", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateIn(List<BigDecimal> values) {
            addCriterion("execute_rate in", values, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateNotIn(List<BigDecimal> values) {
            addCriterion("execute_rate not in", values, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("execute_rate between", value1, value2, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("execute_rate not between", value1, value2, "executeRate");
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

        public Criteria andDefaultLayoutIsNull() {
            addCriterion("default_layout is null");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutIsNotNull() {
            addCriterion("default_layout is not null");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutEqualTo(Boolean value) {
            addCriterion("default_layout =", value, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutNotEqualTo(Boolean value) {
            addCriterion("default_layout <>", value, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutGreaterThan(Boolean value) {
            addCriterion("default_layout >", value, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutGreaterThanOrEqualTo(Boolean value) {
            addCriterion("default_layout >=", value, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutLessThan(Boolean value) {
            addCriterion("default_layout <", value, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutLessThanOrEqualTo(Boolean value) {
            addCriterion("default_layout <=", value, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutIn(List<Boolean> values) {
            addCriterion("default_layout in", values, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutNotIn(List<Boolean> values) {
            addCriterion("default_layout not in", values, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutBetween(Boolean value1, Boolean value2) {
            addCriterion("default_layout between", value1, value2, "defaultLayout");
            return (Criteria) this;
        }

        public Criteria andDefaultLayoutNotBetween(Boolean value1, Boolean value2) {
            addCriterion("default_layout not between", value1, value2, "defaultLayout");
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