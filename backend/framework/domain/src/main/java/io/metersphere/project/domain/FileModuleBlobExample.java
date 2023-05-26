package io.metersphere.project.domain;

import java.util.ArrayList;
import java.util.List;

public class FileModuleBlobExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FileModuleBlobExample() {
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

        public Criteria andRepositoryPathIsNull() {
            addCriterion("repository_path is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathIsNotNull() {
            addCriterion("repository_path is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathEqualTo(String value) {
            addCriterion("repository_path =", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotEqualTo(String value) {
            addCriterion("repository_path <>", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathGreaterThan(String value) {
            addCriterion("repository_path >", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathGreaterThanOrEqualTo(String value) {
            addCriterion("repository_path >=", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathLessThan(String value) {
            addCriterion("repository_path <", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathLessThanOrEqualTo(String value) {
            addCriterion("repository_path <=", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathLike(String value) {
            addCriterion("repository_path like", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotLike(String value) {
            addCriterion("repository_path not like", value, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathIn(List<String> values) {
            addCriterion("repository_path in", values, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotIn(List<String> values) {
            addCriterion("repository_path not in", values, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathBetween(String value1, String value2) {
            addCriterion("repository_path between", value1, value2, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryPathNotBetween(String value1, String value2) {
            addCriterion("repository_path not between", value1, value2, "repositoryPath");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameIsNull() {
            addCriterion("repository_user_name is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameIsNotNull() {
            addCriterion("repository_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameEqualTo(String value) {
            addCriterion("repository_user_name =", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotEqualTo(String value) {
            addCriterion("repository_user_name <>", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameGreaterThan(String value) {
            addCriterion("repository_user_name >", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("repository_user_name >=", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameLessThan(String value) {
            addCriterion("repository_user_name <", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameLessThanOrEqualTo(String value) {
            addCriterion("repository_user_name <=", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameLike(String value) {
            addCriterion("repository_user_name like", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotLike(String value) {
            addCriterion("repository_user_name not like", value, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameIn(List<String> values) {
            addCriterion("repository_user_name in", values, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotIn(List<String> values) {
            addCriterion("repository_user_name not in", values, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameBetween(String value1, String value2) {
            addCriterion("repository_user_name between", value1, value2, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryUserNameNotBetween(String value1, String value2) {
            addCriterion("repository_user_name not between", value1, value2, "repositoryUserName");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenIsNull() {
            addCriterion("repository_token is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenIsNotNull() {
            addCriterion("repository_token is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenEqualTo(String value) {
            addCriterion("repository_token =", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotEqualTo(String value) {
            addCriterion("repository_token <>", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenGreaterThan(String value) {
            addCriterion("repository_token >", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenGreaterThanOrEqualTo(String value) {
            addCriterion("repository_token >=", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenLessThan(String value) {
            addCriterion("repository_token <", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenLessThanOrEqualTo(String value) {
            addCriterion("repository_token <=", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenLike(String value) {
            addCriterion("repository_token like", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotLike(String value) {
            addCriterion("repository_token not like", value, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenIn(List<String> values) {
            addCriterion("repository_token in", values, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotIn(List<String> values) {
            addCriterion("repository_token not in", values, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenBetween(String value1, String value2) {
            addCriterion("repository_token between", value1, value2, "repositoryToken");
            return (Criteria) this;
        }

        public Criteria andRepositoryTokenNotBetween(String value1, String value2) {
            addCriterion("repository_token not between", value1, value2, "repositoryToken");
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