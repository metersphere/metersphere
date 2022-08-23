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
import {hasLicense} from "@/common/js/utils";
import MsCodeEdit from "@/business/components/api/definition/components/MsCodeEdit";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import NotificationTable from "@/business/components/project/notification/NotificationTable";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "ScheduleNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
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
      this.result = this.$get('/notice/search/message/' + this.testId, response => {
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

