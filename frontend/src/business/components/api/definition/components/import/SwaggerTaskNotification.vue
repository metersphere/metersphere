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
    <notice-template v-xpack ref="noticeTemplate"/>
  </div>
</template>

<script>
import {hasLicense} from "@/common/js/utils";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import NotificationTable from "@/business/components/project/notification/NotificationTable";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "SwaggerTaskNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
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
      this.result = this.$get('/notice/search/message/' + this.apiTestId, response => {
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

/deep/ .el-select .el-input.is-disabled .el-input__inner {
  background-color: #F5F7FA;
  border-color: #E4E7ED;
  color: #0a0a0a;
  cursor: not-allowed;
}

/deep/ .el-input.is-disabled .el-input__inner {
  background-color: #F5F7FA;
  border-color: #E4E7ED;
  color: #0a0a0a;
  cursor: not-allowed;
}
</style>

