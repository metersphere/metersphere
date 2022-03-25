<template>
  <ms-edit-dialog
    width="30%"
    :visible.sync="visible"
    @confirm="save"
    :title="title"
    append-to-body
    ref="msEditDialog">

    <el-form :model="form" :rules="rules" label-position="right" size="small" ref="form">
      <el-form-item :label="$t('custom_field.field_name')" prop="name" :label-width="labelWidth">
        <el-input v-if="isSystem" :disabled="isSystem" :value="$t(systemNameMap[form.name])" autocomplete="off"></el-input>
        <el-input v-else v-model="form.name" autocomplete="off"></el-input>
      </el-form-item>

      <el-form-item :label="$t('custom_field.field_remark')" prop="remark" :label-width="labelWidth">
        <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.remark"></el-input>
      </el-form-item>

      <el-form-item :label="$t('custom_field.scene')" prop="type" :label-width="labelWidth">
        <el-select :disabled="isSystem || isTemplateEdit" filterable v-model="form.scene" :placeholder="$t('custom_field.scene')">
          <el-option
            v-for="item in (form.scene === 'PLAN' ? planSceneOptions : sceneOptions)"
            :key="item.value"
            :label="$t(item.text)"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('custom_field.field_type')" prop="type" :label-width="labelWidth">
        <el-select :disabled="isSystem" filterable v-model="form.type" :placeholder="$t('custom_field.field_type')">
          <el-option
            v-for="item in fieldTypeOptions"
            :key="item.value"
            :label="$t(item.text)"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="showOptions"
        :label="$t('custom_field.field_option')"
        prop="options" :label-width="labelWidth">
        <ms-single-handle-drag
            :is-kv="form.scene === 'ISSUE'"
            :disable="form.name === '用例等级'"
            :data="form.options"/>
      </el-form-item>

    </el-form>

  </ms-edit-dialog>
</template>

<script>
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
import MsSingleHandleDrag from "@/business/components/common/components/MsSingleHandleDrag";
import {getCurrentProjectID} from "@/common/js/utils";
import {CUSTOM_FIELD_SCENE_OPTION, CUSTOM_FIELD_TYPE_OPTION, SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import i18n from "@/i18n/i18n";

export default {
  name: "CustomFieldEdit",
  components: {MsSingleHandleDrag, MsEditDialog},
  props: {
    scene: String,
    labelWidth: {
      Object: String,
      default() {
        return '100px';
      }
    }
  },
  data() {
    return {
      form: {
        name: "",
        type: 'input',
        scene: 'TEST_CASE',
        remark: '',
        system: false,
        options: []
      },
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 64, message: this.$t('test_track.length_less_than') + '64', trigger: 'blur'}
        ],
        scene: [{required: true,  trigger: 'change'}],
        type: [{required: true,  trigger: 'change'}],
      },
      visible: false,
      url: '',
      title: this.$t('custom_field.create')
    };
  },
  computed: {
    fieldTypeOptions() {
      return CUSTOM_FIELD_TYPE_OPTION;
    },
    sceneOptions() {
      return CUSTOM_FIELD_SCENE_OPTION;
    },
    planSceneOptions() {
      let tmp = [...CUSTOM_FIELD_SCENE_OPTION];
      tmp.push( {value: 'PLAN',text: i18n.t('workstation.table_name.track_plan')});// 创建和编辑不能选测试计划
      return tmp;
    },
    showOptions() {
      if (['select', 'multipleSelect', 'radio', 'checkbox'].indexOf(this.form.type) > -1) {
        return true;
      }
      return false;
    },
    isSystem() {
      return this.form.system;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    isTemplateEdit() {
      return this.scene ? true : false;
    }
  },
  methods: {
    open(data, title) {
      this.visible = true;
      this.title = title;
      if (data) {
        Object.assign(this.form, data);
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (data.id) {
          this.url = 'custom/field/update';
        } else {
          //copy
          this.url = 'custom/field/add';
        }
      } else {
        this.form = {
          name: "",
          type: 'input',
          scene: 'TEST_CASE',
          remark: '',
          system: false,
          options: []
        };
        if (this.isTemplateEdit) {
          this.form.scene = this.scene;
        }
        this.url = 'custom/field/add';
      }
    },
    save() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          let param = {};
          if (this.form.name.indexOf('.') > -1) {
            this.$error("名称不能包含'.'号");
            return;
          }
          Object.assign(param, this.form);
          param.projectId = getCurrentProjectID();
          if (['select','multipleSelect','radio','checkbox'].indexOf(param.type) > -1) {
            if (param.options.length < 1) {
              this.$warning(this.$t('custom_field.option_check'));
              return;
            }
            for (const item of param.options) {
              if (!item.text || !item.value) {
                this.$warning(this.$t('custom_field.option_value_check'));
                return;
              }
            }
          }
          param.options = JSON.stringify(this.form.options);
          this.result = this.$post(this.url, param, (response) => {
            this.visible = false;
            param.id = response.data;
            this.$success(this.$t('commons.save_success'));
            this.$emit('refresh');
            this.$emit('save', param);
          });
        }
      });
    }
  }
};
</script>

<style scoped>

</style>
