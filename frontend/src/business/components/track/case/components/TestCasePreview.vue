<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="visible" width="66%" top="5vh"
             @close="close" class="case-desc">
    <template v-slot:title>
      <ms-form-divider :title="$t('test_track.case.step_info')"/>
    </template>
    <div v-loading="loading" style="height: 70vh; overflow: auto">
      <el-form :model="data">

        <form-rich-text-item :disabled="true" :label-width="formLabelWidth"
                             :title="$t('test_track.case.prerequisite')" :data="data" prop="prerequisite"/>
        <step-change-item :label-width="formLabelWidth" :form="data"/>
        <form-rich-text-item :disabled="true" :label-width="formLabelWidth" v-if="data.stepModel === 'TEXT'"
                             :title="$t('test_track.case.step_desc')" :data="data" prop="stepDescription"/>
        <form-rich-text-item :disabled="true" :label-width="formLabelWidth" v-if="data.stepModel === 'TEXT'"
                             :title="$t('test_track.case.expected_results')" :data="data" prop="expectedResult"/>
        <test-case-step-item :label-width="formLabelWidth" v-if="data.stepModel === 'STEP' || !data.stepModel"
                             :form="data" :read-only="true"/>
      </el-form>
    </div>
  </el-dialog>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import FormRichTextItem from "@/business/components/track/case/components/FormRichTextItem";
import StepChangeItem from "@/business/components/track/case/components/StepChangeItem";
import TestCaseStepItem from "@/business/components/track/case/components/TestCaseStepItem";

export default {
  name: "TestCasePreview",
  components: {
    MsFormDivider,
    FormRichTextItem,
    StepChangeItem,
    TestCaseStepItem,
  },
  props: ['loading'],
  data() {
    return {
      result: {},
      formLabelWidth: "100px",
      visible: false,
      data: {}
    }
  },
  methods: {
    open() {
      this.visible = true;
    },
    close() {
      this.visible = false;
    },
    setData(data) {
      this.data = data;
    }
  }
}
</script>

<style scoped>

</style>
