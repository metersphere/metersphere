package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanApiCaseExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanApiCaseExample() {
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

        public Criteria andApiCaseIdIsNull() {
            addCriterion("api_case_id is null");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdIsNotNull() {
            addCriterion("api_case_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdEqualTo(String value) {
            addCriterion("api_case_id =", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotEqualTo(String value) {
            addCriterion("api_case_id <>", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdGreaterThan(String value) {
            addCriterion("api_case_id >", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_case_id >=", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdLessThan(String value) {
            addCriterion("api_case_id <", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdLessThanOrEqualTo(String value) {
            addCriterion("api_case_id <=", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdLike(String value) {
            addCriterion("api_case_id like", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotLike(String value) {
            addCriterion("api_case_id not like", value, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdIn(List<String> values) {
            addCriterion("api_case_id in", values, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotIn(List<String> values) {
            addCriterion("api_case_id not in", values, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdBetween(String value1, String value2) {
            addCriterion("api_case_id between", value1, value2, "apiCaseId");
            return (Criteria) this;
        }

        public Criteria andApiCaseIdNotBetween(String value1, String value2) {
            addCriterion("api_case_id not between", value1, value2, "apiCaseId");
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