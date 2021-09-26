<template>
  <div>
    <el-row type="flex">
      <p class="tip">
        <span class="ms-span">{{$t('commons.report_statistics.name')}}</span>
        <el-select v-model="reportType" class="ms-col-type" size="mini" style="width: 120px">
          <el-option :key="t.id" :value="t.id" :label="t.name" v-for="t in reportTypes"/>
        </el-select>
      </p>
    </el-row>
    <transition>
      <keep-alive>
        <report-card @openCard="openCard"/>
      </keep-alive>
    </transition>

    <!-- 测试用例趋势页面 -->
    <ms-drawer :visible="testCaseTrendDrawer" :size="100" @close="close" direction="right" :show-full-screen="false" :is-show-close="false" style="overflow: hidden">
      <template v-slot:header>
        <report-header :title="$t('commons.report_statistics.test_case_analysis')" :history-report-id="historyReportId"
                       @closePage="close" @saveReport="saveReport" @selectAndSaveReport="selectAndSaveReport"/>
      </template>
      <test-analysis-container @initHistoryReportId="initHistoryReportId" ref="testAnalysisContainer"/>
    </ms-drawer>

    <!-- 测试用例分析页面 -->
    <ms-drawer :visible="testCaseCountDrawer" :size="100" @close="close" direction="right" :show-full-screen="false" :is-show-close="false" style="overflow: hidden">
      <template v-slot:header>
        <report-header :title="$t('commons.report_statistics.test_case_count')" :history-report-id="historyReportId"
                       @closePage="close" @saveReport="saveReport" @selectAndSaveReport="selectAndSaveReport"/>
      </template>
      <test-case-count-container @initHistoryReportId="initHistoryReportId" ref="testCaseCountContainer"/>
    </ms-drawer>
  </div>
</template>

<script>
  import ReportCard from "./ReportCard";
  import TestAnalysisContainer from "./track/TestAnalysisContainer";
  import MsDrawer from "@/business/components/common/components/MsDrawer";
  import ReportHeader from './base/ReportHeader';
  import TestCaseCountContainer from "./testCaseCount/TestCaseCountContainer";

  export default {
    name: "ReportAnalysis",
    components: {ReportCard, TestAnalysisContainer, MsDrawer, ReportHeader, TestCaseCountContainer},
    data() {
      return {
        reportType: "track",
        testCaseTrendDrawer: false,
        testCaseCountDrawer: false,
        historyReportId:"",
        reportTypes: [{id: 'track', name: this.$t('test_track.test_track')}],
      }
    },
    methods: {
      openCard(type) {
        if(type === 'trackTestCase'){
          this.testCaseTrendDrawer = true;
        }else if(type === 'countTestCase'){
          this.testCaseCountDrawer = true;
        }
      },
      close() {
        this.testCaseTrendDrawer = false;
        this.testCaseCountDrawer = false;
      },
      saveReport(){
        if(this.testCaseTrendDrawer){
          this.$refs.testAnalysisContainer.saveReport();
        }else if(this.testCaseCountDrawer){
          this.$refs.testCaseCountContainer.saveReport();
        }
      },
      selectAndSaveReport(){
        if(this.testCaseTrendDrawer){
          this.$refs.testAnalysisContainer.selectAndSaveReport();
        }else if(this.testCaseCountDrawer){
          this.$refs.testCaseCountContainer.selectAndSaveReport();
        }
      },
      initHistoryReportId(reportId){
        this.historyReportId = reportId;
      },
    },
  }
</script>

<style scoped>
  .ms-span {
    margin: 10px 10px 0px
  }

  .tip {
    float: left;
    font-size: 14px;
    border-radius: 2px;
    border-left: 2px solid #783887;
    margin: 10px 20px 0px;
  }
</style>
