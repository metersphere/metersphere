<template>
  <el-card>
    <test-plan-report-header :report="report" :plan-id="planId"/>
    <test-plan-functional-report v-if="functionalEnable" :plan-id="planId" :report="report"/>
    <test-plan-api-report v-if="apiEnable" :report="report" :plan-id="planId"/>
    <test-plan-load-report v-if="loadEnable" :report="report" :plan-id="planId"/>
  </el-card>
</template>

<script>
import TestPlanReportHeader from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportHeader";
import TestPlanFunctionalReport
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanFunctionalReport";
import {getTestPlanReport} from "@/network/test-plan";
import TestPlanApiReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanApiReport";
import TestPlanLoadReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanLoadReport";
export default {
  name: "TestPlanReportContent",
  components: {TestPlanLoadReport, TestPlanApiReport, TestPlanFunctionalReport, TestPlanReportHeader},
  props: {
    planId:String,
  },
  data() {
    return {
      report: {}
    };
  },
  created() {
    this.getReport();
  },
  computed: {
    functionalEnable() {
      return this.report.functionResult.caseData.length > 0;
    },
    apiEnable() {
      return this.report.apiResult.apiCaseData.length > 0 || this.report.apiResult.apiScenarioData.length > 0;
    },
    loadEnable() {
      return this.report.loadResult.caseData.length > 0;
    }
  },
  methods: {
    getReport() {
      this.result = getTestPlanReport(this.planId, (data) => {
        this.report = data;
      });
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
