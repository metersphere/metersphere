package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanExecutionQueueExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanExecutionQueueExample() {
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

        public Criteria andExecuteBatchIsNull() {
            addCriterion("execute_batch is null");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchIsNotNull() {
            addCriterion("execute_batch is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchEqualTo(String value) {
            addCriterion("execute_batch =", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchNotEqualTo(String value) {
            addCriterion("execute_batch <>", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchGreaterThan(String value) {
            addCriterion("execute_batch >", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchGreaterThanOrEqualTo(String value) {
            addCriterion("execute_batch >=", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchLessThan(String value) {
            addCriterion("execute_batch <", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchLessThanOrEqualTo(String value) {
            addCriterion("execute_batch <=", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchLike(String value) {
            addCriterion("execute_batch like", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchNotLike(String value) {
            addCriterion("execute_batch not like", value, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchIn(List<String> values) {
            addCriterion("execute_batch in", values, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchNotIn(List<String> values) {
            addCriterion("execute_batch not in", values, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchBetween(String value1, String value2) {
            addCriterion("execute_batch between", value1, value2, "executeBatch");
            return (Criteria) this;
        }

        public Criteria andExecuteBatchNotBetween(String value1, String value2) {
            addCriterion("execute_batch not between", value1, value2, "executeBatch");
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

        public Criteria andTestPlanExecuteRecordIdIsNull() {
            addCriterion("test_plan_execute_record_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdIsNotNull() {
            addCriterion("test_plan_execute_record_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdEqualTo(String value) {
            addCriterion("test_plan_execute_record_id =", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdNotEqualTo(String value) {
            addCriterion("test_plan_execute_record_id <>", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdGreaterThan(String value) {
            addCriterion("test_plan_execute_record_id >", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_execute_record_id >=", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdLessThan(String value) {
            addCriterion("test_plan_execute_record_id <", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_execute_record_id <=", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdLike(String value) {
            addCriterion("test_plan_execute_record_id like", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdNotLike(String value) {
            addCriterion("test_plan_execute_record_id not like", value, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdIn(List<String> values) {
            addCriterion("test_plan_execute_record_id in", values, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdNotIn(List<String> values) {
            addCriterion("test_plan_execute_record_id not in", values, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdBetween(String value1, String value2) {
            addCriterion("test_plan_execute_record_id between", value1, value2, "testPlanExecuteRecordId");
            return (Criteria) this;
        }

        public Criteria andTestPlanExecuteRecordIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_execute_record_id not between", value1, value2, "testPlanExecuteRecordId");
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

        public Criteria andPosIsNull() {
            addCriterion("pos is null");
            return (Criteria) this;
        }

        public Criteria andPosIsNotNull() {
            addCriterion("pos is not null");
            return (Criteria) this;
        }

        public Criteria andPosEqualTo(Integer value) {
            addCriterion("pos =", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotEqualTo(Integer value) {
            addCriterion("pos <>", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosGreaterThan(Integer value) {
            addCriterion("pos >", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosGreaterThanOrEqualTo(Integer value) {
            addCriterion("pos >=", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosLessThan(Integer value) {
            addCriterion("pos <", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosLessThanOrEqualTo(Integer value) {
            addCriterion("pos <=", value, "pos");
            return (Criteria) this;
        }

        public Criteria andPosIn(List<Integer> values) {
            addCriterion("pos in", values, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotIn(List<Integer> values) {
            addCriterion("pos not in", values, "pos");
            return (Criteria) this;
        }

        public Criteria andPosBetween(Integer value1, Integer value2) {
            addCriterion("pos between", value1, value2, "pos");
            return (Criteria) this;
        }

        public Criteria andPosNotBetween(Integer value1, Integer value2) {
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