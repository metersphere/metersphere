<template>

  <field-template-edit
    :form="form"
    :rules="rules"
    :visible.sync="showDialog"
    :url="url"
    @refresh="$emit('refresh')"
    scene="TEST_CASE"
    ref="fieldTemplateEdit">

    <template v-slot:base>
      <el-form-item :label="'用例类型'" prop="type">
        <el-select :disabled="isSystem" filterable v-model="form.type" placeholder="用例类型">
          <el-option
            v-for="item in caseTypeOption"
            :key="item.value"
            :label="item.text"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
    </template>

    <template v-slot:default>
      <el-form-item :label="'用例名称'" prop="caseName">
        <el-input v-model="form.caseName" autocomplete="off"></el-input>
      </el-form-item>
      <form-rich-text-item :title="$t('test_track.case.prerequisite')" :data="form" prop="prerequisite"/>
      <form-rich-text-item :title="$t('test_track.case.step_desc')" :data="form" prop="stepDescription"/>
      <form-rich-text-item :title="$t('test_track.case.expected_results')" :data="form" prop="expectedResult"/>
      <form-rich-text-item :title="$t('test_track.plan_view.actual_result')" :data="form" prop="actualResult"/>
    </template>

  </field-template-edit>

</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {CASE_TYPE_OPTION} from "@/common/js/table-constants";
import CustomFieldFormList from "@/business/components/settings/workspace/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/settings/workspace/template/CustomFieldRelateList";
import FieldTemplateEdit from "@/business/components/settings/workspace/template/FieldTemplateEdit";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";

export default {
  name: "TestCaseTemplateEdit",
  components: {
    FormRichTextItem,
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
        type: 'functional',
        description: '',
        caseName: '',
        prerequisite: '',
        stepDescription: '',
        expectedResult: '',
        actualResult: '',
        customFieldIds: [],
      },
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 64, message: this.$t('test_track.length_less_than') + '64', trigger: 'blur'}
        ],
        type: [{required: true,  trigger: 'change'}],
      },
      result: {},
      url: '',
    };
  },
  computed: {
    caseTypeOption() {
      return CASE_TYPE_OPTION;
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
          this.url = 'field/template/case/add';
        } else {
          this.url = 'field/template/case/update';
        }
      } else {
        this.form = {
          id: "",
          name: "",
          type: 'functional',
          description: '',
          caseName: '',
          prerequisite: '',
          stepDescription: '',
          expectedResult: '',
          actualResult: '',
          customFieldIds: [],
        };
        this.url = 'field/template/case/add';
      }
      this.$refs.fieldTemplateEdit.open(data);
    }
  }
};
</script>

<style scoped>

</style>
