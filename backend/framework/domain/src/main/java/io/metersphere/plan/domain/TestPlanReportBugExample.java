package io.metersphere.plan.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanReportBugExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanReportBugExample() {
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

        public Criteria andTestPlanReportIdIsNull() {
            addCriterion("test_plan_report_id is null");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdIsNotNull() {
            addCriterion("test_plan_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdEqualTo(String value) {
            addCriterion("test_plan_report_id =", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotEqualTo(String value) {
            addCriterion("test_plan_report_id <>", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdGreaterThan(String value) {
            addCriterion("test_plan_report_id >", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("test_plan_report_id >=", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdLessThan(String value) {
            addCriterion("test_plan_report_id <", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdLessThanOrEqualTo(String value) {
            addCriterion("test_plan_report_id <=", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdLike(String value) {
            addCriterion("test_plan_report_id like", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotLike(String value) {
            addCriterion("test_plan_report_id not like", value, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdIn(List<String> values) {
            addCriterion("test_plan_report_id in", values, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotIn(List<String> values) {
            addCriterion("test_plan_report_id not in", values, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdBetween(String value1, String value2) {
            addCriterion("test_plan_report_id between", value1, value2, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andTestPlanReportIdNotBetween(String value1, String value2) {
            addCriterion("test_plan_report_id not between", value1, value2, "testPlanReportId");
            return (Criteria) this;
        }

        public Criteria andBugIdIsNull() {
            addCriterion("bug_id is null");
            return (Criteria) this;
        }

        public Criteria andBugIdIsNotNull() {
            addCriterion("bug_id is not null");
            return (Criteria) this;
        }

        public Criteria andBugIdEqualTo(String value) {
            addCriterion("bug_id =", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdNotEqualTo(String value) {
            addCriterion("bug_id <>", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdGreaterThan(String value) {
            addCriterion("bug_id >", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdGreaterThanOrEqualTo(String value) {
            addCriterion("bug_id >=", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdLessThan(String value) {
            addCriterion("bug_id <", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdLessThanOrEqualTo(String value) {
            addCriterion("bug_id <=", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdLike(String value) {
            addCriterion("bug_id like", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdNotLike(String value) {
            addCriterion("bug_id not like", value, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdIn(List<String> values) {
            addCriterion("bug_id in", values, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdNotIn(List<String> values) {
            addCriterion("bug_id not in", values, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdBetween(String value1, String value2) {
            addCriterion("bug_id between", value1, value2, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugIdNotBetween(String value1, String value2) {
            addCriterion("bug_id not between", value1, value2, "bugId");
            return (Criteria) this;
        }

        public Criteria andBugNumIsNull() {
            addCriterion("bug_num is null");
            return (Criteria) this;
        }

        public Criteria andBugNumIsNotNull() {
            addCriterion("bug_num is not null");
            return (Criteria) this;
        }

        public Criteria andBugNumEqualTo(Long value) {
            addCriterion("bug_num =", value, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumNotEqualTo(Long value) {
            addCriterion("bug_num <>", value, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumGreaterThan(Long value) {
            addCriterion("bug_num >", value, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumGreaterThanOrEqualTo(Long value) {
            addCriterion("bug_num >=", value, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumLessThan(Long value) {
            addCriterion("bug_num <", value, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumLessThanOrEqualTo(Long value) {
            addCriterion("bug_num <=", value, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumIn(List<Long> values) {
            addCriterion("bug_num in", values, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumNotIn(List<Long> values) {
            addCriterion("bug_num not in", values, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumBetween(Long value1, Long value2) {
            addCriterion("bug_num between", value1, value2, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugNumNotBetween(Long value1, Long value2) {
            addCriterion("bug_num not between", value1, value2, "bugNum");
            return (Criteria) this;
        }

        public Criteria andBugTitleIsNull() {
            addCriterion("bug_title is null");
            return (Criteria) this;
        }

        public Criteria andBugTitleIsNotNull() {
            addCriterion("bug_title is not null");
            return (Criteria) this;
        }

        public Criteria andBugTitleEqualTo(String value) {
            addCriterion("bug_title =", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleNotEqualTo(String value) {
            addCriterion("bug_title <>", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleGreaterThan(String value) {
            addCriterion("bug_title >", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleGreaterThanOrEqualTo(String value) {
            addCriterion("bug_title >=", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleLessThan(String value) {
            addCriterion("bug_title <", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleLessThanOrEqualTo(String value) {
            addCriterion("bug_title <=", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleLike(String value) {
            addCriterion("bug_title like", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleNotLike(String value) {
            addCriterion("bug_title not like", value, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleIn(List<String> values) {
            addCriterion("bug_title in", values, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleNotIn(List<String> values) {
            addCriterion("bug_title not in", values, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleBetween(String value1, String value2) {
            addCriterion("bug_title between", value1, value2, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugTitleNotBetween(String value1, String value2) {
            addCriterion("bug_title not between", value1, value2, "bugTitle");
            return (Criteria) this;
        }

        public Criteria andBugStatusIsNull() {
            addCriterion("bug_status is null");
            return (Criteria) this;
        }

        public Criteria andBugStatusIsNotNull() {
            addCriterion("bug_status is not null");
            return (Criteria) this;
        }

        public Criteria andBugStatusEqualTo(String value) {
            addCriterion("bug_status =", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusNotEqualTo(String value) {
            addCriterion("bug_status <>", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusGreaterThan(String value) {
            addCriterion("bug_status >", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusGreaterThanOrEqualTo(String value) {
            addCriterion("bug_status >=", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusLessThan(String value) {
            addCriterion("bug_status <", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusLessThanOrEqualTo(String value) {
            addCriterion("bug_status <=", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusLike(String value) {
            addCriterion("bug_status like", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusNotLike(String value) {
            addCriterion("bug_status not like", value, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusIn(List<String> values) {
            addCriterion("bug_status in", values, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusNotIn(List<String> values) {
            addCriterion("bug_status not in", values, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusBetween(String value1, String value2) {
            addCriterion("bug_status between", value1, value2, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugStatusNotBetween(String value1, String value2) {
            addCriterion("bug_status not between", value1, value2, "bugStatus");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserIsNull() {
            addCriterion("bug_handle_user is null");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserIsNotNull() {
            addCriterion("bug_handle_user is not null");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserEqualTo(String value) {
            addCriterion("bug_handle_user =", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserNotEqualTo(String value) {
            addCriterion("bug_handle_user <>", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserGreaterThan(String value) {
            addCriterion("bug_handle_user >", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserGreaterThanOrEqualTo(String value) {
            addCriterion("bug_handle_user >=", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserLessThan(String value) {
            addCriterion("bug_handle_user <", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserLessThanOrEqualTo(String value) {
            addCriterion("bug_handle_user <=", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserLike(String value) {
            addCriterion("bug_handle_user like", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserNotLike(String value) {
            addCriterion("bug_handle_user not like", value, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserIn(List<String> values) {
            addCriterion("bug_handle_user in", values, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserNotIn(List<String> values) {
            addCriterion("bug_handle_user not in", values, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserBetween(String value1, String value2) {
            addCriterion("bug_handle_user between", value1, value2, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugHandleUserNotBetween(String value1, String value2) {
            addCriterion("bug_handle_user not between", value1, value2, "bugHandleUser");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountIsNull() {
            addCriterion("bug_case_count is null");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountIsNotNull() {
            addCriterion("bug_case_count is not null");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountEqualTo(Long value) {
            addCriterion("bug_case_count =", value, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountNotEqualTo(Long value) {
            addCriterion("bug_case_count <>", value, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountGreaterThan(Long value) {
            addCriterion("bug_case_count >", value, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountGreaterThanOrEqualTo(Long value) {
            addCriterion("bug_case_count >=", value, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountLessThan(Long value) {
            addCriterion("bug_case_count <", value, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountLessThanOrEqualTo(Long value) {
            addCriterion("bug_case_count <=", value, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountIn(List<Long> values) {
            addCriterion("bug_case_count in", values, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountNotIn(List<Long> values) {
            addCriterion("bug_case_count not in", values, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountBetween(Long value1, Long value2) {
            addCriterion("bug_case_count between", value1, value2, "bugCaseCount");
            return (Criteria) this;
        }

        public Criteria andBugCaseCountNotBetween(Long value1, Long value2) {
            addCriterion("bug_case_count not between", value1, value2, "bugCaseCount");
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