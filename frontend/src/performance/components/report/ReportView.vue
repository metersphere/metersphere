<template>
  <div v-loading="result.loading" class="report-view-container">
    <div class="main-content">
      <el-card>
        <el-row>
          <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/' }">{{projectName}}</el-breadcrumb-item>
            <el-breadcrumb-item>{{testName}}</el-breadcrumb-item>
            <el-breadcrumb-item>{{reportName}}</el-breadcrumb-item>
          </el-breadcrumb>
        </el-row>

        <el-row style="margin-top: 15px;margin-left: -300px;">
          <el-button type="primary" plain size="mini">立即停止</el-button>
          <el-button type="success" plain size="mini">再次执行</el-button>
          <el-button type="info" plain size="mini">导出</el-button>
          <el-button type="warning" plain size="mini">比较</el-button>
        </el-row>
        <el-divider></el-divider>

        <el-tabs v-model="active" type="border-card" :stretch="true">
          <el-tab-pane :label="$t('report.test_overview')">
            <ms-report-test-overview />
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_request_statistics')">
            <ms-report-request-statistics />
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_error_log')">
            <ms-report-error-log />
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_log_details')">
            <ms-report-log-details />
          </el-tab-pane>
        </el-tabs>

      </el-card>
    </div>
  </div>
</template>

<script>
  import MsReportErrorLog from './components/ErrorLog';
  import MsReportLogDetails from './components/LogDetails';
  import MsReportRequestStatistics from './components/RequestStatistics';
  import MsReportTestOverview from './components/TestOverview';

  export default {
    name: "ReportView",
    components: {
      MsReportErrorLog,
      MsReportLogDetails,
      MsReportRequestStatistics,
      MsReportTestOverview
    },
    data() {
      return {
        result: {},
        active: '0',
        reportId: '',
        reportName: '',
        testName: '',
        projectName: ''
      }
    },
    methods: {
      initBreadcrumb() {
        this.result = this.$get("report/test/pro/info/" + this.reportId, res => {
          let data = res.data;
          this.reportName = data.name;
          this.testName = data.testName;
          this.projectName = data.projectName;
        })
      }
    },
    created() {
      this.reportId = this.$route.path.split('/')[2];
      this.initBreadcrumb();
    },
  }
</script>

<style scoped>
  .report-view-container {
    float: none;
    text-align: center;
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .report-view-container .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }
</style>
