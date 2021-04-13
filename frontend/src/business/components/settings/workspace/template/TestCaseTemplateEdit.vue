<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    size="100%"
    :modal-append-to-body="false"
    ref="drawer"
    v-loading="result.loading">
    <template v-slot:default="scope">

      <template-component-edit-header :template="form" @cancel="handleClose" @save="handleSave"/>

      <div class="container">

        <ms-form-divider :title="'基础信息'"/>

        <el-form :model="form" :rules="rules" label-position="right" label-width="140px" size="small" ref="form">
          <el-form-item :label="'名称'" prop="name">
            <el-input v-model="form.name" autocomplete="off"></el-input>
          </el-form-item>

          <el-form-item :label="'用例类型'" prop="type">
            <el-select filterable v-model="form.type" placeholder="用例类型">
              <el-option
                v-for="item in caseTypeOption"
                :key="item.value"
                :label="item.text"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item :label="'描述'" prop="description">
            <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.description"></el-input>
          </el-form-item>

          <ms-form-divider :title="'模板设置'"/>

          <el-form-item :label="'用例名称'" prop="caseName">
            <el-input v-model="form.caseName" autocomplete="off"></el-input>
          </el-form-item>

          <el-form-item :label="'前置条件'" prop="prerequisite">
            <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.prerequisite"></el-input>
          </el-form-item>

          <el-form-item :label="'用例步骤'" prop="stepDescription">
            <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.stepDescription"></el-input>
          </el-form-item>

          <el-form-item :label="'预期结果'" prop="actualResult">
            <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.actualResult"></el-input>
          </el-form-item>

          <el-form-item :label="'已选字段'">
            <el-button type="primary" @click="relateField">添加字段</el-button>
            <el-button type="primary" plain>设置自定义字段</el-button>
          </el-form-item>

          <el-form-item>
            <custom-field-form-list
              :table-data="relateFields"
              :custom-field-ids="form.customFieldIds"
              ref="customFieldFormList"
            />
          </el-form-item>
        </el-form>

        <custom-field-relate-list
          :template-id="form.id"
          :template-contain-ids="templateContainIds"
          @save="handleRelate"
          scene="TEST_CASE"
          ref="customFieldRelateList"/>
      </div>
    </template>
  </el-drawer>
</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {CASE_TYPE_OPTION} from "@/common/js/table-constants";
import {getCurrentWorkspaceId} from "@/common/js/utils";
import CustomFieldFormList from "@/business/components/settings/workspace/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/settings/workspace/template/CustomFieldRelateList";

export default {
  name: "TestCaseTemplateEdit",
  components: {
    CustomFieldRelateList,
    CustomFieldFormList,
    MsFormDivider,
    TemplateComponentEditHeader,
    draggable
  },
  data() {
    return {
      showDialog: false,
      form: {
        name: "",
        type: '',
        description: '',
        caseName: '',
        prerequisite: '',
        stepDescription: '',
        expectedResult: '',
        actualResult: '',
        customFieldIds: [],
      },
      rules: {},
      result: {},
      url: '',
      templateContainIds: [],
      relateFields: []
    }
  },
  props: {
    metric: {
      type: Object
    }
  },
  computed: {
    caseTypeOption() {
      return CASE_TYPE_OPTION;
    }
  },
  methods: {
    open(data) {
      this.relateFields = [];
      this.templateContainIds = [];
      if (data) {
        Object.assign(this.form, data);
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (data.id) {
          this.url = 'field/template/case/update';
        } else {
          //copy
          this.url = 'field/template/case/add';
        }
      } else {
        this.form = {
          name: "",
          type: '',
          description: '',
          caseName: '',
          prerequisite: '',
          stepDescription: '',
          expectedResult: '',
          actualResult: '',
        };
        this.url = 'field/template/case/add';
      }
      this.getRelateFields();
      this.showDialog = true;
    },
    initComponents() {
    },
    handleClose() {
      this.showDialog = false;
    },
    handleRelate(data) {
      this.templateContainIds.push(...data);
      this.$refs.customFieldFormList.appendData(data);
    },
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.workspaceId = getCurrentWorkspaceId();
          param.options = JSON.stringify(this.form.options);
          param.customFields = this.relateFields;
          this.result = this.$post(this.url, param, () => {
            this.handleClose();
            this.$success(this.$t('commons.save_success'));
            this.$emit('refresh');
          });
        }
      });
    },
    relateField() {
      this.$refs.customFieldRelateList.open();
    },
    getRelateFields() {
      let condition = {};
      condition.templateId = this.form.id;
      if (this.form.id) {
        this.result = this.$post('custom/field/template/list',
          condition, (response) => {
            this.relateFields = response.data;
            this.relateFields.forEach(item => {
              if (item.options) {
                item.options = JSON.parse(item.options);
              }
            });
        });
      }
    }
  }
}
</script>

<style scoped>

</style>
