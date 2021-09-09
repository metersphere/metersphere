package io.metersphere.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PluginExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PluginExample() {
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

        public Criteria andPluginIdIsNull() {
            addCriterion("plugin_id is null");
            return (Criteria) this;
        }

        public Criteria andPluginIdIsNotNull() {
            addCriterion("plugin_id is not null");
            return (Criteria) this;
        }

        public Criteria andPluginIdEqualTo(String value) {
            addCriterion("plugin_id =", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdNotEqualTo(String value) {
            addCriterion("plugin_id <>", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdGreaterThan(String value) {
            addCriterion("plugin_id >", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdGreaterThanOrEqualTo(String value) {
            addCriterion("plugin_id >=", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdLessThan(String value) {
            addCriterion("plugin_id <", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdLessThanOrEqualTo(String value) {
            addCriterion("plugin_id <=", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdLike(String value) {
            addCriterion("plugin_id like", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdNotLike(String value) {
            addCriterion("plugin_id not like", value, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdIn(List<String> values) {
            addCriterion("plugin_id in", values, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdNotIn(List<String> values) {
            addCriterion("plugin_id not in", values, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdBetween(String value1, String value2) {
            addCriterion("plugin_id between", value1, value2, "pluginId");
            return (Criteria) this;
        }

        public Criteria andPluginIdNotBetween(String value1, String value2) {
            addCriterion("plugin_id not between", value1, value2, "pluginId");
            return (Criteria) this;
        }

        public Criteria andScriptIdIsNull() {
            addCriterion("script_id is null");
            return (Criteria) this;
        }

        public Criteria andScriptIdIsNotNull() {
            addCriterion("script_id is not null");
            return (Criteria) this;
        }

        public Criteria andScriptIdEqualTo(String value) {
            addCriterion("script_id =", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotEqualTo(String value) {
            addCriterion("script_id <>", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdGreaterThan(String value) {
            addCriterion("script_id >", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdGreaterThanOrEqualTo(String value) {
            addCriterion("script_id >=", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLessThan(String value) {
            addCriterion("script_id <", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLessThanOrEqualTo(String value) {
            addCriterion("script_id <=", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLike(String value) {
            addCriterion("script_id like", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotLike(String value) {
            addCriterion("script_id not like", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdIn(List<String> values) {
            addCriterion("script_id in", values, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotIn(List<String> values) {
            addCriterion("script_id not in", values, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdBetween(String value1, String value2) {
            addCriterion("script_id between", value1, value2, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotBetween(String value1, String value2) {
            addCriterion("script_id not between", value1, value2, "scriptId");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzIsNull() {
            addCriterion("jmeter_clazz is null");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzIsNotNull() {
            addCriterion("jmeter_clazz is not null");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzEqualTo(String value) {
            addCriterion("jmeter_clazz =", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzNotEqualTo(String value) {
            addCriterion("jmeter_clazz <>", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzGreaterThan(String value) {
            addCriterion("jmeter_clazz >", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzGreaterThanOrEqualTo(String value) {
            addCriterion("jmeter_clazz >=", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzLessThan(String value) {
            addCriterion("jmeter_clazz <", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzLessThanOrEqualTo(String value) {
            addCriterion("jmeter_clazz <=", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzLike(String value) {
            addCriterion("jmeter_clazz like", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzNotLike(String value) {
            addCriterion("jmeter_clazz not like", value, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzIn(List<String> values) {
            addCriterion("jmeter_clazz in", values, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzNotIn(List<String> values) {
            addCriterion("jmeter_clazz not in", values, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzBetween(String value1, String value2) {
            addCriterion("jmeter_clazz between", value1, value2, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andJmeterClazzNotBetween(String value1, String value2) {
            addCriterion("jmeter_clazz not between", value1, value2, "jmeterClazz");
            return (Criteria) this;
        }

        public Criteria andClazzNameIsNull() {
            addCriterion("clazz_name is null");
            return (Criteria) this;
        }

        public Criteria andClazzNameIsNotNull() {
            addCriterion("clazz_name is not null");
            return (Criteria) this;
        }

        public Criteria andClazzNameEqualTo(String value) {
            addCriterion("clazz_name =", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameNotEqualTo(String value) {
            addCriterion("clazz_name <>", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameGreaterThan(String value) {
            addCriterion("clazz_name >", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameGreaterThanOrEqualTo(String value) {
            addCriterion("clazz_name >=", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameLessThan(String value) {
            addCriterion("clazz_name <", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameLessThanOrEqualTo(String value) {
            addCriterion("clazz_name <=", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameLike(String value) {
            addCriterion("clazz_name like", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameNotLike(String value) {
            addCriterion("clazz_name not like", value, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameIn(List<String> values) {
            addCriterion("clazz_name in", values, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameNotIn(List<String> values) {
            addCriterion("clazz_name not in", values, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameBetween(String value1, String value2) {
            addCriterion("clazz_name between", value1, value2, "clazzName");
            return (Criteria) this;
        }

        public Criteria andClazzNameNotBetween(String value1, String value2) {
            addCriterion("clazz_name not between", value1, value2, "clazzName");
            return (Criteria) this;
        }

        public Criteria andSourcePathIsNull() {
            addCriterion("source_path is null");
            return (Criteria) this;
        }

        public Criteria andSourcePathIsNotNull() {
            addCriterion("source_path is not null");
            return (Criteria) this;
        }

        public Criteria andSourcePathEqualTo(String value) {
            addCriterion("source_path =", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathNotEqualTo(String value) {
            addCriterion("source_path <>", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathGreaterThan(String value) {
            addCriterion("source_path >", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathGreaterThanOrEqualTo(String value) {
            addCriterion("source_path >=", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathLessThan(String value) {
            addCriterion("source_path <", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathLessThanOrEqualTo(String value) {
            addCriterion("source_path <=", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathLike(String value) {
            addCriterion("source_path like", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathNotLike(String value) {
            addCriterion("source_path not like", value, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathIn(List<String> values) {
            addCriterion("source_path in", values, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathNotIn(List<String> values) {
            addCriterion("source_path not in", values, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathBetween(String value1, String value2) {
            addCriterion("source_path between", value1, value2, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourcePathNotBetween(String value1, String value2) {
            addCriterion("source_path not between", value1, value2, "sourcePath");
            return (Criteria) this;
        }

        public Criteria andSourceNameIsNull() {
            addCriterion("source_name is null");
            return (Criteria) this;
        }

        public Criteria andSourceNameIsNotNull() {
            addCriterion("source_name is not null");
            return (Criteria) this;
        }

        public Criteria andSourceNameEqualTo(String value) {
            addCriterion("source_name =", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameNotEqualTo(String value) {
            addCriterion("source_name <>", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameGreaterThan(String value) {
            addCriterion("source_name >", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameGreaterThanOrEqualTo(String value) {
            addCriterion("source_name >=", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameLessThan(String value) {
            addCriterion("source_name <", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameLessThanOrEqualTo(String value) {
            addCriterion("source_name <=", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameLike(String value) {
            addCriterion("source_name like", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameNotLike(String value) {
            addCriterion("source_name not like", value, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameIn(List<String> values) {
            addCriterion("source_name in", values, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameNotIn(List<String> values) {
            addCriterion("source_name not in", values, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameBetween(String value1, String value2) {
            addCriterion("source_name between", value1, value2, "sourceName");
            return (Criteria) this;
        }

        public Criteria andSourceNameNotBetween(String value1, String value2) {
            addCriterion("source_name not between", value1, value2, "sourceName");
            return (Criteria) this;
        }

        public Criteria andExecEntryIsNull() {
            addCriterion("exec_entry is null");
            return (Criteria) this;
        }

        public Criteria andExecEntryIsNotNull() {
            addCriterion("exec_entry is not null");
            return (Criteria) this;
        }

        public Criteria andExecEntryEqualTo(String value) {
            addCriterion("exec_entry =", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryNotEqualTo(String value) {
            addCriterion("exec_entry <>", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryGreaterThan(String value) {
            addCriterion("exec_entry >", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryGreaterThanOrEqualTo(String value) {
            addCriterion("exec_entry >=", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryLessThan(String value) {
            addCriterion("exec_entry <", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryLessThanOrEqualTo(String value) {
            addCriterion("exec_entry <=", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryLike(String value) {
            addCriterion("exec_entry like", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryNotLike(String value) {
            addCriterion("exec_entry not like", value, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryIn(List<String> values) {
            addCriterion("exec_entry in", values, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryNotIn(List<String> values) {
            addCriterion("exec_entry not in", values, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryBetween(String value1, String value2) {
            addCriterion("exec_entry between", value1, value2, "execEntry");
            return (Criteria) this;
        }

        public Criteria andExecEntryNotBetween(String value1, String value2) {
            addCriterion("exec_entry not between", value1, value2, "execEntry");
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

        public Criteria andCreateUserIdIsNull() {
            addCriterion("create_user_id is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdIsNotNull() {
            addCriterion("create_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdEqualTo(String value) {
            addCriterion("create_user_id =", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotEqualTo(String value) {
            addCriterion("create_user_id <>", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdGreaterThan(String value) {
            addCriterion("create_user_id >", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("create_user_id >=", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdLessThan(String value) {
            addCriterion("create_user_id <", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdLessThanOrEqualTo(String value) {
            addCriterion("create_user_id <=", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdLike(String value) {
            addCriterion("create_user_id like", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotLike(String value) {
            addCriterion("create_user_id not like", value, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdIn(List<String> values) {
            addCriterion("create_user_id in", values, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotIn(List<String> values) {
            addCriterion("create_user_id not in", values, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdBetween(String value1, String value2) {
            addCriterion("create_user_id between", value1, value2, "createUserId");
            return (Criteria) this;
        }

        public Criteria andCreateUserIdNotBetween(String value1, String value2) {
            addCriterion("create_user_id not between", value1, value2, "createUserId");
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