package io.metersphere.api.domain;

import java.util.ArrayList;
import java.util.List;

public class ApiDocShareExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ApiDocShareExample() {
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

        public Criteria andIsPrivateIsNull() {
            addCriterion("is_private is null");
            return (Criteria) this;
        }

        public Criteria andIsPrivateIsNotNull() {
            addCriterion("is_private is not null");
            return (Criteria) this;
        }

        public Criteria andIsPrivateEqualTo(Boolean value) {
            addCriterion("is_private =", value, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateNotEqualTo(Boolean value) {
            addCriterion("is_private <>", value, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateGreaterThan(Boolean value) {
            addCriterion("is_private >", value, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_private >=", value, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateLessThan(Boolean value) {
            addCriterion("is_private <", value, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateLessThanOrEqualTo(Boolean value) {
            addCriterion("is_private <=", value, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateIn(List<Boolean> values) {
            addCriterion("is_private in", values, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateNotIn(List<Boolean> values) {
            addCriterion("is_private not in", values, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateBetween(Boolean value1, Boolean value2) {
            addCriterion("is_private between", value1, value2, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andIsPrivateNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_private not between", value1, value2, "isPrivate");
            return (Criteria) this;
        }

        public Criteria andPasswordIsNull() {
            addCriterion("`password` is null");
            return (Criteria) this;
        }

        public Criteria andPasswordIsNotNull() {
            addCriterion("`password` is not null");
            return (Criteria) this;
        }

        public Criteria andPasswordEqualTo(String value) {
            addCriterion("`password` =", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotEqualTo(String value) {
            addCriterion("`password` <>", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordGreaterThan(String value) {
            addCriterion("`password` >", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordGreaterThanOrEqualTo(String value) {
            addCriterion("`password` >=", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLessThan(String value) {
            addCriterion("`password` <", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLessThanOrEqualTo(String value) {
            addCriterion("`password` <=", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLike(String value) {
            addCriterion("`password` like", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotLike(String value) {
            addCriterion("`password` not like", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordIn(List<String> values) {
            addCriterion("`password` in", values, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotIn(List<String> values) {
            addCriterion("`password` not in", values, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordBetween(String value1, String value2) {
            addCriterion("`password` between", value1, value2, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotBetween(String value1, String value2) {
            addCriterion("`password` not between", value1, value2, "password");
            return (Criteria) this;
        }

        public Criteria andAllowExportIsNull() {
            addCriterion("allow_export is null");
            return (Criteria) this;
        }

        public Criteria andAllowExportIsNotNull() {
            addCriterion("allow_export is not null");
            return (Criteria) this;
        }

        public Criteria andAllowExportEqualTo(Boolean value) {
            addCriterion("allow_export =", value, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportNotEqualTo(Boolean value) {
            addCriterion("allow_export <>", value, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportGreaterThan(Boolean value) {
            addCriterion("allow_export >", value, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportGreaterThanOrEqualTo(Boolean value) {
            addCriterion("allow_export >=", value, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportLessThan(Boolean value) {
            addCriterion("allow_export <", value, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportLessThanOrEqualTo(Boolean value) {
            addCriterion("allow_export <=", value, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportIn(List<Boolean> values) {
            addCriterion("allow_export in", values, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportNotIn(List<Boolean> values) {
            addCriterion("allow_export not in", values, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportBetween(Boolean value1, Boolean value2) {
            addCriterion("allow_export between", value1, value2, "allowExport");
            return (Criteria) this;
        }

        public Criteria andAllowExportNotBetween(Boolean value1, Boolean value2) {
            addCriterion("allow_export not between", value1, value2, "allowExport");
            return (Criteria) this;
        }

        public Criteria andApiRangeIsNull() {
            addCriterion("api_range is null");
            return (Criteria) this;
        }

        public Criteria andApiRangeIsNotNull() {
            addCriterion("api_range is not null");
            return (Criteria) this;
        }

        public Criteria andApiRangeEqualTo(String value) {
            addCriterion("api_range =", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeNotEqualTo(String value) {
            addCriterion("api_range <>", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeGreaterThan(String value) {
            addCriterion("api_range >", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeGreaterThanOrEqualTo(String value) {
            addCriterion("api_range >=", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeLessThan(String value) {
            addCriterion("api_range <", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeLessThanOrEqualTo(String value) {
            addCriterion("api_range <=", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeLike(String value) {
            addCriterion("api_range like", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeNotLike(String value) {
            addCriterion("api_range not like", value, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeIn(List<String> values) {
            addCriterion("api_range in", values, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeNotIn(List<String> values) {
            addCriterion("api_range not in", values, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeBetween(String value1, String value2) {
            addCriterion("api_range between", value1, value2, "apiRange");
            return (Criteria) this;
        }

        public Criteria andApiRangeNotBetween(String value1, String value2) {
            addCriterion("api_range not between", value1, value2, "apiRange");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolIsNull() {
            addCriterion("range_match_symbol is null");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolIsNotNull() {
            addCriterion("range_match_symbol is not null");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolEqualTo(String value) {
            addCriterion("range_match_symbol =", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolNotEqualTo(String value) {
            addCriterion("range_match_symbol <>", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolGreaterThan(String value) {
            addCriterion("range_match_symbol >", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolGreaterThanOrEqualTo(String value) {
            addCriterion("range_match_symbol >=", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolLessThan(String value) {
            addCriterion("range_match_symbol <", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolLessThanOrEqualTo(String value) {
            addCriterion("range_match_symbol <=", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolLike(String value) {
            addCriterion("range_match_symbol like", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolNotLike(String value) {
            addCriterion("range_match_symbol not like", value, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolIn(List<String> values) {
            addCriterion("range_match_symbol in", values, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolNotIn(List<String> values) {
            addCriterion("range_match_symbol not in", values, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolBetween(String value1, String value2) {
            addCriterion("range_match_symbol between", value1, value2, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchSymbolNotBetween(String value1, String value2) {
            addCriterion("range_match_symbol not between", value1, value2, "rangeMatchSymbol");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValIsNull() {
            addCriterion("range_match_val is null");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValIsNotNull() {
            addCriterion("range_match_val is not null");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValEqualTo(String value) {
            addCriterion("range_match_val =", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValNotEqualTo(String value) {
            addCriterion("range_match_val <>", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValGreaterThan(String value) {
            addCriterion("range_match_val >", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValGreaterThanOrEqualTo(String value) {
            addCriterion("range_match_val >=", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValLessThan(String value) {
            addCriterion("range_match_val <", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValLessThanOrEqualTo(String value) {
            addCriterion("range_match_val <=", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValLike(String value) {
            addCriterion("range_match_val like", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValNotLike(String value) {
            addCriterion("range_match_val not like", value, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValIn(List<String> values) {
            addCriterion("range_match_val in", values, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValNotIn(List<String> values) {
            addCriterion("range_match_val not in", values, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValBetween(String value1, String value2) {
            addCriterion("range_match_val between", value1, value2, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andRangeMatchValNotBetween(String value1, String value2) {
            addCriterion("range_match_val not between", value1, value2, "rangeMatchVal");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeIsNull() {
            addCriterion("invalid_time is null");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeIsNotNull() {
            addCriterion("invalid_time is not null");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeEqualTo(Long value) {
            addCriterion("invalid_time =", value, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeNotEqualTo(Long value) {
            addCriterion("invalid_time <>", value, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeGreaterThan(Long value) {
            addCriterion("invalid_time >", value, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("invalid_time >=", value, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeLessThan(Long value) {
            addCriterion("invalid_time <", value, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeLessThanOrEqualTo(Long value) {
            addCriterion("invalid_time <=", value, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeIn(List<Long> values) {
            addCriterion("invalid_time in", values, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeNotIn(List<Long> values) {
            addCriterion("invalid_time not in", values, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeBetween(Long value1, Long value2) {
            addCriterion("invalid_time between", value1, value2, "invalidTime");
            return (Criteria) this;
        }

        public Criteria andInvalidTimeNotBetween(Long value1, Long value2) {
            addCriterion("invalid_time not between", value1, value2, "invalidTime");
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

        public Criteria andUpdateUserIsNull() {
            addCriterion("update_user is null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIsNotNull() {
            addCriterion("update_user is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserEqualTo(String value) {
            addCriterion("update_user =", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotEqualTo(String value) {
            addCriterion("update_user <>", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThan(String value) {
            addCriterion("update_user >", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThanOrEqualTo(String value) {
            addCriterion("update_user >=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThan(String value) {
            addCriterion("update_user <", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThanOrEqualTo(String value) {
            addCriterion("update_user <=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLike(String value) {
            addCriterion("update_user like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotLike(String value) {
            addCriterion("update_user not like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIn(List<String> values) {
            addCriterion("update_user in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotIn(List<String> values) {
            addCriterion("update_user not in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserBetween(String value1, String value2) {
            addCriterion("update_user between", value1, value2, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotBetween(String value1, String value2) {
            addCriterion("update_user not between", value1, value2, "updateUser");
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