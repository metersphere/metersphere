<template>
  <div v-loading="cardLoading">
    <el-alert :closable="false"
              type="info">
      <template v-slot:default>
        <span v-html="$t('organization.message.notes')"></span>
      </template>
    </el-alert>
    <div style="padding-top: 10px;"></div>
    <el-collapse accordion class="task-notification">
      <el-collapse-item name="2">
        <template v-slot:title>
          <span style="width: 200px">
            {{ $t('organization.message.track') }}
          </span>
          <span>
            {{ $t('organization.message.notice_count') }}: <span class="primary-text">{{ trackNoticeSize }}</span>
          </span>
        </template>
        <track-home-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                 :receive-type-options="receiveTypeOptions"/>
        <test-case-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                :receive-type-options="receiveTypeOptions"/>
        <test-review-notification @noticeSize="getNoticeSize" :review-receiver-options="reviewReceiverOptions"
                                  :receive-type-options="receiveTypeOptions"/>
        <test-plan-task-notification @noticeSize="getNoticeSize" :test-plan-receiver-options="testPlanReceiverOptions"
                                     :receive-type-options="receiveTypeOptions"/>
        <defect-task-notification @noticeSize="getNoticeSize" :defect-receiver-options="defectReceiverOptions"
                                  :receive-type-options="receiveTypeOptions"/>
        <track-report-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                   :receive-type-options="receiveTypeOptions"/>
      </el-collapse-item>
      <el-collapse-item name="3">
        <template v-slot:title>
          <span style="width: 200px">
            {{ $t('organization.message.api') }}
          </span>
          <span>
            {{ $t('organization.message.notice_count') }}: <span class="primary-text">{{ apiNoticeSize }}</span>
          </span>
        </template>
        <api-home-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                               :receive-type-options="receiveTypeOptions"/>
        <api-definition-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                     :receive-type-options="receiveTypeOptions"/>
        <api-automation-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                     :receive-type-options="receiveTypeOptions"/>
        <api-report-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                 :receive-type-options="receiveTypeOptions"/>
      </el-collapse-item>
      <el-collapse-item name="5" v-xpack>
        <template v-slot:title>
          <span style="width: 200px">
            {{ $t('organization.message.ui') }}
          </span>
          <span>
            {{ $t('organization.message.notice_count') }}: <span class="primary-text">{{ uiNoticeSize }}</span>
          </span>
        </template>
        <ui-automation-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                     :receive-type-options="receiveTypeOptions"/>
        <ui-report-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                 :receive-type-options="receiveTypeOptions"/>
      </el-collapse-item>
      <el-collapse-item name="4">
        <template v-slot:title>
          <span style="width: 200px">
            {{ $t('organization.message.performance') }}
          </span>
          <span>
            {{ $t('organization.message.notice_count') }}: <span class="primary-text">{{ performanceNoticeSize }}</span>
          </span>
        </template>
        <performance-test-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                       :receive-type-options="receiveTypeOptions"/>
        <performance-report-notification @noticeSize="getNoticeSize" :receiver-options="reviewReceiverOptions"
                                         :receive-type-options="receiveTypeOptions"/>
      </el-collapse-item>
      <el-collapse-item name="1">
        <template v-slot:title>
          <span style="width: 200px">{{ $t('organization.message.jenkins_task_notification') }}</span>
          <span>
            {{ $t('organization.message.notice_count') }}: <span class="primary-text">{{ jenkinsNoticeSize }}</span>
          </span>
        </template>
        <jenkins-notification @noticeSize="getNoticeSize" :jenkins-receiver-options="jenkinsReceiverOptions"
                              :receive-type-options="receiveTypeOptions"/>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
