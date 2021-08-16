<template>
  <el-card v-loading="result.loading">

<!--    <el-row v-if="!isTemplate" type="flex" class="head-bar">-->

      <div v-if="!isTemplate" class="head-bar head-right">
        <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExportHtml()">
          {{'导出HTML'}}
        </el-button>
      </div>
<!--    </el-row>-->

    <test-plan-report-header :is-template="isTemplate" :report="report" :plan-id="planId"/>
    <test-plan-functional-report :is-template="isTemplate" v-if="functionalEnable" :plan-id="planId" :report="report"/>
    <test-plan-api-report :is-template="isTemplate" v-if="apiEnable" :report="report" :plan-id="planId"/>
    <test-plan-load-report :is-template="isTemplate" v-if="loadEnable" :report="report" :plan-id="planId"/>
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
  components: {
    TestPlanLoadReport,
    TestPlanApiReport,
    TestPlanFunctionalReport,
    TestPlanReportHeader},
  props: {
    planId:String,
    isTemplate: Boolean
  },
  data() {
    return {
      report: {},
      result: {},
      isTestManagerOrTestUser: false
    };
  },
  created() {
    this.isTestManagerOrTestUser = true;
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
      } else {
        this.result = getTestPlanReport(this.planId, (data) => {
          this.report = data;
        });
      }
    },
    handleExportHtml() {
      let config = {
        url: '/test/plan/report/export/' + this.planId,
        method: 'get',
        responseType: 'blob'
      };
      this.result = this.$download(config, this.report.name + '.html');
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


.head-right {
  text-align: right;
}

.head-bar .el-button {
  margin-bottom: 10px;
  width: 80px;
  margin-right: 10px;
}

.el-button+.el-button {
  margin-left: 0px;
}

/*.head-bar {*/
/*  position: fixed;*/
/*  right: 10px;*/
/*  padding: 20px;*/
/*}*/

</style>
