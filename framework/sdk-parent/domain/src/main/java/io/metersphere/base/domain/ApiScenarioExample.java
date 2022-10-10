package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiScenarioExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiScenarioExample() {
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

        public Criteria andApiScenarioModuleIdIsNull() {
            addCriterion("api_scenario_module_id is null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdIsNotNull() {
            addCriterion("api_scenario_module_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdEqualTo(String value) {
            addCriterion("api_scenario_module_id =", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdNotEqualTo(String value) {
            addCriterion("api_scenario_module_id <>", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdGreaterThan(String value) {
            addCriterion("api_scenario_module_id >", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_scenario_module_id >=", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdLessThan(String value) {
            addCriterion("api_scenario_module_id <", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdLessThanOrEqualTo(String value) {
            addCriterion("api_scenario_module_id <=", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdLike(String value) {
            addCriterion("api_scenario_module_id like", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdNotLike(String value) {
            addCriterion("api_scenario_module_id not like", value, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdIn(List<String> values) {
            addCriterion("api_scenario_module_id in", values, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdNotIn(List<String> values) {
            addCriterion("api_scenario_module_id not in", values, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdBetween(String value1, String value2) {
            addCriterion("api_scenario_module_id between", value1, value2, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andApiScenarioModuleIdNotBetween(String value1, String value2) {
            addCriterion("api_scenario_module_id not between", value1, value2, "apiScenarioModuleId");
            return (Criteria) this;
        }

        public Criteria andModulePathIsNull() {
            addCriterion("module_path is null");
            return (Criteria) this;
        }

        public Criteria andModulePathIsNotNull() {
            addCriterion("module_path is not null");
            return (Criteria) this;
        }

        public Criteria andModulePathEqualTo(String value) {
            addCriterion("module_path =", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathNotEqualTo(String value) {
            addCriterion("module_path <>", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathGreaterThan(String value) {
            addCriterion("module_path >", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathGreaterThanOrEqualTo(String value) {
            addCriterion("module_path >=", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathLessThan(String value) {
            addCriterion("module_path <", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathLessThanOrEqualTo(String value) {
            addCriterion("module_path <=", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathLike(String value) {
            addCriterion("module_path like", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathNotLike(String value) {
            addCriterion("module_path not like", value, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathIn(List<String> values) {
            addCriterion("module_path in", values, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathNotIn(List<String> values) {
            addCriterion("module_path not in", values, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathBetween(String value1, String value2) {
            addCriterion("module_path between", value1, value2, "modulePath");
            return (Criteria) this;
        }

        public Criteria andModulePathNotBetween(String value1, String value2) {
            addCriterion("module_path not between", value1, value2, "modulePath");
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

        public Criteria andLevelIsNull() {
            addCriterion("`level` is null");
            return (Criteria) this;
        }

        public Criteria andLevelIsNotNull() {
            addCriterion("`level` is not null");
            return (Criteria) this;
        }

        public Criteria andLevelEqualTo(String value) {
            addCriterion("`level` =", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotEqualTo(String value) {
            addCriterion("`level` <>", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThan(String value) {
            addCriterion("`level` >", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThanOrEqualTo(String value) {
            addCriterion("`level` >=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThan(String value) {
            addCriterion("`level` <", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThanOrEqualTo(String value) {
            addCriterion("`level` <=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLike(String value) {
            addCriterion("`level` like", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotLike(String value) {
            addCriterion("`level` not like", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelIn(List<String> values) {
            addCriterion("`level` in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotIn(List<String> values) {
            addCriterion("`level` not in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelBetween(String value1, String value2) {
            addCriterion("`level` between", value1, value2, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotBetween(String value1, String value2) {
            addCriterion("`level` not between", value1, value2, "level");
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

        public Criteria andPrincipalIsNull() {
            addCriterion("principal is null");
            return (Criteria) this;
        }

        public Criteria andPrincipalIsNotNull() {
            addCriterion("principal is not null");
            return (Criteria) this;
        }

        public Criteria andPrincipalEqualTo(String value) {
            addCriterion("principal =", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotEqualTo(String value) {
            addCriterion("principal <>", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalGreaterThan(String value) {
            addCriterion("principal >", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalGreaterThanOrEqualTo(String value) {
            addCriterion("principal >=", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalLessThan(String value) {
            addCriterion("principal <", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalLessThanOrEqualTo(String value) {
            addCriterion("principal <=", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalLike(String value) {
            addCriterion("principal like", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotLike(String value) {
            addCriterion("principal not like", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalIn(List<String> values) {
            addCriterion("principal in", values, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotIn(List<String> values) {
            addCriterion("principal not in", values, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalBetween(String value1, String value2) {
            addCriterion("principal between", value1, value2, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotBetween(String value1, String value2) {
            addCriterion("principal not between", value1, value2, "principal");
            return (Criteria) this;
        }

        public Criteria andStepTotalIsNull() {
            addCriterion("step_total is null");
            return (Criteria) this;
        }

        public Criteria andStepTotalIsNotNull() {
            addCriterion("step_total is not null");
            return (Criteria) this;
        }

        public Criteria andStepTotalEqualTo(Integer value) {
            addCriterion("step_total =", value, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalNotEqualTo(Integer value) {
            addCriterion("step_total <>", value, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalGreaterThan(Integer value) {
            addCriterion("step_total >", value, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalGreaterThanOrEqualTo(Integer value) {
            addCriterion("step_total >=", value, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalLessThan(Integer value) {
            addCriterion("step_total <", value, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalLessThanOrEqualTo(Integer value) {
            addCriterion("step_total <=", value, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalIn(List<Integer> values) {
            addCriterion("step_total in", values, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalNotIn(List<Integer> values) {
            addCriterion("step_total not in", values, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalBetween(Integer value1, Integer value2) {
            addCriterion("step_total between", value1, value2, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andStepTotalNotBetween(Integer value1, Integer value2) {
            addCriterion("step_total not between", value1, value2, "stepTotal");
            return (Criteria) this;
        }

        public Criteria andScheduleIsNull() {
            addCriterion("schedule is null");
            return (Criteria) this;
        }

        public Criteria andScheduleIsNotNull() {
            addCriterion("schedule is not null");
            return (Criteria) this;
        }

        public Criteria andScheduleEqualTo(String value) {
            addCriterion("schedule =", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleNotEqualTo(String value) {
            addCriterion("schedule <>", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleGreaterThan(String value) {
            addCriterion("schedule >", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleGreaterThanOrEqualTo(String value) {
            addCriterion("schedule >=", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleLessThan(String value) {
            addCriterion("schedule <", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleLessThanOrEqualTo(String value) {
            addCriterion("schedule <=", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleLike(String value) {
            addCriterion("schedule like", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleNotLike(String value) {
            addCriterion("schedule not like", value, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleIn(List<String> values) {
            addCriterion("schedule in", values, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleNotIn(List<String> values) {
            addCriterion("schedule not in", values, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleBetween(String value1, String value2) {
            addCriterion("schedule between", value1, value2, "schedule");
            return (Criteria) this;
        }

        public Criteria andScheduleNotBetween(String value1, String value2) {
            addCriterion("schedule not between", value1, value2, "schedule");
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

        public Criteria andPassRateIsNull() {
            addCriterion("pass_rate is null");
            return (Criteria) this;
        }

        public Criteria andPassRateIsNotNull() {
            addCriterion("pass_rate is not null");
            return (Criteria) this;
        }

        public Criteria andPassRateEqualTo(String value) {
            addCriterion("pass_rate =", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotEqualTo(String value) {
            addCriterion("pass_rate <>", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThan(String value) {
            addCriterion("pass_rate >", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThanOrEqualTo(String value) {
            addCriterion("pass_rate >=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThan(String value) {
            addCriterion("pass_rate <", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThanOrEqualTo(String value) {
            addCriterion("pass_rate <=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLike(String value) {
            addCriterion("pass_rate like", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotLike(String value) {
            addCriterion("pass_rate not like", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateIn(List<String> values) {
            addCriterion("pass_rate in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotIn(List<String> values) {
            addCriterion("pass_rate not in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateBetween(String value1, String value2) {
            addCriterion("pass_rate between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotBetween(String value1, String value2) {
            addCriterion("pass_rate not between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andLastResultIsNull() {
            addCriterion("last_result is null");
            return (Criteria) this;
        }

        public Criteria andLastResultIsNotNull() {
            addCriterion("last_result is not null");
            return (Criteria) this;
        }

        public Criteria andLastResultEqualTo(String value) {
            addCriterion("last_result =", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultNotEqualTo(String value) {
            addCriterion("last_result <>", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultGreaterThan(String value) {
            addCriterion("last_result >", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultGreaterThanOrEqualTo(String value) {
            addCriterion("last_result >=", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultLessThan(String value) {
            addCriterion("last_result <", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultLessThanOrEqualTo(String value) {
            addCriterion("last_result <=", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultLike(String value) {
            addCriterion("last_result like", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultNotLike(String value) {
            addCriterion("last_result not like", value, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultIn(List<String> values) {
            addCriterion("last_result in", values, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultNotIn(List<String> values) {
            addCriterion("last_result not in", values, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultBetween(String value1, String value2) {
            addCriterion("last_result between", value1, value2, "lastResult");
            return (Criteria) this;
        }

        public Criteria andLastResultNotBetween(String value1, String value2) {
            addCriterion("last_result not between", value1, value2, "lastResult");
            return (Criteria) this;
        }

        public Criteria andReportIdIsNull() {
            addCriterion("report_id is null");
            return (Criteria) this;
        }

        public Criteria andReportIdIsNotNull() {
            addCriterion("report_id is not null");
            return (Criteria) this;
        }

        public Criteria andReportIdEqualTo(String value) {
            addCriterion("report_id =", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotEqualTo(String value) {
            addCriterion("report_id <>", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdGreaterThan(String value) {
            addCriterion("report_id >", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("report_id >=", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLessThan(String value) {
            addCriterion("report_id <", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLessThanOrEqualTo(String value) {
            addCriterion("report_id <=", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLike(String value) {
            addCriterion("report_id like", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotLike(String value) {
            addCriterion("report_id not like", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdIn(List<String> values) {
            addCriterion("report_id in", values, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotIn(List<String> values) {
            addCriterion("report_id not in", values, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdBetween(String value1, String value2) {
            addCriterion("report_id between", value1, value2, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotBetween(String value1, String value2) {
            addCriterion("report_id not between", value1, value2, "reportId");
            return (Criteria) this;
        }

        public Criteria andNumIsNull() {
            addCriterion("num is null");
            return (Criteria) this;
        }

        public Criteria andNumIsNotNull() {
            addCriterion("num is not null");
            return (Criteria) this;
        }

        public Criteria andNumEqualTo(Integer value) {
            addCriterion("num =", value, "num");
            return (Criteria) this;
        }

        public Criteria andNumNotEqualTo(Integer value) {
            addCriterion("num <>", value, "num");
            return (Criteria) this;
        }

        public Criteria andNumGreaterThan(Integer value) {
            addCriterion("num >", value, "num");
            return (Criteria) this;
        }

        public Criteria andNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("num >=", value, "num");
            return (Criteria) this;
        }

        public Criteria andNumLessThan(Integer value) {
            addCriterion("num <", value, "num");
            return (Criteria) this;
        }

        public Criteria andNumLessThanOrEqualTo(Integer value) {
            addCriterion("num <=", value, "num");
            return (Criteria) this;
        }

        public Criteria andNumIn(List<Integer> values) {
            addCriterion("num in", values, "num");
            return (Criteria) this;
        }

        public Criteria andNumNotIn(List<Integer> values) {
            addCriterion("num not in", values, "num");
            return (Criteria) this;
        }

        public Criteria andNumBetween(Integer value1, Integer value2) {
            addCriterion("num between", value1, value2, "num");
            return (Criteria) this;
        }

        public Criteria andNumNotBetween(Integer value1, Integer value2) {
            addCriterion("num not between", value1, value2, "num");
            return (Criteria) this;
        }

        public Criteria andOriginalStateIsNull() {
            addCriterion("original_state is null");
            return (Criteria) this;
        }

        public Criteria andOriginalStateIsNotNull() {
            addCriterion("original_state is not null");
            return (Criteria) this;
        }

        public Criteria andOriginalStateEqualTo(String value) {
            addCriterion("original_state =", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateNotEqualTo(String value) {
            addCriterion("original_state <>", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateGreaterThan(String value) {
            addCriterion("original_state >", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateGreaterThanOrEqualTo(String value) {
            addCriterion("original_state >=", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateLessThan(String value) {
            addCriterion("original_state <", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateLessThanOrEqualTo(String value) {
            addCriterion("original_state <=", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateLike(String value) {
            addCriterion("original_state like", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateNotLike(String value) {
            addCriterion("original_state not like", value, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateIn(List<String> values) {
            addCriterion("original_state in", values, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateNotIn(List<String> values) {
            addCriterion("original_state not in", values, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateBetween(String value1, String value2) {
            addCriterion("original_state between", value1, value2, "originalState");
            return (Criteria) this;
        }

        public Criteria andOriginalStateNotBetween(String value1, String value2) {
            addCriterion("original_state not between", value1, value2, "originalState");
            return (Criteria) this;
        }

        public Criteria andCustomNumIsNull() {
            addCriterion("custom_num is null");
            return (Criteria) this;
        }

        public Criteria andCustomNumIsNotNull() {
            addCriterion("custom_num is not null");
            return (Criteria) this;
        }

        public Criteria andCustomNumEqualTo(String value) {
            addCriterion("custom_num =", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumNotEqualTo(String value) {
            addCriterion("custom_num <>", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumGreaterThan(String value) {
            addCriterion("custom_num >", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumGreaterThanOrEqualTo(String value) {
            addCriterion("custom_num >=", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumLessThan(String value) {
            addCriterion("custom_num <", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumLessThanOrEqualTo(String value) {
            addCriterion("custom_num <=", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumLike(String value) {
            addCriterion("custom_num like", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumNotLike(String value) {
            addCriterion("custom_num not like", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumIn(List<String> values) {
            addCriterion("custom_num in", values, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumNotIn(List<String> values) {
            addCriterion("custom_num not in", values, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumBetween(String value1, String value2) {
            addCriterion("custom_num between", value1, value2, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumNotBetween(String value1, String value2) {
            addCriterion("custom_num not between", value1, value2, "customNum");
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

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Integer value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Integer value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Integer value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Integer value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Integer value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Integer> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Integer> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Integer value1, Integer value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("version not between", value1, value2, "version");
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

        public Criteria andDeleteUserIdIsNull() {
            addCriterion("delete_user_id is null");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdIsNotNull() {
            addCriterion("delete_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdEqualTo(String value) {
            addCriterion("delete_user_id =", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdNotEqualTo(String value) {
            addCriterion("delete_user_id <>", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdGreaterThan(String value) {
            addCriterion("delete_user_id >", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("delete_user_id >=", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdLessThan(String value) {
            addCriterion("delete_user_id <", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdLessThanOrEqualTo(String value) {
            addCriterion("delete_user_id <=", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdLike(String value) {
            addCriterion("delete_user_id like", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdNotLike(String value) {
            addCriterion("delete_user_id not like", value, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdIn(List<String> values) {
            addCriterion("delete_user_id in", values, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdNotIn(List<String> values) {
            addCriterion("delete_user_id not in", values, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdBetween(String value1, String value2) {
            addCriterion("delete_user_id between", value1, value2, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andDeleteUserIdNotBetween(String value1, String value2) {
            addCriterion("delete_user_id not between", value1, value2, "deleteUserId");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesIsNull() {
            addCriterion("execute_times is null");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesIsNotNull() {
            addCriterion("execute_times is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesEqualTo(Integer value) {
            addCriterion("execute_times =", value, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesNotEqualTo(Integer value) {
            addCriterion("execute_times <>", value, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesGreaterThan(Integer value) {
            addCriterion("execute_times >", value, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("execute_times >=", value, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesLessThan(Integer value) {
            addCriterion("execute_times <", value, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesLessThanOrEqualTo(Integer value) {
            addCriterion("execute_times <=", value, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesIn(List<Integer> values) {
            addCriterion("execute_times in", values, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesNotIn(List<Integer> values) {
            addCriterion("execute_times not in", values, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesBetween(Integer value1, Integer value2) {
            addCriterion("execute_times between", value1, value2, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("execute_times not between", value1, value2, "executeTimes");
            return (Criteria) this;
        }

        public Criteria andOrderIsNull() {
            addCriterion("`order` is null");
            return (Criteria) this;
        }

        public Criteria andOrderIsNotNull() {
            addCriterion("`order` is not null");
            return (Criteria) this;
        }

        public Criteria andOrderEqualTo(Long value) {
            addCriterion("`order` =", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotEqualTo(Long value) {
            addCriterion("`order` <>", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderGreaterThan(Long value) {
            addCriterion("`order` >", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderGreaterThanOrEqualTo(Long value) {
            addCriterion("`order` >=", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLessThan(Long value) {
            addCriterion("`order` <", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLessThanOrEqualTo(Long value) {
            addCriterion("`order` <=", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderIn(List<Long> values) {
            addCriterion("`order` in", values, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotIn(List<Long> values) {
            addCriterion("`order` not in", values, "order");
            return (Criteria) this;
        }

        public Criteria andOrderBetween(Long value1, Long value2) {
            addCriterion("`order` between", value1, value2, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotBetween(Long value1, Long value2) {
            addCriterion("`order` not between", value1, value2, "order");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeIsNull() {
            addCriterion("environment_type is null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeIsNotNull() {
            addCriterion("environment_type is not null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeEqualTo(String value) {
            addCriterion("environment_type =", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeNotEqualTo(String value) {
            addCriterion("environment_type <>", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeGreaterThan(String value) {
            addCriterion("environment_type >", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeGreaterThanOrEqualTo(String value) {
            addCriterion("environment_type >=", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeLessThan(String value) {
            addCriterion("environment_type <", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeLessThanOrEqualTo(String value) {
            addCriterion("environment_type <=", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeLike(String value) {
            addCriterion("environment_type like", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeNotLike(String value) {
            addCriterion("environment_type not like", value, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeIn(List<String> values) {
            addCriterion("environment_type in", values, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeNotIn(List<String> values) {
            addCriterion("environment_type not in", values, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeBetween(String value1, String value2) {
            addCriterion("environment_type between", value1, value2, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentTypeNotBetween(String value1, String value2) {
            addCriterion("environment_type not between", value1, value2, "environmentType");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdIsNull() {
            addCriterion("environment_group_id is null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdIsNotNull() {
            addCriterion("environment_group_id is not null");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdEqualTo(String value) {
            addCriterion("environment_group_id =", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdNotEqualTo(String value) {
            addCriterion("environment_group_id <>", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdGreaterThan(String value) {
            addCriterion("environment_group_id >", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdGreaterThanOrEqualTo(String value) {
            addCriterion("environment_group_id >=", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdLessThan(String value) {
            addCriterion("environment_group_id <", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdLessThanOrEqualTo(String value) {
            addCriterion("environment_group_id <=", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdLike(String value) {
            addCriterion("environment_group_id like", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdNotLike(String value) {
            addCriterion("environment_group_id not like", value, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdIn(List<String> values) {
            addCriterion("environment_group_id in", values, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdNotIn(List<String> values) {
            addCriterion("environment_group_id not in", values, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdBetween(String value1, String value2) {
            addCriterion("environment_group_id between", value1, value2, "environmentGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvironmentGroupIdNotBetween(String value1, String value2) {
            addCriterion("environment_group_id not between", value1, value2, "environmentGroupId");
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

        public Criteria andRefIdIsNull() {
            addCriterion("ref_id is null");
            return (Criteria) this;
        }

        public Criteria andRefIdIsNotNull() {
            addCriterion("ref_id is not null");
            return (Criteria) this;
        }

        public Criteria andRefIdEqualTo(String value) {
            addCriterion("ref_id =", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdNotEqualTo(String value) {
            addCriterion("ref_id <>", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdGreaterThan(String value) {
            addCriterion("ref_id >", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdGreaterThanOrEqualTo(String value) {
            addCriterion("ref_id >=", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdLessThan(String value) {
            addCriterion("ref_id <", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdLessThanOrEqualTo(String value) {
            addCriterion("ref_id <=", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdLike(String value) {
            addCriterion("ref_id like", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdNotLike(String value) {
            addCriterion("ref_id not like", value, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdIn(List<String> values) {
            addCriterion("ref_id in", values, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdNotIn(List<String> values) {
            addCriterion("ref_id not in", values, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdBetween(String value1, String value2) {
            addCriterion("ref_id between", value1, value2, "refId");
            return (Criteria) this;
        }

        public Criteria andRefIdNotBetween(String value1, String value2) {
            addCriterion("ref_id not between", value1, value2, "refId");
            return (Criteria) this;
        }

        public Criteria andLatestIsNull() {
            addCriterion("latest is null");
            return (Criteria) this;
        }

        public Criteria andLatestIsNotNull() {
            addCriterion("latest is not null");
            return (Criteria) this;
        }

        public Criteria andLatestEqualTo(Boolean value) {
            addCriterion("latest =", value, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestNotEqualTo(Boolean value) {
            addCriterion("latest <>", value, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestGreaterThan(Boolean value) {
            addCriterion("latest >", value, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestGreaterThanOrEqualTo(Boolean value) {
            addCriterion("latest >=", value, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestLessThan(Boolean value) {
            addCriterion("latest <", value, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestLessThanOrEqualTo(Boolean value) {
            addCriterion("latest <=", value, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestIn(List<Boolean> values) {
            addCriterion("latest in", values, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestNotIn(List<Boolean> values) {
            addCriterion("latest not in", values, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestBetween(Boolean value1, Boolean value2) {
            addCriterion("latest between", value1, value2, "latest");
            return (Criteria) this;
        }

        public Criteria andLatestNotBetween(Boolean value1, Boolean value2) {
            addCriterion("latest not between", value1, value2, "latest");
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