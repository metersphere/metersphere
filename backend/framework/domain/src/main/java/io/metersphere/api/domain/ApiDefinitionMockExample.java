package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiDefinitionMockExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiDefinitionMockExample() {
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

        public Criteria andApiPathIsNull() {
            addCriterion("api_path is null");
            return (Criteria) this;
        }

        public Criteria andApiPathIsNotNull() {
            addCriterion("api_path is not null");
            return (Criteria) this;
        }

        public Criteria andApiPathEqualTo(String value) {
            addCriterion("api_path =", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathNotEqualTo(String value) {
            addCriterion("api_path <>", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathGreaterThan(String value) {
            addCriterion("api_path >", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathGreaterThanOrEqualTo(String value) {
            addCriterion("api_path >=", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathLessThan(String value) {
            addCriterion("api_path <", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathLessThanOrEqualTo(String value) {
            addCriterion("api_path <=", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathLike(String value) {
            addCriterion("api_path like", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathNotLike(String value) {
            addCriterion("api_path not like", value, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathIn(List<String> values) {
            addCriterion("api_path in", values, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathNotIn(List<String> values) {
            addCriterion("api_path not in", values, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathBetween(String value1, String value2) {
            addCriterion("api_path between", value1, value2, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiPathNotBetween(String value1, String value2) {
            addCriterion("api_path not between", value1, value2, "apiPath");
            return (Criteria) this;
        }

        public Criteria andApiMethodIsNull() {
            addCriterion("api_method is null");
            return (Criteria) this;
        }

        public Criteria andApiMethodIsNotNull() {
            addCriterion("api_method is not null");
            return (Criteria) this;
        }

        public Criteria andApiMethodEqualTo(String value) {
            addCriterion("api_method =", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodNotEqualTo(String value) {
            addCriterion("api_method <>", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodGreaterThan(String value) {
            addCriterion("api_method >", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodGreaterThanOrEqualTo(String value) {
            addCriterion("api_method >=", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodLessThan(String value) {
            addCriterion("api_method <", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodLessThanOrEqualTo(String value) {
            addCriterion("api_method <=", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodLike(String value) {
            addCriterion("api_method like", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodNotLike(String value) {
            addCriterion("api_method not like", value, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodIn(List<String> values) {
            addCriterion("api_method in", values, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodNotIn(List<String> values) {
            addCriterion("api_method not in", values, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodBetween(String value1, String value2) {
            addCriterion("api_method between", value1, value2, "apiMethod");
            return (Criteria) this;
        }

        public Criteria andApiMethodNotBetween(String value1, String value2) {
            addCriterion("api_method not between", value1, value2, "apiMethod");
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

        public Criteria andTagsIsNull() {
            addCriterion("tags is null");
            return (Criteria) this;
        }

        public Criteria andTagsIsNotNull() {
            addCriterion("tags is not null");
            return (Criteria) this;
        }

        public Criteria andTagsEqualTo(String value) {
            addCriterion("tags =", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsNotEqualTo(String value) {
            addCriterion("tags <>", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsGreaterThan(String value) {
            addCriterion("tags >", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsGreaterThanOrEqualTo(String value) {
            addCriterion("tags >=", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsLessThan(String value) {
            addCriterion("tags <", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsLessThanOrEqualTo(String value) {
            addCriterion("tags <=", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsLike(String value) {
            addCriterion("tags like", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsNotLike(String value) {
            addCriterion("tags not like", value, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsIn(List<String> values) {
            addCriterion("tags in", values, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsNotIn(List<String> values) {
            addCriterion("tags not in", values, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsBetween(String value1, String value2) {
            addCriterion("tags between", value1, value2, "tags");
            return (Criteria) this;
        }

        public Criteria andTagsNotBetween(String value1, String value2) {
            addCriterion("tags not between", value1, value2, "tags");
            return (Criteria) this;
        }

        public Criteria andEnableIsNull() {
            addCriterion("`enable` is null");
            return (Criteria) this;
        }

        public Criteria andEnableIsNotNull() {
            addCriterion("`enable` is not null");
            return (Criteria) this;
        }

        public Criteria andEnableEqualTo(Boolean value) {
            addCriterion("`enable` =", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableNotEqualTo(Boolean value) {
            addCriterion("`enable` <>", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableGreaterThan(Boolean value) {
            addCriterion("`enable` >", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("`enable` >=", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableLessThan(Boolean value) {
            addCriterion("`enable` <", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableLessThanOrEqualTo(Boolean value) {
            addCriterion("`enable` <=", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableIn(List<Boolean> values) {
            addCriterion("`enable` in", values, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableNotIn(List<Boolean> values) {
            addCriterion("`enable` not in", values, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableBetween(Boolean value1, Boolean value2) {
            addCriterion("`enable` between", value1, value2, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("`enable` not between", value1, value2, "enable");
            return (Criteria) this;
        }

        public Criteria andExpectNumIsNull() {
            addCriterion("expect_num is null");
            return (Criteria) this;
        }

        public Criteria andExpectNumIsNotNull() {
            addCriterion("expect_num is not null");
            return (Criteria) this;
        }

        public Criteria andExpectNumEqualTo(String value) {
            addCriterion("expect_num =", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumNotEqualTo(String value) {
            addCriterion("expect_num <>", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumGreaterThan(String value) {
            addCriterion("expect_num >", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumGreaterThanOrEqualTo(String value) {
            addCriterion("expect_num >=", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumLessThan(String value) {
            addCriterion("expect_num <", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumLessThanOrEqualTo(String value) {
            addCriterion("expect_num <=", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumLike(String value) {
            addCriterion("expect_num like", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumNotLike(String value) {
            addCriterion("expect_num not like", value, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumIn(List<String> values) {
            addCriterion("expect_num in", values, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumNotIn(List<String> values) {
            addCriterion("expect_num not in", values, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumBetween(String value1, String value2) {
            addCriterion("expect_num between", value1, value2, "expectNum");
            return (Criteria) this;
        }

        public Criteria andExpectNumNotBetween(String value1, String value2) {
            addCriterion("expect_num not between", value1, value2, "expectNum");
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

        public Criteria andApiDefinitionIdIsNull() {
            addCriterion("api_definition_id is null");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdIsNotNull() {
            addCriterion("api_definition_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdEqualTo(String value) {
            addCriterion("api_definition_id =", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdNotEqualTo(String value) {
            addCriterion("api_definition_id <>", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdGreaterThan(String value) {
            addCriterion("api_definition_id >", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_definition_id >=", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdLessThan(String value) {
            addCriterion("api_definition_id <", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdLessThanOrEqualTo(String value) {
            addCriterion("api_definition_id <=", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdLike(String value) {
            addCriterion("api_definition_id like", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdNotLike(String value) {
            addCriterion("api_definition_id not like", value, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdIn(List<String> values) {
            addCriterion("api_definition_id in", values, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdNotIn(List<String> values) {
            addCriterion("api_definition_id not in", values, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdBetween(String value1, String value2) {
            addCriterion("api_definition_id between", value1, value2, "apiDefinitionId");
            return (Criteria) this;
        }

        public Criteria andApiDefinitionIdNotBetween(String value1, String value2) {
            addCriterion("api_definition_id not between", value1, value2, "apiDefinitionId");
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