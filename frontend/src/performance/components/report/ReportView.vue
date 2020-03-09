<template>
  <div v-loading="result.loading">
    <div class="main-content">
      <el-card>
        <el-row>

        </el-row>

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
        active: '0'
      }
    },
    methods: {

    }
  }
</script>

<style scoped>

</style>
