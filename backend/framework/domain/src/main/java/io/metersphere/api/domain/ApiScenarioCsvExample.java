package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiScenarioCsvExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiScenarioCsvExample() {
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

        public Criteria andFileIdIsNull() {
            addCriterion("file_id is null");
            return (Criteria) this;
        }

        public Criteria andFileIdIsNotNull() {
            addCriterion("file_id is not null");
            return (Criteria) this;
        }

        public Criteria andFileIdEqualTo(String value) {
            addCriterion("file_id =", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotEqualTo(String value) {
            addCriterion("file_id <>", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThan(String value) {
            addCriterion("file_id >", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThanOrEqualTo(String value) {
            addCriterion("file_id >=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThan(String value) {
            addCriterion("file_id <", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThanOrEqualTo(String value) {
            addCriterion("file_id <=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLike(String value) {
            addCriterion("file_id like", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotLike(String value) {
            addCriterion("file_id not like", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdIn(List<String> values) {
            addCriterion("file_id in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotIn(List<String> values) {
            addCriterion("file_id not in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdBetween(String value1, String value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotBetween(String value1, String value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdIsNull() {
            addCriterion("scenario_id is null");
            return (Criteria) this;
        }

        public Criteria andScenarioIdIsNotNull() {
            addCriterion("scenario_id is not null");
            return (Criteria) this;
        }

        public Criteria andScenarioIdEqualTo(String value) {
            addCriterion("scenario_id =", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotEqualTo(String value) {
            addCriterion("scenario_id <>", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdGreaterThan(String value) {
            addCriterion("scenario_id >", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdGreaterThanOrEqualTo(String value) {
            addCriterion("scenario_id >=", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdLessThan(String value) {
            addCriterion("scenario_id <", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdLessThanOrEqualTo(String value) {
            addCriterion("scenario_id <=", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdLike(String value) {
            addCriterion("scenario_id like", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotLike(String value) {
            addCriterion("scenario_id not like", value, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdIn(List<String> values) {
            addCriterion("scenario_id in", values, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotIn(List<String> values) {
            addCriterion("scenario_id not in", values, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdBetween(String value1, String value2) {
            addCriterion("scenario_id between", value1, value2, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andScenarioIdNotBetween(String value1, String value2) {
            addCriterion("scenario_id not between", value1, value2, "scenarioId");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andFileNameIsNull() {
            addCriterion("file_name is null");
            return (Criteria) this;
        }

        public Criteria andFileNameIsNotNull() {
            addCriterion("file_name is not null");
            return (Criteria) this;
        }

        public Criteria andFileNameEqualTo(String value) {
            addCriterion("file_name =", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotEqualTo(String value) {
            addCriterion("file_name <>", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameGreaterThan(String value) {
            addCriterion("file_name >", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameGreaterThanOrEqualTo(String value) {
            addCriterion("file_name >=", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLessThan(String value) {
            addCriterion("file_name <", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLessThanOrEqualTo(String value) {
            addCriterion("file_name <=", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLike(String value) {
            addCriterion("file_name like", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotLike(String value) {
            addCriterion("file_name not like", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameIn(List<String> values) {
            addCriterion("file_name in", values, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotIn(List<String> values) {
            addCriterion("file_name not in", values, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameBetween(String value1, String value2) {
            addCriterion("file_name between", value1, value2, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotBetween(String value1, String value2) {
            addCriterion("file_name not between", value1, value2, "fileName");
            return (Criteria) this;
        }

        public Criteria andScopeIsNull() {
            addCriterion("`scope` is null");
            return (Criteria) this;
        }

        public Criteria andScopeIsNotNull() {
            addCriterion("`scope` is not null");
            return (Criteria) this;
        }

        public Criteria andScopeEqualTo(String value) {
            addCriterion("`scope` =", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeNotEqualTo(String value) {
            addCriterion("`scope` <>", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeGreaterThan(String value) {
            addCriterion("`scope` >", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeGreaterThanOrEqualTo(String value) {
            addCriterion("`scope` >=", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeLessThan(String value) {
            addCriterion("`scope` <", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeLessThanOrEqualTo(String value) {
            addCriterion("`scope` <=", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeLike(String value) {
            addCriterion("`scope` like", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeNotLike(String value) {
            addCriterion("`scope` not like", value, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeIn(List<String> values) {
            addCriterion("`scope` in", values, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeNotIn(List<String> values) {
            addCriterion("`scope` not in", values, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeBetween(String value1, String value2) {
            addCriterion("`scope` between", value1, value2, "scope");
            return (Criteria) this;
        }

        public Criteria andScopeNotBetween(String value1, String value2) {
            addCriterion("`scope` not between", value1, value2, "scope");
            return (Criteria) this;
        }

        public Criteria andEnableIsNull() {
            addCriterion("`enable` is null");
            return (Criteria) this;
        }

        public Criteria andEnableIsNotNull() {
            addCriterion("`enable` is not null");
            return (Criteria) this;
        }

        public Criteria andEnableEqualTo(Boolean value) {
            addCriterion("`enable` =", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableNotEqualTo(Boolean value) {
            addCriterion("`enable` <>", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableGreaterThan(Boolean value) {
            addCriterion("`enable` >", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("`enable` >=", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableLessThan(Boolean value) {
            addCriterion("`enable` <", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableLessThanOrEqualTo(Boolean value) {
            addCriterion("`enable` <=", value, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableIn(List<Boolean> values) {
            addCriterion("`enable` in", values, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableNotIn(List<Boolean> values) {
            addCriterion("`enable` not in", values, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableBetween(Boolean value1, Boolean value2) {
            addCriterion("`enable` between", value1, value2, "enable");
            return (Criteria) this;
        }

        public Criteria andEnableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("`enable` not between", value1, value2, "enable");
            return (Criteria) this;
        }

        public Criteria andAssociationIsNull() {
            addCriterion("association is null");
            return (Criteria) this;
        }

        public Criteria andAssociationIsNotNull() {
            addCriterion("association is not null");
            return (Criteria) this;
        }

        public Criteria andAssociationEqualTo(Boolean value) {
            addCriterion("association =", value, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationNotEqualTo(Boolean value) {
            addCriterion("association <>", value, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationGreaterThan(Boolean value) {
            addCriterion("association >", value, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationGreaterThanOrEqualTo(Boolean value) {
            addCriterion("association >=", value, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationLessThan(Boolean value) {
            addCriterion("association <", value, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationLessThanOrEqualTo(Boolean value) {
            addCriterion("association <=", value, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationIn(List<Boolean> values) {
            addCriterion("association in", values, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationNotIn(List<Boolean> values) {
            addCriterion("association not in", values, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationBetween(Boolean value1, Boolean value2) {
            addCriterion("association between", value1, value2, "association");
            return (Criteria) this;
        }

        public Criteria andAssociationNotBetween(Boolean value1, Boolean value2) {
            addCriterion("association not between", value1, value2, "association");
            return (Criteria) this;
        }

        public Criteria andEncodingIsNull() {
            addCriterion("`encoding` is null");
            return (Criteria) this;
        }

        public Criteria andEncodingIsNotNull() {
            addCriterion("`encoding` is not null");
            return (Criteria) this;
        }

        public Criteria andEncodingEqualTo(String value) {
            addCriterion("`encoding` =", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingNotEqualTo(String value) {
            addCriterion("`encoding` <>", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingGreaterThan(String value) {
            addCriterion("`encoding` >", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingGreaterThanOrEqualTo(String value) {
            addCriterion("`encoding` >=", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingLessThan(String value) {
            addCriterion("`encoding` <", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingLessThanOrEqualTo(String value) {
            addCriterion("`encoding` <=", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingLike(String value) {
            addCriterion("`encoding` like", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingNotLike(String value) {
            addCriterion("`encoding` not like", value, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingIn(List<String> values) {
            addCriterion("`encoding` in", values, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingNotIn(List<String> values) {
            addCriterion("`encoding` not in", values, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingBetween(String value1, String value2) {
            addCriterion("`encoding` between", value1, value2, "encoding");
            return (Criteria) this;
        }

        public Criteria andEncodingNotBetween(String value1, String value2) {
            addCriterion("`encoding` not between", value1, value2, "encoding");
            return (Criteria) this;
        }

        public Criteria andRandomIsNull() {
            addCriterion("random is null");
            return (Criteria) this;
        }

        public Criteria andRandomIsNotNull() {
            addCriterion("random is not null");
            return (Criteria) this;
        }

        public Criteria andRandomEqualTo(Boolean value) {
            addCriterion("random =", value, "random");
            return (Criteria) this;
        }

        public Criteria andRandomNotEqualTo(Boolean value) {
            addCriterion("random <>", value, "random");
            return (Criteria) this;
        }

        public Criteria andRandomGreaterThan(Boolean value) {
            addCriterion("random >", value, "random");
            return (Criteria) this;
        }

        public Criteria andRandomGreaterThanOrEqualTo(Boolean value) {
            addCriterion("random >=", value, "random");
            return (Criteria) this;
        }

        public Criteria andRandomLessThan(Boolean value) {
            addCriterion("random <", value, "random");
            return (Criteria) this;
        }

        public Criteria andRandomLessThanOrEqualTo(Boolean value) {
            addCriterion("random <=", value, "random");
            return (Criteria) this;
        }

        public Criteria andRandomIn(List<Boolean> values) {
            addCriterion("random in", values, "random");
            return (Criteria) this;
        }

        public Criteria andRandomNotIn(List<Boolean> values) {
            addCriterion("random not in", values, "random");
            return (Criteria) this;
        }

        public Criteria andRandomBetween(Boolean value1, Boolean value2) {
            addCriterion("random between", value1, value2, "random");
            return (Criteria) this;
        }

        public Criteria andRandomNotBetween(Boolean value1, Boolean value2) {
            addCriterion("random not between", value1, value2, "random");
            return (Criteria) this;
        }

        public Criteria andVariableNamesIsNull() {
            addCriterion("variable_names is null");
            return (Criteria) this;
        }

        public Criteria andVariableNamesIsNotNull() {
            addCriterion("variable_names is not null");
            return (Criteria) this;
        }

        public Criteria andVariableNamesEqualTo(String value) {
            addCriterion("variable_names =", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesNotEqualTo(String value) {
            addCriterion("variable_names <>", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesGreaterThan(String value) {
            addCriterion("variable_names >", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesGreaterThanOrEqualTo(String value) {
            addCriterion("variable_names >=", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesLessThan(String value) {
            addCriterion("variable_names <", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesLessThanOrEqualTo(String value) {
            addCriterion("variable_names <=", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesLike(String value) {
            addCriterion("variable_names like", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesNotLike(String value) {
            addCriterion("variable_names not like", value, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesIn(List<String> values) {
            addCriterion("variable_names in", values, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesNotIn(List<String> values) {
            addCriterion("variable_names not in", values, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesBetween(String value1, String value2) {
            addCriterion("variable_names between", value1, value2, "variableNames");
            return (Criteria) this;
        }

        public Criteria andVariableNamesNotBetween(String value1, String value2) {
            addCriterion("variable_names not between", value1, value2, "variableNames");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineIsNull() {
            addCriterion("ignore_first_line is null");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineIsNotNull() {
            addCriterion("ignore_first_line is not null");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineEqualTo(Boolean value) {
            addCriterion("ignore_first_line =", value, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineNotEqualTo(Boolean value) {
            addCriterion("ignore_first_line <>", value, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineGreaterThan(Boolean value) {
            addCriterion("ignore_first_line >", value, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineGreaterThanOrEqualTo(Boolean value) {
            addCriterion("ignore_first_line >=", value, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineLessThan(Boolean value) {
            addCriterion("ignore_first_line <", value, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineLessThanOrEqualTo(Boolean value) {
            addCriterion("ignore_first_line <=", value, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineIn(List<Boolean> values) {
            addCriterion("ignore_first_line in", values, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineNotIn(List<Boolean> values) {
            addCriterion("ignore_first_line not in", values, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineBetween(Boolean value1, Boolean value2) {
            addCriterion("ignore_first_line between", value1, value2, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andIgnoreFirstLineNotBetween(Boolean value1, Boolean value2) {
            addCriterion("ignore_first_line not between", value1, value2, "ignoreFirstLine");
            return (Criteria) this;
        }

        public Criteria andDelimiterIsNull() {
            addCriterion("`delimiter` is null");
            return (Criteria) this;
        }

        public Criteria andDelimiterIsNotNull() {
            addCriterion("`delimiter` is not null");
            return (Criteria) this;
        }

        public Criteria andDelimiterEqualTo(String value) {
            addCriterion("`delimiter` =", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterNotEqualTo(String value) {
            addCriterion("`delimiter` <>", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterGreaterThan(String value) {
            addCriterion("`delimiter` >", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterGreaterThanOrEqualTo(String value) {
            addCriterion("`delimiter` >=", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterLessThan(String value) {
            addCriterion("`delimiter` <", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterLessThanOrEqualTo(String value) {
            addCriterion("`delimiter` <=", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterLike(String value) {
            addCriterion("`delimiter` like", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterNotLike(String value) {
            addCriterion("`delimiter` not like", value, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterIn(List<String> values) {
            addCriterion("`delimiter` in", values, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterNotIn(List<String> values) {
            addCriterion("`delimiter` not in", values, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterBetween(String value1, String value2) {
            addCriterion("`delimiter` between", value1, value2, "delimiter");
            return (Criteria) this;
        }

        public Criteria andDelimiterNotBetween(String value1, String value2) {
            addCriterion("`delimiter` not between", value1, value2, "delimiter");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataIsNull() {
            addCriterion("allow_quoted_data is null");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataIsNotNull() {
            addCriterion("allow_quoted_data is not null");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataEqualTo(Boolean value) {
            addCriterion("allow_quoted_data =", value, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataNotEqualTo(Boolean value) {
            addCriterion("allow_quoted_data <>", value, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataGreaterThan(Boolean value) {
            addCriterion("allow_quoted_data >", value, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataGreaterThanOrEqualTo(Boolean value) {
            addCriterion("allow_quoted_data >=", value, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataLessThan(Boolean value) {
            addCriterion("allow_quoted_data <", value, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataLessThanOrEqualTo(Boolean value) {
            addCriterion("allow_quoted_data <=", value, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataIn(List<Boolean> values) {
            addCriterion("allow_quoted_data in", values, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataNotIn(List<Boolean> values) {
            addCriterion("allow_quoted_data not in", values, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataBetween(Boolean value1, Boolean value2) {
            addCriterion("allow_quoted_data between", value1, value2, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andAllowQuotedDataNotBetween(Boolean value1, Boolean value2) {
            addCriterion("allow_quoted_data not between", value1, value2, "allowQuotedData");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofIsNull() {
            addCriterion("recycle_on_eof is null");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofIsNotNull() {
            addCriterion("recycle_on_eof is not null");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofEqualTo(Boolean value) {
            addCriterion("recycle_on_eof =", value, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofNotEqualTo(Boolean value) {
            addCriterion("recycle_on_eof <>", value, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofGreaterThan(Boolean value) {
            addCriterion("recycle_on_eof >", value, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofGreaterThanOrEqualTo(Boolean value) {
            addCriterion("recycle_on_eof >=", value, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofLessThan(Boolean value) {
            addCriterion("recycle_on_eof <", value, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofLessThanOrEqualTo(Boolean value) {
            addCriterion("recycle_on_eof <=", value, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofIn(List<Boolean> values) {
            addCriterion("recycle_on_eof in", values, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofNotIn(List<Boolean> values) {
            addCriterion("recycle_on_eof not in", values, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofBetween(Boolean value1, Boolean value2) {
            addCriterion("recycle_on_eof between", value1, value2, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andRecycleOnEofNotBetween(Boolean value1, Boolean value2) {
            addCriterion("recycle_on_eof not between", value1, value2, "recycleOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofIsNull() {
            addCriterion("stop_thread_on_eof is null");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofIsNotNull() {
            addCriterion("stop_thread_on_eof is not null");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofEqualTo(Boolean value) {
            addCriterion("stop_thread_on_eof =", value, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofNotEqualTo(Boolean value) {
            addCriterion("stop_thread_on_eof <>", value, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofGreaterThan(Boolean value) {
            addCriterion("stop_thread_on_eof >", value, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofGreaterThanOrEqualTo(Boolean value) {
            addCriterion("stop_thread_on_eof >=", value, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofLessThan(Boolean value) {
            addCriterion("stop_thread_on_eof <", value, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofLessThanOrEqualTo(Boolean value) {
            addCriterion("stop_thread_on_eof <=", value, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofIn(List<Boolean> values) {
            addCriterion("stop_thread_on_eof in", values, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofNotIn(List<Boolean> values) {
            addCriterion("stop_thread_on_eof not in", values, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofBetween(Boolean value1, Boolean value2) {
            addCriterion("stop_thread_on_eof between", value1, value2, "stopThreadOnEof");
            return (Criteria) this;
        }

        public Criteria andStopThreadOnEofNotBetween(Boolean value1, Boolean value2) {
            addCriterion("stop_thread_on_eof not between", value1, value2, "stopThreadOnEof");
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