package io.metersphere.system.domain;

import java.util.ArrayList;
import java.util.List;

public class StatusDefinitionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public StatusDefinitionExample() {
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

        public Criteria andStatusIdIsNull() {
            addCriterion("status_id is null");
            return (Criteria) this;
        }

        public Criteria andStatusIdIsNotNull() {
            addCriterion("status_id is not null");
            return (Criteria) this;
        }

        public Criteria andStatusIdEqualTo(String value) {
            addCriterion("status_id =", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdNotEqualTo(String value) {
            addCriterion("status_id <>", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdGreaterThan(String value) {
            addCriterion("status_id >", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdGreaterThanOrEqualTo(String value) {
            addCriterion("status_id >=", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdLessThan(String value) {
            addCriterion("status_id <", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdLessThanOrEqualTo(String value) {
            addCriterion("status_id <=", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdLike(String value) {
            addCriterion("status_id like", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdNotLike(String value) {
            addCriterion("status_id not like", value, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdIn(List<String> values) {
            addCriterion("status_id in", values, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdNotIn(List<String> values) {
            addCriterion("status_id not in", values, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdBetween(String value1, String value2) {
            addCriterion("status_id between", value1, value2, "statusId");
            return (Criteria) this;
        }

        public Criteria andStatusIdNotBetween(String value1, String value2) {
            addCriterion("status_id not between", value1, value2, "statusId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdIsNull() {
            addCriterion("definition_id is null");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdIsNotNull() {
            addCriterion("definition_id is not null");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdEqualTo(String value) {
            addCriterion("definition_id =", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdNotEqualTo(String value) {
            addCriterion("definition_id <>", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdGreaterThan(String value) {
            addCriterion("definition_id >", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdGreaterThanOrEqualTo(String value) {
            addCriterion("definition_id >=", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdLessThan(String value) {
            addCriterion("definition_id <", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdLessThanOrEqualTo(String value) {
            addCriterion("definition_id <=", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdLike(String value) {
            addCriterion("definition_id like", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdNotLike(String value) {
            addCriterion("definition_id not like", value, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdIn(List<String> values) {
            addCriterion("definition_id in", values, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdNotIn(List<String> values) {
            addCriterion("definition_id not in", values, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdBetween(String value1, String value2) {
            addCriterion("definition_id between", value1, value2, "definitionId");
            return (Criteria) this;
        }

        public Criteria andDefinitionIdNotBetween(String value1, String value2) {
            addCriterion("definition_id not between", value1, value2, "definitionId");
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