<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h3>{{ $t('organization.message.jenkins_task_notification') }}</h3>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel"
                   v-permission="['PROJECT_MESSAGE:READ+EDIT']">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table :table-data="jenkinsTask"
                            :event-options="jenkinsEventOptions"
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

const TASK_TYPE = 'JENKINS_TASK';

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "JenkinsNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
  },
  props: {
    jenkinsReceiverOptions: {
      type: Array
    },
    receiveTypeOptions: {
      type: Array
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle: "${operator}执行 Jenkins 成功: ${name}",
      jenkinsTask: [{
        taskType: "jenkinsTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
      }],
      jenkinsEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
    };
  },
  activated() {
    this.initForm();
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message/type/' + TASK_TYPE, response => {
        this.jenkinsTask = response.data;
        // 上报通知数
        this.$emit("noticeSize", {module: 'jenkins', data: this.jenkinsTask, taskType: TASK_TYPE});
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
      Task.taskType = TASK_TYPE;
      this.jenkinsTask.unshift(Task);
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
      row.receiverOptions = JSON.parse(JSON.stringify(this.jenkinsReceiverOptions));
    }
  }
};
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}

.el-button {
  margin-right: 10px;
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
