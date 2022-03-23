<template>
  <test-plan-report-container id='summary' :title="$t('test_track.report.report_summary')">
    <template v-slot:title>
      <el-link class="edit-link" v-if="!isTemplate && !isShare && !isDb && !isEdit" @click="isEdit = true">
        {{ $t('commons.edit') }}
      </el-link>
      <el-link class="edit-link" v-if="!isTemplate && !isShare && !isDb && isEdit" @click="saveSummary">
        {{ $t('commons.save')}}
      </el-link>
    </template>
    <el-form class="form-info" v-loading="result.loading">
      <div v-if="!isEdit">
        <div class="rich-text-content" v-html="report.summary"></div>
      </div>
      <div v-else>
        <ms-rich-text :content.sync="report.summary"/>
      </div>
    </el-form>
  </test-plan-report-container>

</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {editPlanReport} from "@/network/test-plan";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import MsRichText from "@/business/components/track/case/components/MsRichText";
export default {
  name: "TestPlanSummaryReport",
  components: {MsRichText, TestPlanReportContainer, MsFormDivider},
  props: {
    planId: String,
    report: Object,
    isTemplate: Boolean,
    isShare: Boolean,
    isDb: Boolean,
  },
  data() {
    return {
      result: {},
      isEdit: false
    }
  },
  methods: {
    saveSummary() {
      editPlanReport({
        id: this.planId,
        reportSummary: this.report.summary ? this.report.summary : ''
      });
      this.isEdit = false;
    }
  }
}
</script>

<style scoped>
.el-link >>> .el-link--inner {
  line-height: 40px;
  font-size: 14px;
  height: 42.8px;
}

.form-info {
  padding: 25px;
}

.edit-link {
  margin-left: 10px;
}

.rich-text-content >>> .table td {
  border: solid 1px #e6e6e6;
  min-width: 2em;
  padding: .4em;
}
</style>
