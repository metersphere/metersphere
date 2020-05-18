<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="result.loading">
        <el-row>
          <el-col :span="16">
            <el-row>
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/performance/test/' + this.projectId }">{{projectName}}
                </el-breadcrumb-item>
                <el-breadcrumb-item :to="{ path: '/performance/test/edit/' + this.testId }">{{testName}}
                </el-breadcrumb-item>
                <el-breadcrumb-item>{{reportName}}</el-breadcrumb-item>
              </el-breadcrumb>
            </el-row>
            <el-row class="ms-report-view-btns">
              <el-button type="primary" plain size="mini">{{$t('report.test_stop_now')}}</el-button>
              <el-button type="success" plain size="mini">{{$t('report.test_execute_again')}}</el-button>
              <el-button type="info" plain size="mini">{{$t('report.export')}}</el-button>
              <el-button type="warning" plain size="mini">{{$t('report.compare')}}</el-button>
            </el-row>
          </el-col>
          <el-col :span="8">
            <span class="ms-report-time-desc">
              {{$t('report.test_duration', [this.minutes, this.seconds])}}
            </span>
            <span class="ms-report-time-desc">
              {{$t('report.test_start_time')}}：{{startTime}}
            </span>
            <span class="ms-report-time-desc">
              {{$t('report.test_end_time')}}：{{endTime}}
            </span>
          </el-col>
        </el-row>

        <el-divider></el-divider>

        <el-tabs v-model="active" type="border-card" :stretch="true">
          <el-tab-pane :label="$t('report.test_overview')">
            <ms-report-test-overview :id="reportId" :status="status"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_request_statistics')">
            <ms-report-request-statistics :id="reportId" :status="status"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_error_log')">
            <ms-report-error-log :id="reportId" :status="status"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_log_details')">
            <ms-report-log-details :id="reportId" :status="status"/>
          </el-tab-pane>
        </el-tabs>

      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
  import MsReportErrorLog from './components/ErrorLog';
  import MsReportLogDetails from './components/LogDetails';
  import MsReportRequestStatistics from './components/RequestStatistics';
  import MsReportTestOverview from './components/TestOverview';
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";

  export default {
    name: "PerformanceReportView",
    components: {
      MsReportErrorLog,
      MsReportLogDetails,
      MsReportRequestStatistics,
      MsReportTestOverview,
      MsContainer,
      MsMainContainer
    },
    data() {
      return {
        result: {},
        active: '0',
        reportId: '',
        status: '',
        reportName: '',
        testId: '',
        testName: '',
        projectId: '',
        projectName: '',
        startTime: '0',
        endTime: '0',
        minutes: '0',
        seconds: '0',
        title: 'Logging',
      }
    },
    methods: {
      initBreadcrumb() {
        if (this.reportId) {
          this.result = this.$get("/performance/report/test/pro/info/" + this.reportId, res => {
            let data = res.data;
            if (data) {
              this.reportName = data.name;
              this.testId = data.testId;
              this.testName = data.testName;
              this.projectId = data.projectId;
              this.projectName = data.projectName;
            }
          })
        }
      },
      initReportTimeInfo() {
        if (this.reportId) {
          this.result = this.$get("/performance/report/content/report_time/" + this.reportId, res => {
            let data = res.data;
            if (data) {
              this.startTime = data.startTime;
              this.endTime = data.endTime;
              let duration = data.duration;
              this.minutes = Math.floor(duration / 60);
              this.seconds = duration % 60;
            }
          })
        }
      },
    },
    mounted() {
      this.reportId = this.$route.path.split('/')[4];
      this.result = this.$get("/performance/report/" + this.reportId, res => {
        let data = res.data;
        this.status = data.status;
        switch (data.status) {
          case 'Error':
            this.$warning(this.$t('report.generation_error'));
            break;
          case 'Starting':
          case 'Reporting':
            this.$info(this.$t('report.being_generated'));
            break;
          case 'Running':
            break;
          default:
            break;
        }
      })
      this.initBreadcrumb();
      this.initReportTimeInfo();
    },
    watch: {
      '$route'(to) {
        let reportId = to.path.split('/')[4];
        if (reportId) {
          this.$get("/performance/report/test/pro/info/" + reportId, response => {
            let data = response.data;
            if (data) {
              this.reportName = data.name;
              this.testName = data.testName;
              this.projectName = data.projectName;
            }
          });
          this.result = this.$get("/performance/report/content/report_time/" + this.reportId, res => {
            let data = res.data;
            if (data) {
              this.startTime = data.startTime;
              this.endTime = data.endTime;
              let duration = data.duration;
              this.minutes = Math.floor(duration / 60);
              this.seconds = duration % 60;
            }
          })
          window.location.reload();
        }
      }
    }
  }
</script>

<style scoped>

  .ms-report-view-btns {
    margin-top: 15px;
  }

  .ms-report-time-desc {
    text-align: left;
    display: block;
    color: #5C7878;
  }

</style>
