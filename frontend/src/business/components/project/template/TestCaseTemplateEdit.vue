<template>

  <field-template-edit
    :label-width="labelWidth"
    :form="form"
    :rules="rules"
    :visible.sync="showDialog"
    :url="url"
    @refresh="$emit('refresh')"
    scene="TEST_CASE"
    ref="fieldTemplateEdit">

    <template v-slot:base>
      <el-form-item :label="$t('api_test.home_page.failed_case_list.table_coloum.case_type')" :label-width="labelWidth"
                    prop="type">
        <el-select v-model="form.type" :disabled="isSystem"
                   :placeholder="$t('api_test.home_page.failed_case_list.table_coloum.case_type')"
                   filterable>
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
      <el-form-item :label="$t('test_track.case.name')" prop="caseName" :label-width="labelWidth">
        <el-input v-model="form.caseName" autocomplete="off" maxlength="64" show-word-limit></el-input>
      </el-form-item>

      <form-rich-text-item :data="form" :label-width="labelWidth" :title="$t('test_track.case.prerequisite')"
                           prop="prerequisite"/>

      <step-change-item :form="form"/>
      <test-case-step-item :label-width="labelWidth" v-if="form.stepModel === 'STEP'" :form="form"/>
      <form-rich-text-item v-if="form.stepModel === 'TEXT'" :data="form"
                           :label-width="labelWidth" :title="$t('test_track.case.step_desc')" prop="stepDescription"/>
      <form-rich-text-item v-if="form.stepModel === 'TEXT'" :data="form"
                           :label-width="labelWidth" :title="$t('test_track.case.expected_results')"
                           prop="expectedResult"/>
      <form-rich-text-item v-if="form.stepModel === 'TEXT'" :data="form"
                           :label-width="labelWidth" :title="$t('test_track.plan_view.actual_result')"
                           prop="actualResult"/>
    </template>

  </field-template-edit>

</template>

<script>

import draggable from 'vuedraggable';
import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {CASE_TYPE_OPTION} from "@/common/js/table-constants";
import CustomFieldFormList from "@/business/components/project/template/CustomFieldFormList";
import CustomFieldRelateList from "@/business/components/project/template/CustomFieldRelateList";
import FieldTemplateEdit from "@/business/components/project/template/FieldTemplateEdit";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import TestCaseStepItem from "@/business/components/track/case/components/TestCaseStepItem";
import StepChangeItem from "@/business/components/track/case/components/StepChangeItem";
import {listenGoBack} from "@/common/js/utils";

export default {
  name: "TestCaseTemplateEdit",
  components: {
    StepChangeItem,
    TestCaseStepItem,
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
        stepModel: 'STEP',
        steps: [],
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
    };
  },
  computed: {
    caseTypeOption() {
      return new CASE_TYPE_OPTION();
    },
    isSystem() {
      return this.form.system;
    }
  },
  methods: {
    open(data, isCopy) {
      if (data) {
        Object.assign(this.form, data);
        this.form.steps = data.steps ? JSON.parse(data.steps) : [];
        if (!(data.options instanceof Array)) {
          this.form.options = data.options ? JSON.parse(data.options) : [];
        }
        if (isCopy) {
          this.url = 'field/template/case/add';
        } else {
          this.url = 'field/template/case/update';
        }
        listenGoBack(() => {
          this.showDialog = false;
        });
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
          steps: [],
          stepModel: 'STEP',
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
