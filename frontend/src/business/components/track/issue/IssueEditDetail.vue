<template>
  <el-main v-loading="result.loading" class="container">
    <el-scrollbar>
      <el-form :model="form" :rules="rules" label-position="right" label-width="140px" size="small" ref="form">

        <el-form-item :label="'标题'" prop="title">
          <el-input v-model="form.title" autocomplete="off"></el-input>
        </el-form-item>

        <!-- 自定义字段 -->
        <el-form v-if="isFormAlive" :model="customFieldForm" :rules="customFieldRules" ref="customFieldForm"
                 class="case-form">
          <el-row class="custom-field-row">
            <el-col :span="7" v-for="(item, index) in issueTemplate.customFields" :key="index">
              <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item.name"
                            :label-width="formLabelWidth">
                <custom-filed-component @reload="reloadForm" :data="item" :form="customFieldForm" prop="defaultValue"/>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <form-rich-text-item :title="$t('缺陷内容')" :data="form" prop="description"/>

        <el-form-item>
          <test-case-issue-list :test-case-contain-ids="testCaseContainIds" :issues-id="form.id" ref="testCaseIssueList"/>
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
      formLabelWidth: "120px",
      issueTemplate: {},
      customFieldForm: {},
      customFieldRules: {},
      rules: {
        title: [
          {required: true, message: this.$t('标题'), trigger: 'blur'},
          {max: 64, message: this.$t('test_track.length_less_than') + '64', trigger: 'blur'}
        ],
      },
      testCaseContainIds: new Set(),
      url: '',
      form:{
        title: '',
        description: ''
      }
    };
  },
  computed: {
    isSystem() {
      return this.form.system;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    projectId() {
      return this.$store.state.projectId
    }
  },
  methods: {
    open(data) {
      let initAddFuc = this.initEdit;
      getTemplate('field/template/issue/get/relate/', this)
        .then((template) => {
          this.issueTemplate = template;
          initAddFuc(data);
        });
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
        }
      } else {
        this.form = {
          title: '',
          description: ''
        }
        this.url = 'issues/add';
      }
      parseCustomField(this.form, this.issueTemplate, this.customFieldForm, this.customFieldRules, null);
      this.$nextTick(() => {
        this.$refs.testCaseIssueList.initTableData();
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
    _save() {
      let param = {};
      Object.assign(param, this.form);
      param.projectId = this.projectId;
      buildCustomFields(this.form, param, this.issueTemplate);
      param.testCaseIds = Array.from(this.testCaseContainIds);
      this.parseOldFields(param);
      this.result = this.$post(this.url, param, () => {
        this.$emit('close');
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
      });
    },
    parseOldFields(param) {
      let customFieldsStr =  param.customFields;
      if (customFieldsStr) {
        let customFields = JSON.parse(customFieldsStr);
        if (customFields['i43sf4_issueStatus']) {
          param.status = JSON.parse(customFields['i43sf4_issueStatus']);
        }
      }
    },
  }
};
</script>

<style scoped>

.container {
  height: calc(100vh - 62px);
}

.filed-list {
  margin-top: 30px;
}

.custom-field-row {
  padding-left: 18px;
}
</style>
