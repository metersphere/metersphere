<template>

  <field-template-edit
    :label-width="labelWidth"
    :form="form"
    :visible.sync="showDialog"
    :url="url"
    :rules="rules"
    @refresh="$emit('refresh')"
    scene="ISSUE"
    ref="fieldTemplateEdit">

    <template v-slot:base>
      <el-form-item :label="$t('custom_field.issue_platform')" prop="platform" :label-width="labelWidth" >
        <el-select :disabled="isSystem"  filterable v-model="form.platform" :placeholder="$t('api_test.home_page.failed_case_list.table_coloum.case_type')">
          <el-option
            v-for="item in platformOption"
            :key="item.value"
            :label="item.text"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
    </template>

    <template v-slot:default>
      <el-form-item :label="$t('commons.title')" prop="title" :label-width="labelWidth">
        <el-input v-model="form.title" autocomplete="off"></el-input>
      </el-form-item>

      <form-r-ich-text-item :label-width="labelWidth" :title="$t('custom_field.issue_content')" :data="form" prop="content"/>

    </template>

  </field-template-edit>

</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {ISSUE_PLATFORM_OPTION} from "@/common/js/table-constants";
import CustomFieldFormList from "@/business/components/settings/workspace/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/settings/workspace/template/CustomFieldRelateList";
import FieldTemplateEdit from "@/business/components/settings/workspace/template/FieldTemplateEdit";
import FormRIchTextItem from "@/business/components/track/case/components/FormRichTextItem";

export default {
  name: "IssueTemplateEdit",
  components: {
    FormRIchTextItem,
    FieldTemplateEdit,
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
        platform: 'metersphere',
        description: '',
        title: '',
        content: '',
        customFieldIds: []
      },
      labelWidth: '120px',
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 64, message: this.$t('test_track.length_less_than') + '64', trigger: 'blur'}
        ],
        platform: [{required: true,  trigger: 'change'}],
      },
      result: {},
      url: '',
    };
  },
  computed: {
    platformOption() {
      return ISSUE_PLATFORM_OPTION;
    },
    isSystem() {
      return this.form.system;
    }
  },
  methods: {
    open(data, isCopy) {
      if (data) {
        Object.assign(this.form, data);
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (isCopy) {
          this.url = 'field/template/issue/add';
        } else {
          this.url = 'field/template/issue/update';
        }
      } else {
        this.form = {
          id: "",
          name: "",
          platform: 'metersphere',
          description: '',
          title: '',
          content: '',
          customFieldIds: [],
        };
        this.url = 'field/template/issue/add';
      }
      this.$refs.fieldTemplateEdit.open(data);
    }
  }
};
</script>

<style scoped>

</style>
