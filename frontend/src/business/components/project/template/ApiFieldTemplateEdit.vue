<template>

  <el-drawer
    ref="drawer"
    v-loading="result.loading"
    :before-close="handleClose"
    :modal="false"
    :visible.sync="showDialog"
    :with-header="false"
    class="field-template-edit"
    size="100%">
    <template v-slot:default="scope">

      <template-component-edit-header :template="form" @cancel="handleClose" @save="handleSave"/>

      <el-main class="container">
        <el-scrollbar>
          <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

          <el-form ref="form" :model="form" :rules="rules" label-position="right" label-width="80px" size="small">
            <el-form-item :label="$t('table.template_name')" :label-width="labelWidth" prop="name">
              <el-input v-model="form.name" :disabled="isSystem" autocomplete="off" maxlength="64"
                        show-word-limit></el-input>
            </el-form-item>

            <el-form-item :label="$t('commons.description')" :label-width="labelWidth" prop="description">
              <el-input v-model="form.description" :autosize="{ minRows: 2, maxRows: 4}" maxlength="255"
                        show-word-limit type="textarea"></el-input>
            </el-form-item>

            <ms-form-divider :title="$t('custom_field.template_setting')"/>

            <slot></slot>

            <el-form-item :label="$t('table.base_fields')" :label-width="labelWidth" class="filed-list">
            </el-form-item>
            <el-form-item>
              <el-row>
                <el-col :span="6">
                  <el-form-item :label="$t('commons.name')">
                    <el-input :disabled="true" class="ms-http-input" size="small"/>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item :label="$t('test_track.module.module')">
                    <el-input :disabled="true" class="ms-http-input" size="small"/>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item :label="$t('commons.status')">
                    <el-input :disabled="true" class="ms-http-input" size="small"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="6">
                  <el-form-item :label="$t('api_test.definition.request.responsible')">
                    <el-input
                      :disabled="true"
                      class="ms-http-input"
                      filterable size="small" style="width: 100%"/>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item :label="$t('commons.tag')">
                    <el-input ref="tag" :disabled="true"/>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item :label="$t('commons.description')">
                    <el-input :autosize="{ minRows: 1, maxRows: 10}"
                              :disabled="true"
                              :rows="1"
                              class="ms-http-textarea" size="small" type="textarea"/>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form-item>

            <el-form-item :label="$t('table.selected_custom_fields')" :label-width="labelWidth" class="filed-list">
              <el-button type="primary" @click="relateField">{{ $t('custom_field.add_field') }}</el-button>
              <el-button plain type="primary" @click="addField">{{
                  $t('custom_field.custom_field_setting')
                }}
              </el-button>
            </el-form-item>

            <el-form-item :label-width="labelWidth">
              <custom-field-form-list
                ref="customFieldFormList"
                :custom-field-ids="form.customFieldIds"
                :platform="form.platform"
                :scene="scene"
                :table-data="relateFields"
                :template-contain-ids="templateContainIds"
              />
            </el-form-item>

          </el-form>

          <custom-field-relate-list
            ref="customFieldRelateList"
            :scene="scene"
            :template-contain-ids="templateContainIds"
            :template-id="form.id"
            @save="handleRelate"/>

          <custom-field-edit ref="customFieldEdit" :label-width="labelWidth" :scene="scene"
                             @save="handleCustomFieldAdd"/>

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
import CustomFieldFormList from "@/business/components/project/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/project/template/CustomFieldRelateList";
import {getCurrentProjectID, listenGoBack} from "@/common/js/utils";
import CustomFieldEdit from "@/business/components/project/template/CustomFieldEdit";
import {generateTableHeaderKey, getCustomFieldsKeys} from "@/common/js/tableUtils";

export default {
  name: "FieldTemplateEdit",
  components: {
    CustomFieldEdit,
    CustomFieldRelateList,
    CustomFieldFormList,
    MsFormDivider,
    TemplateComponentEditHeader,
    draggable,
  },
  data() {
    return {
      templateContainIds: new Set(),
      relateFields: [],
      showDialog: false,
      form: {
        name: "",
        description: '',
        customFieldIds: [],
      },
      labelWidth: '120px',
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 64, message: this.$t('test_track.length_less_than') + '64', trigger: 'blur'}
        ],
        type: [{required: true, trigger: 'change'}],
      },
      result: {},
      url: '',
      scene: 'API',
      isSystem: false
    };
  },
  methods: {
    open() {
      this.$nextTick(() => {
        this.init();
        this.getRelateFields();
        this.showDialog = true;
      });
    },
    openEdit(data, isCopy) {
      if (data) {
        Object.assign(this.form, data);
        this.isSystem = this.form.system;
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (isCopy) {
          this.url = 'project/field/template/api/add';
        } else {
          this.url = 'project/field/template/api/update';
        }
        listenGoBack(() => {
          this.showDialog = false;
        });
      } else {
        this.isSystem = false;
        this.form = {
          id: "",
          name: "",
          description: '',
          customFieldIds: [],
        };
        this.url = 'project/field/template/api/add';
      }
      this.open(data);
    },
    init() {
      this.relateFields = [];
      this.templateContainIds = new Set();
    },
    handleClose() {
      this.showDialog = false;
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
          this.result = this.$post(this.url, param, () => {
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
        this.result = this.$post('custom/field/template/list',
          condition, (response) => {
            this.relateFields = response.data;
            this.relateFields.forEach(item => {
              if (item.options) {
                item.options = JSON.parse(item.options);
              }
              if (item.defaultValue) {
                item.defaultValue = JSON.parse(item.defaultValue);
              } else if (item.type === 'checkbox') {
                item.defaultValue = [];
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
      this.result = this.$post('custom/field/default', condition, (response) => {
        let data = response.data;
        data.forEach(item => {
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
