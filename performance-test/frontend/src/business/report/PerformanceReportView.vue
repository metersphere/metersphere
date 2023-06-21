<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="loading">
        <el-row :gutter="10">
          <el-col :span="14">
            <el-row>
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item @click.native="toListPage()" class="el-link">{{ projectName }}</el-breadcrumb-item>
                <el-breadcrumb-item v-if="!testDeleted" class="el-link" @click.native="toEditTestPage()">{{ testName }}</el-breadcrumb-item>
                <el-breadcrumb-item v-else>{{ testName }}</el-breadcrumb-item>
                <el-breadcrumb-item>{{ reportName }}</el-breadcrumb-item>
              </el-breadcrumb>
            </el-row>
            <el-row class="ms-report-view-btns">
              <el-button :disabled="isReadOnly || report.status !== 'Running' || testDeleted" type="primary" plain
                         size="mini"
                         @click="dialogFormVisible=true">
                {{ $t('report.test_stop_now') }}
              </el-button>
              <el-button :disabled="isReadOnly || report.status !== 'Completed' || testDeleted" type="success" plain
                         size="mini"
                         @click="rerun(testId)">
                {{ $t('report.test_execute_again') }}
              </el-button>
              <el-button :disabled="isReadOnly" type="info" plain size="mini" @click="handleExport(reportName)"
                         v-permission="['PROJECT_PERFORMANCE_REPORT:READ+EXPORT']">
                {{ $t('test_track.plan_view.export_report') }}
              </el-button>
              <el-popover
                v-permission="['PROJECT_PERFORMANCE_REPORT:READ+EXPORT']"
                style="padding: 0 10px;"
                placement="bottom"
                width="300">
                <p>{{ shareUrl }}</p>
                <span style="color: red;float: left;margin-left: 10px;" v-if="application.typeValue">{{
                    $t('commons.validity_period') + application.typeValue
                  }}</span>
                <div style="text-align: right; margin: 0">
                  <el-button type="primary" size="mini" :disabled="!shareUrl"
                             v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}
                  </el-button>
                </div>
                <el-button slot="reference" :disabled="isReadOnly" type="danger" plain size="mini"
                           @click="handleShare(report)">
                  {{ $t('test_track.plan_view.share_report') }}
                </el-button>
              </el-popover>
              <el-button :disabled="report.status !== 'Completed'" type="default" plain
                         size="mini" v-permission="['PROJECT_PERFORMANCE_REPORT:READ+COMPARE']"
                         @click="compareReports()">
                {{ $t('report.compare') }}
              </el-button>
              <el-button type="warning" plain size="mini"
                         :disabled="isReadOnly || report.status !== 'Completed' || testDeleted"
                         @click="downloadJtl()">
                {{ $t('report.downloadJtl') }}
              </el-button>
              <el-button type="default" :disabled="testDeleted" plain size="mini" @click="downloadZipFile()">
                {{ $t('report.downloadZipFile') }}
              </el-button>
            </el-row>
          </el-col>
          <el-col :span="10">
            <div style="float: right;">
              <span class="ms-report-time-desc">
                {{ $t('performance_test.report.test_duration_tips', [hours, minutes, seconds]) }}
              </span>
              <span class="ms-report-time-desc" v-if="startTime !== '0'">
                {{ $t('report.test_start_time') }}：{{ startTime | datetimeFormat }}
              </span>
              <span class="ms-report-time-desc" v-else>
                {{ $t('report.test_start_time') }}：-
              </span>
              <span class="ms-report-time-desc" v-if="report.status === 'Completed' && endTime !== '0'">
                {{ $t('report.test_end_time') }}：{{ endTime | datetimeFormat }}
              </span>
              <span class="ms-report-time-desc" v-else>
                {{ $t('report.test_end_time') }}：-
              </span>
            </div>
            <div style="float: right;margin-right: 10px;">
              <div v-if="showProjectEnv" type="flex">
                <span> {{ $t('commons.environment') + ':' }} </span>
                <div v-for="(values,key) in projectEnvMap" :key="key" style="margin-right: 10px">
                  {{ key + ":" }}
                  <ms-tag v-for="(item,index) in values" :key="index" type="success" :content="item"
                          style="margin-left: 2px"/>
                </div>
                <div v-show="showMoreProjectEnvMap">
                  <el-link icon="el-icon-more" @click="showAllProjectInfo"></el-link>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-divider/>
        <div ref="resume">
          <el-tabs v-model="active">
            <el-tab-pane :label="$t('report.test_overview')">
              <ms-report-test-overview :report="report" ref="testOverview"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_details')">
              <ms-report-test-details :report="report" ref="testDetails"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_request_statistics')">
              <ms-report-request-statistics :report="report" ref="requestStatistics"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_error_log')">
              <ms-report-error-log :report="report" ref="errorLog"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_log_details')">
              <ms-report-log-details :report="report"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_monitor_details')">
              <monitor-card :report="report"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_config')">
              <ms-test-configuration :test="test" :report-id="reportId"/>
            </el-tab-pane>
          </el-tabs>
        </div>

        <ms-performance-report-export :title="reportName" id="performanceReportExport" v-show="reportExportVisible"
                                      :project-env-map="allProjectEnvMap"
                                      :report="report"/>

      </el-card>
      <el-dialog :title="$t('report.test_stop_now_confirm')" :visible.sync="dialogFormVisible" width="30%">
        <p v-html="$t('report.force_stop_tips')"/>
        <p v-html="$t('report.stop_tips')"/>
        <div slot="footer" class="dialog-footer">
          <el-button type="danger" size="small" @click="stopTest(true)">{{ $t('report.force_stop_btn') }}
          </el-button>
          <el-button type="primary" size="small" @click="stopTest(false)">{{ $t('report.stop_btn') }}
          </el-button>
        </div>
      </el-dialog>
      <project-environment-dialog ref="projectEnvDialog"></project-environment-dialog>
    </ms-main-container>
    <same-test-reports ref="compareReports"/>
  </ms-container>
