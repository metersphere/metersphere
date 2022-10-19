<template>
  <div>
    <el-row>
      <el-col :span="10">
        <el-button icon="el-icon-circle-plus-outline" plain size="mini"
                   @click="handleAddTaskModel">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table :table-data="scheduleTask"
                            :event-options="scheduleEventOptions"
                            :receive-type-options="receiveTypeOptions"
                            @handleReceivers="handleReceivers"
                            @handleTemplate="handleTemplate"
                            @refresh="initForm"/>
      </el-col>
    </el-row>
    <notice-template v-xpack ref="noticeTemplate" :variables="variables"/>
  </div>
</template>

<script>
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import NotificationTable from "metersphere-frontend/src/components/notification/NotificationTable";
import MxNoticeTemplate from "metersphere-frontend/src/components/MxNoticeTemplate";
import {searchNoticeById} from "metersphere-frontend/src/api/notification";

export default {
  name: "ScheduleNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": MxNoticeTemplate
  },
  props: {
    testId: String,
    scheduleReceiverOptions: Array,
    isTesterPermission: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle: "${operator}执行测试计划成功: ${name}, 报告: ${planShareUrl}",
      scheduleTask: [{
        taskType: "scheduleTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
        testId: this.testId,
      }],
      scheduleEventOptions: [
        {value: 'COMPLETE', label: this.$t('commons.run_completed')},
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('commons.run_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('commons.run_fail')},
      ],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')},
        {value: 'LARK', label: this.$t('organization.message.lark')},
        {value: 'WEBHOOK', label: this.$t('organization.message.webhook')},
      ],
      variables: [
        {
          label: this.$t('group.operator'),
          value: 'operator',
        },
        {
          label: 'id',
          value: 'id',
        },
        {
          label: this.$t('workspace.id'),
          value: 'workspaceId',
        },
        {
          label: this.$t('commons.name'),
          value: 'name',
        },
        {
          label: this.$t('commons.description'),
          value: 'description',
        },
        {
          label: this.$t('commons.status'),
          value: 'status',
        },
        {
          label: this.$t('test_track.plan.plan_stage'),
          value: 'stage',
        },
        {
          label: this.$t('commons.create_time'),
          value: 'createTime',
        },
        {
          label: this.$t('commons.update_time'),
          value: 'updateTime',
        },
        {
          label: this.$t('test_track.plan.actual_start_time'),
          value: 'actualStartTime',
        },
        {
          label: this.$t('test_track.plan.actual_end_time'),
          value: 'actualEndTime',
        },
        {
          label: this.$t('test_track.plan.planned_start_time'),
          value: 'plannedStartTime',
        },
        {
          label: this.$t('test_track.plan.planned_end_time'),
          value: 'plannedEndTime',
        },
        {
          label: this.$t('commons.create_user'),
          value: 'creator',
        },
        {
          label: this.$t('project.id'),
          value: 'projectId',
        },
        {
          label: this.$t('test_track.automatic_status_update'),
          value: 'automaticStatusUpdate',
        },
        {
          label: this.$t('test_track.pass_rate'),
          value: 'passRate',
        },
        {
          label: this.$t('report.plan_share_url'),
          value: 'planShareUrl',
        },
        {
          label: this.$t('test_track.report.exacutive_rate'),
          value: 'executeRate'
        },
        {
          label: this.$t('test_track.report.total_number_tests'),
          value: 'caseCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_count'),
          value: 'functionAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_success_count'),
          value: 'functionSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_failed_count'),
          value: 'functionFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_blocked_count'),
          value: 'functionBlockedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_prepared_count'),
          value: 'functionPreparedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_skipped_count'),
          value: 'functionSkippedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_count'),
          value: 'apiCaseAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_success_count'),
          value: 'apiCaseSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_failed_count'),
          value: 'apiCaseFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_un_execute_count'),
          value: 'apiCaseUnExecuteCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_error_report_count'),
          value: 'apiCaseErrorReportCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_count'),
          value: 'apiScenarioAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_success_count'),
          value: 'apiScenarioSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_failed_count'),
          value: 'apiScenarioFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_un_execute_count'),
          value: 'apiScenarioUnExecuteCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_error_report_count'),
          value: 'apiScenarioErrorReportCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_count'),
          value: 'uiScenarioAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_success_count'),
          value: 'uiScenarioSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_failed_count'),
          value: 'uiScenarioFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_un_execute_count'),
          value: 'uiScenarioUnExecuteCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_load_case_count'),
          value: 'loadCaseAllCount'
        },
      ],
    };
  },
  mounted() {
    this.initForm();
  },
  activated() {
    this.initForm();
  },
  watch: {
    testId() {
      this.initForm();
    }
  },
  methods: {
    initForm() {
      this.result = searchNoticeById(this.testId).then(response=> {
        this.scheduleTask = response.data;
      });
    },
    handleAddTaskModel() {
      let Task = {};
      Task.event = '';
      Task.userIds = [];
      Task.type = '';
      Task.webhook = '';
      Task.isSet = true;
      Task.identification = '';
      Task.taskType = 'SCHEDULE_TASK';
      Task.testId = this.testId;
      this.scheduleTask.unshift(Task);
    },
    handleTemplate(index, row) {
      if (hasLicense()) {
        let robotTemplate = "";
        switch (row.event) {
          case 'EXECUTE_SUCCESSFUL':
            robotTemplate = this.robotTitle;
            break;
          case 'EXECUTE_FAILED':
            robotTemplate = this.robotTitle.replace('成功', '失败');
            break;
          case 'COMPLETE':
            robotTemplate = this.robotTitle.replace('成功', '完成');
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, robotTemplate);
      }
    },
    handleReceivers(row) {
      row.receiverOptions = JSON.parse(JSON.stringify(this.scheduleReceiverOptions));
    },
  }
};
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
</style>

