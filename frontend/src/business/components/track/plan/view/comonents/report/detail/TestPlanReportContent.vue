<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="result ? result.loading : false">
        <test-plan-report-buttons :is-db="isDb" :plan-id="planId" :is-share="isShare" :report="report"
                                  v-if="!isTemplate && !isShare"/>
        <test-plan-overview-report v-if="overviewEnable" :report="report"/>
        <test-plan-summary-report v-if="summaryEnable" :is-db="isDb" :is-template="isTemplate" :is-share="isShare" :report="report" :plan-id="planId"/>
        <test-plan-functional-report v-if="functionalEnable" :is-db="isDb" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :plan-id="planId" :report="report"/>
        <test-plan-api-report v-if="apiEnable" :is-db="isDb" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId"/>
        <test-plan-load-report v-if="loadEnable" :is-db="isDb" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :report="report" :plan-id="planId"/>
      </el-card>
    </ms-main-container>
    <test-plan-report-navigation-bar
      :api-enable="apiEnable"
      :summary-enable="summaryEnable"
      :functional-enable="functionalEnable"
      :load-enable="loadEnable"
      :overview-enable="overviewEnable"
      :is-template="isTemplate"/>
  </ms-container>
</template>

<script>
import TestPlanFunctionalReport
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanFunctionalReport";
import {
  getExportReport,
  getShareTestPlanReport,
  getShareTestPlanReportContent,
  getTestPlanReport,
  getTestPlanReportContent
} from "@/network/test-plan";
import TestPlanApiReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanApiReport";
import TestPlanLoadReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanLoadReport";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import TestPlanOverviewReport
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanOverviewReport";
import TestPlanSummaryReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanSummaryReport";
import TestPlanReportButtons from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportButtons";
import TestPlanReportNavigationBar
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportNavigationBar";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
export default {
  name: "TestPlanReportContent",
  components: {
    MsMainContainer,
    MsContainer,
    TestPlanReportNavigationBar,
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
    isDb: Boolean,
    shareId: String,
    reportId: String
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
    },
    reportId() {
      this.getReport();
    },
    planReportTemplate() {
      if (this.planReportTemplate) {
        this.init();
      }
    }
  },
  created() {
    this.getReport();
  },
  computed: {
    overviewEnable() {
      let disable = this.report.config
        && this.report.config.overview && this.report.config.overview.enable === false;
      return !disable;
    },
    summaryEnable() {
      let disable = this.report.config && this.report.config.summary
        && this.report.config.summary.enable === false;
      return !disable;
    },
    functionalEnable() {
      let disable = this.report.config && this.report.config.functional.enable === false;
      return !disable && this.report.functionResult
        && this.report.functionResult.caseData && this.report.functionResult.caseData.length > 0 ;
    },
    apiEnable() {
      let disable = this.report.config && this.report.config.api.enable === false;
      return !disable && this.report.apiResult &&
        (
          (this.report.apiResult.apiCaseData && this.report.apiResult.apiCaseData.length  > 0)
          || (this.report.apiResult.apiScenarioData && this.report.apiResult.apiScenarioData.length > 0)
        );
    },
    loadEnable() {
      let disable = this.report.config && this.report.config.load.enable === false;
      return !disable && this.report.loadResult
        && this.report.loadResult.caseData && this.report.loadResult.caseData.length > 0;
    }
  },
  methods: {
    getReport() {
      if (this.isTemplate) {
        this.report = "#report";

        // this.report = {}; 测试代码
        // this.result = getExportReport(this.planId, (data) => {
        //   data.config = JSON.parse(data.config);
        //   this.report = data;
        // });

        this.report.config = this.getDefaultConfig(this.report);
      }  else if (this.isDb) {
        if (this.isShare) {
          //持久化的报告分享
          this.result = getShareTestPlanReportContent(this.shareId, this.reportId, (data) => {
            this.report = data;
            this.report.config = this.getDefaultConfig(this.report);
          });
        } else {
          this.result = getTestPlanReportContent(this.reportId, (data) => {
            this.report = data;
            this.report.config = this.getDefaultConfig(this.report);
          });
        }
      } else if (this.isShare) {
        this.result = getShareTestPlanReport(this.shareId, this.planId, (data) => {
          this.report = data;
          this.report.config = this.getDefaultConfig(this.report);
        });
      } else {
        this.result = getTestPlanReport(this.planId, (data) => {
          this.report = data;
          this.report.config = this.getDefaultConfig(this.report);
        });
      }
    },
    getDefaultConfig(report) {
      if (report && report.config) {
        let configStr = report.config;
        if (configStr) {
          return JSON.parse(configStr);
        }
      }
      return {
        overview: {
          enable: true,
          name: this.$t('test_track.report.overview')
        },
        summary: {
          enable: true,
          name: this.$t('test_track.report.report_summary')
        },
        functional: {
          enable: true,
          name: this.$t('test_track.report.analysis_functional'),
          children: {
            result: {
              enable: true,
              name: this.$t('test_track.report.test_result'),
            },
            failure: {
              enable: true,
              name: this.$t('test_track.report.fail_case'),
            },
            issue: {
              enable: true,
              name: this.$t('test_track.report.issue_list'),
            },
            all: {
              enable: true,
              name: this.$t('test_track.report.all_case'),
            }
          }
        },
        api: {
          enable: true,
          name: this.$t('test_track.report.analysis_api'),
          children: {
            result: {
              enable: true,
              name: this.$t('test_track.report.test_result'),
            },
            failure: {
              enable: true,
              name: this.$t('test_track.report.fail_case'),
            },
            errorReport: {
              enable: true,
              name: this.$t('error_report_library.option.name'),
            },
            unExecute: {
              enable: true,
              name: this.$t('api_test.home_page.detail_card.unexecute'),
            },
            all: {
              enable: true,
              name: this.$t('test_track.report.all_case'),
            }
          }
        },
        load: {
          enable: true,
          name: this.$t('test_track.report.analysis_load'),
          children: {
            result: {
              enable: true,
              name: this.$t('test_track.report.test_result'),
            },
            failure: {
              enable: true,
              name: this.$t('test_track.report.fail_case'),
            },
            all: {
              enable: true,
              name: this.$t('test_track.report.all_case'),
            }
          }
        }
      };
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

/deep/ .empty {
  text-align: center;
  width: 100%;
  padding: 40px;
}

/deep/ .padding-col {
  padding: 5px;
}

/deep/ .el-scrollbar {
  height: 100%;
}

/deep/ .el-card .ms-table {
  cursor: pointer;
}
</style>
