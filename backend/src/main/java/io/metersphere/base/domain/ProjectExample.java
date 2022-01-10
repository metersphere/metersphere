package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class ProjectExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ProjectExample() {
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

        public Criteria andWorkspaceIdIsNull() {
            addCriterion("workspace_id is null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNotNull() {
            addCriterion("workspace_id is not null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdEqualTo(String value) {
            addCriterion("workspace_id =", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotEqualTo(String value) {
            addCriterion("workspace_id <>", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThan(String value) {
            addCriterion("workspace_id >", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThanOrEqualTo(String value) {
            addCriterion("workspace_id >=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThan(String value) {
            addCriterion("workspace_id <", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThanOrEqualTo(String value) {
            addCriterion("workspace_id <=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLike(String value) {
            addCriterion("workspace_id like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotLike(String value) {
            addCriterion("workspace_id not like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIn(List<String> values) {
            addCriterion("workspace_id in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotIn(List<String> values) {
            addCriterion("workspace_id not in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdBetween(String value1, String value2) {
            addCriterion("workspace_id between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotBetween(String value1, String value2) {
            addCriterion("workspace_id not between", value1, value2, "workspaceId");
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

        public Criteria andDescriptionIsNull() {
            addCriterion("description is null");
            return (Criteria) this;
        }

        public Criteria andDescriptionIsNotNull() {
            addCriterion("description is not null");
            return (Criteria) this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            addCriterion("description =", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotEqualTo(String value) {
            addCriterion("description <>", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThan(String value) {
            addCriterion("description >", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("description >=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThan(String value) {
            addCriterion("description <", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThanOrEqualTo(String value) {
            addCriterion("description <=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLike(String value) {
            addCriterion("description like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotLike(String value) {
            addCriterion("description not like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionIn(List<String> values) {
            addCriterion("description in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotIn(List<String> values) {
            addCriterion("description not in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionBetween(String value1, String value2) {
            addCriterion("description between", value1, value2, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotBetween(String value1, String value2) {
            addCriterion("description not between", value1, value2, "description");
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

        public Criteria andTapdIdIsNull() {
            addCriterion("tapd_id is null");
            return (Criteria) this;
        }

        public Criteria andTapdIdIsNotNull() {
            addCriterion("tapd_id is not null");
            return (Criteria) this;
        }

        public Criteria andTapdIdEqualTo(String value) {
            addCriterion("tapd_id =", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotEqualTo(String value) {
            addCriterion("tapd_id <>", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdGreaterThan(String value) {
            addCriterion("tapd_id >", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdGreaterThanOrEqualTo(String value) {
            addCriterion("tapd_id >=", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdLessThan(String value) {
            addCriterion("tapd_id <", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdLessThanOrEqualTo(String value) {
            addCriterion("tapd_id <=", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdLike(String value) {
            addCriterion("tapd_id like", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotLike(String value) {
            addCriterion("tapd_id not like", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdIn(List<String> values) {
            addCriterion("tapd_id in", values, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotIn(List<String> values) {
            addCriterion("tapd_id not in", values, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdBetween(String value1, String value2) {
            addCriterion("tapd_id between", value1, value2, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotBetween(String value1, String value2) {
            addCriterion("tapd_id not between", value1, value2, "tapdId");
            return (Criteria) this;
        }

        public Criteria andJiraKeyIsNull() {
            addCriterion("jira_key is null");
            return (Criteria) this;
        }

        public Criteria andJiraKeyIsNotNull() {
            addCriterion("jira_key is not null");
            return (Criteria) this;
        }

        public Criteria andJiraKeyEqualTo(String value) {
            addCriterion("jira_key =", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotEqualTo(String value) {
            addCriterion("jira_key <>", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyGreaterThan(String value) {
            addCriterion("jira_key >", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyGreaterThanOrEqualTo(String value) {
            addCriterion("jira_key >=", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyLessThan(String value) {
            addCriterion("jira_key <", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyLessThanOrEqualTo(String value) {
            addCriterion("jira_key <=", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyLike(String value) {
            addCriterion("jira_key like", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotLike(String value) {
            addCriterion("jira_key not like", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyIn(List<String> values) {
            addCriterion("jira_key in", values, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotIn(List<String> values) {
            addCriterion("jira_key not in", values, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyBetween(String value1, String value2) {
            addCriterion("jira_key between", value1, value2, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotBetween(String value1, String value2) {
            addCriterion("jira_key not between", value1, value2, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andZentaoIdIsNull() {
            addCriterion("zentao_id is null");
            return (Criteria) this;
        }

        public Criteria andZentaoIdIsNotNull() {
            addCriterion("zentao_id is not null");
            return (Criteria) this;
        }

        public Criteria andZentaoIdEqualTo(String value) {
            addCriterion("zentao_id =", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotEqualTo(String value) {
            addCriterion("zentao_id <>", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdGreaterThan(String value) {
            addCriterion("zentao_id >", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdGreaterThanOrEqualTo(String value) {
            addCriterion("zentao_id >=", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdLessThan(String value) {
            addCriterion("zentao_id <", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdLessThanOrEqualTo(String value) {
            addCriterion("zentao_id <=", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdLike(String value) {
            addCriterion("zentao_id like", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotLike(String value) {
            addCriterion("zentao_id not like", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdIn(List<String> values) {
            addCriterion("zentao_id in", values, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotIn(List<String> values) {
            addCriterion("zentao_id not in", values, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdBetween(String value1, String value2) {
            addCriterion("zentao_id between", value1, value2, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotBetween(String value1, String value2) {
            addCriterion("zentao_id not between", value1, value2, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdIsNull() {
            addCriterion("azure_devops_id is null");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdIsNotNull() {
            addCriterion("azure_devops_id is not null");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdEqualTo(String value) {
            addCriterion("azure_devops_id =", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdNotEqualTo(String value) {
            addCriterion("azure_devops_id <>", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdGreaterThan(String value) {
            addCriterion("azure_devops_id >", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdGreaterThanOrEqualTo(String value) {
            addCriterion("azure_devops_id >=", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdLessThan(String value) {
            addCriterion("azure_devops_id <", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdLessThanOrEqualTo(String value) {
            addCriterion("azure_devops_id <=", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdLike(String value) {
            addCriterion("azure_devops_id like", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdNotLike(String value) {
            addCriterion("azure_devops_id not like", value, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdIn(List<String> values) {
            addCriterion("azure_devops_id in", values, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdNotIn(List<String> values) {
            addCriterion("azure_devops_id not in", values, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdBetween(String value1, String value2) {
            addCriterion("azure_devops_id between", value1, value2, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andAzureDevopsIdNotBetween(String value1, String value2) {
            addCriterion("azure_devops_id not between", value1, value2, "azureDevopsId");
            return (Criteria) this;
        }

        public Criteria andRepeatableIsNull() {
            addCriterion("`repeatable` is null");
            return (Criteria) this;
        }

        public Criteria andRepeatableIsNotNull() {
            addCriterion("`repeatable` is not null");
            return (Criteria) this;
        }

        public Criteria andRepeatableEqualTo(Boolean value) {
            addCriterion("`repeatable` =", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableNotEqualTo(Boolean value) {
            addCriterion("`repeatable` <>", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableGreaterThan(Boolean value) {
            addCriterion("`repeatable` >", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("`repeatable` >=", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableLessThan(Boolean value) {
            addCriterion("`repeatable` <", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableLessThanOrEqualTo(Boolean value) {
            addCriterion("`repeatable` <=", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableIn(List<Boolean> values) {
            addCriterion("`repeatable` in", values, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableNotIn(List<Boolean> values) {
            addCriterion("`repeatable` not in", values, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableBetween(Boolean value1, Boolean value2) {
            addCriterion("`repeatable` between", value1, value2, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("`repeatable` not between", value1, value2, "repeatable");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdIsNull() {
            addCriterion("case_template_id is null");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdIsNotNull() {
            addCriterion("case_template_id is not null");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdEqualTo(String value) {
            addCriterion("case_template_id =", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdNotEqualTo(String value) {
            addCriterion("case_template_id <>", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdGreaterThan(String value) {
            addCriterion("case_template_id >", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdGreaterThanOrEqualTo(String value) {
            addCriterion("case_template_id >=", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdLessThan(String value) {
            addCriterion("case_template_id <", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdLessThanOrEqualTo(String value) {
            addCriterion("case_template_id <=", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdLike(String value) {
            addCriterion("case_template_id like", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdNotLike(String value) {
            addCriterion("case_template_id not like", value, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdIn(List<String> values) {
            addCriterion("case_template_id in", values, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdNotIn(List<String> values) {
            addCriterion("case_template_id not in", values, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdBetween(String value1, String value2) {
            addCriterion("case_template_id between", value1, value2, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andCaseTemplateIdNotBetween(String value1, String value2) {
            addCriterion("case_template_id not between", value1, value2, "caseTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdIsNull() {
            addCriterion("issue_template_id is null");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdIsNotNull() {
            addCriterion("issue_template_id is not null");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdEqualTo(String value) {
            addCriterion("issue_template_id =", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdNotEqualTo(String value) {
            addCriterion("issue_template_id <>", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdGreaterThan(String value) {
            addCriterion("issue_template_id >", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdGreaterThanOrEqualTo(String value) {
            addCriterion("issue_template_id >=", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdLessThan(String value) {
            addCriterion("issue_template_id <", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdLessThanOrEqualTo(String value) {
            addCriterion("issue_template_id <=", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdLike(String value) {
            addCriterion("issue_template_id like", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdNotLike(String value) {
            addCriterion("issue_template_id not like", value, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdIn(List<String> values) {
            addCriterion("issue_template_id in", values, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdNotIn(List<String> values) {
            addCriterion("issue_template_id not in", values, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdBetween(String value1, String value2) {
            addCriterion("issue_template_id between", value1, value2, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andIssueTemplateIdNotBetween(String value1, String value2) {
            addCriterion("issue_template_id not between", value1, value2, "issueTemplateId");
            return (Criteria) this;
        }

        public Criteria andCustomNumIsNull() {
            addCriterion("custom_num is null");
            return (Criteria) this;
        }

        public Criteria andCustomNumIsNotNull() {
            addCriterion("custom_num is not null");
            return (Criteria) this;
        }

        public Criteria andCustomNumEqualTo(Boolean value) {
            addCriterion("custom_num =", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumNotEqualTo(Boolean value) {
            addCriterion("custom_num <>", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumGreaterThan(Boolean value) {
            addCriterion("custom_num >", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumGreaterThanOrEqualTo(Boolean value) {
            addCriterion("custom_num >=", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumLessThan(Boolean value) {
            addCriterion("custom_num <", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumLessThanOrEqualTo(Boolean value) {
            addCriterion("custom_num <=", value, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumIn(List<Boolean> values) {
            addCriterion("custom_num in", values, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumNotIn(List<Boolean> values) {
            addCriterion("custom_num not in", values, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumBetween(Boolean value1, Boolean value2) {
            addCriterion("custom_num between", value1, value2, "customNum");
            return (Criteria) this;
        }

        public Criteria andCustomNumNotBetween(Boolean value1, Boolean value2) {
            addCriterion("custom_num not between", value1, value2, "customNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumIsNull() {
            addCriterion("scenario_custom_num is null");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumIsNotNull() {
            addCriterion("scenario_custom_num is not null");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumEqualTo(Boolean value) {
            addCriterion("scenario_custom_num =", value, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumNotEqualTo(Boolean value) {
            addCriterion("scenario_custom_num <>", value, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumGreaterThan(Boolean value) {
            addCriterion("scenario_custom_num >", value, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumGreaterThanOrEqualTo(Boolean value) {
            addCriterion("scenario_custom_num >=", value, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumLessThan(Boolean value) {
            addCriterion("scenario_custom_num <", value, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumLessThanOrEqualTo(Boolean value) {
            addCriterion("scenario_custom_num <=", value, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumIn(List<Boolean> values) {
            addCriterion("scenario_custom_num in", values, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumNotIn(List<Boolean> values) {
            addCriterion("scenario_custom_num not in", values, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumBetween(Boolean value1, Boolean value2) {
            addCriterion("scenario_custom_num between", value1, value2, "scenarioCustomNum");
            return (Criteria) this;
        }

        public Criteria andScenarioCustomNumNotBetween(Boolean value1, Boolean value2) {
            addCriterion("scenario_custom_num not between", value1, value2, "scenarioCustomNum");
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

        public Criteria andSystemIdIsNull() {
            addCriterion("system_id is null");
            return (Criteria) this;
        }

        public Criteria andSystemIdIsNotNull() {
            addCriterion("system_id is not null");
            return (Criteria) this;
        }

        public Criteria andSystemIdEqualTo(String value) {
            addCriterion("system_id =", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotEqualTo(String value) {
            addCriterion("system_id <>", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdGreaterThan(String value) {
            addCriterion("system_id >", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdGreaterThanOrEqualTo(String value) {
            addCriterion("system_id >=", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdLessThan(String value) {
            addCriterion("system_id <", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdLessThanOrEqualTo(String value) {
            addCriterion("system_id <=", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdLike(String value) {
            addCriterion("system_id like", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotLike(String value) {
            addCriterion("system_id not like", value, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdIn(List<String> values) {
            addCriterion("system_id in", values, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotIn(List<String> values) {
            addCriterion("system_id not in", values, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdBetween(String value1, String value2) {
            addCriterion("system_id between", value1, value2, "systemId");
            return (Criteria) this;
        }

        public Criteria andSystemIdNotBetween(String value1, String value2) {
            addCriterion("system_id not between", value1, value2, "systemId");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortIsNull() {
            addCriterion("mock_tcp_port is null");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortIsNotNull() {
            addCriterion("mock_tcp_port is not null");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortEqualTo(Integer value) {
            addCriterion("mock_tcp_port =", value, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortNotEqualTo(Integer value) {
            addCriterion("mock_tcp_port <>", value, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortGreaterThan(Integer value) {
            addCriterion("mock_tcp_port >", value, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("mock_tcp_port >=", value, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortLessThan(Integer value) {
            addCriterion("mock_tcp_port <", value, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortLessThanOrEqualTo(Integer value) {
            addCriterion("mock_tcp_port <=", value, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortIn(List<Integer> values) {
            addCriterion("mock_tcp_port in", values, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortNotIn(List<Integer> values) {
            addCriterion("mock_tcp_port not in", values, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortBetween(Integer value1, Integer value2) {
            addCriterion("mock_tcp_port between", value1, value2, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andMockTcpPortNotBetween(Integer value1, Integer value2) {
            addCriterion("mock_tcp_port not between", value1, value2, "mockTcpPort");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenIsNull() {
            addCriterion("is_mock_tcp_open is null");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenIsNotNull() {
            addCriterion("is_mock_tcp_open is not null");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenEqualTo(Boolean value) {
            addCriterion("is_mock_tcp_open =", value, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenNotEqualTo(Boolean value) {
            addCriterion("is_mock_tcp_open <>", value, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenGreaterThan(Boolean value) {
            addCriterion("is_mock_tcp_open >", value, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_mock_tcp_open >=", value, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenLessThan(Boolean value) {
            addCriterion("is_mock_tcp_open <", value, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenLessThanOrEqualTo(Boolean value) {
            addCriterion("is_mock_tcp_open <=", value, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenIn(List<Boolean> values) {
            addCriterion("is_mock_tcp_open in", values, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenNotIn(List<Boolean> values) {
            addCriterion("is_mock_tcp_open not in", values, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenBetween(Boolean value1, Boolean value2) {
            addCriterion("is_mock_tcp_open between", value1, value2, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andIsMockTcpOpenNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_mock_tcp_open not between", value1, value2, "isMockTcpOpen");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdIsNull() {
            addCriterion("azure_filter_id is null");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdIsNotNull() {
            addCriterion("azure_filter_id is not null");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdEqualTo(String value) {
            addCriterion("azure_filter_id =", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdNotEqualTo(String value) {
            addCriterion("azure_filter_id <>", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdGreaterThan(String value) {
            addCriterion("azure_filter_id >", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdGreaterThanOrEqualTo(String value) {
            addCriterion("azure_filter_id >=", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdLessThan(String value) {
            addCriterion("azure_filter_id <", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdLessThanOrEqualTo(String value) {
            addCriterion("azure_filter_id <=", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdLike(String value) {
            addCriterion("azure_filter_id like", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdNotLike(String value) {
            addCriterion("azure_filter_id not like", value, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdIn(List<String> values) {
            addCriterion("azure_filter_id in", values, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdNotIn(List<String> values) {
            addCriterion("azure_filter_id not in", values, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdBetween(String value1, String value2) {
            addCriterion("azure_filter_id between", value1, value2, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andAzureFilterIdNotBetween(String value1, String value2) {
            addCriterion("azure_filter_id not between", value1, value2, "azureFilterId");
            return (Criteria) this;
        }

        public Criteria andApiQuickIsNull() {
            addCriterion("api_quick is null");
            return (Criteria) this;
        }

        public Criteria andApiQuickIsNotNull() {
            addCriterion("api_quick is not null");
            return (Criteria) this;
        }

        public Criteria andApiQuickEqualTo(String value) {
            addCriterion("api_quick =", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickNotEqualTo(String value) {
            addCriterion("api_quick <>", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickGreaterThan(String value) {
            addCriterion("api_quick >", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickGreaterThanOrEqualTo(String value) {
            addCriterion("api_quick >=", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickLessThan(String value) {
            addCriterion("api_quick <", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickLessThanOrEqualTo(String value) {
            addCriterion("api_quick <=", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickLike(String value) {
            addCriterion("api_quick like", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickNotLike(String value) {
            addCriterion("api_quick not like", value, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickIn(List<String> values) {
            addCriterion("api_quick in", values, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickNotIn(List<String> values) {
            addCriterion("api_quick not in", values, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickBetween(String value1, String value2) {
            addCriterion("api_quick between", value1, value2, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andApiQuickNotBetween(String value1, String value2) {
            addCriterion("api_quick not between", value1, value2, "apiQuick");
            return (Criteria) this;
        }

        public Criteria andCasePublicIsNull() {
            addCriterion("case_public is null");
            return (Criteria) this;
        }

        public Criteria andCasePublicIsNotNull() {
            addCriterion("case_public is not null");
            return (Criteria) this;
        }

        public Criteria andCasePublicEqualTo(Boolean value) {
            addCriterion("case_public =", value, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicNotEqualTo(Boolean value) {
            addCriterion("case_public <>", value, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicGreaterThan(Boolean value) {
            addCriterion("case_public >", value, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicGreaterThanOrEqualTo(Boolean value) {
            addCriterion("case_public >=", value, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicLessThan(Boolean value) {
            addCriterion("case_public <", value, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicLessThanOrEqualTo(Boolean value) {
            addCriterion("case_public <=", value, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicIn(List<Boolean> values) {
            addCriterion("case_public in", values, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicNotIn(List<Boolean> values) {
            addCriterion("case_public not in", values, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicBetween(Boolean value1, Boolean value2) {
            addCriterion("case_public between", value1, value2, "casePublic");
            return (Criteria) this;
        }

        public Criteria andCasePublicNotBetween(Boolean value1, Boolean value2) {
            addCriterion("case_public not between", value1, value2, "casePublic");
            return (Criteria) this;
        }

        public Criteria andPlatformIsNull() {
            addCriterion("platform is null");
            return (Criteria) this;
        }

        public Criteria andPlatformIsNotNull() {
            addCriterion("platform is not null");
            return (Criteria) this;
        }

        public Criteria andPlatformEqualTo(String value) {
            addCriterion("platform =", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformNotEqualTo(String value) {
            addCriterion("platform <>", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformGreaterThan(String value) {
            addCriterion("platform >", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformGreaterThanOrEqualTo(String value) {
            addCriterion("platform >=", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformLessThan(String value) {
            addCriterion("platform <", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformLessThanOrEqualTo(String value) {
            addCriterion("platform <=", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformLike(String value) {
            addCriterion("platform like", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformNotLike(String value) {
            addCriterion("platform not like", value, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformIn(List<String> values) {
            addCriterion("platform in", values, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformNotIn(List<String> values) {
            addCriterion("platform not in", values, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformBetween(String value1, String value2) {
            addCriterion("platform between", value1, value2, "platform");
            return (Criteria) this;
        }

        public Criteria andPlatformNotBetween(String value1, String value2) {
            addCriterion("platform not between", value1, value2, "platform");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateIsNull() {
            addCriterion("third_part_template is null");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateIsNotNull() {
            addCriterion("third_part_template is not null");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateEqualTo(Boolean value) {
            addCriterion("third_part_template =", value, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateNotEqualTo(Boolean value) {
            addCriterion("third_part_template <>", value, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateGreaterThan(Boolean value) {
            addCriterion("third_part_template >", value, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateGreaterThanOrEqualTo(Boolean value) {
            addCriterion("third_part_template >=", value, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateLessThan(Boolean value) {
            addCriterion("third_part_template <", value, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateLessThanOrEqualTo(Boolean value) {
            addCriterion("third_part_template <=", value, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateIn(List<Boolean> values) {
            addCriterion("third_part_template in", values, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateNotIn(List<Boolean> values) {
            addCriterion("third_part_template not in", values, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateBetween(Boolean value1, Boolean value2) {
            addCriterion("third_part_template between", value1, value2, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andThirdPartTemplateNotBetween(Boolean value1, Boolean value2) {
            addCriterion("third_part_template not between", value1, value2, "thirdPartTemplate");
            return (Criteria) this;
        }

        public Criteria andVersionEnableIsNull() {
            addCriterion("version_enable is null");
            return (Criteria) this;
        }

        public Criteria andVersionEnableIsNotNull() {
            addCriterion("version_enable is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEnableEqualTo(Boolean value) {
            addCriterion("version_enable =", value, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableNotEqualTo(Boolean value) {
            addCriterion("version_enable <>", value, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableGreaterThan(Boolean value) {
            addCriterion("version_enable >", value, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("version_enable >=", value, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableLessThan(Boolean value) {
            addCriterion("version_enable <", value, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableLessThanOrEqualTo(Boolean value) {
            addCriterion("version_enable <=", value, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableIn(List<Boolean> values) {
            addCriterion("version_enable in", values, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableNotIn(List<Boolean> values) {
            addCriterion("version_enable not in", values, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableBetween(Boolean value1, Boolean value2) {
            addCriterion("version_enable between", value1, value2, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andVersionEnableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("version_enable not between", value1, value2, "versionEnable");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportIsNull() {
            addCriterion("clean_track_report is null");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportIsNotNull() {
            addCriterion("clean_track_report is not null");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportEqualTo(Boolean value) {
            addCriterion("clean_track_report =", value, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportNotEqualTo(Boolean value) {
            addCriterion("clean_track_report <>", value, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportGreaterThan(Boolean value) {
            addCriterion("clean_track_report >", value, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportGreaterThanOrEqualTo(Boolean value) {
            addCriterion("clean_track_report >=", value, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportLessThan(Boolean value) {
            addCriterion("clean_track_report <", value, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportLessThanOrEqualTo(Boolean value) {
            addCriterion("clean_track_report <=", value, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportIn(List<Boolean> values) {
            addCriterion("clean_track_report in", values, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportNotIn(List<Boolean> values) {
            addCriterion("clean_track_report not in", values, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportBetween(Boolean value1, Boolean value2) {
            addCriterion("clean_track_report between", value1, value2, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportNotBetween(Boolean value1, Boolean value2) {
            addCriterion("clean_track_report not between", value1, value2, "cleanTrackReport");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprIsNull() {
            addCriterion("clean_track_report_expr is null");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprIsNotNull() {
            addCriterion("clean_track_report_expr is not null");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprEqualTo(String value) {
            addCriterion("clean_track_report_expr =", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprNotEqualTo(String value) {
            addCriterion("clean_track_report_expr <>", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprGreaterThan(String value) {
            addCriterion("clean_track_report_expr >", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprGreaterThanOrEqualTo(String value) {
            addCriterion("clean_track_report_expr >=", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprLessThan(String value) {
            addCriterion("clean_track_report_expr <", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprLessThanOrEqualTo(String value) {
            addCriterion("clean_track_report_expr <=", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprLike(String value) {
            addCriterion("clean_track_report_expr like", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprNotLike(String value) {
            addCriterion("clean_track_report_expr not like", value, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprIn(List<String> values) {
            addCriterion("clean_track_report_expr in", values, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprNotIn(List<String> values) {
            addCriterion("clean_track_report_expr not in", values, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprBetween(String value1, String value2) {
            addCriterion("clean_track_report_expr between", value1, value2, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanTrackReportExprNotBetween(String value1, String value2) {
            addCriterion("clean_track_report_expr not between", value1, value2, "cleanTrackReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportIsNull() {
            addCriterion("clean_api_report is null");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportIsNotNull() {
            addCriterion("clean_api_report is not null");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportEqualTo(Boolean value) {
            addCriterion("clean_api_report =", value, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportNotEqualTo(Boolean value) {
            addCriterion("clean_api_report <>", value, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportGreaterThan(Boolean value) {
            addCriterion("clean_api_report >", value, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportGreaterThanOrEqualTo(Boolean value) {
            addCriterion("clean_api_report >=", value, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportLessThan(Boolean value) {
            addCriterion("clean_api_report <", value, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportLessThanOrEqualTo(Boolean value) {
            addCriterion("clean_api_report <=", value, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportIn(List<Boolean> values) {
            addCriterion("clean_api_report in", values, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportNotIn(List<Boolean> values) {
            addCriterion("clean_api_report not in", values, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportBetween(Boolean value1, Boolean value2) {
            addCriterion("clean_api_report between", value1, value2, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportNotBetween(Boolean value1, Boolean value2) {
            addCriterion("clean_api_report not between", value1, value2, "cleanApiReport");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprIsNull() {
            addCriterion("clean_api_report_expr is null");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprIsNotNull() {
            addCriterion("clean_api_report_expr is not null");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprEqualTo(String value) {
            addCriterion("clean_api_report_expr =", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprNotEqualTo(String value) {
            addCriterion("clean_api_report_expr <>", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprGreaterThan(String value) {
            addCriterion("clean_api_report_expr >", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprGreaterThanOrEqualTo(String value) {
            addCriterion("clean_api_report_expr >=", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprLessThan(String value) {
            addCriterion("clean_api_report_expr <", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprLessThanOrEqualTo(String value) {
            addCriterion("clean_api_report_expr <=", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprLike(String value) {
            addCriterion("clean_api_report_expr like", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprNotLike(String value) {
            addCriterion("clean_api_report_expr not like", value, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprIn(List<String> values) {
            addCriterion("clean_api_report_expr in", values, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprNotIn(List<String> values) {
            addCriterion("clean_api_report_expr not in", values, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprBetween(String value1, String value2) {
            addCriterion("clean_api_report_expr between", value1, value2, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanApiReportExprNotBetween(String value1, String value2) {
            addCriterion("clean_api_report_expr not between", value1, value2, "cleanApiReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportIsNull() {
            addCriterion("clean_load_report is null");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportIsNotNull() {
            addCriterion("clean_load_report is not null");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportEqualTo(Boolean value) {
            addCriterion("clean_load_report =", value, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportNotEqualTo(Boolean value) {
            addCriterion("clean_load_report <>", value, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportGreaterThan(Boolean value) {
            addCriterion("clean_load_report >", value, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportGreaterThanOrEqualTo(Boolean value) {
            addCriterion("clean_load_report >=", value, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportLessThan(Boolean value) {
            addCriterion("clean_load_report <", value, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportLessThanOrEqualTo(Boolean value) {
            addCriterion("clean_load_report <=", value, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportIn(List<Boolean> values) {
            addCriterion("clean_load_report in", values, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportNotIn(List<Boolean> values) {
            addCriterion("clean_load_report not in", values, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportBetween(Boolean value1, Boolean value2) {
            addCriterion("clean_load_report between", value1, value2, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportNotBetween(Boolean value1, Boolean value2) {
            addCriterion("clean_load_report not between", value1, value2, "cleanLoadReport");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprIsNull() {
            addCriterion("clean_load_report_expr is null");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprIsNotNull() {
            addCriterion("clean_load_report_expr is not null");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprEqualTo(String value) {
            addCriterion("clean_load_report_expr =", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprNotEqualTo(String value) {
            addCriterion("clean_load_report_expr <>", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprGreaterThan(String value) {
            addCriterion("clean_load_report_expr >", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprGreaterThanOrEqualTo(String value) {
            addCriterion("clean_load_report_expr >=", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprLessThan(String value) {
            addCriterion("clean_load_report_expr <", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprLessThanOrEqualTo(String value) {
            addCriterion("clean_load_report_expr <=", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprLike(String value) {
            addCriterion("clean_load_report_expr like", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprNotLike(String value) {
            addCriterion("clean_load_report_expr not like", value, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprIn(List<String> values) {
            addCriterion("clean_load_report_expr in", values, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprNotIn(List<String> values) {
            addCriterion("clean_load_report_expr not in", values, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprBetween(String value1, String value2) {
            addCriterion("clean_load_report_expr between", value1, value2, "cleanLoadReportExpr");
            return (Criteria) this;
        }

        public Criteria andCleanLoadReportExprNotBetween(String value1, String value2) {
            addCriterion("clean_load_report_expr not between", value1, value2, "cleanLoadReportExpr");
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