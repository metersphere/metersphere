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
      <el-tab-pane v-if="apiTemplateEnable" :label="$t('workspace.api_template_manage')" name="apiTemplate">
        <api-template-list v-if="activeName === 'apiTemplate'"/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import CustomFieldList from "@/business/components/project/template/CustomFieldList";
import TestCaseTemplateList from "@/business/components/project/template/TestCaseTemplateList";
import IssuesTemplateList from "@/business/components/project/template/IssuesTemplateList";
import {hasPermissions} from "@/common/js/utils";
import ApiTemplateList from "@/business/components/project/template/ApiTemplateList";

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
      if (hasPermissions('PROJECT_TEMPLATE:READ+API_TEMPLATE')) {
        this.activeName = 'apiTemplate';
        this.apiTemplateEnable = true;
      }
      // 1
      if (hasPermissions('PROJECT_TEMPLATE:READ+ISSUE_TEMPLATE')) {
        this.activeName = 'issueTemplate';
        this.issueTemplateEnable = true;
      }

      // 2
      if (hasPermissions('PROJECT_TEMPLATE:READ+CASE_TEMPLATE')) {
        this.activeName = 'caseTemplate';
        this.caseTemplateEnable = true;
      }

      // 3
      if (hasPermissions('PROJECT_TEMPLATE:READ+CUSTOM')) {
        this.activeName = 'field';
        this.fieldEnable = true;
      }

    }
  }
};
</script>

<style scoped>

</style>
