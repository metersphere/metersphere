<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="visible"
    :with-header="false"
    size="100%"
    :modal-append-to-body="false"
    ref="drawer"
    v-loading="result.loading">
    <template v-slot:default="scope">

      <template-component-edit-header :template="form" @cancel="handleClose" @save="handleSave"/>

      <el-main class="container">
        <el-scrollbar>
          <ms-form-divider :title="'基础信息'"/>

          <el-form :model="form" :rules="rules" label-position="right" label-width="140px" size="small" ref="form">
            <el-form-item :label="'名称'" prop="name">
              <el-input :disabled="isSystem" v-model="form.name" autocomplete="off"></el-input>
            </el-form-item>

            <slot name="base"></slot>

            <el-form-item :label="'描述'" prop="description">
              <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.description"></el-input>
            </el-form-item>

            <ms-form-divider :title="'模板设置'"/>

            <slot></slot>

            <el-form-item :label="'已选字段'">
              <el-button type="primary" @click="relateField">添加字段</el-button>
              <el-button type="primary" plain>设置自定义字段</el-button>
            </el-form-item>

            <el-form-item>
              <custom-field-form-list
                :table-data="relateFields"
                :scene="scene"
                :template-contain-ids="templateContainIds"
                :custom-field-ids="form.customFieldIds"
                ref="customFieldFormList"
              />
            </el-form-item>

          </el-form>

          <custom-field-relate-list
            :template-id="form.id"
            :template-contain-ids="templateContainIds"
            @save="handleRelate"
            :scene="scene"
            ref="customFieldRelateList"/>

        </el-scrollbar>
      </el-main>
    </template>
  </el-drawer>
</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import CustomFieldFormList from "@/business/components/settings/workspace/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/settings/workspace/template/CustomFieldRelateList";
import {getCurrentWorkspaceId} from "@/common/js/utils";

export default {
  name: "FieldTemplateEdit",
  components: {
    CustomFieldRelateList,
    CustomFieldFormList,
    MsFormDivider,
    TemplateComponentEditHeader,
    draggable
  },
  data() {
    return {
      result: {},
      templateContainIds: new Set(),
      relateFields: []
    };
  },
  props: {
    visible:{
      type: Boolean,
      default() {
        return false;
      }
    },
    scene: String,
    url:String,
    rules: Object,
    form:{
      type: Object,
      default() {
        return {};
      }
    }
  },
  computed: {
    isSystem() {
      return this.form.system;
    }
  },
  methods: {
    open() {
      this.$nextTick(() => {
        this.init();
        this.getRelateFields();
        this.$emit('update:visible', true);
      });
    },
    init() {
      this.relateFields = [];
      this.templateContainIds = new Set();
    },
    handleClose() {
      this.$emit('update:visible', false);
    },
    handleRelate(data) {
      this.templateContainIds.add(...data);
      this.$refs.customFieldFormList.appendData(data);
    },
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.options = JSON.stringify(this.form.options);
          param.workspaceId = getCurrentWorkspaceId();
          let customFields = this.relateFields;
          if (customFields) {
            customFields.forEach(item => {
              item.defaultValue = JSON.stringify(item.defaultValue);
            });
          }
          param.customFields = customFields;
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
              if (item.defaultValue) {
                item.defaultValue = JSON.parse(item.defaultValue);
              }
              this.templateContainIds.add(item.fieldId);
            });
          });
      } else {
        this.appendDefaultFiled();
      }
    },
    appendDefaultFiled() {
      let condition = {
        workspaceId: getCurrentWorkspaceId(),
        scene: this.scene
      };
      this.result = this.$post('custom/field/default', condition, (response) => {
          let data = response.data;
          data.forEach(item => {
            if (item.id) {
              this.templateContainIds.add(item.id);
            }
            item.fieldId = item.id;
            item.id = null;
            item.options = JSON.parse(item.options);
          });
          this.relateFields.push(...data);
        });
    }
  }
}
</script>

<style scoped>

.container {
  height: calc(100vh - 62px);
}
</style>
