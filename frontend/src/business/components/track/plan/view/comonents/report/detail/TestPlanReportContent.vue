<template>
  <el-card v-loading="result ? result.loading : false">

      <div v-if="!isTemplate && !isShare" class="head-bar head-right">
        <el-row>
          <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExportHtml()">
            {{'导出HTML'}}
          </el-button>
        </el-row>
        <el-row>
          <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleEditTemplate()">
            {{'编辑模板'}}
          </el-button>
        </el-row>
        <el-row>
          <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleShare()">
            {{'分享连接'}}
          </el-button>
        </el-row>
      </div>

    <test-plan-report-header :is-template="isTemplate" :is-share="isShare" :report="report" :plan-id="planId"/>
    <test-plan-functional-report :is-share="isShare" :is-template="isTemplate" v-if="functionalEnable" :plan-id="planId" :report="report"/>
    <test-plan-api-report :is-share="isShare" :is-template="isTemplate" v-if="apiEnable" :report="report" :plan-id="planId"/>
    <test-plan-load-report :is-share="isShare" :is-template="isTemplate" v-if="loadEnable" :report="report" :plan-id="planId"/>

  </el-card>
</template>

<script>
import TestPlanReportHeader from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportHeader";
import TestPlanFunctionalReport
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanFunctionalReport";
import {getShareTestPlanReport, getTestPlanReport} from "@/network/test-plan";
import TestPlanApiReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanApiReport";
import TestPlanLoadReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanLoadReport";
import {generateApiDocumentShareInfo, generateShareInfo} from "@/network/share";
export default {
  name: "TestPlanReportContent",
  components: {
    TestPlanLoadReport,
    TestPlanApiReport,
    TestPlanFunctionalReport,
    TestPlanReportHeader},
  props: {
    planId:String,
    isTemplate: Boolean,
    isShare: Boolean,
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
      } else if (this.isShare) {
        this.result = getShareTestPlanReport(this.planId, (data) => {
          this.report = data;
        });
      } else {
        this.result = getTestPlanReport(this.planId, (data) => {
          this.report = data;
        });
      }
    },
    handleEditTemplate() {

    },
    handleExportHtml() {
      let config = {
        url: '/test/plan/report/export/' + this.planId,
        method: 'get',
        responseType: 'blob'
      };
      if (this.isShare) {
        config.url = '/share' + config.url;
      }
      this.result = this.$download(config, this.report.name + '.html');
    },
    handleShare()  {
      let param = {
        customData: this.planId,
        shareType: 'PLAN_REPORT'
      };
      generateShareInfo(param, (data) => {
        let thisHost = window.location.host;
        let shareUrl = thisHost + "/sharePlanReport" + data.shareUrl;
        console.log(shareUrl);
      });
    }
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
  float: right;
}

.head-bar .el-button {
  margin-bottom: 10px;
  width: 80px;
  margin-right: 10px;
  display: block;
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
