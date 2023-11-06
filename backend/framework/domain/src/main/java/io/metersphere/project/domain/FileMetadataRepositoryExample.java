package io.metersphere.project.domain;

import java.util.ArrayList;
import java.util.List;

public class FileMetadataRepositoryExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FileMetadataRepositoryExample() {
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

        public Criteria andFileMetadataIdIsNull() {
            addCriterion("file_metadata_id is null");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdIsNotNull() {
            addCriterion("file_metadata_id is not null");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdEqualTo(String value) {
            addCriterion("file_metadata_id =", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdNotEqualTo(String value) {
            addCriterion("file_metadata_id <>", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdGreaterThan(String value) {
            addCriterion("file_metadata_id >", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdGreaterThanOrEqualTo(String value) {
            addCriterion("file_metadata_id >=", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdLessThan(String value) {
            addCriterion("file_metadata_id <", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdLessThanOrEqualTo(String value) {
            addCriterion("file_metadata_id <=", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdLike(String value) {
            addCriterion("file_metadata_id like", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdNotLike(String value) {
            addCriterion("file_metadata_id not like", value, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdIn(List<String> values) {
            addCriterion("file_metadata_id in", values, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdNotIn(List<String> values) {
            addCriterion("file_metadata_id not in", values, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdBetween(String value1, String value2) {
            addCriterion("file_metadata_id between", value1, value2, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andFileMetadataIdNotBetween(String value1, String value2) {
            addCriterion("file_metadata_id not between", value1, value2, "fileMetadataId");
            return (Criteria) this;
        }

        public Criteria andBranchIsNull() {
            addCriterion("branch is null");
            return (Criteria) this;
        }

        public Criteria andBranchIsNotNull() {
            addCriterion("branch is not null");
            return (Criteria) this;
        }

        public Criteria andBranchEqualTo(String value) {
            addCriterion("branch =", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchNotEqualTo(String value) {
            addCriterion("branch <>", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchGreaterThan(String value) {
            addCriterion("branch >", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchGreaterThanOrEqualTo(String value) {
            addCriterion("branch >=", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchLessThan(String value) {
            addCriterion("branch <", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchLessThanOrEqualTo(String value) {
            addCriterion("branch <=", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchLike(String value) {
            addCriterion("branch like", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchNotLike(String value) {
            addCriterion("branch not like", value, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchIn(List<String> values) {
            addCriterion("branch in", values, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchNotIn(List<String> values) {
            addCriterion("branch not in", values, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchBetween(String value1, String value2) {
            addCriterion("branch between", value1, value2, "branch");
            return (Criteria) this;
        }

        public Criteria andBranchNotBetween(String value1, String value2) {
            addCriterion("branch not between", value1, value2, "branch");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlIsNull() {
            addCriterion("repository_url is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlIsNotNull() {
            addCriterion("repository_url is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlEqualTo(String value) {
            addCriterion("repository_url =", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlNotEqualTo(String value) {
            addCriterion("repository_url <>", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlGreaterThan(String value) {
            addCriterion("repository_url >", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlGreaterThanOrEqualTo(String value) {
            addCriterion("repository_url >=", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlLessThan(String value) {
            addCriterion("repository_url <", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlLessThanOrEqualTo(String value) {
            addCriterion("repository_url <=", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlLike(String value) {
            addCriterion("repository_url like", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlNotLike(String value) {
            addCriterion("repository_url not like", value, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlIn(List<String> values) {
            addCriterion("repository_url in", values, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlNotIn(List<String> values) {
            addCriterion("repository_url not in", values, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlBetween(String value1, String value2) {
            addCriterion("repository_url between", value1, value2, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andRepositoryUrlNotBetween(String value1, String value2) {
            addCriterion("repository_url not between", value1, value2, "repositoryUrl");
            return (Criteria) this;
        }

        public Criteria andTokenIsNull() {
            addCriterion("token is null");
            return (Criteria) this;
        }

        public Criteria andTokenIsNotNull() {
            addCriterion("token is not null");
            return (Criteria) this;
        }

        public Criteria andTokenEqualTo(String value) {
            addCriterion("token =", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotEqualTo(String value) {
            addCriterion("token <>", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenGreaterThan(String value) {
            addCriterion("token >", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenGreaterThanOrEqualTo(String value) {
            addCriterion("token >=", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenLessThan(String value) {
            addCriterion("token <", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenLessThanOrEqualTo(String value) {
            addCriterion("token <=", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenLike(String value) {
            addCriterion("token like", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotLike(String value) {
            addCriterion("token not like", value, "token");
            return (Criteria) this;
        }

        public Criteria andTokenIn(List<String> values) {
            addCriterion("token in", values, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotIn(List<String> values) {
            addCriterion("token not in", values, "token");
            return (Criteria) this;
        }

        public Criteria andTokenBetween(String value1, String value2) {
            addCriterion("token between", value1, value2, "token");
            return (Criteria) this;
        }

        public Criteria andTokenNotBetween(String value1, String value2) {
            addCriterion("token not between", value1, value2, "token");
            return (Criteria) this;
        }

        public Criteria andCommitIdIsNull() {
            addCriterion("commit_id is null");
            return (Criteria) this;
        }

        public Criteria andCommitIdIsNotNull() {
            addCriterion("commit_id is not null");
            return (Criteria) this;
        }

        public Criteria andCommitIdEqualTo(String value) {
            addCriterion("commit_id =", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdNotEqualTo(String value) {
            addCriterion("commit_id <>", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdGreaterThan(String value) {
            addCriterion("commit_id >", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdGreaterThanOrEqualTo(String value) {
            addCriterion("commit_id >=", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdLessThan(String value) {
            addCriterion("commit_id <", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdLessThanOrEqualTo(String value) {
            addCriterion("commit_id <=", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdLike(String value) {
            addCriterion("commit_id like", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdNotLike(String value) {
            addCriterion("commit_id not like", value, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdIn(List<String> values) {
            addCriterion("commit_id in", values, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdNotIn(List<String> values) {
            addCriterion("commit_id not in", values, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdBetween(String value1, String value2) {
            addCriterion("commit_id between", value1, value2, "commitId");
            return (Criteria) this;
        }

        public Criteria andCommitIdNotBetween(String value1, String value2) {
            addCriterion("commit_id not between", value1, value2, "commitId");
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