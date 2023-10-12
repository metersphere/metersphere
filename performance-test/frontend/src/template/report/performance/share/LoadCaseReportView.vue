<template>
  <ms-container>
    <el-main>
      <el-card v-loading="loading" v-if="show">

        <el-row v-if="isShare">
          <el-col :span="24">
            <div style="float:left;">
              <div>
                <span class="ms-report-time-desc-share">{{ $t('commons.name') }}：{{ report.name }}</span>
                <span class="ms-report-time-desc-share">{{ $t('commons.executor') }}：{{ report.userName }}</span>
              </div>
              <div>
                <span class="ms-report-time-desc-share">{{ $t('report.test_duration', [minutes, seconds]) }}</span>
                <span class="ms-report-time-desc-share" v-if="startTime !== '0'">
                  {{ $t('report.test_start_time') }}：{{ startTime | datetimeFormat }}
              </span>
                <span class="ms-report-time-desc-share" v-else>{{ $t('report.test_start_time') }}：-</span>
                <span class="ms-report-time-desc-share" v-if="report.status === 'Completed' && endTime !== '0'">
                  {{ $t('report.test_end_time') }}：{{ endTime | datetimeFormat }}
              </span>
                <span class="ms-report-time-desc-share" v-else>{{ $t('report.test_end_time') }}：- </span>
              </div>
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

        <el-row v-else>
          <el-col :span="16">
            <el-row v-if="!isPlanReport">
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/performance/test/' + this.projectId }">{{ projectName }}
                </el-breadcrumb-item>
                <el-breadcrumb-item :to="{ path: '/performance/test/edit/' + this.testId }">{{ testName }}
                </el-breadcrumb-item>
                <el-breadcrumb-item>{{ reportName }}</el-breadcrumb-item>
              </el-breadcrumb>
            </el-row>
            <el-row class="ms-report-view-btns" v-if="!isPlanReport">
              <el-button :disabled="isReadOnly || report.status !== 'Running'" type="primary" plain size="mini"
                         @click="dialogFormVisible=true">
                {{ $t('report.test_stop_now') }}
              </el-button>
              <el-button :disabled="isReadOnly" type="info" plain size="mini" @click="handleExport(reportName)">
                {{ $t('test_track.plan_view.export_report') }}
              </el-button>
              <el-button :disabled="isReadOnly" type="warning" plain size="mini" @click="downloadJtl()">
                {{ $t('report.downloadJtl') }}
              </el-button>
            </el-row>
          </el-col>
          <el-col :span="8">
            <div v-if="isPlanReport" style="float: right;margin-right: 10px;">
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
            <div style="float: left">
              <span class="ms-report-time-desc">
              {{
                  $t('report.test_duration', [templateMinutes ? templateMinutes : minutes,
                    templateSeconds ? templateSeconds : seconds])
                }}
            </span>
              <span class="ms-report-time-desc" v-if="startTime !== '0'">
              {{ $t('report.test_start_time') }}：{{ startTime | datetimeFormat }}
            </span>
              <span class="ms-report-time-desc" v-else-if="planReportTemplate && planReportTemplate.startTime">
              {{ $t('report.test_start_time') }}：{{ planReportTemplate.startTime | datetimeFormat }}
            </span>
              <span class="ms-report-time-desc" v-else>
              {{ $t('report.test_start_time') }}：-
            </span>
              <span class="ms-report-time-desc" v-if="report.status === 'Completed' && endTime !== '0'">
              {{ $t('report.test_end_time') }}：{{ endTime | datetimeFormat }}
            </span>
              <span class="ms-report-time-desc" v-else-if="planReportTemplate && planReportTemplate.endTime">
              {{ $t('report.test_end_time') }}：{{ planReportTemplate.endTime | datetimeFormat }}
            </span>
              <span class="ms-report-time-desc" v-else>
              {{ $t('report.test_end_time') }}：-
            </span>
            </div>
          </el-col>
        </el-row>

        <el-divider/>
        <div ref="resume">
          <el-tabs v-model="active">
            <el-tab-pane :label="$t('report.test_overview')">
              <ms-report-test-overview :report="report" :is-share="isShare" :plan-report-template="planReportTemplate"
                                       :share-id="shareId" ref="testOverview"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_details')">
              <ms-report-test-details :report="report" :is-share="isShare" :plan-report-template="planReportTemplate"
                                      :share-id="shareId" ref="testDetails"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_request_statistics')">
              <ms-report-request-statistics :report="report" :is-share="isShare"
                                            :plan-report-template="planReportTemplate"
                                            :share-id="shareId" ref="requestStatistics"/>
            </el-tab-pane>

            <el-tab-pane v-if="haveErrorSamples" :label="$t('report.test_error_log')">
              <samples-tabs :samples="errorSamples" ref="errorSamples"/>
            </el-tab-pane>

            <el-tab-pane v-else :label="$t('report.test_error_log')">
              <ms-report-error-log :report="report" :is-share="isShare" :plan-report-template="planReportTemplate"
                                   :share-id="shareId" ref="errorLog"/>
            </el-tab-pane>


            <el-tab-pane :label="$t('report.test_log_details')">
              <ms-report-log-details :report="report" :is-share="isShare" :plan-report-template="planReportTemplate"
                                     :share-id="shareId"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_monitor_details')">
              <monitor-card :report="report" :is-share="isShare" :plan-report-template="planReportTemplate"
                            :share-id="shareId"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('report.test_config')">
              <ms-test-configuration :report-id="reportId" :report="report"
                                     :plan-report-template="planReportTemplate"
                                     :is-share="isShare" :share-id="shareId"/>
            </el-tab-pane>
          </el-tabs>
        </div>

        <ms-performance-report-export v-if="!isShare && !planReportTemplate" :title="reportName"
                                      id="performanceReportExport" v-show="reportExportVisible"
                                      :report="report"/>

      </el-card>
      <el-dialog :title="$t('report.test_stop_now_confirm')" :visible.sync="dialogFormVisible" width="30%"
                 :append-to-body="true">
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
    </el-main>
  </ms-container>
