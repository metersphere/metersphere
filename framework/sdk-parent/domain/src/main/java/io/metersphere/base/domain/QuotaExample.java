package io.metersphere.base.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class QuotaExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QuotaExample() {
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

        public Criteria andApiIsNull() {
            addCriterion("api is null");
            return (Criteria) this;
        }

        public Criteria andApiIsNotNull() {
            addCriterion("api is not null");
            return (Criteria) this;
        }

        public Criteria andApiEqualTo(Integer value) {
            addCriterion("api =", value, "api");
            return (Criteria) this;
        }

        public Criteria andApiNotEqualTo(Integer value) {
            addCriterion("api <>", value, "api");
            return (Criteria) this;
        }

        public Criteria andApiGreaterThan(Integer value) {
            addCriterion("api >", value, "api");
            return (Criteria) this;
        }

        public Criteria andApiGreaterThanOrEqualTo(Integer value) {
            addCriterion("api >=", value, "api");
            return (Criteria) this;
        }

        public Criteria andApiLessThan(Integer value) {
            addCriterion("api <", value, "api");
            return (Criteria) this;
        }

        public Criteria andApiLessThanOrEqualTo(Integer value) {
            addCriterion("api <=", value, "api");
            return (Criteria) this;
        }

        public Criteria andApiIn(List<Integer> values) {
            addCriterion("api in", values, "api");
            return (Criteria) this;
        }

        public Criteria andApiNotIn(List<Integer> values) {
            addCriterion("api not in", values, "api");
            return (Criteria) this;
        }

        public Criteria andApiBetween(Integer value1, Integer value2) {
            addCriterion("api between", value1, value2, "api");
            return (Criteria) this;
        }

        public Criteria andApiNotBetween(Integer value1, Integer value2) {
            addCriterion("api not between", value1, value2, "api");
            return (Criteria) this;
        }

        public Criteria andPerformanceIsNull() {
            addCriterion("performance is null");
            return (Criteria) this;
        }

        public Criteria andPerformanceIsNotNull() {
            addCriterion("performance is not null");
            return (Criteria) this;
        }

        public Criteria andPerformanceEqualTo(Integer value) {
            addCriterion("performance =", value, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceNotEqualTo(Integer value) {
            addCriterion("performance <>", value, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceGreaterThan(Integer value) {
            addCriterion("performance >", value, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceGreaterThanOrEqualTo(Integer value) {
            addCriterion("performance >=", value, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceLessThan(Integer value) {
            addCriterion("performance <", value, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceLessThanOrEqualTo(Integer value) {
            addCriterion("performance <=", value, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceIn(List<Integer> values) {
            addCriterion("performance in", values, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceNotIn(List<Integer> values) {
            addCriterion("performance not in", values, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceBetween(Integer value1, Integer value2) {
            addCriterion("performance between", value1, value2, "performance");
            return (Criteria) this;
        }

        public Criteria andPerformanceNotBetween(Integer value1, Integer value2) {
            addCriterion("performance not between", value1, value2, "performance");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsIsNull() {
            addCriterion("max_threads is null");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsIsNotNull() {
            addCriterion("max_threads is not null");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsEqualTo(Integer value) {
            addCriterion("max_threads =", value, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsNotEqualTo(Integer value) {
            addCriterion("max_threads <>", value, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsGreaterThan(Integer value) {
            addCriterion("max_threads >", value, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsGreaterThanOrEqualTo(Integer value) {
            addCriterion("max_threads >=", value, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsLessThan(Integer value) {
            addCriterion("max_threads <", value, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsLessThanOrEqualTo(Integer value) {
            addCriterion("max_threads <=", value, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsIn(List<Integer> values) {
            addCriterion("max_threads in", values, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsNotIn(List<Integer> values) {
            addCriterion("max_threads not in", values, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsBetween(Integer value1, Integer value2) {
            addCriterion("max_threads between", value1, value2, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andMaxThreadsNotBetween(Integer value1, Integer value2) {
            addCriterion("max_threads not between", value1, value2, "maxThreads");
            return (Criteria) this;
        }

        public Criteria andDurationIsNull() {
            addCriterion("duration is null");
            return (Criteria) this;
        }

        public Criteria andDurationIsNotNull() {
            addCriterion("duration is not null");
            return (Criteria) this;
        }

        public Criteria andDurationEqualTo(Integer value) {
            addCriterion("duration =", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationNotEqualTo(Integer value) {
            addCriterion("duration <>", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationGreaterThan(Integer value) {
            addCriterion("duration >", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationGreaterThanOrEqualTo(Integer value) {
            addCriterion("duration >=", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationLessThan(Integer value) {
            addCriterion("duration <", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationLessThanOrEqualTo(Integer value) {
            addCriterion("duration <=", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationIn(List<Integer> values) {
            addCriterion("duration in", values, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationNotIn(List<Integer> values) {
            addCriterion("duration not in", values, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationBetween(Integer value1, Integer value2) {
            addCriterion("duration between", value1, value2, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationNotBetween(Integer value1, Integer value2) {
            addCriterion("duration not between", value1, value2, "duration");
            return (Criteria) this;
        }

        public Criteria andResourcePoolIsNull() {
            addCriterion("resource_pool is null");
            return (Criteria) this;
        }

        public Criteria andResourcePoolIsNotNull() {
            addCriterion("resource_pool is not null");
            return (Criteria) this;
        }

        public Criteria andResourcePoolEqualTo(String value) {
            addCriterion("resource_pool =", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolNotEqualTo(String value) {
            addCriterion("resource_pool <>", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolGreaterThan(String value) {
            addCriterion("resource_pool >", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolGreaterThanOrEqualTo(String value) {
            addCriterion("resource_pool >=", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolLessThan(String value) {
            addCriterion("resource_pool <", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolLessThanOrEqualTo(String value) {
            addCriterion("resource_pool <=", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolLike(String value) {
            addCriterion("resource_pool like", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolNotLike(String value) {
            addCriterion("resource_pool not like", value, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolIn(List<String> values) {
            addCriterion("resource_pool in", values, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolNotIn(List<String> values) {
            addCriterion("resource_pool not in", values, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolBetween(String value1, String value2) {
            addCriterion("resource_pool between", value1, value2, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andResourcePoolNotBetween(String value1, String value2) {
            addCriterion("resource_pool not between", value1, value2, "resourcePool");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNull() {
            addCriterion("workspace_id is null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNotNull() {
            addCriterion("workspace_id is not null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdEqualTo(String value) {
            addCriterion("workspace_id =", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotEqualTo(String value) {
            addCriterion("workspace_id <>", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThan(String value) {
            addCriterion("workspace_id >", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThanOrEqualTo(String value) {
            addCriterion("workspace_id >=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThan(String value) {
            addCriterion("workspace_id <", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThanOrEqualTo(String value) {
            addCriterion("workspace_id <=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLike(String value) {
            addCriterion("workspace_id like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotLike(String value) {
            addCriterion("workspace_id not like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIn(List<String> values) {
            addCriterion("workspace_id in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotIn(List<String> values) {
            addCriterion("workspace_id not in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdBetween(String value1, String value2) {
            addCriterion("workspace_id between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotBetween(String value1, String value2) {
            addCriterion("workspace_id not between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andUseDefaultIsNull() {
            addCriterion("use_default is null");
            return (Criteria) this;
        }

        public Criteria andUseDefaultIsNotNull() {
            addCriterion("use_default is not null");
            return (Criteria) this;
        }

        public Criteria andUseDefaultEqualTo(Boolean value) {
            addCriterion("use_default =", value, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultNotEqualTo(Boolean value) {
            addCriterion("use_default <>", value, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultGreaterThan(Boolean value) {
            addCriterion("use_default >", value, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultGreaterThanOrEqualTo(Boolean value) {
            addCriterion("use_default >=", value, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultLessThan(Boolean value) {
            addCriterion("use_default <", value, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultLessThanOrEqualTo(Boolean value) {
            addCriterion("use_default <=", value, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultIn(List<Boolean> values) {
            addCriterion("use_default in", values, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultNotIn(List<Boolean> values) {
            addCriterion("use_default not in", values, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultBetween(Boolean value1, Boolean value2) {
            addCriterion("use_default between", value1, value2, "useDefault");
            return (Criteria) this;
        }

        public Criteria andUseDefaultNotBetween(Boolean value1, Boolean value2) {
            addCriterion("use_default not between", value1, value2, "useDefault");
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

        public Criteria andMemberIsNull() {
            addCriterion("`member` is null");
            return (Criteria) this;
        }

        public Criteria andMemberIsNotNull() {
            addCriterion("`member` is not null");
            return (Criteria) this;
        }

        public Criteria andMemberEqualTo(Integer value) {
            addCriterion("`member` =", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberNotEqualTo(Integer value) {
            addCriterion("`member` <>", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberGreaterThan(Integer value) {
            addCriterion("`member` >", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberGreaterThanOrEqualTo(Integer value) {
            addCriterion("`member` >=", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberLessThan(Integer value) {
            addCriterion("`member` <", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberLessThanOrEqualTo(Integer value) {
            addCriterion("`member` <=", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberIn(List<Integer> values) {
            addCriterion("`member` in", values, "member");
            return (Criteria) this;
        }

        public Criteria andMemberNotIn(List<Integer> values) {
            addCriterion("`member` not in", values, "member");
            return (Criteria) this;
        }

        public Criteria andMemberBetween(Integer value1, Integer value2) {
            addCriterion("`member` between", value1, value2, "member");
            return (Criteria) this;
        }

        public Criteria andMemberNotBetween(Integer value1, Integer value2) {
            addCriterion("`member` not between", value1, value2, "member");
            return (Criteria) this;
        }

        public Criteria andProjectIsNull() {
            addCriterion("project is null");
            return (Criteria) this;
        }

        public Criteria andProjectIsNotNull() {
            addCriterion("project is not null");
            return (Criteria) this;
        }

        public Criteria andProjectEqualTo(Integer value) {
            addCriterion("project =", value, "project");
            return (Criteria) this;
        }

        public Criteria andProjectNotEqualTo(Integer value) {
            addCriterion("project <>", value, "project");
            return (Criteria) this;
        }

        public Criteria andProjectGreaterThan(Integer value) {
            addCriterion("project >", value, "project");
            return (Criteria) this;
        }

        public Criteria andProjectGreaterThanOrEqualTo(Integer value) {
            addCriterion("project >=", value, "project");
            return (Criteria) this;
        }

        public Criteria andProjectLessThan(Integer value) {
            addCriterion("project <", value, "project");
            return (Criteria) this;
        }

        public Criteria andProjectLessThanOrEqualTo(Integer value) {
            addCriterion("project <=", value, "project");
            return (Criteria) this;
        }

        public Criteria andProjectIn(List<Integer> values) {
            addCriterion("project in", values, "project");
            return (Criteria) this;
        }

        public Criteria andProjectNotIn(List<Integer> values) {
            addCriterion("project not in", values, "project");
            return (Criteria) this;
        }

        public Criteria andProjectBetween(Integer value1, Integer value2) {
            addCriterion("project between", value1, value2, "project");
            return (Criteria) this;
        }

        public Criteria andProjectNotBetween(Integer value1, Integer value2) {
            addCriterion("project not between", value1, value2, "project");
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

        public Criteria andVumTotalIsNull() {
            addCriterion("vum_total is null");
            return (Criteria) this;
        }

        public Criteria andVumTotalIsNotNull() {
            addCriterion("vum_total is not null");
            return (Criteria) this;
        }

        public Criteria andVumTotalEqualTo(BigDecimal value) {
            addCriterion("vum_total =", value, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalNotEqualTo(BigDecimal value) {
            addCriterion("vum_total <>", value, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalGreaterThan(BigDecimal value) {
            addCriterion("vum_total >", value, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("vum_total >=", value, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalLessThan(BigDecimal value) {
            addCriterion("vum_total <", value, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalLessThanOrEqualTo(BigDecimal value) {
            addCriterion("vum_total <=", value, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalIn(List<BigDecimal> values) {
            addCriterion("vum_total in", values, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalNotIn(List<BigDecimal> values) {
            addCriterion("vum_total not in", values, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("vum_total between", value1, value2, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumTotalNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("vum_total not between", value1, value2, "vumTotal");
            return (Criteria) this;
        }

        public Criteria andVumUsedIsNull() {
            addCriterion("vum_used is null");
            return (Criteria) this;
        }

        public Criteria andVumUsedIsNotNull() {
            addCriterion("vum_used is not null");
            return (Criteria) this;
        }

        public Criteria andVumUsedEqualTo(BigDecimal value) {
            addCriterion("vum_used =", value, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedNotEqualTo(BigDecimal value) {
            addCriterion("vum_used <>", value, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedGreaterThan(BigDecimal value) {
            addCriterion("vum_used >", value, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("vum_used >=", value, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedLessThan(BigDecimal value) {
            addCriterion("vum_used <", value, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedLessThanOrEqualTo(BigDecimal value) {
            addCriterion("vum_used <=", value, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedIn(List<BigDecimal> values) {
            addCriterion("vum_used in", values, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedNotIn(List<BigDecimal> values) {
            addCriterion("vum_used not in", values, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("vum_used between", value1, value2, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andVumUsedNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("vum_used not between", value1, value2, "vumUsed");
            return (Criteria) this;
        }

        public Criteria andModuleSettingIsNull() {
            addCriterion("module_setting is null");
            return (Criteria) this;
        }

        public Criteria andModuleSettingIsNotNull() {
            addCriterion("module_setting is not null");
            return (Criteria) this;
        }

        public Criteria andModuleSettingEqualTo(String value) {
            addCriterion("module_setting =", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingNotEqualTo(String value) {
            addCriterion("module_setting <>", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingGreaterThan(String value) {
            addCriterion("module_setting >", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingGreaterThanOrEqualTo(String value) {
            addCriterion("module_setting >=", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingLessThan(String value) {
            addCriterion("module_setting <", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingLessThanOrEqualTo(String value) {
            addCriterion("module_setting <=", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingLike(String value) {
            addCriterion("module_setting like", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingNotLike(String value) {
            addCriterion("module_setting not like", value, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingIn(List<String> values) {
            addCriterion("module_setting in", values, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingNotIn(List<String> values) {
            addCriterion("module_setting not in", values, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingBetween(String value1, String value2) {
            addCriterion("module_setting between", value1, value2, "moduleSetting");
            return (Criteria) this;
        }

        public Criteria andModuleSettingNotBetween(String value1, String value2) {
            addCriterion("module_setting not between", value1, value2, "moduleSetting");
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