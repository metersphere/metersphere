<template>
  <div>
    <el-row>
      <el-col :span="10">
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table
          v-loading="loading"
          :table-data="scheduleTask"
          :event-options="scheduleEventOptions"
          :receive-type-options="receiveTypeOptions"
          @handleReceivers="handleReceivers"
          @handleTemplate="handleTemplate"
          @refresh="initForm" />
      </el-col>
    </el-row>
    <mx-notice-template v-xpack ref="noticeTemplate" :variables="variables" />
  </div>
</template>

<script>
import { hasLicense } from 'metersphere-frontend/src/utils/permission';
import MsCodeEdit from '@/business/definition/components/MsCodeEdit';
import MsTipButton from 'metersphere-frontend/src/components/MsTipButton';
import NotificationTable from 'metersphere-frontend/src/components/notification/NotificationTable';
import { getMessageById } from '@/api/notice';

export default {
  name: 'ScheduleNotification',
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    MxNoticeTemplate: () => import('metersphere-frontend/src/components/MxNoticeTemplate'),
  },
  props: {
    testId: String,
    scheduleReceiverOptions: Array,
    isTesterPermission: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      loading: false,
      modes: ['text', 'html'],
      robotTitle: '${operator}执行接口测试成功: ${name}, 报告: ${reportUrl}',
      scheduleTask: [
        {
          taskType: 'scheduleTask',
          event: '',
          userIds: [],
          type: [],
          webhook: '',
          isSet: true,
          identification: '',
          isReadOnly: false,
          testId: this.testId,
        },
      ],
      scheduleEventOptions: [
        {
          value: 'EXECUTE_SUCCESSFUL',
          label: this.$t('schedule.event_success'),
        },
        { value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed') },
      ],
      receiveTypeOptions: [
        {value: 'IN_SITE', label: this.$t('organization.message.in_site')},
        { value: 'EMAIL', label: this.$t('organization.message.mail') },
        {
          value: 'NAIL_ROBOT',
          label: this.$t('organization.message.nail_robot'),
        },
        {
          value: 'WECHAT_ROBOT',
          label: this.$t('organization.message.enterprise_wechat_robot'),
        },
        { value: 'LARK', label: this.$t('organization.message.lark') },
        { value: 'WEBHOOK', label: this.$t('organization.message.webhook') },
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
          label: this.$t('load_test.report.url'),
          value: 'reportUrl',
        },
        {
          label: this.$t('project.id'),
          value: 'projectId',
        },
        {
          label: this.$t('commons.tag'),
          value: 'tags',
        },
        {
          label: this.$t('user.id'),
          value: 'userId',
        },
        {
          label: this.$t('api_test.scenario.module_id'),
          value: 'apiScenarioModuleId',
        },
        {
          label: this.$t('module.path'),
          value: 'modulePath',
        },
        {
          label: this.$t('commons.name'),
          value: 'name',
        },
        {
          label: this.$t('commons.level'),
          value: 'level',
        },
        {
          label: this.$t('commons.status'),
          value: 'status',
        },
        {
          label: this.$t('api_test.automation.scenario.principal'),
          value: 'principal',
        },
        {
          label: this.$t('report.plan_share_url'),
          value: 'scenarioShareUrl',
        },
        {
          label: this.$t('api_test.automation.step_total'),
          value: 'stepTotal',
        },
        {
          label: this.$t('api_test.automation.schedule'),
          value: 'schedule',
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
          label: this.$t('test_track.pass_rate'),
          value: 'passRate',
        },
        {
          label: this.$t('api_test.automation.last_result'),
          value: 'lastResult',
        },
        {
          label: this.$t('report.id'),
          value: 'reportId',
        },
        {
          label: this.$t('test_track.case.number'),
          value: 'num',
        },
        {
          label: this.$t('commons.original_state'),
          value: 'originalState',
        },
        {
          label: this.$t('commons.custom_num'),
          value: 'customNum',
        },
        {
          label: this.$t('commons.create_user'),
          value: 'createUser',
        },
        {
          label: this.$t('commons.delete_time'),
          value: 'deleteTime',
        },
        {
          label: this.$t('commons.delete_user_id'),
          value: 'deleteUserId',
        },
        {
          label: this.$t('commons.execution_times'),
          value: 'executeTimes',
        },
        {
          label: this.$t('api_test.definition.document.order'),
          value: 'order',
        },
        {
          label: this.$t('api_test.environment.environment_type'),
          value: 'environmentType',
        },
        {
          label: this.$t('api_test.environment.environment_json'),
          value: 'environmentJson',
        },
        {
          label: this.$t('api_test.environment.environment_group_id'),
          value: 'environmentGroupId',
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
      if (this.testId) {
        this.initForm();
      }
    },
  },
  methods: {
    initForm() {
      this.loading = getMessageById(this.testId).then((response) => {
        this.scheduleTask = response.data;
        this.scheduleTask.forEach((task) => {
          this.handleReceivers(task);
        });
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
        let robotTemplate = '';
        switch (row.event) {
          case 'EXECUTE_SUCCESSFUL':
            robotTemplate = this.robotTitle;
            break;
          case 'EXECUTE_FAILED':
            robotTemplate = this.robotTitle.replace('成功', '失败');
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
  },
};
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
</style>
