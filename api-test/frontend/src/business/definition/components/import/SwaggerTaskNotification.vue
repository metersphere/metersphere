<template>
  <div>
    <el-row>
      <el-col :span="20">
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
    <mx-notice-template v-xpack ref="noticeTemplate"/>
  </div>
</template>

<script>
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import NotificationTable from "@/business/commons/NotificationTable";
import {getMessageById} from "@/api/notice";

export default {
  name: "SwaggerTaskNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    MxNoticeTemplate: () => import("metersphere-frontend/src/components/MxNoticeTemplate")
  },
  props: {
    apiTestId: String,
    scheduleReceiverOptions: Array,
    isTesterPermission: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle:
        "swagger:${url}导入成功",
      scheduleTask: [{
        taskType: "swaggerTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
        testId: this.apiTestId,
      }],
      scheduleEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')},
        {value: 'LARK', label: this.$t('organization.message.lark')},
        {value: 'WEBHOOK', label: this.$t('organization.message.webhook')},
      ],
    };
  },
  mounted() {
  },
  created() {
  },
  methods: {
    initForm() {
      this.result = getMessageById(this.apiTestId).then(response => {
        this.scheduleTask = response.data;
      });
    },
    handleAddTaskModel() {
      let task = {};
      task.event = [];
      task.userIds = [];
      task.type = '';
      task.webhook = '';
      task.isSet = true;
      task.identification = '';
      task.taskType = 'SWAGGER_URL';
      task.testId = this.apiTestId;
      this.scheduleTask.unshift(task);
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

:deep(.el-select .el-input.is-disabled .el-input__inner) {
  background-color: #F5F7FA;
  border-color: #E4E7ED;
  color: #0a0a0a;
  cursor: not-allowed;
}

:deep(.el-input.is-disabled .el-input__inner) {
  background-color: #F5F7FA;
  border-color: #E4E7ED;
  color: #0a0a0a;
  cursor: not-allowed;
}
</style>