</template>

<script>

import {exportPdf} from "metersphere-frontend/src/utils";
import html2canvas from 'html2canvas';
import {Message} from "element-ui";
import MsPerformanceReportExport from "../../../../business/report/PerformanceReportExport";
import MsReportErrorLog from "../../../../business/report/components/ErrorLog";
import MsReportLogDetails from "../../../../business/report/components/LogDetails";
import MsReportRequestStatistics from "../../../../business/report/components/RequestStatistics";
import MsReportTestOverview from "../../../../business/report/components/TestOverview";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MonitorCard from "../../../../business/report/components/MonitorCard";
import MsReportTestDetails from '../../../../business/report/components/TestDetails';
import ProjectEnvironmentDialog from "../../../../business/report/components/ProjectEnvironmentDialog";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {
  getPerformanceReport,
  getPerformanceReportErrorSamples,
  getPerformanceReportTime,
  getSharePerformanceReport, getSharePerformanceReportErrorSamples,
  getSharePerformanceReportTime
} from "../../../../api/load-test";
import MsTestConfiguration from "../../../../business/report/components/TestConfiguration";
import {getTestProInfo, stopTest} from "../../../../api/report";
import SamplesTabs from "@/business/report/components/samples/SamplesTabs.vue";


export default {
  name: "LoadCaseReportView",
  components: {
    SamplesTabs,
    MsTestConfiguration,
    MonitorCard,
    MsPerformanceReportExport,
    MsReportErrorLog,
    MsReportLogDetails,
    MsReportRequestStatistics,
    MsReportTestOverview,
    MsReportTestDetails,
    MsContainer,
    MsMainContainer,
    ProjectEnvironmentDialog,
    MsTag,
  },
  data() {
    return {
      loading: false,
      active: '0',
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
      projectEnvMap: null,
      showMoreProjectEnvMap: false,
      allProjectEnvMap: null,
      report: {},
      websocket: null,
      dialogFormVisible: false,
      reportExportVisible: false,
      haveErrorSamples: false,
      testPlan: {testResourcePoolId: null},
      show: true,
      errorSamples: {},
      test: {testResourcePoolId: null},
    };
  },
  props: {
    reportId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    isPlanReport: Boolean,
    isShare: Boolean,
    shareId: String,
    planReportTemplate: Object
  },
  watch: {
    reportId() {
      this.init();
    }
  },
  created() {
    if (this.reportId) {
      this.init();
    }
  },
  computed: {
    showProjectEnv() {
      return this.projectEnvMap && JSON.stringify(this.projectEnvMap) !== '{}';
    },
    templateMinutes() {
      if (this.planReportTemplate && this.planReportTemplate.duration) {
        let duration = this.planReportTemplate.duration;
        return Math.floor(duration / 60);
      }
      return null;
    },
    templateSeconds() {
      if (this.planReportTemplate && this.planReportTemplate.duration) {
        let duration = this.planReportTemplate.duration;
        return duration % 60;
      }
      return null;
    }
  },
  methods: {
    showAllProjectInfo() {
      this.$refs.projectEnvDialog.open(this.allProjectEnvMap);
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
    initBreadcrumb(callback) {
      if (this.isPlanReport) {
        return;
      }
      if (this.reportId) {
        this.loading = getTestProInfo(this.reportId)
          .then(res => {
            let data = res.data;
            if (data) {
              this.reportName = data.name;
              this.testId = data.testId;
              this.testName = data.testName;
              this.projectId = data.projectId;
              this.projectName = data.projectName;
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
      if (this.planReportTemplate) {
        this.handleInitReportTimeInfo(this.planReportTemplate);
      } else if (this.isShare) {
        if (this.reportId) {
          this.loading = getSharePerformanceReportTime(this.shareId, this.reportId)
            .then(({data}) => {
              this.handleInitReportTimeInfo(data);
            }).catch(() => {
              this.clearData();
            });
        }
      } else {
        if (this.reportId) {
          this.loading = getPerformanceReportTime(this.reportId)
            .then(({data}) => {
              this.handleInitReportTimeInfo(data);
            }).catch(() => {
              this.clearData();
            });
        }
      }
    },
    handleInitReportTimeInfo(data) {
      if (data) {
        this.startTime = data.startTime;
        this.endTime = data.endTime;
        let duration = data.duration;
        this.minutes = Math.floor(duration / 60);
        this.seconds = duration % 60;
      }
    },
    initWebSocket() {
      if (this.isPlanReport) {
        return;
      }
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
      this.show = false;
      this.startTime = '0';
      this.endTime = '0';
      this.minutes = '0';
      this.seconds = '0';
      this.$nextTick(() => {
        this.show = true;
      });
    },
    stopTest(forceStop) {
      this.loading = stopTest(this.reportId, forceStop)
        .then(() => {
          this.$success(this.$t('report.test_stop_success'));
          if (forceStop) {
            this.$router.push('/performance/report/all');
          } else {
            this.report.status = 'Completed';
          }
        });
      this.dialogFormVisible = false;
    },
    onOpen() {
    },
    onError(e) {
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
      this.loading = false;
    },
    downloadJtl() {
      let config = {
        url: "/performance/report/jtl/download/" + this.reportId,
        method: 'get',
        responseType: 'blob'
      };
      this.loading = this.$request(config).then(response => {
        const content = response.data;
        const blob = new Blob([content]);
        if ("download" in document.createElement("a")) {
          // 非IE下载
          //  chrome/firefox
          let aTag = document.createElement('a');
          aTag.download = this.reportName + ".jtl";
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
    init() {
      this.clearData();
      if (this.planReportTemplate) {
        this.handleInit(this.planReportTemplate);
      } else if (this.isShare) {
        this.loading = getSharePerformanceReport(this.shareId, this.reportId)
          .then(({data}) => {
            this.handleInit(data);
          });
        this.checkSampleResults(this.reportId);
      } else {
        this.loading = getPerformanceReport(this.reportId)
          .then(({data}) => {
            this.handleInit(data);
          });
      }
    },
    checkSampleResults(reportId) {
      getSharePerformanceReportErrorSamples(this.shareId,reportId)
          .then(res => {
            if (res.data) {
              this.errorSamples = res.data;
              this.haveErrorSamples = true;
            } else {
              this.haveErrorSamples = false;
            }
          });
    },
    handleInit(data) {
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
        this.initBreadcrumb();
        this.initWebSocket();
      } else {
        this.$error(this.$t('report.not_exist'));
      }
    }
  },
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

.ms-report-time-desc-share {
  text-align: left;
  color: #5C7878;
  padding-right: 20px;
}

.report-export .el-card {
  margin-bottom: 15px;
}

</style>

