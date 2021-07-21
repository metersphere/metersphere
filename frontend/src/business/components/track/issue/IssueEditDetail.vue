<template>
  <el-main v-loading="result.loading" class="container" :style="isPlan ? '' : 'height: calc(100vh - 62px)'">
    <el-scrollbar>
      <el-form :model="form" :rules="rules" label-position="right" label-width="80px" ref="form">

        <el-form-item :label="$t('commons.title')" prop="title">
          <el-input v-model="form.title" autocomplete="off"></el-input>
        </el-form-item>

        <!-- 自定义字段 -->
        <el-form v-if="isFormAlive" :model="customFieldForm" :rules="customFieldRules" ref="customFieldForm"
                 class="case-form">
          <el-row class="custom-field-row">
            <el-col :span="8" v-for="(item, index) in issueTemplate.customFields" :key="index">
              <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item.name"
                            :label-width="formLabelWidth">
                <custom-filed-component @reload="reloadForm" :data="item" :form="customFieldForm" prop="defaultValue"/>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <form-rich-text-item :title="$t('custom_field.issue_content')" :data="form" prop="description"/>

        <el-row class="custom-field-row">
          <el-col :span="8" v-if="hasTapdId">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.tapd_current_owner')" prop="tapdUsers">
              <el-select v-model="form.tapdUsers" multiple filterable
                         :placeholder="$t('test_track.issue.please_choose_current_owner')">
                <el-option v-for="(userInfo, index) in tapdUsers" :key="index" :label="userInfo.user"
                           :value="userInfo.user"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="hasZentaoId">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.zentao_bug_build')" prop="zentaoBuilds">
              <el-select v-model="form.zentaoBuilds" multiple filterable
                         :placeholder="$t('test_track.issue.zentao_bug_build')">
                <el-option v-for="(build, index) in Builds" :key="index" :label="build.name"
                           :value="build.id"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="hasZentaoId">
            <el-form-item :label-width="formLabelWidth" :label="$t('test_track.issue.zentao_bug_assigned')" prop="zentaoAssigned">
              <el-select v-model="form.zentaoAssigned" filterable
                         :placeholder="$t('test_track.issue.please_choose_current_owner')">
                <el-option v-for="(userInfo, index) in zentaoUsers" :key="index" :label="userInfo.name"
                           :value="userInfo.user"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="!isPlan">
          <test-case-issue-list :test-case-contain-ids="testCaseContainIds" :issues-id="form.id"
                                ref="testCaseIssueList"/>
        </el-form-item>

      </el-form>
    </el-scrollbar>

  </el-main>
</template>

<script>

import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import CustomFieldFormList from "@/business/components/settings/workspace/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/settings/workspace/template/CustomFieldRelateList";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import {buildCustomFields, getTemplate, parseCustomField} from "@/common/js/custom_field";
import CustomFiledComponent from "@/business/components/settings/workspace/template/CustomFiledComponent";
import TestCaseIssueList from "@/business/components/track/issue/TestCaseIssueList";
import IssueEditDetail from "@/business/components/track/issue/IssueEditDetail";
import {getCurrentOrganizationId, getCurrentProjectID, getCurrentUserId} from "@/common/js/utils";
import {getIssueTemplate} from "@/network/custom-field-template";

