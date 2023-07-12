<template>

  <el-drawer
    class="field-template-edit"
    :before-close="handleClose"
    :visible.sync="visible"
    :with-header="false"
    size="100%"
    :modal="false"
    ref="drawer"
    v-loading="loading">
    <template>

      <template-component-edit-header :template="form" @cancel="handleClose" @save="handleSave"/>

      <el-main class="container">
        <el-scrollbar>
          <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

          <el-form :model="form" :rules="rules" label-position="right" label-width="80px" size="small" ref="form">
            <el-form-item :label="$t('commons.template_name')" prop="name" :label-width="labelWidth">
              <el-input :disabled="isSystem" v-model="form.name" autocomplete="off"></el-input>
            </el-form-item>

            <slot name="base"></slot>

            <el-form-item :label="$t('commons.description')" prop="description" :label-width="labelWidth">
              <el-input :autosize="{ minRows: 3, maxRows: 4}" type="textarea" v-model="form.description" maxlength="255" show-word-limit></el-input>
            </el-form-item>

            <ms-form-divider :title="$t('custom_field.template_setting')"/>

            <slot></slot>

            <el-form-item :label="$t('table.selected_fields')" class="filed-list" :label-width="labelWidth">
              <el-button type="primary" @click="relateField">{{ $t('custom_field.add_field') }}</el-button>
              <el-button type="primary" @click="addField" plain>
                {{ $t('custom_field.custom_field_setting') }}
              </el-button>
            </el-form-item>

            <el-form-item :label-width="labelWidth">
              <custom-field-form-list
                :table-data="relateFields"
                :scene="scene"
                :platform="form.platform"
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

          <custom-field-edit
            :label-width="labelWidth"
            :scene="scene"
            @save="handleCustomFieldAdd"
            ref="customFieldEdit"/>

        </el-scrollbar>
      </el-main>
    </template>
  </el-drawer>
</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "./ext/TemplateComponentEditHeader";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import CustomFieldFormList from "./CustomFieldFormList";
import CustomFieldRelateList from "./CustomFieldRelateList";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import CustomFieldEdit from "./CustomFieldEdit";
import {generateTableHeaderKey, getCustomFieldsKeys} from "metersphere-frontend/src/utils/tableUtils";
import {getCustomFieldDefault, getCustomFieldTemplates} from "../../../api/template";
import {handleResourceSave} from "../../../api/custom-field";

export default {
  name: "FieldTemplateEdit",
  components: {
    CustomFieldEdit,
    CustomFieldRelateList,
    CustomFieldFormList,
    MsFormDivider,
    TemplateComponentEditHeader,
    draggable
  },
  data() {
    return {
      result: {},
      loading: false,
      templateContainIds: new Set(),
      relateFields: []
    };
  },
  props: {
    visible: {
      type: Boolean,
      default() {
        return false;
      }
    },
    scene: String,
    url: String,
    rules: Object,
    labelWidth: String,
    form: {
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
          if (this.form.steps) {
            param.steps = JSON.stringify(this.form.steps);
          }
          param.options = JSON.stringify(this.form.options);
          param.projectId = getCurrentProjectID();
          let customFields = this.relateFields;
          if (customFields) {
            let keys = getCustomFieldsKeys(customFields);
            customFields.forEach(item => {
              if (!item.key) {
                item.key = generateTableHeaderKey(keys, customFields);
              }
              item.defaultValue = JSON.stringify(item.defaultValue);
            });
          }
          param.customFields = customFields;
          this.loading = handleResourceSave(this.url, param).then(() => {
            this.handleClose();
            this.$success(this.$t('commons.save_success'));
            this.$emit('refresh');
          });
        }
      });
    },
    handleCustomFieldAdd(data) {
      this.templateContainIds.add(data.id);
      data.fieldId = data.id;
      data.id = null;
      data.options = JSON.parse(data.options);
      if (data.type === 'checkbox') {
        data.defaultValue = [];
      }
      this.relateFields.push(data);
    },
    relateField() {
      this.$refs.customFieldRelateList.open();
    },
    addField() {
      this.$refs.customFieldEdit.open(null, this.$t('custom_field.create'));
    },
    getRelateFields() {
      let condition = {};
      condition.templateId = this.form.id;
      if (this.form.id) {
        this.loading = getCustomFieldTemplates(condition).then((response) => {
          this.relateFields = response.data;
          this.relateFields.forEach(item => {
            if (item.name === '用例等级' && item.system) {
              item.disabled = true;
            }
            if (item.options) {
              item.options = JSON.parse(item.options);
            }
            if (item.defaultValue) {
              item.defaultValue = JSON.parse(item.defaultValue);
            } else if (item.type === 'checkbox') {
              item.defaultValue = [];
            }
            if (item.type === 'int' && item.defaultValue === null) {
              // el-input-number 需要设置成 undefined，默认值才能设置为空
              item.defaultValue = undefined;
            }
            this.templateContainIds.add(item.fieldId);
          });
          this.$refs.customFieldFormList.refreshTable();
        });
      } else {
        this.appendDefaultFiled();
      }
    },
    appendDefaultFiled() {
      let condition = {
        projectId: getCurrentProjectID(),
        scene: this.scene
      };
      this.loading = getCustomFieldDefault(condition).then((response) => {
        let data = response.data;
        data.forEach(item => {
          if (item.name === '用例等级' && item.system && item.scene === 'TEST_CASE') {
            item.required = true;
            item.disabled = true;
          }
          if (item.id) {
            this.templateContainIds.add(item.id);
          }
          item.fieldId = item.id;
          item.id = null;
          item.options = JSON.parse(item.options);
          if (item.type === 'checkbox') {
            item.defaultValue = [];
          }
        });
        this.relateFields.push(...data);
      });
    }
  }
};
</script>

<style scoped>

.container {
  height: calc(100vh - 42px);
}

.filed-list {
  margin-top: 30px;
}

.field-template-edit {
  z-index: 1500 !important;
}

</style>
