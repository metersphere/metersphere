package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class EnvGroupExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public EnvGroupExample() {
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

        public Criteria andEnvGroupIdIsNull() {
            addCriterion("env_group_id is null");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdIsNotNull() {
            addCriterion("env_group_id is not null");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdEqualTo(String value) {
            addCriterion("env_group_id =", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdNotEqualTo(String value) {
            addCriterion("env_group_id <>", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdGreaterThan(String value) {
            addCriterion("env_group_id >", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdGreaterThanOrEqualTo(String value) {
            addCriterion("env_group_id >=", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdLessThan(String value) {
            addCriterion("env_group_id <", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdLessThanOrEqualTo(String value) {
            addCriterion("env_group_id <=", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdLike(String value) {
            addCriterion("env_group_id like", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdNotLike(String value) {
            addCriterion("env_group_id not like", value, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdIn(List<String> values) {
            addCriterion("env_group_id in", values, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdNotIn(List<String> values) {
            addCriterion("env_group_id not in", values, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdBetween(String value1, String value2) {
            addCriterion("env_group_id between", value1, value2, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupIdNotBetween(String value1, String value2) {
            addCriterion("env_group_id not between", value1, value2, "envGroupId");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameIsNull() {
            addCriterion("env_group_name is null");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameIsNotNull() {
            addCriterion("env_group_name is not null");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameEqualTo(String value) {
            addCriterion("env_group_name =", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameNotEqualTo(String value) {
            addCriterion("env_group_name <>", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameGreaterThan(String value) {
            addCriterion("env_group_name >", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameGreaterThanOrEqualTo(String value) {
            addCriterion("env_group_name >=", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameLessThan(String value) {
            addCriterion("env_group_name <", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameLessThanOrEqualTo(String value) {
            addCriterion("env_group_name <=", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameLike(String value) {
            addCriterion("env_group_name like", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameNotLike(String value) {
            addCriterion("env_group_name not like", value, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameIn(List<String> values) {
            addCriterion("env_group_name in", values, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameNotIn(List<String> values) {
            addCriterion("env_group_name not in", values, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameBetween(String value1, String value2) {
            addCriterion("env_group_name between", value1, value2, "envGroupName");
            return (Criteria) this;
        }

        public Criteria andEnvGroupNameNotBetween(String value1, String value2) {
            addCriterion("env_group_name not between", value1, value2, "envGroupName");
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