export default {
  name: "IssueEditDetail",
  components: {
    IssueEditDetail,
    TestCaseIssueList,
    CustomFiledComponent,
    FormRichTextItem,
    CustomFieldRelateList,
    CustomFieldFormList,
    MsFormDivider,
    TemplateComponentEditHeader
  },
  data() {
    return {
      result: {},
      relateFields: [],
      isFormAlive: true,
      formLabelWidth: "150px",
      issueTemplate: {},
      customFieldForm: {},
      customFieldRules: {},
      rules: {
        title: [
          {required: true, message: this.$t('commons.please_fill_content'), trigger: 'blur'},
          {max: 64, message: this.$t('test_track.length_less_than') + '64', trigger: 'blur'}
        ],
        description: [
          {required: true, message: this.$t('commons.please_fill_content'), trigger: 'blur'},
        ]
      },
      testCaseContainIds: new Set(),
      url: '',
      form: {
        title: '',
        description: '',
        creator: null
      },
      tapdUsers: [],
      zentaoUsers: [],
      Builds: [],
      hasTapdId: false,
      hasZentaoId: false
    };
  },
  props: {
    isPlan: {
      type: Boolean,
      default() {
        return false;
      }
    },
    caseId: String,
    planId: String
  },
  computed: {
    isSystem() {
      return this.form.system;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    open(data) {
      let initAddFuc = this.initEdit;
      getIssueTemplate()
        .then((template) => {
          this.issueTemplate = template;
          this.getThirdPartyInfo();
          initAddFuc(data);
        });
    },
    getThirdPartyInfo() {
      let platform = this.issueTemplate.platform;
      if (platform === 'Zentao') {
        this.hasZentaoId = true;
        this.result = this.$post("/issues/zentao/builds", {projectId: this.projectId, organizationId: getCurrentOrganizationId()}, response => {
          if (response.data) {
            this.Builds = response.data;
          }
          this.result = this.$post("/issues/zentao/user", {projectId: this.projectId, organizationId: getCurrentOrganizationId()}, response => {
            this.zentaoUsers = response.data;
          });
        });
      }
      if (platform === 'Tapd') {
        this.hasTapdId = true;
        this.result = this.$post("/issues/tapd/user", {projectId: this.projectId, organizationId: getCurrentOrganizationId()}, (response) => {
          this.tapdUsers = response.data;
        });
      }
    },
    initEdit(data) {
      this.testCaseContainIds = new Set();
      if (data) {
        Object.assign(this.form, data);
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (data.id) {
          this.url = 'issues/update';
        } else {
          //copy
          this.url = 'issues/add';
          if (!this.form.creator) {
            this.form.creator = getCurrentUserId();
          }
          this.form.title = data.title + '_copy';
        }
      } else {
        this.form = {
          title: this.issueTemplate.title,
          description: this.issueTemplate.content
        }
        this.url = 'issues/add';
        if (!this.form.creator) {
          this.form.creator = getCurrentUserId();
        }
      }
      parseCustomField(this.form, this.issueTemplate, this.customFieldForm, this.customFieldRules, null);
      this.$nextTick(() => {
        if (this.$refs.testCaseIssueList) {
          this.$refs.testCaseIssueList.initTableData();
        }
      });
    },
    reloadForm() {
      this.isFormAlive = false;
      this.$nextTick(() => (this.isFormAlive = true));
    },
    save() {
      let isValidate = true;
      this.$refs['form'].validate((valid) => {
        if (!valid) {
          isValidate = false;
          return false;
        }
      });
      this.$refs['customFieldForm'].validate((valid) => {
        if (!valid) {
          isValidate = false;
          return false;
        }
      });
      if (isValidate) {
        this._save();
      }
    },
    buildPram() {
      let param = {};
      Object.assign(param, this.form);
      param.projectId = this.projectId;
      param.organizationId = getCurrentOrganizationId();
      buildCustomFields(this.form, param, this.issueTemplate);
      if (this.isPlan) {
        param.testCaseIds = [this.caseId];
      } else {
        param.testCaseIds = Array.from(this.testCaseContainIds);
      }
      if (this.planId) {
        param.resourceId = this.planId;
      }
      return param;
    },
    _save() {
      let param = this.buildPram();
      this.parseOldFields(param);
      this.result = this.$post(this.url, param, () => {
        this.$emit('close');
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
      });
    },
    parseOldFields(param) {
      let customFieldsStr = param.customFields;
      if (customFieldsStr) {
        let customFields = JSON.parse(customFieldsStr);
        customFields.forEach(item => {
          if (item.name === '状态') {
            param.status = item.value;
          }
        });
      }
    },
  }
};
</script>

<style scoped>


.filed-list {
  margin-top: 30px;
}

.custom-field-row {
  padding-left: 18px;
}
</style>
