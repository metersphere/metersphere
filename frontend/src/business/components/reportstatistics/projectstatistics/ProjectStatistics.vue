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
                       @closePage="close" @saveReport="openSaveReportDialog('save')" @selectAndSaveReport="openSaveReportDialog('saveAs')"/>
      </template>
      <test-analysis-container @initHistoryReportId="initHistoryReportId" ref="testAnalysisContainer"/>
    </ms-drawer>

    <!-- 测试用例分析页面 -->
    <ms-drawer :visible="testCaseCountDrawer" :size="100" @close="close" direction="right" :show-full-screen="false" :is-show-close="false" style="overflow: hidden">
      <template v-slot:header>
        <report-header :title="$t('commons.report_statistics.test_case_count')" :history-report-id="historyReportId"
                       @closePage="close" @saveReport="openSaveReportDialog('save')" @selectAndSaveReport="openSaveReportDialog('saveAs')"/>
      </template>
      <test-case-count-container @initHistoryReportId="initHistoryReportId" ref="testCaseCountContainer"/>
    </ms-drawer>

    <el-dialog
      :title="$t('commons.save')"
      :visible.sync="dialogFormVisible"
      width="30%"
      :before-close="handleCloseSaveReportDialog">

      <el-form :model="form" :rules="saveReportRules" ref="saveReportRuleForm">
        <el-form-item :label="$t('commons.input_name')" prop="reportName" label-width="120px">
          <el-input v-model="form.reportName" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveReport">{{$t('commons.confirm')}}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import ReportCard from "@/business/components/reportstatistics/projectstatistics/ReportCard";
  import TestAnalysisContainer from "@/business/components/reportstatistics/projectstatistics/track/TestAnalysisContainer";
  import MsDrawer from "@/business/components/common/components/MsDrawer";
  import ReportHeader from "@/business/components/reportstatistics/base/ReportHeader";
  import TestCaseCountContainer from "@/business/components/reportstatistics/projectstatistics/casecount/TestCaseCountContainer";


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
        dialogFormVisible: false,
        form: {
          reportName: "",
          saveType: "",
        },
        saveReportRules: {
          reportName: [
            { required: true, message: this.$t('commons.input_name'), trigger: 'blur' },
            { min: 1, max: 20, message: '长度不大于20个字符', trigger: 'blur' }
          ],
        }
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
      openSaveReportDialog(saveType){
        this.form.saveType = saveType;
        this.dialogFormVisible = true;
      },
      saveReport(){
        this.$refs['saveReportRuleForm'].validate((valid) => {
          if (valid) {
            if(this.testCaseTrendDrawer){
              this.$refs.testAnalysisContainer.saveAndSaveAsReport(this.form.reportName,this.form.saveType);
            }else if(this.testCaseCountDrawer){
              this.$refs.testCaseCountContainer.saveAndSaveAsReport(this.form.reportName,this.form.saveType);
            }
            this.form.reportName = "";
            this.form.saveType = "";
            this.dialogFormVisible = false;
          } else {
            return false;
          }
        });
      },
      initHistoryReportId(reportId){
        this.historyReportId = reportId;
      },
      handleCloseSaveReportDialog(){
        this.form.reportName = "";
        this.form.saveType = "";
        this.dialogFormVisible = false;
      }
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
