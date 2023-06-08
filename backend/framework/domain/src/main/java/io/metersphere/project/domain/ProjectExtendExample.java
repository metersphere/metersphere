package io.metersphere.project.domain;

import java.util.ArrayList;
import java.util.List;

public class ProjectExtendExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ProjectExtendExample() {
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

        public Criteria andIssueConfigIsNull() {
            addCriterion("issue_config is null");
            return (Criteria) this;
        }

        public Criteria andIssueConfigIsNotNull() {
            addCriterion("issue_config is not null");
            return (Criteria) this;
        }

        public Criteria andIssueConfigEqualTo(String value) {
            addCriterion("issue_config =", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigNotEqualTo(String value) {
            addCriterion("issue_config <>", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigGreaterThan(String value) {
            addCriterion("issue_config >", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigGreaterThanOrEqualTo(String value) {
            addCriterion("issue_config >=", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigLessThan(String value) {
            addCriterion("issue_config <", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigLessThanOrEqualTo(String value) {
            addCriterion("issue_config <=", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigLike(String value) {
            addCriterion("issue_config like", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigNotLike(String value) {
            addCriterion("issue_config not like", value, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigIn(List<String> values) {
            addCriterion("issue_config in", values, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigNotIn(List<String> values) {
            addCriterion("issue_config not in", values, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigBetween(String value1, String value2) {
            addCriterion("issue_config between", value1, value2, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andIssueConfigNotBetween(String value1, String value2) {
            addCriterion("issue_config not between", value1, value2, "issueConfig");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdIsNull() {
            addCriterion("api_template_id is null");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdIsNotNull() {
            addCriterion("api_template_id is not null");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdEqualTo(String value) {
            addCriterion("api_template_id =", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdNotEqualTo(String value) {
            addCriterion("api_template_id <>", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdGreaterThan(String value) {
            addCriterion("api_template_id >", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdGreaterThanOrEqualTo(String value) {
            addCriterion("api_template_id >=", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdLessThan(String value) {
            addCriterion("api_template_id <", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdLessThanOrEqualTo(String value) {
            addCriterion("api_template_id <=", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdLike(String value) {
            addCriterion("api_template_id like", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdNotLike(String value) {
            addCriterion("api_template_id not like", value, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdIn(List<String> values) {
            addCriterion("api_template_id in", values, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdNotIn(List<String> values) {
            addCriterion("api_template_id not in", values, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdBetween(String value1, String value2) {
            addCriterion("api_template_id between", value1, value2, "apiTemplateId");
            return (Criteria) this;
        }

        public Criteria andApiTemplateIdNotBetween(String value1, String value2) {
            addCriterion("api_template_id not between", value1, value2, "apiTemplateId");
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