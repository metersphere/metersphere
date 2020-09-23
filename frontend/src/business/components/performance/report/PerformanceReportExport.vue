<template>
  <el-card class="report-export">
    <ms-report-title :title="$t('report.load_test_report')"/>
    <el-card id="testOverview">
      <template v-slot:header >
        <span class="title">{{$t('report.test_overview')}}</span>
      </template>
      <ms-report-test-overview :report="report" ref="testOverview"/>
    </el-card>
    <el-card id="requestStatistics" title="'requestStatistics'">
      <template v-slot:header >
        <span class="title">{{$t('report.test_request_statistics')}}</span>
      </template>
      <ms-report-request-statistics :report="report" ref="requestStatistics"/>
    </el-card>
    <el-card id="errorLog" title="'errorLog'">
      <template v-slot:header >
        <span class="title">{{$t('report.test_error_log')}}</span>
      </template>
      <ms-report-error-log :report="report" ref="errorLog"/>
    </el-card>
  </el-card>
</template>

<script>

  import MsReportErrorLog from './components/ErrorLog';
  import MsReportRequestStatistics from './components/RequestStatistics';
  import MsReportTestOverview from './components/TestOverview';

  import {checkoutTestManagerOrTestUser} from "@/common/js/utils";
  import MsPerformanceReportExport from "./PerformanceReportExport";
  import MsReportTitle from "../../common/components/MsReportTitle";


  export default {
    name: "MsPerformanceReportExport",
    components: {
      MsReportTitle,
      MsReportErrorLog,
      MsReportRequestStatistics,
      MsReportTestOverview,
    },
    props: ['report']
  }
</script>

<style scoped>

  .report-export .el-card {
    margin-top: 20px;
  }

</style>
