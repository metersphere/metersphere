package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class LoadTestReportResultPartExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LoadTestReportResultPartExample() {
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

        public Criteria andReportKeyIsNull() {
            addCriterion("report_key is null");
            return (Criteria) this;
        }

        public Criteria andReportKeyIsNotNull() {
            addCriterion("report_key is not null");
            return (Criteria) this;
        }

        public Criteria andReportKeyEqualTo(String value) {
            addCriterion("report_key =", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyNotEqualTo(String value) {
            addCriterion("report_key <>", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyGreaterThan(String value) {
            addCriterion("report_key >", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyGreaterThanOrEqualTo(String value) {
            addCriterion("report_key >=", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyLessThan(String value) {
            addCriterion("report_key <", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyLessThanOrEqualTo(String value) {
            addCriterion("report_key <=", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyLike(String value) {
            addCriterion("report_key like", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyNotLike(String value) {
            addCriterion("report_key not like", value, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyIn(List<String> values) {
            addCriterion("report_key in", values, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyNotIn(List<String> values) {
            addCriterion("report_key not in", values, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyBetween(String value1, String value2) {
            addCriterion("report_key between", value1, value2, "reportKey");
            return (Criteria) this;
        }

        public Criteria andReportKeyNotBetween(String value1, String value2) {
            addCriterion("report_key not between", value1, value2, "reportKey");
            return (Criteria) this;
        }

        public Criteria andResourceIndexIsNull() {
            addCriterion("resource_index is null");
            return (Criteria) this;
        }

        public Criteria andResourceIndexIsNotNull() {
            addCriterion("resource_index is not null");
            return (Criteria) this;
        }

        public Criteria andResourceIndexEqualTo(Integer value) {
            addCriterion("resource_index =", value, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexNotEqualTo(Integer value) {
            addCriterion("resource_index <>", value, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexGreaterThan(Integer value) {
            addCriterion("resource_index >", value, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexGreaterThanOrEqualTo(Integer value) {
            addCriterion("resource_index >=", value, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexLessThan(Integer value) {
            addCriterion("resource_index <", value, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexLessThanOrEqualTo(Integer value) {
            addCriterion("resource_index <=", value, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexIn(List<Integer> values) {
            addCriterion("resource_index in", values, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexNotIn(List<Integer> values) {
            addCriterion("resource_index not in", values, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexBetween(Integer value1, Integer value2) {
            addCriterion("resource_index between", value1, value2, "resourceIndex");
            return (Criteria) this;
        }

        public Criteria andResourceIndexNotBetween(Integer value1, Integer value2) {
            addCriterion("resource_index not between", value1, value2, "resourceIndex");
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