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
      <el-form-item :label="$t('custom_field.issue_platform')" prop="platform" :label-width="labelWidth">
        <el-select
          :disabled="isSystem"
          filterable v-model="form.platform"
          :placeholder="$t('api_test.home_page.failed_case_list.table_coloum.case_type')">
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

      <form-r-ich-text-item
        :label-width="labelWidth"
        :title="$t('custom_field.issue_content')"
        :data="form"
        prop="content"/>

    </template>

  </field-template-edit>

</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "./ext/TemplateComponentEditHeader";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import CustomFieldFormList from "./CustomFieldFormList";
import CustomFieldRelateList from "./CustomFieldRelateList";
import FieldTemplateEdit from "./FieldTemplateEdit";
import FormRIchTextItem from "./ext/FormRichTextItem";
import {LOCAL} from "metersphere-frontend/src/utils/constants";

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
        platform: LOCAL,
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
        platform: [{required: true, trigger: 'change'}],
      },
      result: {},
      url: '',
    };
  },
  props: {
    platformOption: Array
  },
  computed: {
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
        this.url = isCopy ? 'field/template/issue/add' : 'field/template/issue/update';
      } else {
        this.form = {
          id: "",
          name: "",
          platform: LOCAL,
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
