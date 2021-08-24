package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class TestPlanReportContentExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TestPlanReportContentExample() {
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

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(Long value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(Long value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(Long value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(Long value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(Long value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<Long> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<Long> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(Long value1, Long value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(Long value1, Long value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andCaseCountIsNull() {
            addCriterion("case_count is null");
            return (Criteria) this;
        }

        public Criteria andCaseCountIsNotNull() {
            addCriterion("case_count is not null");
            return (Criteria) this;
        }

        public Criteria andCaseCountEqualTo(Long value) {
            addCriterion("case_count =", value, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountNotEqualTo(Long value) {
            addCriterion("case_count <>", value, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountGreaterThan(Long value) {
            addCriterion("case_count >", value, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountGreaterThanOrEqualTo(Long value) {
            addCriterion("case_count >=", value, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountLessThan(Long value) {
            addCriterion("case_count <", value, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountLessThanOrEqualTo(Long value) {
            addCriterion("case_count <=", value, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountIn(List<Long> values) {
            addCriterion("case_count in", values, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountNotIn(List<Long> values) {
            addCriterion("case_count not in", values, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountBetween(Long value1, Long value2) {
            addCriterion("case_count between", value1, value2, "caseCount");
            return (Criteria) this;
        }

        public Criteria andCaseCountNotBetween(Long value1, Long value2) {
            addCriterion("case_count not between", value1, value2, "caseCount");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return (Criteria) this;
        }

        public Criteria andEndTimeEqualTo(Long value) {
            addCriterion("end_time =", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotEqualTo(Long value) {
            addCriterion("end_time <>", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThan(Long value) {
            addCriterion("end_time >", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("end_time >=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThan(Long value) {
            addCriterion("end_time <", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(Long value) {
            addCriterion("end_time <=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIn(List<Long> values) {
            addCriterion("end_time in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotIn(List<Long> values) {
            addCriterion("end_time not in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeBetween(Long value1, Long value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotBetween(Long value1, Long value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andExecuteRateIsNull() {
            addCriterion("execute_rate is null");
            return (Criteria) this;
        }

        public Criteria andExecuteRateIsNotNull() {
            addCriterion("execute_rate is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteRateEqualTo(Double value) {
            addCriterion("execute_rate =", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateNotEqualTo(Double value) {
            addCriterion("execute_rate <>", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateGreaterThan(Double value) {
            addCriterion("execute_rate >", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateGreaterThanOrEqualTo(Double value) {
            addCriterion("execute_rate >=", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateLessThan(Double value) {
            addCriterion("execute_rate <", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateLessThanOrEqualTo(Double value) {
            addCriterion("execute_rate <=", value, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateIn(List<Double> values) {
            addCriterion("execute_rate in", values, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateNotIn(List<Double> values) {
            addCriterion("execute_rate not in", values, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateBetween(Double value1, Double value2) {
            addCriterion("execute_rate between", value1, value2, "executeRate");
            return (Criteria) this;
        }

        public Criteria andExecuteRateNotBetween(Double value1, Double value2) {
            addCriterion("execute_rate not between", value1, value2, "executeRate");
            return (Criteria) this;
        }

        public Criteria andPassRateIsNull() {
            addCriterion("pass_rate is null");
            return (Criteria) this;
        }

        public Criteria andPassRateIsNotNull() {
            addCriterion("pass_rate is not null");
            return (Criteria) this;
        }

        public Criteria andPassRateEqualTo(Double value) {
            addCriterion("pass_rate =", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotEqualTo(Double value) {
            addCriterion("pass_rate <>", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThan(Double value) {
            addCriterion("pass_rate >", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThanOrEqualTo(Double value) {
            addCriterion("pass_rate >=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThan(Double value) {
            addCriterion("pass_rate <", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThanOrEqualTo(Double value) {
            addCriterion("pass_rate <=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateIn(List<Double> values) {
            addCriterion("pass_rate in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotIn(List<Double> values) {
            addCriterion("pass_rate not in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateBetween(Double value1, Double value2) {
            addCriterion("pass_rate between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotBetween(Double value1, Double value2) {
            addCriterion("pass_rate not between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueIsNull() {
            addCriterion("is_third_part_issue is null");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueIsNotNull() {
            addCriterion("is_third_part_issue is not null");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueEqualTo(Boolean value) {
            addCriterion("is_third_part_issue =", value, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueNotEqualTo(Boolean value) {
            addCriterion("is_third_part_issue <>", value, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueGreaterThan(Boolean value) {
            addCriterion("is_third_part_issue >", value, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_third_part_issue >=", value, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueLessThan(Boolean value) {
            addCriterion("is_third_part_issue <", value, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueLessThanOrEqualTo(Boolean value) {
            addCriterion("is_third_part_issue <=", value, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueIn(List<Boolean> values) {
            addCriterion("is_third_part_issue in", values, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueNotIn(List<Boolean> values) {
            addCriterion("is_third_part_issue not in", values, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueBetween(Boolean value1, Boolean value2) {
            addCriterion("is_third_part_issue between", value1, value2, "isThirdPartIssue");
            return (Criteria) this;
        }

        public Criteria andIsThirdPartIssueNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_third_part_issue not between", value1, value2, "isThirdPartIssue");
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