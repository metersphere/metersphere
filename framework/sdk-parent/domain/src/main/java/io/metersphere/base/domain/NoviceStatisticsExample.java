package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class NoviceStatisticsExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public NoviceStatisticsExample() {
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

        public Criteria andGuideStepIsNull() {
            addCriterion("guide_step is null");
            return (Criteria) this;
        }

        public Criteria andGuideStepIsNotNull() {
            addCriterion("guide_step is not null");
            return (Criteria) this;
        }

        public Criteria andGuideStepEqualTo(Integer value) {
            addCriterion("guide_step =", value, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepNotEqualTo(Integer value) {
            addCriterion("guide_step <>", value, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepGreaterThan(Integer value) {
            addCriterion("guide_step >", value, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepGreaterThanOrEqualTo(Integer value) {
            addCriterion("guide_step >=", value, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepLessThan(Integer value) {
            addCriterion("guide_step <", value, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepLessThanOrEqualTo(Integer value) {
            addCriterion("guide_step <=", value, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepIn(List<Integer> values) {
            addCriterion("guide_step in", values, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepNotIn(List<Integer> values) {
            addCriterion("guide_step not in", values, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepBetween(Integer value1, Integer value2) {
            addCriterion("guide_step between", value1, value2, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideStepNotBetween(Integer value1, Integer value2) {
            addCriterion("guide_step not between", value1, value2, "guideStep");
            return (Criteria) this;
        }

        public Criteria andGuideNumIsNull() {
            addCriterion("guide_num is null");
            return (Criteria) this;
        }

        public Criteria andGuideNumIsNotNull() {
            addCriterion("guide_num is not null");
            return (Criteria) this;
        }

        public Criteria andGuideNumEqualTo(Integer value) {
            addCriterion("guide_num =", value, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumNotEqualTo(Integer value) {
            addCriterion("guide_num <>", value, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumGreaterThan(Integer value) {
            addCriterion("guide_num >", value, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("guide_num >=", value, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumLessThan(Integer value) {
            addCriterion("guide_num <", value, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumLessThanOrEqualTo(Integer value) {
            addCriterion("guide_num <=", value, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumIn(List<Integer> values) {
            addCriterion("guide_num in", values, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumNotIn(List<Integer> values) {
            addCriterion("guide_num not in", values, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumBetween(Integer value1, Integer value2) {
            addCriterion("guide_num between", value1, value2, "guideNum");
            return (Criteria) this;
        }

        public Criteria andGuideNumNotBetween(Integer value1, Integer value2) {
            addCriterion("guide_num not between", value1, value2, "guideNum");
            return (Criteria) this;
        }

        public Criteria andDataOptionIsNull() {
            addCriterion("data_option is null");
            return (Criteria) this;
        }

        public Criteria andDataOptionIsNotNull() {
            addCriterion("data_option is not null");
            return (Criteria) this;
        }

        public Criteria andDataOptionEqualTo(String value) {
            addCriterion("data_option =", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionNotEqualTo(String value) {
            addCriterion("data_option <>", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionGreaterThan(String value) {
            addCriterion("data_option >", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionGreaterThanOrEqualTo(String value) {
            addCriterion("data_option >=", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionLessThan(String value) {
            addCriterion("data_option <", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionLessThanOrEqualTo(String value) {
            addCriterion("data_option <=", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionLike(String value) {
            addCriterion("data_option like", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionNotLike(String value) {
            addCriterion("data_option not like", value, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionIn(List<String> values) {
            addCriterion("data_option in", values, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionNotIn(List<String> values) {
            addCriterion("data_option not in", values, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionBetween(String value1, String value2) {
            addCriterion("data_option between", value1, value2, "dataOption");
            return (Criteria) this;
        }

        public Criteria andDataOptionNotBetween(String value1, String value2) {
            addCriterion("data_option not between", value1, value2, "dataOption");
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

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("`status` not between", value1, value2, "status");
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