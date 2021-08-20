<template>
  <div class="container">
    <el-main>
      <el-card v-loading="result ? result.loading : false">
        <test-plan-report-buttons :plan-id="planId" :is-share="isShare" :report="report"
                                  v-if="!isTemplate && !isShare"/>
        <test-plan-overview-report :report="report"/>
        <test-plan-summary-report :is-template="isTemplate" :is-share="isShare" :report="report" :plan-id="planId"/>
        <test-plan-functional-report :share-id="shareId" :is-share="isShare" :is-template="isTemplate" v-if="functionalEnable" :plan-id="planId" :report="report"/>
        <test-plan-api-report :share-id="shareId" :is-share="isShare" :is-template="isTemplate" v-if="apiEnable" :report="report" :plan-id="planId"/>
        <test-plan-load-report :share-id="shareId" :is-share="isShare" :is-template="isTemplate" v-if="loadEnable" :report="report" :plan-id="planId"/>

        <test-plan-report-edit ref="reportEdit"/>
      </el-card>
    </el-main>
  </div>
</template>

<script>
import TestPlanFunctionalReport
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanFunctionalReport";
import {getShareTestPlanReport, getTestPlanReport} from "@/network/test-plan";
import TestPlanApiReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanApiReport";
import TestPlanLoadReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanLoadReport";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import TestPlanOverviewReport
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanOverviewReport";
import TestPlanSummaryReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanSummaryReport";
import TestPlanReportButtons from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportButtons";
export default {
  name: "TestPlanReportContent",
  components: {
    TestPlanReportButtons,
    TestPlanSummaryReport,
    TestPlanOverviewReport,
    TestPlanReportContainer,
    TestPlanLoadReport,
    TestPlanApiReport,
    TestPlanFunctionalReport,
    },
  props: {
    planId:String,
    isTemplate: Boolean,
    isShare: Boolean,
    shareId: String
  },
  data() {
    return {
      report: {},
      result: {},
      shareUrl: ''
    };
  },
  watch: {
    planId() {
      this.getReport();
    }
  },
  created() {
    this.getReport();
  },
  computed: {
    functionalEnable() {
      return this.report.functionResult && this.report.functionResult.caseData.length > 0;
    },
    apiEnable() {
      return this.report.apiResult && (this.report.apiResult.apiCaseData.length > 0 || this.report.apiResult.apiScenarioData.length) > 0;
    },
    loadEnable() {
      return this.report.loadResult && this.report.loadResult.caseData.length > 0;
    }
  },
  methods: {
    getReport() {
      if (this.isTemplate) {
        this.report = "#report";
      } else if (this.isShare) {
        this.result = getShareTestPlanReport(this.shareId, this.planId, (data) => {
          this.report = data;
        });
      } else {
        this.result = getTestPlanReport(this.planId, (data) => {
          this.report = data;
        });
      }
    },
  }
}
</script>

<style scoped>
.el-card {
  /*width: 95% !important;*/
  padding: 15px;
}

/deep/ .el-tabs .el-tabs__header {
  padding-left: 15px;
  padding-right: 15px;
  padding-top: 15px;
}

</style>
