<template>
  <ms-container>
    <el-main>
      <el-card v-loading="loading" v-if="show">
        <el-row>
          <el-col :span="16">
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
      </el-card>
      <project-environment-dialog ref="projectEnvDialog"></project-environment-dialog>
    </el-main>
  </ms-container>
</template>

<script>

import MsReportErrorLog from "./ErrorLog";
import MsReportLogDetails from "./LogDetails";
import MsReportRequestStatistics from "./RequestStatistics";
import MsReportTestOverview from "./TestOverview";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MonitorCard from "./MonitorCard";
import MsReportTestDetails from './TestDetails';
import ProjectEnvironmentDialog from "./ProjectEnvironmentDialog";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsTestConfiguration from "./TestConfiguration";
import SamplesTabs from "./samples/SamplesTabs";

export default   {
  name: "LoadCaseReportView",
  components: {
    MsTestConfiguration,
    MonitorCard,
    MsReportErrorLog,
    MsReportLogDetails,
    MsReportRequestStatistics,
    MsReportTestOverview,
    MsReportTestDetails,
    MsContainer,
    MsMainContainer,
    ProjectEnvironmentDialog,
    MsTag,
    SamplesTabs,
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
      testPlan: {testResourcePoolId: null},
      show: true,
      test: {testResourcePoolId: null},
      haveErrorSamples: false,
      errorSamples: {},
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
  created(){
    if (this.planReportTemplate) {
      if (this.planReportTemplate.errorSamples) {
        this.errorSamples = this.planReportTemplate.errorSamples;
        this.haveErrorSamples = true;
      } else {
        this.haveErrorSamples = false;
      }
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
    },
    initReportTimeInfo() {
      if (this.status === 'Starting') {
        this.clearData();
        return;
      }
      if (this.planReportTemplate) {
        this.handleInitReportTimeInfo(this.planReportTemplate);
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
    init() {
      alert(1);
      this.clearData();
      if (this.planReportTemplate) {
        this.handleInit(this.planReportTemplate);
      }
    },

    handleInit(data) {
      if (data) {
        if (data.errorSamples) {
          this.errorSamples = data.errorSamples;
          this.haveErrorSamples = true;
        } else {
          this.haveErrorSamples = false;
        }
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

</style>

