package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class LoadTestReportExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LoadTestReportExample() {
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

        public Criteria andTestIdIsNull() {
            addCriterion("test_id is null");
            return (Criteria) this;
        }

        public Criteria andTestIdIsNotNull() {
            addCriterion("test_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestIdEqualTo(String value) {
            addCriterion("test_id =", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdNotEqualTo(String value) {
            addCriterion("test_id <>", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdGreaterThan(String value) {
            addCriterion("test_id >", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_id >=", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdLessThan(String value) {
            addCriterion("test_id <", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdLessThanOrEqualTo(String value) {
            addCriterion("test_id <=", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdLike(String value) {
            addCriterion("test_id like", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdNotLike(String value) {
            addCriterion("test_id not like", value, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdIn(List<String> values) {
            addCriterion("test_id in", values, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdNotIn(List<String> values) {
            addCriterion("test_id not in", values, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdBetween(String value1, String value2) {
            addCriterion("test_id between", value1, value2, "testId");
            return (Criteria) this;
        }

        public Criteria andTestIdNotBetween(String value1, String value2) {
            addCriterion("test_id not between", value1, value2, "testId");
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

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Long value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Long value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Long value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Long value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Long value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Long> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Long> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Long value1, Long value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Long value1, Long value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("`status` like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("`status` not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(String value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(String value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(String value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(String value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(String value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLike(String value) {
            addCriterion("user_id like", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotLike(String value) {
            addCriterion("user_id not like", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<String> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<String> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(String value1, String value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(String value1, String value2) {
            addCriterion("user_id not between", value1, value2, "userId");
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

        public Criteria andFileIdIsNull() {
            addCriterion("file_id is null");
            return (Criteria) this;
        }

        public Criteria andFileIdIsNotNull() {
            addCriterion("file_id is not null");
            return (Criteria) this;
        }

        public Criteria andFileIdEqualTo(String value) {
            addCriterion("file_id =", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotEqualTo(String value) {
            addCriterion("file_id <>", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThan(String value) {
            addCriterion("file_id >", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThanOrEqualTo(String value) {
            addCriterion("file_id >=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThan(String value) {
            addCriterion("file_id <", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThanOrEqualTo(String value) {
            addCriterion("file_id <=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLike(String value) {
            addCriterion("file_id like", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotLike(String value) {
            addCriterion("file_id not like", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdIn(List<String> values) {
            addCriterion("file_id in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotIn(List<String> values) {
            addCriterion("file_id not in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdBetween(String value1, String value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotBetween(String value1, String value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andMaxUsersIsNull() {
            addCriterion("max_users is null");
            return (Criteria) this;
        }

        public Criteria andMaxUsersIsNotNull() {
            addCriterion("max_users is not null");
            return (Criteria) this;
        }

        public Criteria andMaxUsersEqualTo(String value) {
            addCriterion("max_users =", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersNotEqualTo(String value) {
            addCriterion("max_users <>", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersGreaterThan(String value) {
            addCriterion("max_users >", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersGreaterThanOrEqualTo(String value) {
            addCriterion("max_users >=", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersLessThan(String value) {
            addCriterion("max_users <", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersLessThanOrEqualTo(String value) {
            addCriterion("max_users <=", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersLike(String value) {
            addCriterion("max_users like", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersNotLike(String value) {
            addCriterion("max_users not like", value, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersIn(List<String> values) {
            addCriterion("max_users in", values, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersNotIn(List<String> values) {
            addCriterion("max_users not in", values, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersBetween(String value1, String value2) {
            addCriterion("max_users between", value1, value2, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andMaxUsersNotBetween(String value1, String value2) {
            addCriterion("max_users not between", value1, value2, "maxUsers");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeIsNull() {
            addCriterion("avg_response_time is null");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeIsNotNull() {
            addCriterion("avg_response_time is not null");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeEqualTo(String value) {
            addCriterion("avg_response_time =", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeNotEqualTo(String value) {
            addCriterion("avg_response_time <>", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeGreaterThan(String value) {
            addCriterion("avg_response_time >", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeGreaterThanOrEqualTo(String value) {
            addCriterion("avg_response_time >=", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeLessThan(String value) {
            addCriterion("avg_response_time <", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeLessThanOrEqualTo(String value) {
            addCriterion("avg_response_time <=", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeLike(String value) {
            addCriterion("avg_response_time like", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeNotLike(String value) {
            addCriterion("avg_response_time not like", value, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeIn(List<String> values) {
            addCriterion("avg_response_time in", values, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeNotIn(List<String> values) {
            addCriterion("avg_response_time not in", values, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeBetween(String value1, String value2) {
            addCriterion("avg_response_time between", value1, value2, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andAvgResponseTimeNotBetween(String value1, String value2) {
            addCriterion("avg_response_time not between", value1, value2, "avgResponseTime");
            return (Criteria) this;
        }

        public Criteria andTpsIsNull() {
            addCriterion("tps is null");
            return (Criteria) this;
        }

        public Criteria andTpsIsNotNull() {
            addCriterion("tps is not null");
            return (Criteria) this;
        }

        public Criteria andTpsEqualTo(String value) {
            addCriterion("tps =", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsNotEqualTo(String value) {
            addCriterion("tps <>", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsGreaterThan(String value) {
            addCriterion("tps >", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsGreaterThanOrEqualTo(String value) {
            addCriterion("tps >=", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsLessThan(String value) {
            addCriterion("tps <", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsLessThanOrEqualTo(String value) {
            addCriterion("tps <=", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsLike(String value) {
            addCriterion("tps like", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsNotLike(String value) {
            addCriterion("tps not like", value, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsIn(List<String> values) {
            addCriterion("tps in", values, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsNotIn(List<String> values) {
            addCriterion("tps not in", values, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsBetween(String value1, String value2) {
            addCriterion("tps between", value1, value2, "tps");
            return (Criteria) this;
        }

        public Criteria andTpsNotBetween(String value1, String value2) {
            addCriterion("tps not between", value1, value2, "tps");
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

        public Criteria andTestNameIsNull() {
            addCriterion("test_name is null");
            return (Criteria) this;
        }

        public Criteria andTestNameIsNotNull() {
            addCriterion("test_name is not null");
            return (Criteria) this;
        }

        public Criteria andTestNameEqualTo(String value) {
            addCriterion("test_name =", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameNotEqualTo(String value) {
            addCriterion("test_name <>", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameGreaterThan(String value) {
            addCriterion("test_name >", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameGreaterThanOrEqualTo(String value) {
            addCriterion("test_name >=", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameLessThan(String value) {
            addCriterion("test_name <", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameLessThanOrEqualTo(String value) {
            addCriterion("test_name <=", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameLike(String value) {
            addCriterion("test_name like", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameNotLike(String value) {
            addCriterion("test_name not like", value, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameIn(List<String> values) {
            addCriterion("test_name in", values, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameNotIn(List<String> values) {
            addCriterion("test_name not in", values, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameBetween(String value1, String value2) {
            addCriterion("test_name between", value1, value2, "testName");
            return (Criteria) this;
        }

        public Criteria andTestNameNotBetween(String value1, String value2) {
            addCriterion("test_name not between", value1, value2, "testName");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeIsNull() {
            addCriterion("test_start_time is null");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeIsNotNull() {
            addCriterion("test_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeEqualTo(Long value) {
            addCriterion("test_start_time =", value, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeNotEqualTo(Long value) {
            addCriterion("test_start_time <>", value, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeGreaterThan(Long value) {
            addCriterion("test_start_time >", value, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("test_start_time >=", value, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeLessThan(Long value) {
            addCriterion("test_start_time <", value, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeLessThanOrEqualTo(Long value) {
            addCriterion("test_start_time <=", value, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeIn(List<Long> values) {
            addCriterion("test_start_time in", values, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeNotIn(List<Long> values) {
            addCriterion("test_start_time not in", values, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeBetween(Long value1, Long value2) {
            addCriterion("test_start_time between", value1, value2, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestStartTimeNotBetween(Long value1, Long value2) {
            addCriterion("test_start_time not between", value1, value2, "testStartTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeIsNull() {
            addCriterion("test_end_time is null");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeIsNotNull() {
            addCriterion("test_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeEqualTo(Long value) {
            addCriterion("test_end_time =", value, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeNotEqualTo(Long value) {
            addCriterion("test_end_time <>", value, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeGreaterThan(Long value) {
            addCriterion("test_end_time >", value, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("test_end_time >=", value, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeLessThan(Long value) {
            addCriterion("test_end_time <", value, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeLessThanOrEqualTo(Long value) {
            addCriterion("test_end_time <=", value, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeIn(List<Long> values) {
            addCriterion("test_end_time in", values, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeNotIn(List<Long> values) {
            addCriterion("test_end_time not in", values, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeBetween(Long value1, Long value2) {
            addCriterion("test_end_time between", value1, value2, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestEndTimeNotBetween(Long value1, Long value2) {
            addCriterion("test_end_time not between", value1, value2, "testEndTime");
            return (Criteria) this;
        }

        public Criteria andTestDurationIsNull() {
            addCriterion("test_duration is null");
            return (Criteria) this;
        }

        public Criteria andTestDurationIsNotNull() {
            addCriterion("test_duration is not null");
            return (Criteria) this;
        }

        public Criteria andTestDurationEqualTo(Long value) {
            addCriterion("test_duration =", value, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationNotEqualTo(Long value) {
            addCriterion("test_duration <>", value, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationGreaterThan(Long value) {
            addCriterion("test_duration >", value, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationGreaterThanOrEqualTo(Long value) {
            addCriterion("test_duration >=", value, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationLessThan(Long value) {
            addCriterion("test_duration <", value, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationLessThanOrEqualTo(Long value) {
            addCriterion("test_duration <=", value, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationIn(List<Long> values) {
            addCriterion("test_duration in", values, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationNotIn(List<Long> values) {
            addCriterion("test_duration not in", values, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationBetween(Long value1, Long value2) {
            addCriterion("test_duration between", value1, value2, "testDuration");
            return (Criteria) this;
        }

        public Criteria andTestDurationNotBetween(Long value1, Long value2) {
            addCriterion("test_duration not between", value1, value2, "testDuration");
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