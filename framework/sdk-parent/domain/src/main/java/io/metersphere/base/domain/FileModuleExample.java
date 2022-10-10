package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class FileModuleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FileModuleExample() {
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

        public Criteria andLevelIsNull() {
            addCriterion("`level` is null");
            return (Criteria) this;
        }

        public Criteria andLevelIsNotNull() {
            addCriterion("`level` is not null");
            return (Criteria) this;
        }

        public Criteria andLevelEqualTo(Integer value) {
            addCriterion("`level` =", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotEqualTo(Integer value) {
            addCriterion("`level` <>", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThan(Integer value) {
            addCriterion("`level` >", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThanOrEqualTo(Integer value) {
            addCriterion("`level` >=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThan(Integer value) {
            addCriterion("`level` <", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThanOrEqualTo(Integer value) {
            addCriterion("`level` <=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelIn(List<Integer> values) {
            addCriterion("`level` in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotIn(List<Integer> values) {
            addCriterion("`level` not in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelBetween(Integer value1, Integer value2) {
            addCriterion("`level` between", value1, value2, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotBetween(Integer value1, Integer value2) {
            addCriterion("`level` not between", value1, value2, "level");
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

        public Criteria andPosIsNull() {
            addCriterion("pos is null");
            return (Criteria) this;
        }

        public Criteria andPosIsNotNull() {
            addCriterion("pos is not null");
            return (Criteria) this;
        }

        public Criteria andPosEqualTo(Double value) {
            addCriterion("pos =", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotEqualTo(Double value) {
            addCriterion("pos <>", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosGreaterThan(Double value) {
            addCriterion("pos >", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosGreaterThanOrEqualTo(Double value) {
            addCriterion("pos >=", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosLessThan(Double value) {
            addCriterion("pos <", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosLessThanOrEqualTo(Double value) {
            addCriterion("pos <=", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosIn(List<Double> values) {
            addCriterion("pos in", values, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotIn(List<Double> values) {
            addCriterion("pos not in", values, "pos");
            return (Criteria) this;
        }

        public Criteria andPosBetween(Double value1, Double value2) {
            addCriterion("pos between", value1, value2, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotBetween(Double value1, Double value2) {
            addCriterion("pos not between", value1, value2, "pos");
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

        public Criteria andModuleTypeIsNull() {
            addCriterion("module_type is null");
            return (Criteria) this;
        }

        public Criteria andModuleTypeIsNotNull() {
            addCriterion("module_type is not null");
            return (Criteria) this;
        }

        public Criteria andModuleTypeEqualTo(String value) {
            addCriterion("module_type =", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeNotEqualTo(String value) {
            addCriterion("module_type <>", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeGreaterThan(String value) {
            addCriterion("module_type >", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeGreaterThanOrEqualTo(String value) {
            addCriterion("module_type >=", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeLessThan(String value) {
            addCriterion("module_type <", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeLessThanOrEqualTo(String value) {
            addCriterion("module_type <=", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeLike(String value) {
            addCriterion("module_type like", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeNotLike(String value) {
            addCriterion("module_type not like", value, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeIn(List<String> values) {
            addCriterion("module_type in", values, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeNotIn(List<String> values) {
            addCriterion("module_type not in", values, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeBetween(String value1, String value2) {
            addCriterion("module_type between", value1, value2, "moduleType");
            return (Criteria) this;
        }

        public Criteria andModuleTypeNotBetween(String value1, String value2) {
            addCriterion("module_type not between", value1, value2, "moduleType");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathIsNull() {
            addCriterion("repository_path is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathIsNotNull() {
            addCriterion("repository_path is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathEqualTo(String value) {
            addCriterion("repository_path =", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotEqualTo(String value) {
            addCriterion("repository_path <>", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathGreaterThan(String value) {
            addCriterion("repository_path >", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathGreaterThanOrEqualTo(String value) {
            addCriterion("repository_path >=", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathLessThan(String value) {
            addCriterion("repository_path <", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathLessThanOrEqualTo(String value) {
            addCriterion("repository_path <=", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathLike(String value) {
            addCriterion("repository_path like", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotLike(String value) {
            addCriterion("repository_path not like", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathIn(List<String> values) {
            addCriterion("repository_path in", values, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotIn(List<String> values) {
            addCriterion("repository_path not in", values, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathBetween(String value1, String value2) {
            addCriterion("repository_path between", value1, value2, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotBetween(String value1, String value2) {
            addCriterion("repository_path not between", value1, value2, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenIsNull() {
            addCriterion("repository_token is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenIsNotNull() {
            addCriterion("repository_token is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenEqualTo(String value) {
            addCriterion("repository_token =", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotEqualTo(String value) {
            addCriterion("repository_token <>", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenGreaterThan(String value) {
            addCriterion("repository_token >", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenGreaterThanOrEqualTo(String value) {
            addCriterion("repository_token >=", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenLessThan(String value) {
            addCriterion("repository_token <", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenLessThanOrEqualTo(String value) {
            addCriterion("repository_token <=", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenLike(String value) {
            addCriterion("repository_token like", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotLike(String value) {
            addCriterion("repository_token not like", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenIn(List<String> values) {
            addCriterion("repository_token in", values, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotIn(List<String> values) {
            addCriterion("repository_token not in", values, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenBetween(String value1, String value2) {
            addCriterion("repository_token between", value1, value2, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotBetween(String value1, String value2) {
            addCriterion("repository_token not between", value1, value2, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameIsNull() {
            addCriterion("repository_user_name is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameIsNotNull() {
            addCriterion("repository_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameEqualTo(String value) {
            addCriterion("repository_user_name =", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotEqualTo(String value) {
            addCriterion("repository_user_name <>", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameGreaterThan(String value) {
            addCriterion("repository_user_name >", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("repository_user_name >=", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameLessThan(String value) {
            addCriterion("repository_user_name <", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameLessThanOrEqualTo(String value) {
            addCriterion("repository_user_name <=", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameLike(String value) {
            addCriterion("repository_user_name like", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotLike(String value) {
            addCriterion("repository_user_name not like", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameIn(List<String> values) {
            addCriterion("repository_user_name in", values, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotIn(List<String> values) {
            addCriterion("repository_user_name not in", values, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameBetween(String value1, String value2) {
            addCriterion("repository_user_name between", value1, value2, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotBetween(String value1, String value2) {
            addCriterion("repository_user_name not between", value1, value2, "repositoryUserName");
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