</template>

<script>
import MsReportErrorLog from './components/ErrorLog';
import MsReportLogDetails from './components/LogDetails';
import MsReportRequestStatistics from './components/RequestStatistics';
import MsReportTestDetails from './components/TestDetails';
import MsReportTestOverview from './components/TestOverview';
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {exportPdf} from "metersphere-frontend/src/utils";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import html2canvas from 'html2canvas';
import MsPerformanceReportExport from "./PerformanceReportExport";
import {Message} from "element-ui";
import SameTestReports from "./components/SameTestReports";
import MonitorCard from "./components/MonitorCard";
import MsTestConfiguration from "./components/TestConfiguration";
import {generateShareInfoWithExpired, getShareRedirectUrl} from "@/api/share";
import ProjectEnvironmentDialog from "./components/ProjectEnvironmentDialog";
import {downloadJtl, downloadZip, getProjectApplication, getReport, getReportTime, getTestProInfo, initReportSocket, stopTest} from "@/api/report";
import {getTest, runTest} from "@/api/performance";


export default {
  name: "PerformanceReportView",
  components: {
    MsTestConfiguration,
    MonitorCard,
    SameTestReports,
    MsPerformanceReportExport,
    MsReportErrorLog,
    MsReportLogDetails,
    MsReportRequestStatistics,
    MsReportTestOverview,
    MsContainer,
    MsMainContainer,
    MsReportTestDetails,
    MsTag,
    ProjectEnvironmentDialog
  },
  props: {},
  inject: [
    // 'reload'
  ],
  data() {
    return {
      loading: false,
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
      hours: '0',
      minutes: '0',
      seconds: '0',
      title: 'Logging',
      isReadOnly: false,
      report: {},
      websocket: null,
      dialogFormVisible: false,
      reportExportVisible: false,
      test: {testResourcePoolId: null},
      projectEnvMap: null,
      showMoreProjectEnvMap: false,
      allProjectEnvMap: null,
      refreshTime: localStorage.getItem("reportRefreshTime") || "10",
      refreshTimes: [
        {value: '1', label: '1s'},
        {value: '3', label: '3s'},
        {value: '5', label: '5s'},
        {value: '10', label: '10s'},
        {value: '20', label: '20s'},
        {value: '30', label: '30s'},
        {value: '60', label: '1m'},
        {value: '300', label: '5m'}
      ],
      testDeleted: false,
      shareUrl: "",
      application: {}
    };
  },
  computed: {
    showProjectEnv() {
      return this.projectEnvMap && JSON.stringify(this.projectEnvMap) !== '{}';
    },
    perReportId() {
      return this.getRouteParam('reportId');
    },
  },
  methods: {
    showAllProjectInfo() {
      this.$refs.projectEnvDialog.open(this.allProjectEnvMap);
    },
    initBreadcrumb(callback) {
      if (this.reportId) {
        this.result = getTestProInfo(this.reportId)
          .then(res => {
            let data = res.data;
            if (data) {
              this.reportName = data.name;
              this.testId = data.testId;
              this.testName = data.testName;
              this.projectId = data.projectId;
              this.projectName = data.projectName;
              //
              if (callback) callback(res);
            } else {
              this.$error(this.$t('report.not_exist'));
            }
          });
      }
    },
    initReportTimeInfo() {
      if (this.status === 'Starting') {
        this.clearData();
        return;
      }
      if (this.reportId) {
        getReportTime(this.reportId)
          .then(res => {
            let data = res.data;
            if (data) {
              this.startTime = data.startTime;
              this.endTime = data.endTime;
              let duration = data.duration;
              this.hours = Math.floor(duration / 60 / 60);
              this.minutes = Math.floor(duration / 60 % 60);
              this.seconds = duration % 60;
            }
          })
          .catch(() => {
            this.clearData();
          });
      }
    },
    initWebSocket() {
      this.websocket = initReportSocket(this.reportId)
      this.websocket.onmessage = this.onMessage;
      this.websocket.onopen = this.onOpen;
      this.websocket.onerror = this.onError;
      this.websocket.onclose = this.onClose;
    },
    checkReportStatus(status) {
      switch (status) {
        case 'Error':
          // this.$warning(this.$t('report.generation_error'));
          this.active = '4';
          break;
        case 'Starting':
          this.$alert(this.$t('report.start_status'));
          break;
        case 'Reporting':
        case 'Running':
        case 'Completed':
        default:
          break;
      }
    },
    clearData() {
      this.startTime = '0';
      this.endTime = '0';
      this.minutes = '0';
      this.seconds = '0';
    },
    stopTest(forceStop) {
      stopTest(this.reportId, forceStop)
        .then(() => {
          this.$success(this.$t('report.test_stop_success'));
          if (forceStop) {
            this.$router.push('/performance/report/all');
            this.websocket.close();
          } else {
            this.report.status = 'Completed';
          }
        });
      this.dialogFormVisible = false;
    },
    rerun(testId) {
      this.$confirm(this.$t('report.test_rerun_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.loading = runTest({id: testId})
          .then((response) => {
            this.reportId = response.data;
            this.$router.push({path: '/performance/report/view/' + this.reportId});
            this.clearData();
            // this.reload();
          });
      }).catch(() => {
      });
    },
    onOpen() {
      this.refresh();
    },
    onError(e) {
      // window.console.error(e)
    },
    onMessage(e) {
      this.$set(this.report, "refresh", e.data); // 触发刷新
      if (e.data.startsWith('Error')) {
        this.$set(this.report, "status", 'Error');
        this.$error(e.data);
        return;
      }
      this.$set(this.report, "status", 'Running');
      this.status = 'Running';
      this.initReportTimeInfo();
    },
    onClose(e) {
      if (e.code === 1005) {
        // 强制删除之后关闭socket，不用刷新report
        return;
      }
      this.$set(this.report, "refresh", Math.random()); // 触发刷新
      this.$set(this.report, "status", 'Completed');
      this.initReportTimeInfo();
    },
    handleExport(name) {
      this.loading = true;
      this.reportExportVisible = true;
      let reset = this.exportReportReset;

      this.$nextTick(function () {
        setTimeout(() => {
          let ids = ['testOverview', 'testDetails', 'requestStatistics', 'errorLog', 'monitorCard'];
          let promises = [];
          ids.forEach(id => {
            let promise = html2canvas(document.getElementById(id), {scale: 2});
            promises.push(promise);
          });
          Promise.all(promises).then(function (canvas) {
            exportPdf(name, canvas);
            reset();
          });
        }, 1000);
      });
    },
    handleShare(report) {
      let pram = {};
      pram.customData = report.id;
      pram.shareType = 'PERFORMANCE_REPORT';
      generateShareInfoWithExpired(pram)
        .then(({data}) => {
          this.shareUrl = getShareRedirectUrl(data);
        });
    },
    getProjectApplication() {
      getProjectApplication(getCurrentProjectID())
        .then(res => {
          if (res.data) {
            let quantity = res.data.typeValue.substring(0, res.data.typeValue.length - 1);
            let unit = res.data.typeValue.substring(res.data.typeValue.length - 1);
            if (unit === 'H') {
              res.data.typeValue = quantity + this.$t('commons.date_unit.hour');
            } else if (unit === 'D') {
              res.data.typeValue = quantity + this.$t('commons.date_unit.day');
            } else if (unit === 'M') {
              res.data.typeValue = quantity + this.$t('commons.workspace_unit') + this.$t('commons.date_unit.month');
            } else if (unit === 'Y') {
              res.data.typeValue = quantity + this.$t('commons.date_unit.year');
            }
            this.application = res.data;
          }
        });
    },
    exportReportReset() {
      this.reportExportVisible = false;
      this.loading = false;
    },
    downloadJtl() {
      downloadJtl(this.reportId)
        .then(response => {
          const content = response.data;
          const blob = new Blob([content], {type: "application/octet-stream"});
          if ("download" in document.createElement("a")) {
            // 非IE下载
            //  chrome/firefox
            let aTag = document.createElement('a');
            aTag.download = this.reportName + ".zip";
            aTag.href = URL.createObjectURL(blob);
            aTag.click();
            URL.revokeObjectURL(aTag.href);
          } else {
            // IE10+下载
            navigator.msSaveBlob(blob, this.filename);
          }
        }).catch(e => {
        let text = e.response.data.text();
        text.then((data) => {
          Message.error({message: JSON.parse(data).message || e.message, showClose: true});
        });
      });
    },
    downloadZipFile() {
      downloadZip(this.report)
        .then(response => {
          const filename = this.report.testId + ".zip";
          const blob = new Blob([response.data]);
          if ("download" in document.createElement("a")) {
            // 非IE下载
            //  chrome/firefox
            let aTag = document.createElement('a');
            aTag.download = filename;
            aTag.href = URL.createObjectURL(blob);
            aTag.click();
            URL.revokeObjectURL(aTag.href);
          } else {
            // IE10+下载
            navigator.msSaveBlob(blob, filename);
          }
        });
    },
    compareReports() {
      this.$refs.compareReports.open(this.report);
    },
    isProjectEnvShowMore(projectEnvMap) {
      this.showMoreProjectEnvMap = false;
      this.projectEnvMap = {};
      if (projectEnvMap) {
        let keySize = 0;
        for (let key in projectEnvMap) {
          keySize++;
          if (keySize > 1) {
            this.showMoreProjectEnvMap = true;
            return;
          } else {
            this.projectEnvMap = {};
            this.$set(this.projectEnvMap, key, projectEnvMap[key]);
          }
        }
      }
    },
    getReport(reportId) {
      this.loading = getReport(reportId)
        .then(res => {
          let data = res.data;
          if (data) {
            this.allProjectEnvMap = data.projectEnvMap;
            this.isProjectEnvShowMore(data.projectEnvMap);
            this.status = data.status;
            this.$set(this, "report", data);
            this.$set(this.test, "testResourcePoolId", data.testResourcePoolId);
            this.checkReportStatus(data.status);
            if (this.status === "Completed" || this.status === "Running") {
              this.initReportTimeInfo();
            }

            getTest(data.testId)
              .then(() => this.testDeleted = false)
              .catch(() => this.testDeleted = true);

            this.initBreadcrumb();
            this.initWebSocket();
          } else {
            this.$error(this.$t('report.not_exist'));
          }
        });
    },
    refresh() {
      if (this.status === 'Running' || this.status === 'Starting') {
        if (this.websocket && this.websocket.readyState === 1) {
          this.websocket.send(this.refreshTime);
        }
      }
      localStorage.setItem("reportRefreshTime", this.refreshTime);
    },
    handleProjectChange() {
      if (this.$route.path.startsWith('/performance/report/view')) {
        this.$nextTick(() => {
          this.$router.push('/performance/report/all');
        })
      }
    },
    getRouteParam(name) {
      if (this.$route) {
        return this.$route.params[name]
      } else {
        return null;
      }
    },
    toListPage() {
      window.open(`/#/performance/test/all`, '_blank');
    },
    toEditTestPage() {
      window.open('/#/performance/test/edit/' + this.testId, '_blank');
    }
  },
  created() {
    this.isReadOnly = !hasPermission('PROJECT_PERFORMANCE_REPORT:READ+DELETE');
    this.reportId = this.$route.path.split('/')[4];
    if (this.perReportId) {
      this.reportId = this.perReportId;
    }
    this.getReport(this.reportId);
    this.$EventBus.$on('projectChange', this.handleProjectChange);
    this.getProjectApplication();
  },
  destroyed() {
    this.$EventBus.$off('projectChange', this.handleProjectChange);
    this.$route.params ['reportId'] = null;
  },
  watch: {
    '$route'(to) {
      if (to.name === "perReportView") {
        this.reportId = to.path.split('/')[4];
        if (this.perReportId) {
          this.reportId = this.perReportId;
        }
        this.getReport(this.reportId);
        this.initBreadcrumb((response) => {
          this.initReportTimeInfo();
        });
      } else {
        this.websocket.close(); //离开路由之后断开websocket连接
      }
    },
    perReportId() {
      this.getReport(this.perReportId);
    }
  }
};
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

.report-export .el-card {
  margin-bottom: 15px;
}

</style>
