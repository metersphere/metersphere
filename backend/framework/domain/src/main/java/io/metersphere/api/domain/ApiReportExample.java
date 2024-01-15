package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiReportExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiReportExample() {
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

        public Criteria andResourceIdIsNull() {
            addCriterion("resource_id is null");
            return (Criteria) this;
        }

        public Criteria andResourceIdIsNotNull() {
            addCriterion("resource_id is not null");
            return (Criteria) this;
        }

        public Criteria andResourceIdEqualTo(String value) {
            addCriterion("resource_id =", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotEqualTo(String value) {
            addCriterion("resource_id <>", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdGreaterThan(String value) {
            addCriterion("resource_id >", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdGreaterThanOrEqualTo(String value) {
            addCriterion("resource_id >=", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdLessThan(String value) {
            addCriterion("resource_id <", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdLessThanOrEqualTo(String value) {
            addCriterion("resource_id <=", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdLike(String value) {
            addCriterion("resource_id like", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotLike(String value) {
            addCriterion("resource_id not like", value, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdIn(List<String> values) {
            addCriterion("resource_id in", values, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotIn(List<String> values) {
            addCriterion("resource_id not in", values, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdBetween(String value1, String value2) {
            addCriterion("resource_id between", value1, value2, "resourceId");
            return (Criteria) this;
        }

        public Criteria andResourceIdNotBetween(String value1, String value2) {
            addCriterion("resource_id not between", value1, value2, "resourceId");
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

        public Criteria andDeleteTimeIsNull() {
            addCriterion("delete_time is null");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeIsNotNull() {
            addCriterion("delete_time is not null");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeEqualTo(Long value) {
            addCriterion("delete_time =", value, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeNotEqualTo(Long value) {
            addCriterion("delete_time <>", value, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeGreaterThan(Long value) {
            addCriterion("delete_time >", value, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("delete_time >=", value, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeLessThan(Long value) {
            addCriterion("delete_time <", value, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeLessThanOrEqualTo(Long value) {
            addCriterion("delete_time <=", value, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeIn(List<Long> values) {
            addCriterion("delete_time in", values, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeNotIn(List<Long> values) {
            addCriterion("delete_time not in", values, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeBetween(Long value1, Long value2) {
            addCriterion("delete_time between", value1, value2, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteTimeNotBetween(Long value1, Long value2) {
            addCriterion("delete_time not between", value1, value2, "deleteTime");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIsNull() {
            addCriterion("delete_user is null");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIsNotNull() {
            addCriterion("delete_user is not null");
            return (Criteria) this;
        }

        public Criteria andDeleteUserEqualTo(String value) {
            addCriterion("delete_user =", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserNotEqualTo(String value) {
            addCriterion("delete_user <>", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserGreaterThan(String value) {
            addCriterion("delete_user >", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserGreaterThanOrEqualTo(String value) {
            addCriterion("delete_user >=", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserLessThan(String value) {
            addCriterion("delete_user <", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserLessThanOrEqualTo(String value) {
            addCriterion("delete_user <=", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserLike(String value) {
            addCriterion("delete_user like", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserNotLike(String value) {
            addCriterion("delete_user not like", value, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIn(List<String> values) {
            addCriterion("delete_user in", values, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserNotIn(List<String> values) {
            addCriterion("delete_user not in", values, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserBetween(String value1, String value2) {
            addCriterion("delete_user between", value1, value2, "deleteUser");
            return (Criteria) this;
        }

        public Criteria andDeleteUserNotBetween(String value1, String value2) {
            addCriterion("delete_user not between", value1, value2, "deleteUser");
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

        public Criteria andUpdateUserIsNull() {
            addCriterion("update_user is null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIsNotNull() {
            addCriterion("update_user is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserEqualTo(String value) {
            addCriterion("update_user =", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotEqualTo(String value) {
            addCriterion("update_user <>", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThan(String value) {
            addCriterion("update_user >", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThanOrEqualTo(String value) {
            addCriterion("update_user >=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThan(String value) {
            addCriterion("update_user <", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThanOrEqualTo(String value) {
            addCriterion("update_user <=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLike(String value) {
            addCriterion("update_user like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotLike(String value) {
            addCriterion("update_user not like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIn(List<String> values) {
            addCriterion("update_user in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotIn(List<String> values) {
            addCriterion("update_user not in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserBetween(String value1, String value2) {
            addCriterion("update_user between", value1, value2, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotBetween(String value1, String value2) {
            addCriterion("update_user not between", value1, value2, "updateUser");
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

        public Criteria andRequestDurationIsNull() {
            addCriterion("request_duration is null");
            return (Criteria) this;
        }

        public Criteria andRequestDurationIsNotNull() {
            addCriterion("request_duration is not null");
            return (Criteria) this;
        }

        public Criteria andRequestDurationEqualTo(Long value) {
            addCriterion("request_duration =", value, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationNotEqualTo(Long value) {
            addCriterion("request_duration <>", value, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationGreaterThan(Long value) {
            addCriterion("request_duration >", value, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationGreaterThanOrEqualTo(Long value) {
            addCriterion("request_duration >=", value, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationLessThan(Long value) {
            addCriterion("request_duration <", value, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationLessThanOrEqualTo(Long value) {
            addCriterion("request_duration <=", value, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationIn(List<Long> values) {
            addCriterion("request_duration in", values, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationNotIn(List<Long> values) {
            addCriterion("request_duration not in", values, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationBetween(Long value1, Long value2) {
            addCriterion("request_duration between", value1, value2, "requestDuration");
            return (Criteria) this;
        }

        public Criteria andRequestDurationNotBetween(Long value1, Long value2) {
            addCriterion("request_duration not between", value1, value2, "requestDuration");
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

        public Criteria andRunModeIsNull() {
            addCriterion("run_mode is null");
            return (Criteria) this;
        }

        public Criteria andRunModeIsNotNull() {
            addCriterion("run_mode is not null");
            return (Criteria) this;
        }

        public Criteria andRunModeEqualTo(String value) {
            addCriterion("run_mode =", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeNotEqualTo(String value) {
            addCriterion("run_mode <>", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeGreaterThan(String value) {
            addCriterion("run_mode >", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeGreaterThanOrEqualTo(String value) {
            addCriterion("run_mode >=", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeLessThan(String value) {
            addCriterion("run_mode <", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeLessThanOrEqualTo(String value) {
            addCriterion("run_mode <=", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeLike(String value) {
            addCriterion("run_mode like", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeNotLike(String value) {
            addCriterion("run_mode not like", value, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeIn(List<String> values) {
            addCriterion("run_mode in", values, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeNotIn(List<String> values) {
            addCriterion("run_mode not in", values, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeBetween(String value1, String value2) {
            addCriterion("run_mode between", value1, value2, "runMode");
            return (Criteria) this;
        }

        public Criteria andRunModeNotBetween(String value1, String value2) {
            addCriterion("run_mode not between", value1, value2, "runMode");
            return (Criteria) this;
        }

        public Criteria andPoolIdIsNull() {
            addCriterion("pool_id is null");
            return (Criteria) this;
        }

        public Criteria andPoolIdIsNotNull() {
            addCriterion("pool_id is not null");
            return (Criteria) this;
        }

        public Criteria andPoolIdEqualTo(String value) {
            addCriterion("pool_id =", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdNotEqualTo(String value) {
            addCriterion("pool_id <>", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdGreaterThan(String value) {
            addCriterion("pool_id >", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdGreaterThanOrEqualTo(String value) {
            addCriterion("pool_id >=", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdLessThan(String value) {
            addCriterion("pool_id <", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdLessThanOrEqualTo(String value) {
            addCriterion("pool_id <=", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdLike(String value) {
            addCriterion("pool_id like", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdNotLike(String value) {
            addCriterion("pool_id not like", value, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdIn(List<String> values) {
            addCriterion("pool_id in", values, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdNotIn(List<String> values) {
            addCriterion("pool_id not in", values, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdBetween(String value1, String value2) {
            addCriterion("pool_id between", value1, value2, "poolId");
            return (Criteria) this;
        }

        public Criteria andPoolIdNotBetween(String value1, String value2) {
            addCriterion("pool_id not between", value1, value2, "poolId");
            return (Criteria) this;
        }

        public Criteria andVersionIdIsNull() {
            addCriterion("version_id is null");
            return (Criteria) this;
        }

        public Criteria andVersionIdIsNotNull() {
            addCriterion("version_id is not null");
            return (Criteria) this;
        }

        public Criteria andVersionIdEqualTo(String value) {
            addCriterion("version_id =", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdNotEqualTo(String value) {
            addCriterion("version_id <>", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdGreaterThan(String value) {
            addCriterion("version_id >", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdGreaterThanOrEqualTo(String value) {
            addCriterion("version_id >=", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdLessThan(String value) {
            addCriterion("version_id <", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdLessThanOrEqualTo(String value) {
            addCriterion("version_id <=", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdLike(String value) {
            addCriterion("version_id like", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdNotLike(String value) {
            addCriterion("version_id not like", value, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdIn(List<String> values) {
            addCriterion("version_id in", values, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdNotIn(List<String> values) {
            addCriterion("version_id not in", values, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdBetween(String value1, String value2) {
            addCriterion("version_id between", value1, value2, "versionId");
            return (Criteria) this;
        }

        public Criteria andVersionIdNotBetween(String value1, String value2) {
            addCriterion("version_id not between", value1, value2, "versionId");
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

        public Criteria andErrorCountIsNull() {
            addCriterion("error_count is null");
            return (Criteria) this;
        }

        public Criteria andErrorCountIsNotNull() {
            addCriterion("error_count is not null");
            return (Criteria) this;
        }

        public Criteria andErrorCountEqualTo(Long value) {
            addCriterion("error_count =", value, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountNotEqualTo(Long value) {
            addCriterion("error_count <>", value, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountGreaterThan(Long value) {
            addCriterion("error_count >", value, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountGreaterThanOrEqualTo(Long value) {
            addCriterion("error_count >=", value, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountLessThan(Long value) {
            addCriterion("error_count <", value, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountLessThanOrEqualTo(Long value) {
            addCriterion("error_count <=", value, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountIn(List<Long> values) {
            addCriterion("error_count in", values, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountNotIn(List<Long> values) {
            addCriterion("error_count not in", values, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountBetween(Long value1, Long value2) {
            addCriterion("error_count between", value1, value2, "errorCount");
            return (Criteria) this;
        }

        public Criteria andErrorCountNotBetween(Long value1, Long value2) {
            addCriterion("error_count not between", value1, value2, "errorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountIsNull() {
            addCriterion("fake_error_count is null");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountIsNotNull() {
            addCriterion("fake_error_count is not null");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountEqualTo(Long value) {
            addCriterion("fake_error_count =", value, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountNotEqualTo(Long value) {
            addCriterion("fake_error_count <>", value, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountGreaterThan(Long value) {
            addCriterion("fake_error_count >", value, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountGreaterThanOrEqualTo(Long value) {
            addCriterion("fake_error_count >=", value, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountLessThan(Long value) {
            addCriterion("fake_error_count <", value, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountLessThanOrEqualTo(Long value) {
            addCriterion("fake_error_count <=", value, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountIn(List<Long> values) {
            addCriterion("fake_error_count in", values, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountNotIn(List<Long> values) {
            addCriterion("fake_error_count not in", values, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountBetween(Long value1, Long value2) {
            addCriterion("fake_error_count between", value1, value2, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andFakeErrorCountNotBetween(Long value1, Long value2) {
            addCriterion("fake_error_count not between", value1, value2, "fakeErrorCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountIsNull() {
            addCriterion("pending_count is null");
            return (Criteria) this;
        }

        public Criteria andPendingCountIsNotNull() {
            addCriterion("pending_count is not null");
            return (Criteria) this;
        }

        public Criteria andPendingCountEqualTo(Long value) {
            addCriterion("pending_count =", value, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountNotEqualTo(Long value) {
            addCriterion("pending_count <>", value, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountGreaterThan(Long value) {
            addCriterion("pending_count >", value, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountGreaterThanOrEqualTo(Long value) {
            addCriterion("pending_count >=", value, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountLessThan(Long value) {
            addCriterion("pending_count <", value, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountLessThanOrEqualTo(Long value) {
            addCriterion("pending_count <=", value, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountIn(List<Long> values) {
            addCriterion("pending_count in", values, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountNotIn(List<Long> values) {
            addCriterion("pending_count not in", values, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountBetween(Long value1, Long value2) {
            addCriterion("pending_count between", value1, value2, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andPendingCountNotBetween(Long value1, Long value2) {
            addCriterion("pending_count not between", value1, value2, "pendingCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountIsNull() {
            addCriterion("success_count is null");
            return (Criteria) this;
        }

        public Criteria andSuccessCountIsNotNull() {
            addCriterion("success_count is not null");
            return (Criteria) this;
        }

        public Criteria andSuccessCountEqualTo(Long value) {
            addCriterion("success_count =", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountNotEqualTo(Long value) {
            addCriterion("success_count <>", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountGreaterThan(Long value) {
            addCriterion("success_count >", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountGreaterThanOrEqualTo(Long value) {
            addCriterion("success_count >=", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountLessThan(Long value) {
            addCriterion("success_count <", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountLessThanOrEqualTo(Long value) {
            addCriterion("success_count <=", value, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountIn(List<Long> values) {
            addCriterion("success_count in", values, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountNotIn(List<Long> values) {
            addCriterion("success_count not in", values, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountBetween(Long value1, Long value2) {
            addCriterion("success_count between", value1, value2, "successCount");
            return (Criteria) this;
        }

        public Criteria andSuccessCountNotBetween(Long value1, Long value2) {
            addCriterion("success_count not between", value1, value2, "successCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountIsNull() {
            addCriterion("assertion_count is null");
            return (Criteria) this;
        }

        public Criteria andAssertionCountIsNotNull() {
            addCriterion("assertion_count is not null");
            return (Criteria) this;
        }

        public Criteria andAssertionCountEqualTo(Long value) {
            addCriterion("assertion_count =", value, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountNotEqualTo(Long value) {
            addCriterion("assertion_count <>", value, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountGreaterThan(Long value) {
            addCriterion("assertion_count >", value, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountGreaterThanOrEqualTo(Long value) {
            addCriterion("assertion_count >=", value, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountLessThan(Long value) {
            addCriterion("assertion_count <", value, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountLessThanOrEqualTo(Long value) {
            addCriterion("assertion_count <=", value, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountIn(List<Long> values) {
            addCriterion("assertion_count in", values, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountNotIn(List<Long> values) {
            addCriterion("assertion_count not in", values, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountBetween(Long value1, Long value2) {
            addCriterion("assertion_count between", value1, value2, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andAssertionCountNotBetween(Long value1, Long value2) {
            addCriterion("assertion_count not between", value1, value2, "assertionCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountIsNull() {
            addCriterion("pass_assertions_count is null");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountIsNotNull() {
            addCriterion("pass_assertions_count is not null");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountEqualTo(Long value) {
            addCriterion("pass_assertions_count =", value, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountNotEqualTo(Long value) {
            addCriterion("pass_assertions_count <>", value, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountGreaterThan(Long value) {
            addCriterion("pass_assertions_count >", value, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountGreaterThanOrEqualTo(Long value) {
            addCriterion("pass_assertions_count >=", value, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountLessThan(Long value) {
            addCriterion("pass_assertions_count <", value, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountLessThanOrEqualTo(Long value) {
            addCriterion("pass_assertions_count <=", value, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountIn(List<Long> values) {
            addCriterion("pass_assertions_count in", values, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountNotIn(List<Long> values) {
            addCriterion("pass_assertions_count not in", values, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountBetween(Long value1, Long value2) {
            addCriterion("pass_assertions_count between", value1, value2, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andPassAssertionsCountNotBetween(Long value1, Long value2) {
            addCriterion("pass_assertions_count not between", value1, value2, "passAssertionsCount");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateIsNull() {
            addCriterion("request_execution_rate is null");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateIsNotNull() {
            addCriterion("request_execution_rate is not null");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateEqualTo(Long value) {
            addCriterion("request_execution_rate =", value, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateNotEqualTo(Long value) {
            addCriterion("request_execution_rate <>", value, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateGreaterThan(Long value) {
            addCriterion("request_execution_rate >", value, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateGreaterThanOrEqualTo(Long value) {
            addCriterion("request_execution_rate >=", value, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateLessThan(Long value) {
            addCriterion("request_execution_rate <", value, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateLessThanOrEqualTo(Long value) {
            addCriterion("request_execution_rate <=", value, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateIn(List<Long> values) {
            addCriterion("request_execution_rate in", values, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateNotIn(List<Long> values) {
            addCriterion("request_execution_rate not in", values, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateBetween(Long value1, Long value2) {
            addCriterion("request_execution_rate between", value1, value2, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestExecutionRateNotBetween(Long value1, Long value2) {
            addCriterion("request_execution_rate not between", value1, value2, "requestExecutionRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateIsNull() {
            addCriterion("request_approval_rate is null");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateIsNotNull() {
            addCriterion("request_approval_rate is not null");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateEqualTo(Long value) {
            addCriterion("request_approval_rate =", value, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateNotEqualTo(Long value) {
            addCriterion("request_approval_rate <>", value, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateGreaterThan(Long value) {
            addCriterion("request_approval_rate >", value, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateGreaterThanOrEqualTo(Long value) {
            addCriterion("request_approval_rate >=", value, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateLessThan(Long value) {
            addCriterion("request_approval_rate <", value, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateLessThanOrEqualTo(Long value) {
            addCriterion("request_approval_rate <=", value, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateIn(List<Long> values) {
            addCriterion("request_approval_rate in", values, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateNotIn(List<Long> values) {
            addCriterion("request_approval_rate not in", values, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateBetween(Long value1, Long value2) {
            addCriterion("request_approval_rate between", value1, value2, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andRequestApprovalRateNotBetween(Long value1, Long value2) {
            addCriterion("request_approval_rate not between", value1, value2, "requestApprovalRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateIsNull() {
            addCriterion("assertion_pass_rate is null");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateIsNotNull() {
            addCriterion("assertion_pass_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateEqualTo(Long value) {
            addCriterion("assertion_pass_rate =", value, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateNotEqualTo(Long value) {
            addCriterion("assertion_pass_rate <>", value, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateGreaterThan(Long value) {
            addCriterion("assertion_pass_rate >", value, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateGreaterThanOrEqualTo(Long value) {
            addCriterion("assertion_pass_rate >=", value, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateLessThan(Long value) {
            addCriterion("assertion_pass_rate <", value, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateLessThanOrEqualTo(Long value) {
            addCriterion("assertion_pass_rate <=", value, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateIn(List<Long> values) {
            addCriterion("assertion_pass_rate in", values, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateNotIn(List<Long> values) {
            addCriterion("assertion_pass_rate not in", values, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateBetween(Long value1, Long value2) {
            addCriterion("assertion_pass_rate between", value1, value2, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andAssertionPassRateNotBetween(Long value1, Long value2) {
            addCriterion("assertion_pass_rate not between", value1, value2, "assertionPassRate");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierIsNull() {
            addCriterion("script_identifier is null");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierIsNotNull() {
            addCriterion("script_identifier is not null");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierEqualTo(String value) {
            addCriterion("script_identifier =", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotEqualTo(String value) {
            addCriterion("script_identifier <>", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierGreaterThan(String value) {
            addCriterion("script_identifier >", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierGreaterThanOrEqualTo(String value) {
            addCriterion("script_identifier >=", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierLessThan(String value) {
            addCriterion("script_identifier <", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierLessThanOrEqualTo(String value) {
            addCriterion("script_identifier <=", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierLike(String value) {
            addCriterion("script_identifier like", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotLike(String value) {
            addCriterion("script_identifier not like", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierIn(List<String> values) {
            addCriterion("script_identifier in", values, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotIn(List<String> values) {
            addCriterion("script_identifier not in", values, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierBetween(String value1, String value2) {
            addCriterion("script_identifier between", value1, value2, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotBetween(String value1, String value2) {
            addCriterion("script_identifier not between", value1, value2, "scriptIdentifier");
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