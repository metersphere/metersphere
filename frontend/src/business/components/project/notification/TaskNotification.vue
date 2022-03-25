<template>
  <div v-loading="result.loading">
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
import {getCurrentProjectID, getCurrentUser} from "@/common/js/utils";
import JenkinsNotification from "@/business/components/project/notification/jenkins/JenkinsNotification";
import TestPlanTaskNotification from "@/business/components/project/notification/track/TestPlanTaskNotification";
import TestReviewNotification from "@/business/components/project/notification/track/TestReviewNotification";
import DefectTaskNotification from "@/business/components/project/notification/track/DefectTaskNotification";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import HomeNotification from "@/business/components/project/notification/track/TrackHomeNotification";
import TrackHomeNotification from "@/business/components/project/notification/track/TrackHomeNotification";
import TestCaseNotification from "@/business/components/project/notification/track/TestCaseNotification";
import TrackReportNotification from "@/business/components/project/notification/track/TrackReportNotification";
import ApiDefinitionNotification from "@/business/components/project/notification/api/ApiDefinitionNotification";
import ApiAutomationNotification from "@/business/components/project/notification/api/ApiAutomationNotification";
import ApiReportNotification from "@/business/components/project/notification/api/ApiReportNotification";
import PerformanceTestNotification
  from "@/business/components/project/notification/performance/PerformanceTestNotification";
import PerformanceReportNotification
  from "@/business/components/project/notification/performance/PerformanceReportNotification";
import ApiHomeNotification from "@/business/components/project/notification/api/ApiHomeNotification";

let taskData = {
  jenkins: [],
  api: [],
  performance: [],
  track: [],
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
  },
  data() {

    return {
      jenkinsNoticeSize: 0,
      apiNoticeSize: 0,
      performanceNoticeSize: 0,
      trackNoticeSize: 0,
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
        {value: 'LARK', label: this.$t('organization.message.lark')}
      ],
      result: {}
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
      this.result = this.$post('/user/project/member/list', {projectId: getCurrentProjectID()}, response => {
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
  height: calc(100vh - 230px);
  overflow: auto;
}

.primary-text {
  color: #783887;
}
</style>