import {getCurrentUser} from "metersphere-frontend/src/utils/token";
import JenkinsNotification from "./jenkins/JenkinsNotification";
import TestPlanTaskNotification from "./track/TestPlanTaskNotification";
import TestReviewNotification from "./track/TestReviewNotification";
import DefectTaskNotification from "./track/DefectTaskNotification";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import HomeNotification from "./track/TrackHomeNotification";
import TrackHomeNotification from "./track/TrackHomeNotification";
import TestCaseNotification from "./track/TestCaseNotification";
import TrackReportNotification from "./track/TrackReportNotification";
import ApiDefinitionNotification from "./api/ApiDefinitionNotification";
import ApiAutomationNotification from "./api/ApiAutomationNotification";
import ApiReportNotification from "./api/ApiReportNotification";
import PerformanceTestNotification from "./performance/PerformanceTestNotification";
import PerformanceReportNotification from "./performance/PerformanceReportNotification";
import ApiHomeNotification from "./api/ApiHomeNotification";
import UiAutomationNotification from "./ui/UiAutomationNotification";
import UiReportNotification from "./ui/UiReportNotification";
import {getUserProjectMemberList} from "../../../api/user";

let taskData = {
  jenkins: [],
  api: [],
  performance: [],
  track: [],
  ui: [],
};

export default {
  name: "TaskNotification",
  components: {
    ApiHomeNotification,
    PerformanceReportNotification,
    PerformanceTestNotification,
    ApiReportNotification,
    ApiAutomationNotification,
    ApiDefinitionNotification,
    TrackReportNotification,
    TestCaseNotification,
    TrackHomeNotification,
    HomeNotification,
    DefectTaskNotification, TestReviewNotification, TestPlanTaskNotification, JenkinsNotification, MsContainer,
    MsMainContainer,
    UiAutomationNotification,
    UiReportNotification
  },
  data() {

    return {
      jenkinsNoticeSize: 0,
      apiNoticeSize: 0,
      performanceNoticeSize: 0,
      trackNoticeSize: 0,
      uiNoticeSize: 0,
      jenkinsReceiverOptions: [],
      //测试计划
      testPlanReceiverOptions: [],
      //评审
      reviewReceiverOptions: [],
      //缺陷
      defectReceiverOptions: [],
      receiveTypeOptions: [
        {value: 'IN_SITE', label: this.$t('organization.message.in_site')},
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')},
        {value: 'LARK', label: this.$t('organization.message.lark')},
        {value: 'WEBHOOK', label: this.$t('organization.message.webhook')},
      ],
      result: {},
      cardLoading: false
    };
  },

  activated() {
    this.initUserList();
  },
  methods: {
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL') {
        data.isReadOnly = !data.isReadOnly;
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },

    initUserList() {
      this.cardLoading = getUserProjectMemberList().then(response => {
        this.jenkinsReceiverOptions = response.data;
        this.reviewReceiverOptions = response.data;
        this.defectReceiverOptions = response.data;
        this.testPlanReceiverOptions = response.data;
      });
    },
    getNoticeSize(config) {
      switch (config.module) {
        case 'jenkins':
          taskData.jenkins = taskData.jenkins.filter(t => t.taskType !== config.taskType);
          taskData.jenkins = taskData.jenkins.concat(config.data);
          this.jenkinsNoticeSize = taskData.jenkins.length;
          break;
        case 'performance':
          taskData.performance = taskData.performance.filter(t => t.taskType !== config.taskType);
          taskData.performance = taskData.performance.concat(config.data);
          this.performanceNoticeSize = taskData.performance.length;
          break;
        case 'api':
          taskData.api = taskData.api.filter(t => t.taskType !== config.taskType);
          taskData.api = taskData.api.concat(config.data);
          this.apiNoticeSize = taskData.api.length;
          break;
        case 'track':
          taskData.track = taskData.track.filter(t => t.taskType !== config.taskType);
          taskData.track = taskData.track.concat(config.data);
          this.trackNoticeSize = taskData.track.length;
          break;
        case 'ui':
          taskData.ui = taskData.ui.filter(t => t.taskType !== config.taskType);
          taskData.ui = taskData.ui.concat(config.data);
          this.uiNoticeSize = taskData.ui.length;
          break;
        default:
          break;
      }
      return 0;
    }
  }
};
</script>

<style scoped>
.task-notification {
  height: calc(100vh - 190px);
  overflow: auto;
}

.primary-text {
  color: #783887;
}

:deep(.el-table td ) {
  text-align: left !important;
}
</style>
