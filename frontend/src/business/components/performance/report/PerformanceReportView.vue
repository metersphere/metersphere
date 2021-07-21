<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="result.loading">
        <el-row>
          <el-col :span="16">
            <el-row>
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/performance/test/' + this.projectId }">{{ projectName }}
                </el-breadcrumb-item>
                <el-breadcrumb-item v-if="!testDeleted" :to="{ path: '/performance/test/edit/' + this.testId }">
                  {{ testName }}
                </el-breadcrumb-item>
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
              <el-button :disabled="report.status !== 'Completed'" type="default" plain
                         size="mini"
                         @click="compareReports()">
                {{ $t('report.compare') }}
              </el-button>
              <el-button type="warning" plain size="mini" @click="downloadJtl()">
                {{ $t('report.downloadJtl') }}
              </el-button>
            </el-row>
          </el-col>
          <el-col :span="6">
            <span class="ms-report-time-desc">
              {{ $t('report.test_duration', [this.minutes, this.seconds]) }}
            </span>
            <span class="ms-report-time-desc" v-if="startTime !== '0'">
              {{ $t('report.test_start_time') }}：{{ startTime | timestampFormatDate }}
            </span>
            <span class="ms-report-time-desc" v-else>
              {{ $t('report.test_start_time') }}：-
            </span>
            <span class="ms-report-time-desc" v-if="report.status === 'Completed' && endTime !== '0'">
              {{ $t('report.test_end_time') }}：{{ endTime | timestampFormatDate }}
            </span>
            <span class="ms-report-time-desc" v-else>
              {{ $t('report.test_end_time') }}：-
            </span>
          </el-col>
          <el-col :span="2">
            <el-select v-model="refreshTime"
                       size="mini"
                       :disabled="report.status === 'Completed' || report.status === 'Error'"
                       @change="refresh"
                       style="width: 100%;">
              <template slot="prefix">
                <i class="el-icon-refresh" style="cursor: pointer;padding-top: 8px;" @click="refresh"></i>
              </template>
              <el-option
                v-for="item in refreshTimes"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-col>
        </el-row>

        <el-divider/>
        <div ref="resume">
          <el-tabs v-model="active">
            <el-tab-pane :label="$t('load_test.pressure_config')">
              <ms-performance-pressure-config :is-read-only="true" :report="report"/>
            </el-tab-pane>
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
          </el-tabs>
        </div>

        <ms-performance-report-export :title="reportName" id="performanceReportExport" v-show="reportExportVisible"
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
import MsPerformancePressureConfig from "./components/PerformancePressureConfig";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";

import {exportPdf, hasPermission} from "@/common/js/utils";
import html2canvas from 'html2canvas';
import MsPerformanceReportExport from "./PerformanceReportExport";
import {Message} from "element-ui";
import SameTestReports from "@/business/components/performance/report/components/SameTestReports";
import MonitorCard from "@/business/components/performance/report/components/MonitorCard";


export default {
  name: "PerformanceReportView",
  components: {
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
    MsPerformancePressureConfig
  },
  data() {
    return {
      result: {},
      active: '1',
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
      isReadOnly: false,
      report: {},
      websocket: null,
      dialogFormVisible: false,
      reportExportVisible: false,
      testPlan: {testResourcePoolId: null},
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
    };
  },
  methods: {
    initBreadcrumb(callback) {
      if (this.reportId) {
        this.result = this.$get("/performance/report/test/pro/info/" + this.reportId, res => {
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
        this.result = this.$get("/performance/report/content/report_time/" + this.reportId)
          .then(res => {
            let data = res.data.data;
            if (data) {
              this.startTime = data.startTime;
              this.endTime = data.endTime;
              let duration = data.duration;
              this.minutes = Math.floor(duration / 60);
              this.seconds = duration % 60;
            }
          }).catch(() => {
            this.clearData();
          });
      }
    },
    initWebSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      const uri = protocol + window.location.host + "/performance/report/" + this.reportId;
      this.websocket = new WebSocket(uri);
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
      this.result = this.$get('/performance/stop/' + this.reportId + '/' + forceStop, () => {
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
        this.result = this.$post('/performance/run', {id: testId, triggerMode: 'MANUAL'}, (response) => {
          this.reportId = response.data;
          this.$router.push({path: '/performance/report/view/' + this.reportId});
          this.clearData();
        });
      }).catch(() => {
      });
    },
    onOpen() {
      this.refresh();
      // window.console.log("socket opening.");
    },
    onError(e) {
      // window.console.error(e)
    },
    onMessage(e) {
      this.$set(this.report, "refresh", e.data); // 触发刷新
      if (e.data.startsWith('Error')) {
        this.$set(this.report, "status", 'Error');
        this.$warning(e.data);
        return;
      }
      this.$set(this.report, "status", 'Running');
      this.status = 'Running';
      this.initReportTimeInfo();
      // window.console.log('receive a message:', e.data);
    },
    onClose(e) {
      if (e.code === 1005) {
        // 强制删除之后关闭socket，不用刷新report
        return;
      }
      this.$set(this.report, "refresh", Math.random()); // 触发刷新
      this.$set(this.report, "status", 'Completed');
      this.initReportTimeInfo();
      // window.console.log("socket closed.");
    },
    handleExport(name) {
      this.result.loading = true;
      this.reportExportVisible = true;
      let reset = this.exportReportReset;

      this.$nextTick(function () {
        setTimeout(() => {
          let ids = ['testOverview', 'testDetails', 'requestStatistics', 'errorLog'];
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
    exportReportReset() {
      this.reportExportVisible = false;
      this.result.loading = false;
    },
    downloadJtl() {
      let config = {
        url: "/performance/report/jtl/download/" + this.reportId,
        method: 'get',
        responseType: 'blob'
      };
      this.result = this.$request(config).then(response => {
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
    compareReports() {
      this.$refs.compareReports.open(this.report);
    },
    getReport(reportId) {
      this.result = this.$get("/performance/report/" + reportId, res => {
        let data = res.data;
        if (data) {
          this.status = data.status;
          this.$set(this.report, "id", data.id);
          this.$set(this.report, "status", data.status);
          this.$set(this.report, "testId", data.testId);
          this.$set(this.report, "name", data.name);
          this.$set(this.report, "createTime", data.createTime);
          this.$set(this.report, "loadConfiguration", data.loadConfiguration);
          this.checkReportStatus(data.status);
          if (this.status === "Completed" || this.status === "Running") {
            this.initReportTimeInfo();
          }

          this.$get('/performance/get/' + data.testId)
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
    }
  },
  created() {
    this.isReadOnly = !hasPermission('PROJECT_PERFORMANCE_REPORT:READ+DELETE');
    this.reportId = this.$route.path.split('/')[4];
    this.getReport(this.reportId);
  },
  watch: {
    '$route'(to) {
      if (to.name === "perReportView") {
        this.reportId = to.path.split('/')[4];
        this.getReport(this.reportId);
        this.initBreadcrumb((response) => {
          this.initReportTimeInfo();
        });
      } else {
        // console.log("close socket.");
        this.websocket.close(); //离开路由之后断开websocket连接
      }
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
