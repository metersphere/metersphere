package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class OperatingLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public OperatingLogExample() {
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

        public Criteria andOperMethodIsNull() {
            addCriterion("oper_method is null");
            return (Criteria) this;
        }

        public Criteria andOperMethodIsNotNull() {
            addCriterion("oper_method is not null");
            return (Criteria) this;
        }

        public Criteria andOperMethodEqualTo(String value) {
            addCriterion("oper_method =", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodNotEqualTo(String value) {
            addCriterion("oper_method <>", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodGreaterThan(String value) {
            addCriterion("oper_method >", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodGreaterThanOrEqualTo(String value) {
            addCriterion("oper_method >=", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodLessThan(String value) {
            addCriterion("oper_method <", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodLessThanOrEqualTo(String value) {
            addCriterion("oper_method <=", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodLike(String value) {
            addCriterion("oper_method like", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodNotLike(String value) {
            addCriterion("oper_method not like", value, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodIn(List<String> values) {
            addCriterion("oper_method in", values, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodNotIn(List<String> values) {
            addCriterion("oper_method not in", values, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodBetween(String value1, String value2) {
            addCriterion("oper_method between", value1, value2, "operMethod");
            return (Criteria) this;
        }

        public Criteria andOperMethodNotBetween(String value1, String value2) {
            addCriterion("oper_method not between", value1, value2, "operMethod");
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

        public Criteria andOperUserIsNull() {
            addCriterion("oper_user is null");
            return (Criteria) this;
        }

        public Criteria andOperUserIsNotNull() {
            addCriterion("oper_user is not null");
            return (Criteria) this;
        }

        public Criteria andOperUserEqualTo(String value) {
            addCriterion("oper_user =", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserNotEqualTo(String value) {
            addCriterion("oper_user <>", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserGreaterThan(String value) {
            addCriterion("oper_user >", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserGreaterThanOrEqualTo(String value) {
            addCriterion("oper_user >=", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserLessThan(String value) {
            addCriterion("oper_user <", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserLessThanOrEqualTo(String value) {
            addCriterion("oper_user <=", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserLike(String value) {
            addCriterion("oper_user like", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserNotLike(String value) {
            addCriterion("oper_user not like", value, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserIn(List<String> values) {
            addCriterion("oper_user in", values, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserNotIn(List<String> values) {
            addCriterion("oper_user not in", values, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserBetween(String value1, String value2) {
            addCriterion("oper_user between", value1, value2, "operUser");
            return (Criteria) this;
        }

        public Criteria andOperUserNotBetween(String value1, String value2) {
            addCriterion("oper_user not between", value1, value2, "operUser");
            return (Criteria) this;
        }

        public Criteria andSourceIdIsNull() {
            addCriterion("source_id is null");
            return (Criteria) this;
        }

        public Criteria andSourceIdIsNotNull() {
            addCriterion("source_id is not null");
            return (Criteria) this;
        }

        public Criteria andSourceIdEqualTo(String value) {
            addCriterion("source_id =", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdNotEqualTo(String value) {
            addCriterion("source_id <>", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdGreaterThan(String value) {
            addCriterion("source_id >", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdGreaterThanOrEqualTo(String value) {
            addCriterion("source_id >=", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdLessThan(String value) {
            addCriterion("source_id <", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdLessThanOrEqualTo(String value) {
            addCriterion("source_id <=", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdLike(String value) {
            addCriterion("source_id like", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdNotLike(String value) {
            addCriterion("source_id not like", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdIn(List<String> values) {
            addCriterion("source_id in", values, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdNotIn(List<String> values) {
            addCriterion("source_id not in", values, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdBetween(String value1, String value2) {
            addCriterion("source_id between", value1, value2, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdNotBetween(String value1, String value2) {
            addCriterion("source_id not between", value1, value2, "sourceId");
            return (Criteria) this;
        }

        public Criteria andOperTypeIsNull() {
            addCriterion("oper_type is null");
            return (Criteria) this;
        }

        public Criteria andOperTypeIsNotNull() {
            addCriterion("oper_type is not null");
            return (Criteria) this;
        }

        public Criteria andOperTypeEqualTo(String value) {
            addCriterion("oper_type =", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeNotEqualTo(String value) {
            addCriterion("oper_type <>", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeGreaterThan(String value) {
            addCriterion("oper_type >", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeGreaterThanOrEqualTo(String value) {
            addCriterion("oper_type >=", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeLessThan(String value) {
            addCriterion("oper_type <", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeLessThanOrEqualTo(String value) {
            addCriterion("oper_type <=", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeLike(String value) {
            addCriterion("oper_type like", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeNotLike(String value) {
            addCriterion("oper_type not like", value, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeIn(List<String> values) {
            addCriterion("oper_type in", values, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeNotIn(List<String> values) {
            addCriterion("oper_type not in", values, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeBetween(String value1, String value2) {
            addCriterion("oper_type between", value1, value2, "operType");
            return (Criteria) this;
        }

        public Criteria andOperTypeNotBetween(String value1, String value2) {
            addCriterion("oper_type not between", value1, value2, "operType");
            return (Criteria) this;
        }

        public Criteria andOperModuleIsNull() {
            addCriterion("oper_module is null");
            return (Criteria) this;
        }

        public Criteria andOperModuleIsNotNull() {
            addCriterion("oper_module is not null");
            return (Criteria) this;
        }

        public Criteria andOperModuleEqualTo(String value) {
            addCriterion("oper_module =", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleNotEqualTo(String value) {
            addCriterion("oper_module <>", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleGreaterThan(String value) {
            addCriterion("oper_module >", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleGreaterThanOrEqualTo(String value) {
            addCriterion("oper_module >=", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleLessThan(String value) {
            addCriterion("oper_module <", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleLessThanOrEqualTo(String value) {
            addCriterion("oper_module <=", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleLike(String value) {
            addCriterion("oper_module like", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleNotLike(String value) {
            addCriterion("oper_module not like", value, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleIn(List<String> values) {
            addCriterion("oper_module in", values, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleNotIn(List<String> values) {
            addCriterion("oper_module not in", values, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleBetween(String value1, String value2) {
            addCriterion("oper_module between", value1, value2, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperModuleNotBetween(String value1, String value2) {
            addCriterion("oper_module not between", value1, value2, "operModule");
            return (Criteria) this;
        }

        public Criteria andOperTitleIsNull() {
            addCriterion("oper_title is null");
            return (Criteria) this;
        }

        public Criteria andOperTitleIsNotNull() {
            addCriterion("oper_title is not null");
            return (Criteria) this;
        }

        public Criteria andOperTitleEqualTo(String value) {
            addCriterion("oper_title =", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleNotEqualTo(String value) {
            addCriterion("oper_title <>", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleGreaterThan(String value) {
            addCriterion("oper_title >", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleGreaterThanOrEqualTo(String value) {
            addCriterion("oper_title >=", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleLessThan(String value) {
            addCriterion("oper_title <", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleLessThanOrEqualTo(String value) {
            addCriterion("oper_title <=", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleLike(String value) {
            addCriterion("oper_title like", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleNotLike(String value) {
            addCriterion("oper_title not like", value, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleIn(List<String> values) {
            addCriterion("oper_title in", values, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleNotIn(List<String> values) {
            addCriterion("oper_title not in", values, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleBetween(String value1, String value2) {
            addCriterion("oper_title between", value1, value2, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperTitleNotBetween(String value1, String value2) {
            addCriterion("oper_title not between", value1, value2, "operTitle");
            return (Criteria) this;
        }

        public Criteria andOperPathIsNull() {
            addCriterion("oper_path is null");
            return (Criteria) this;
        }

        public Criteria andOperPathIsNotNull() {
            addCriterion("oper_path is not null");
            return (Criteria) this;
        }

        public Criteria andOperPathEqualTo(String value) {
            addCriterion("oper_path =", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathNotEqualTo(String value) {
            addCriterion("oper_path <>", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathGreaterThan(String value) {
            addCriterion("oper_path >", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathGreaterThanOrEqualTo(String value) {
            addCriterion("oper_path >=", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathLessThan(String value) {
            addCriterion("oper_path <", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathLessThanOrEqualTo(String value) {
            addCriterion("oper_path <=", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathLike(String value) {
            addCriterion("oper_path like", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathNotLike(String value) {
            addCriterion("oper_path not like", value, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathIn(List<String> values) {
            addCriterion("oper_path in", values, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathNotIn(List<String> values) {
            addCriterion("oper_path not in", values, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathBetween(String value1, String value2) {
            addCriterion("oper_path between", value1, value2, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperPathNotBetween(String value1, String value2) {
            addCriterion("oper_path not between", value1, value2, "operPath");
            return (Criteria) this;
        }

        public Criteria andOperTimeIsNull() {
            addCriterion("oper_time is null");
            return (Criteria) this;
        }

        public Criteria andOperTimeIsNotNull() {
            addCriterion("oper_time is not null");
            return (Criteria) this;
        }

        public Criteria andOperTimeEqualTo(Long value) {
            addCriterion("oper_time =", value, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeNotEqualTo(Long value) {
            addCriterion("oper_time <>", value, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeGreaterThan(Long value) {
            addCriterion("oper_time >", value, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("oper_time >=", value, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeLessThan(Long value) {
            addCriterion("oper_time <", value, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeLessThanOrEqualTo(Long value) {
            addCriterion("oper_time <=", value, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeIn(List<Long> values) {
            addCriterion("oper_time in", values, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeNotIn(List<Long> values) {
            addCriterion("oper_time not in", values, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeBetween(Long value1, Long value2) {
            addCriterion("oper_time between", value1, value2, "operTime");
            return (Criteria) this;
        }

        public Criteria andOperTimeNotBetween(Long value1, Long value2) {
            addCriterion("oper_time not between", value1, value2, "operTime");
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