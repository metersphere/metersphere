<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName" @change="changeTab">
      <el-tab-pane :label="$t('custom_field.name')" name="field" v-if="fieldEnable">
        <custom-field-list v-if="activeName === 'field'"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('workspace.case_template_manage')" name="caseTemplate" v-if="caseTemplateEnable">
        <test-case-template-list v-if="activeName === 'caseTemplate'"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('workspace.issue_template_manage')" name="issueTemplate" v-if="issueTemplateEnable">
        <issues-template-list v-if="activeName === 'issueTemplate'"/>
      </el-tab-pane>
      <el-tab-pane v-if="apiTemplateEnable" :label="$t('custom_template.api_template')" name="apiTemplate">
        <api-template-list v-if="activeName === 'apiTemplate'"/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import CustomFieldList from "./CustomFieldList";
import TestCaseTemplateList from "./TestCaseTemplateList";
import IssuesTemplateList from "./IssuesTemplateList";
import {hasPermissions} from "metersphere-frontend/src/utils/permission";
import ApiTemplateList from "./ApiTemplateList";

export default {
  name: "TemplateSetting",
  components: {IssuesTemplateList, TestCaseTemplateList, CustomFieldList, ApiTemplateList},
  data() {
    return {
      activeName: 'field',
      fieldEnable: false,
      caseTemplateEnable: false,
      issueTemplateEnable: false,
      apiTemplateEnable: false
    };
  },
  created() {
    this.changeTab();
  },
  methods: {
    hasPermissions,
    changeTab() {
      let editPermission = false;
      if (hasPermissions('PROJECT_TEMPLATE:READ+API_TEMPLATE')) {
        this.activeName = 'apiTemplate';
        this.apiTemplateEnable = true;
        editPermission= true;
      }
      // 1
      if (hasPermissions('PROJECT_TEMPLATE:READ+ISSUE_TEMPLATE')) {
        this.activeName = 'issueTemplate';
        this.issueTemplateEnable = true;
        editPermission= true;
      }

      // 2
      if (hasPermissions('PROJECT_TEMPLATE:READ+CASE_TEMPLATE')) {
        this.activeName = 'caseTemplate';
        this.caseTemplateEnable = true;
        editPermission= true;
      }

      // 3
      if (hasPermissions('PROJECT_TEMPLATE:READ+CUSTOM')) {
        this.activeName = 'field';
        this.fieldEnable = true;
        editPermission= true;
      }

      if (!editPermission) {
        this.$error(this.$t('custom_template.no_edit_permission'));
      }
    }
  }
};
</script>

<style scoped>

</style>
