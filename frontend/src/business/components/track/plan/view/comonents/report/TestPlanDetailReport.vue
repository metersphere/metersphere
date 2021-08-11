<template>
  <div v-loading="result.loading">

    <el-row type="flex" class="head-bar">
      <el-col :span="12">
      </el-col>
      <el-col :span="11" class="head-right">
<!--        <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExport(report.name)">-->
<!--          {{$t('test_track.plan_view.export_report')}}-->
<!--        </el-button>-->
        <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExportHtml(report.name)">
          {{'导出HTML'}}
        </el-button>
      </el-col>
    </el-row>

    <div class="container" ref="resume" id="app">
      <el-main>
        <test-plan-report-content :plan-id="planId"/>
      </el-main>
    </div>
  </div>
</template>

<script>
import {exportPdf} from "@/common/js/utils";
import BaseInfoComponent from "./TemplateComponent/BaseInfoComponent";
import TestResultChartComponent from "./TemplateComponent/TestResultChartComponent";
import TestResultComponent from "./TemplateComponent/TestResultComponent";
import RichTextComponent from "./TemplateComponent/RichTextComponent";
import TestCaseReportTemplateEdit from "./TestCaseReportTemplateEdit";
import TemplateComponent from "./TemplateComponent/TemplateComponent";
import html2canvas from "html2canvas";
import MsTestCaseReportExport from "../TestCaseReportExport";
import TestReportTemplateList from "../TestReportTemplateList";
import TestPlanReportContent from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContent";

export default {
  name: "TestPlanDetailReport",
  components: {
    TestPlanReportContent,
    TestReportTemplateList,
    MsTestCaseReportExport,
    TemplateComponent,
    TestCaseReportTemplateEdit,
    RichTextComponent, TestResultComponent, TestResultChartComponent, BaseInfoComponent
  },
  data() {
    return {
      result: {},
      imgUrl: "",
      previews: [],
      report: {},
      metric: {},
      reportExportVisible: false,
      isTestManagerOrTestUser: false
    }
  },
  mounted() {
    this.isTestManagerOrTestUser = true;
  },
  computed: {
    planId() {
      return this.testPlan.id;
    },
  },
  props: ['testPlan'],
  methods: {
    handleExport(name) {
      this.result.loading = true;
      this.reportExportVisible = true;
      let reset = this.exportReportReset;

      this.$nextTick(function () {
        setTimeout(() => {
          html2canvas(document.getElementById('testCaseReportExport'), {
            scale: 2
          }).then(function(canvas) {
            exportPdf(name, [canvas]);
            reset();
          });
        }, 1000);
      });
    },
    handleExportHtml(name) {
      let config = {
        url: '/test/plan/report/export/' + this.planId,
        method: 'get',
        responseType: 'blob'
      };
      this.$download(config, name + '.html');
    },
    exportReportReset() {
      this.reportExportVisible = false;
      this.result.loading = false;
    },
  }
}
</script>cd

<style scoped>

.head-right {
  text-align: right;
}

.head-bar .el-button {
  margin-bottom: 10px;
  width: 80px;
}

.el-button+.el-button {
  margin-left: 0px;
}

.head-bar {
  position: fixed;
  right: 10px;
  padding: 20px;
}
</style>
