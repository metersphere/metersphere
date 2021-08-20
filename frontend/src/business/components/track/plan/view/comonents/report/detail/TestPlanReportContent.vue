<template>
  <div class="container">
    <el-main>
      <el-card v-loading="result ? result.loading : false">

          <div v-if="!isTemplate && !isShare" class="head-bar head-right">

            <ms-share-button :share-url="shareUrl" @click="shareApiDocument"/>

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
          </div>

        <test-plan-report-header :is-template="isTemplate" :is-share="isShare" :report="report" :plan-id="planId"/>
        <test-plan-functional-report :share-id="shareId" :is-share="isShare" :is-template="isTemplate" v-if="functionalEnable" :plan-id="planId" :report="report"/>
        <test-plan-api-report :share-id="shareId" :is-share="isShare" :is-template="isTemplate" v-if="apiEnable" :report="report" :plan-id="planId"/>
        <test-plan-load-report :share-id="shareId" :is-share="isShare" :is-template="isTemplate" v-if="loadEnable" :report="report" :plan-id="planId"/>

      </el-card>
    </el-main>
  </div>
</template>

<script>
import TestPlanReportHeader from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportHeader";
import TestPlanFunctionalReport
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanFunctionalReport";
import {getShareTestPlanReport, getTestPlanReport} from "@/network/test-plan";
import TestPlanApiReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanApiReport";
import TestPlanLoadReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanLoadReport";
import {generateShareInfo} from "@/network/share";
import MsShareButton from "@/business/components/common/components/MsShareButton";
export default {
  name: "TestPlanReportContent",
  components: {
    MsShareButton,
    TestPlanLoadReport,
    TestPlanApiReport,
    TestPlanFunctionalReport,
    TestPlanReportHeader},
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
      isTestManagerOrTestUser: false,
      shareUrl: ''
    };
  },
  watch: {
    planId() {
      this.getReport();
    }
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
        this.result = getShareTestPlanReport(this.shareId, this.planId, (data) => {
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
    shareApiDocument(){
      let pram = {};
      pram.customData = this.planId;
      pram.shareType = 'PLAN_REPORT';
      generateShareInfo(pram, (data) => {
        let thisHost = window.location.host;
          this.shareUrl = thisHost + "/sharePlanReport" + data.shareUrl;
      });
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
