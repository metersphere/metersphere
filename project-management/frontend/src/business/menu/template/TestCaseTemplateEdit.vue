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

    <template v-slot:default>
      <el-form-item :label="$t('test_track.case.name')" prop="caseName" :label-width="labelWidth">
        <el-input v-model="form.caseName" autocomplete="off" maxlength="64" show-word-limit></el-input>
      </el-form-item>

      <form-rich-text-item
        :label-width="labelWidth"
        :title="$t('test_track.case.prerequisite')"
        :data="form"
        prop="prerequisite"/>

      <step-change-item :form="form"/>
      <test-case-step-item :label-width="labelWidth" v-if="form.stepModel === 'STEP'" :form="form"/>
      <form-rich-text-item :label-width="labelWidth" v-if="form.stepModel === 'TEXT'"
                           :title="$t('test_track.case.step_desc')" :data="form" prop="stepDescription"/>
      <form-rich-text-item :label-width="labelWidth" v-if="form.stepModel === 'TEXT'"
                           :title="$t('test_track.case.expected_results')" :data="form" prop="expectedResult"/>
      <form-rich-text-item :label-width="labelWidth" v-if="form.stepModel === 'TEXT'"
                           :title="$t('test_track.plan_view.actual_result')" :data="form" prop="actualResult"/>
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
import FormRichTextItem from "./ext/FormRichTextItem";
import TestCaseStepItem from "./ext/TestCaseStepItem";
import StepChangeItem from "./ext/StepChangeItem.vue";
import {listenGoBack} from "metersphere-frontend/src/utils";

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
        this.url = isCopy ? 'field/template/case/add' : 'field/template/case/update';
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
