<template>
  <ms-edit-dialog
    width="30%"
    :visible.sync="visible"
    @confirm="save"
    :title="title"
    append-to-body
    ref="msEditDialog">

      <el-form  :model="customFieldForm" :rules="customFieldRules" ref="customFieldForm" class="api-form">
        <custom-filed-form-item :form="customFieldForm" :form-label-width="labelWidth"
                                :issue-template="apiTemplate" :col-span="24" :col-num="1"/>
      </el-form>

  </ms-edit-dialog>
</template>

<script>
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
import MsSingleHandleDrag from "@/business/components/common/components/MsSingleHandleDrag";
import {getCurrentWorkspaceId} from "@/common/js/utils";
import {CUSTOM_FIELD_SCENE_OPTION, CUSTOM_FIELD_TYPE_OPTION, SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import i18n from "@/i18n/i18n";
import {getApiTemplate} from "@/network/custom-field-template";
import {buildCustomFields, parseCustomField} from "@/common/js/custom_field";
import CustomFiledFormItem from "@/business/components/common/components/form/CustomFiledFormItem";
export default {
  name: "ImportCustomFieldEdit",
  components: {MsSingleHandleDrag, MsEditDialog,CustomFiledFormItem},
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
      customFieldForm:{},
      customFieldRules: {},
      apiTemplate:{},
      form: {},
      visible: false,
      title: this.$t('custom_field.name')
    };
  },
  computed: {

  },
  created() {

  },
  methods: {
    open() {
      this.visible = true;
      getApiTemplate()
        .then((template) => {
          this.apiTemplate = template;
          //设置自定义熟悉默认值
          this.customFieldForm = parseCustomField(this.form, this.apiTemplate, this.customFieldRules,null);
        });
    },
    save() {
      this.$refs['customFieldForm'].validate((valid) => {
        if (valid){
          let param = {customFields:''};
          buildCustomFields(this.form ,param ,this.apiTemplate);
          this.$emit('saveCustomFields', param.customFields);
          this.visible = false;
        }
      });
    }
  }
};
</script>

<style scoped>

</style>
