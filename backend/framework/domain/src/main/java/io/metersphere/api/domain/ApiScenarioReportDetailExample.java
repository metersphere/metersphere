package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiScenarioReportDetailExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiScenarioReportDetailExample() {
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

        public Criteria andStepIdIsNull() {
            addCriterion("step_id is null");
            return (Criteria) this;
        }

        public Criteria andStepIdIsNotNull() {
            addCriterion("step_id is not null");
            return (Criteria) this;
        }

        public Criteria andStepIdEqualTo(String value) {
            addCriterion("step_id =", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdNotEqualTo(String value) {
            addCriterion("step_id <>", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdGreaterThan(String value) {
            addCriterion("step_id >", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdGreaterThanOrEqualTo(String value) {
            addCriterion("step_id >=", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdLessThan(String value) {
            addCriterion("step_id <", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdLessThanOrEqualTo(String value) {
            addCriterion("step_id <=", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdLike(String value) {
            addCriterion("step_id like", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdNotLike(String value) {
            addCriterion("step_id not like", value, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdIn(List<String> values) {
            addCriterion("step_id in", values, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdNotIn(List<String> values) {
            addCriterion("step_id not in", values, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdBetween(String value1, String value2) {
            addCriterion("step_id between", value1, value2, "stepId");
            return (Criteria) this;
        }

        public Criteria andStepIdNotBetween(String value1, String value2) {
            addCriterion("step_id not between", value1, value2, "stepId");
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

        public Criteria andStatusEqualTo(String value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("`status` like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("`status` not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andFakeCodeIsNull() {
            addCriterion("fake_code is null");
            return (Criteria) this;
        }

        public Criteria andFakeCodeIsNotNull() {
            addCriterion("fake_code is not null");
            return (Criteria) this;
        }

        public Criteria andFakeCodeEqualTo(String value) {
            addCriterion("fake_code =", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeNotEqualTo(String value) {
            addCriterion("fake_code <>", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeGreaterThan(String value) {
            addCriterion("fake_code >", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeGreaterThanOrEqualTo(String value) {
            addCriterion("fake_code >=", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeLessThan(String value) {
            addCriterion("fake_code <", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeLessThanOrEqualTo(String value) {
            addCriterion("fake_code <=", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeLike(String value) {
            addCriterion("fake_code like", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeNotLike(String value) {
            addCriterion("fake_code not like", value, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeIn(List<String> values) {
            addCriterion("fake_code in", values, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeNotIn(List<String> values) {
            addCriterion("fake_code not in", values, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeBetween(String value1, String value2) {
            addCriterion("fake_code between", value1, value2, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andFakeCodeNotBetween(String value1, String value2) {
            addCriterion("fake_code not between", value1, value2, "fakeCode");
            return (Criteria) this;
        }

        public Criteria andRequestNameIsNull() {
            addCriterion("request_name is null");
            return (Criteria) this;
        }

        public Criteria andRequestNameIsNotNull() {
            addCriterion("request_name is not null");
            return (Criteria) this;
        }

        public Criteria andRequestNameEqualTo(String value) {
            addCriterion("request_name =", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameNotEqualTo(String value) {
            addCriterion("request_name <>", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameGreaterThan(String value) {
            addCriterion("request_name >", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameGreaterThanOrEqualTo(String value) {
            addCriterion("request_name >=", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameLessThan(String value) {
            addCriterion("request_name <", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameLessThanOrEqualTo(String value) {
            addCriterion("request_name <=", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameLike(String value) {
            addCriterion("request_name like", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameNotLike(String value) {
            addCriterion("request_name not like", value, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameIn(List<String> values) {
            addCriterion("request_name in", values, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameNotIn(List<String> values) {
            addCriterion("request_name not in", values, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameBetween(String value1, String value2) {
            addCriterion("request_name between", value1, value2, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestNameNotBetween(String value1, String value2) {
            addCriterion("request_name not between", value1, value2, "requestName");
            return (Criteria) this;
        }

        public Criteria andRequestTimeIsNull() {
            addCriterion("request_time is null");
            return (Criteria) this;
        }

        public Criteria andRequestTimeIsNotNull() {
            addCriterion("request_time is not null");
            return (Criteria) this;
        }

        public Criteria andRequestTimeEqualTo(Long value) {
            addCriterion("request_time =", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeNotEqualTo(Long value) {
            addCriterion("request_time <>", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeGreaterThan(Long value) {
            addCriterion("request_time >", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("request_time >=", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeLessThan(Long value) {
            addCriterion("request_time <", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeLessThanOrEqualTo(Long value) {
            addCriterion("request_time <=", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeIn(List<Long> values) {
            addCriterion("request_time in", values, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeNotIn(List<Long> values) {
            addCriterion("request_time not in", values, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeBetween(Long value1, Long value2) {
            addCriterion("request_time between", value1, value2, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeNotBetween(Long value1, Long value2) {
            addCriterion("request_time not between", value1, value2, "requestTime");
            return (Criteria) this;
        }

        public Criteria andCodeIsNull() {
            addCriterion("code is null");
            return (Criteria) this;
        }

        public Criteria andCodeIsNotNull() {
            addCriterion("code is not null");
            return (Criteria) this;
        }

        public Criteria andCodeEqualTo(String value) {
            addCriterion("code =", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeNotEqualTo(String value) {
            addCriterion("code <>", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeGreaterThan(String value) {
            addCriterion("code >", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeGreaterThanOrEqualTo(String value) {
            addCriterion("code >=", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeLessThan(String value) {
            addCriterion("code <", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeLessThanOrEqualTo(String value) {
            addCriterion("code <=", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeLike(String value) {
            addCriterion("code like", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeNotLike(String value) {
            addCriterion("code not like", value, "code");
            return (Criteria) this;
        }

        public Criteria andCodeIn(List<String> values) {
            addCriterion("code in", values, "code");
            return (Criteria) this;
        }

        public Criteria andCodeNotIn(List<String> values) {
            addCriterion("code not in", values, "code");
            return (Criteria) this;
        }

        public Criteria andCodeBetween(String value1, String value2) {
            addCriterion("code between", value1, value2, "code");
            return (Criteria) this;
        }

        public Criteria andCodeNotBetween(String value1, String value2) {
            addCriterion("code not between", value1, value2, "code");
            return (Criteria) this;
        }

        public Criteria andResponseSizeIsNull() {
            addCriterion("response_size is null");
            return (Criteria) this;
        }

        public Criteria andResponseSizeIsNotNull() {
            addCriterion("response_size is not null");
            return (Criteria) this;
        }

        public Criteria andResponseSizeEqualTo(Long value) {
            addCriterion("response_size =", value, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeNotEqualTo(Long value) {
            addCriterion("response_size <>", value, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeGreaterThan(Long value) {
            addCriterion("response_size >", value, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeGreaterThanOrEqualTo(Long value) {
            addCriterion("response_size >=", value, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeLessThan(Long value) {
            addCriterion("response_size <", value, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeLessThanOrEqualTo(Long value) {
            addCriterion("response_size <=", value, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeIn(List<Long> values) {
            addCriterion("response_size in", values, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeNotIn(List<Long> values) {
            addCriterion("response_size not in", values, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeBetween(Long value1, Long value2) {
            addCriterion("response_size between", value1, value2, "responseSize");
            return (Criteria) this;
        }

        public Criteria andResponseSizeNotBetween(Long value1, Long value2) {
            addCriterion("response_size not between", value1, value2, "responseSize");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierIsNull() {
            addCriterion("script_identifier is null");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierIsNotNull() {
            addCriterion("script_identifier is not null");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierEqualTo(String value) {
            addCriterion("script_identifier =", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotEqualTo(String value) {
            addCriterion("script_identifier <>", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierGreaterThan(String value) {
            addCriterion("script_identifier >", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierGreaterThanOrEqualTo(String value) {
            addCriterion("script_identifier >=", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierLessThan(String value) {
            addCriterion("script_identifier <", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierLessThanOrEqualTo(String value) {
            addCriterion("script_identifier <=", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierLike(String value) {
            addCriterion("script_identifier like", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotLike(String value) {
            addCriterion("script_identifier not like", value, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierIn(List<String> values) {
            addCriterion("script_identifier in", values, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotIn(List<String> values) {
            addCriterion("script_identifier not in", values, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierBetween(String value1, String value2) {
            addCriterion("script_identifier between", value1, value2, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andScriptIdentifierNotBetween(String value1, String value2) {
            addCriterion("script_identifier not between", value1, value2, "scriptIdentifier");
            return (Criteria) this;
        }

        public Criteria andSortIsNull() {
            addCriterion("sort is null");
            return (Criteria) this;
        }

        public Criteria andSortIsNotNull() {
            addCriterion("sort is not null");
            return (Criteria) this;
        }

        public Criteria andSortEqualTo(Long value) {
            addCriterion("sort =", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortNotEqualTo(Long value) {
            addCriterion("sort <>", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortGreaterThan(Long value) {
            addCriterion("sort >", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortGreaterThanOrEqualTo(Long value) {
            addCriterion("sort >=", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortLessThan(Long value) {
            addCriterion("sort <", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortLessThanOrEqualTo(Long value) {
            addCriterion("sort <=", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortIn(List<Long> values) {
            addCriterion("sort in", values, "sort");
            return (Criteria) this;
        }

        public Criteria andSortNotIn(List<Long> values) {
            addCriterion("sort not in", values, "sort");
            return (Criteria) this;
        }

        public Criteria andSortBetween(Long value1, Long value2) {
            addCriterion("sort between", value1, value2, "sort");
            return (Criteria) this;
        }

        public Criteria andSortNotBetween(Long value1, Long value2) {
            addCriterion("sort not between", value1, value2, "sort");
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