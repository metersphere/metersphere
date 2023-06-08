package io.metersphere.system.domain;

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

        public Criteria andOrganizationIdIsNull() {
            addCriterion("organization_id is null");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdIsNotNull() {
            addCriterion("organization_id is not null");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdEqualTo(String value) {
            addCriterion("organization_id =", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdNotEqualTo(String value) {
            addCriterion("organization_id <>", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdGreaterThan(String value) {
            addCriterion("organization_id >", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdGreaterThanOrEqualTo(String value) {
            addCriterion("organization_id >=", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdLessThan(String value) {
            addCriterion("organization_id <", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdLessThanOrEqualTo(String value) {
            addCriterion("organization_id <=", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdLike(String value) {
            addCriterion("organization_id like", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdNotLike(String value) {
            addCriterion("organization_id not like", value, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdIn(List<String> values) {
            addCriterion("organization_id in", values, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdNotIn(List<String> values) {
            addCriterion("organization_id not in", values, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdBetween(String value1, String value2) {
            addCriterion("organization_id between", value1, value2, "organizationId");
            return (Criteria) this;
        }

        public Criteria andOrganizationIdNotBetween(String value1, String value2) {
            addCriterion("organization_id not between", value1, value2, "organizationId");
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

        public Criteria andFunctionalCaseIsNull() {
            addCriterion("functional_case is null");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseIsNotNull() {
            addCriterion("functional_case is not null");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseEqualTo(Integer value) {
            addCriterion("functional_case =", value, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseNotEqualTo(Integer value) {
            addCriterion("functional_case <>", value, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseGreaterThan(Integer value) {
            addCriterion("functional_case >", value, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseGreaterThanOrEqualTo(Integer value) {
            addCriterion("functional_case >=", value, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseLessThan(Integer value) {
            addCriterion("functional_case <", value, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseLessThanOrEqualTo(Integer value) {
            addCriterion("functional_case <=", value, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseIn(List<Integer> values) {
            addCriterion("functional_case in", values, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseNotIn(List<Integer> values) {
            addCriterion("functional_case not in", values, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseBetween(Integer value1, Integer value2) {
            addCriterion("functional_case between", value1, value2, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andFunctionalCaseNotBetween(Integer value1, Integer value2) {
            addCriterion("functional_case not between", value1, value2, "functionalCase");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalIsNull() {
            addCriterion("load_test_vum_total is null");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalIsNotNull() {
            addCriterion("load_test_vum_total is not null");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_total =", value, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalNotEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_total <>", value, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalGreaterThan(BigDecimal value) {
            addCriterion("load_test_vum_total >", value, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_total >=", value, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalLessThan(BigDecimal value) {
            addCriterion("load_test_vum_total <", value, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalLessThanOrEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_total <=", value, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalIn(List<BigDecimal> values) {
            addCriterion("load_test_vum_total in", values, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalNotIn(List<BigDecimal> values) {
            addCriterion("load_test_vum_total not in", values, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("load_test_vum_total between", value1, value2, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumTotalNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("load_test_vum_total not between", value1, value2, "loadTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedIsNull() {
            addCriterion("load_test_vum_used is null");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedIsNotNull() {
            addCriterion("load_test_vum_used is not null");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_used =", value, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedNotEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_used <>", value, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedGreaterThan(BigDecimal value) {
            addCriterion("load_test_vum_used >", value, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_used >=", value, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedLessThan(BigDecimal value) {
            addCriterion("load_test_vum_used <", value, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedLessThanOrEqualTo(BigDecimal value) {
            addCriterion("load_test_vum_used <=", value, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedIn(List<BigDecimal> values) {
            addCriterion("load_test_vum_used in", values, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedNotIn(List<BigDecimal> values) {
            addCriterion("load_test_vum_used not in", values, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("load_test_vum_used between", value1, value2, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestVumUsedNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("load_test_vum_used not between", value1, value2, "loadTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsIsNull() {
            addCriterion("load_test_max_threads is null");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsIsNotNull() {
            addCriterion("load_test_max_threads is not null");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsEqualTo(Integer value) {
            addCriterion("load_test_max_threads =", value, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsNotEqualTo(Integer value) {
            addCriterion("load_test_max_threads <>", value, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsGreaterThan(Integer value) {
            addCriterion("load_test_max_threads >", value, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsGreaterThanOrEqualTo(Integer value) {
            addCriterion("load_test_max_threads >=", value, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsLessThan(Integer value) {
            addCriterion("load_test_max_threads <", value, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsLessThanOrEqualTo(Integer value) {
            addCriterion("load_test_max_threads <=", value, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsIn(List<Integer> values) {
            addCriterion("load_test_max_threads in", values, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsNotIn(List<Integer> values) {
            addCriterion("load_test_max_threads not in", values, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsBetween(Integer value1, Integer value2) {
            addCriterion("load_test_max_threads between", value1, value2, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestMaxThreadsNotBetween(Integer value1, Integer value2) {
            addCriterion("load_test_max_threads not between", value1, value2, "loadTestMaxThreads");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationIsNull() {
            addCriterion("load_test_duration is null");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationIsNotNull() {
            addCriterion("load_test_duration is not null");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationEqualTo(Integer value) {
            addCriterion("load_test_duration =", value, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationNotEqualTo(Integer value) {
            addCriterion("load_test_duration <>", value, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationGreaterThan(Integer value) {
            addCriterion("load_test_duration >", value, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationGreaterThanOrEqualTo(Integer value) {
            addCriterion("load_test_duration >=", value, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationLessThan(Integer value) {
            addCriterion("load_test_duration <", value, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationLessThanOrEqualTo(Integer value) {
            addCriterion("load_test_duration <=", value, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationIn(List<Integer> values) {
            addCriterion("load_test_duration in", values, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationNotIn(List<Integer> values) {
            addCriterion("load_test_duration not in", values, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationBetween(Integer value1, Integer value2) {
            addCriterion("load_test_duration between", value1, value2, "loadTestDuration");
            return (Criteria) this;
        }

        public Criteria andLoadTestDurationNotBetween(Integer value1, Integer value2) {
            addCriterion("load_test_duration not between", value1, value2, "loadTestDuration");
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

        public Criteria andApiTestVumTotalIsNull() {
            addCriterion("api_test_vum_total is null");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalIsNotNull() {
            addCriterion("api_test_vum_total is not null");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_total =", value, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalNotEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_total <>", value, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalGreaterThan(BigDecimal value) {
            addCriterion("api_test_vum_total >", value, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_total >=", value, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalLessThan(BigDecimal value) {
            addCriterion("api_test_vum_total <", value, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalLessThanOrEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_total <=", value, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalIn(List<BigDecimal> values) {
            addCriterion("api_test_vum_total in", values, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalNotIn(List<BigDecimal> values) {
            addCriterion("api_test_vum_total not in", values, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("api_test_vum_total between", value1, value2, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumTotalNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("api_test_vum_total not between", value1, value2, "apiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedIsNull() {
            addCriterion("api_test_vum_used is null");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedIsNotNull() {
            addCriterion("api_test_vum_used is not null");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_used =", value, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedNotEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_used <>", value, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedGreaterThan(BigDecimal value) {
            addCriterion("api_test_vum_used >", value, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_used >=", value, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedLessThan(BigDecimal value) {
            addCriterion("api_test_vum_used <", value, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedLessThanOrEqualTo(BigDecimal value) {
            addCriterion("api_test_vum_used <=", value, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedIn(List<BigDecimal> values) {
            addCriterion("api_test_vum_used in", values, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedNotIn(List<BigDecimal> values) {
            addCriterion("api_test_vum_used not in", values, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("api_test_vum_used between", value1, value2, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andApiTestVumUsedNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("api_test_vum_used not between", value1, value2, "apiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalIsNull() {
            addCriterion("ui_test_vum_total is null");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalIsNotNull() {
            addCriterion("ui_test_vum_total is not null");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_total =", value, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalNotEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_total <>", value, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalGreaterThan(BigDecimal value) {
            addCriterion("ui_test_vum_total >", value, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_total >=", value, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalLessThan(BigDecimal value) {
            addCriterion("ui_test_vum_total <", value, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalLessThanOrEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_total <=", value, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalIn(List<BigDecimal> values) {
            addCriterion("ui_test_vum_total in", values, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalNotIn(List<BigDecimal> values) {
            addCriterion("ui_test_vum_total not in", values, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ui_test_vum_total between", value1, value2, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumTotalNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ui_test_vum_total not between", value1, value2, "uiTestVumTotal");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedIsNull() {
            addCriterion("ui_test_vum_used is null");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedIsNotNull() {
            addCriterion("ui_test_vum_used is not null");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_used =", value, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedNotEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_used <>", value, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedGreaterThan(BigDecimal value) {
            addCriterion("ui_test_vum_used >", value, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_used >=", value, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedLessThan(BigDecimal value) {
            addCriterion("ui_test_vum_used <", value, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedLessThanOrEqualTo(BigDecimal value) {
            addCriterion("ui_test_vum_used <=", value, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedIn(List<BigDecimal> values) {
            addCriterion("ui_test_vum_used in", values, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedNotIn(List<BigDecimal> values) {
            addCriterion("ui_test_vum_used not in", values, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ui_test_vum_used between", value1, value2, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andUiTestVumUsedNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ui_test_vum_used not between", value1, value2, "uiTestVumUsed");
            return (Criteria) this;
        }

        public Criteria andFileStorageIsNull() {
            addCriterion("file_storage is null");
            return (Criteria) this;
        }

        public Criteria andFileStorageIsNotNull() {
            addCriterion("file_storage is not null");
            return (Criteria) this;
        }

        public Criteria andFileStorageEqualTo(Long value) {
            addCriterion("file_storage =", value, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageNotEqualTo(Long value) {
            addCriterion("file_storage <>", value, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageGreaterThan(Long value) {
            addCriterion("file_storage >", value, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageGreaterThanOrEqualTo(Long value) {
            addCriterion("file_storage >=", value, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageLessThan(Long value) {
            addCriterion("file_storage <", value, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageLessThanOrEqualTo(Long value) {
            addCriterion("file_storage <=", value, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageIn(List<Long> values) {
            addCriterion("file_storage in", values, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageNotIn(List<Long> values) {
            addCriterion("file_storage not in", values, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageBetween(Long value1, Long value2) {
            addCriterion("file_storage between", value1, value2, "fileStorage");
            return (Criteria) this;
        }

        public Criteria andFileStorageNotBetween(Long value1, Long value2) {
            addCriterion("file_storage not between", value1, value2, "fileStorage");
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