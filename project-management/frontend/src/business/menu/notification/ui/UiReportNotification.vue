<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('i18n.report') }}</h5>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel"
                   v-permission="['PROJECT_MESSAGE:READ+EDIT']">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table
          v-loading="loading"
          :table-data="defectTask"
          :event-options="eventOptions"
          :receive-type-options="receiveTypeOptions"
          @handleReceivers="handleReceivers"
          @handleTemplate="handleTemplate"
          @refresh="initForm"/>
      </el-col>
    </el-row>
    <mx-notice-template v-xpack ref="noticeTemplate" :variables="variables"/>
  </div>
</template>

<script>
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import MsCodeEdit from "metersphere-frontend/src/components/MsCodeEdit";
import MsTipButton from "metersphere-frontend/src/components/MsTipButton";
import NotificationTable from "metersphere-frontend/src/components/notification/NotificationTable";
import {searchNoticeByType} from "../../../../api/notification";
import MxNoticeTemplate from "metersphere-frontend/src/components/MxNoticeTemplate";

const TASK_TYPE = 'UI_REPORT_TASK';

export default {
  name: "UiReportNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    MxNoticeTemplate
  },
  props: {
    receiverOptions: {
      type: Array
    },
    receiveTypeOptions: {
      type: Array
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle: "${operator}删除了测试报告: ${name}",
      loading: false,
      defectTask: [{
        taskType: "defectTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
      }],
      eventOptions: [
        {value: 'DELETE', label: this.$t('commons.delete')},
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
          label: this.$t('project.id'),
          value: 'projectId',
        },
        {
          label: this.$t('commons.name'),
          value: 'name',
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
          label: this.$t('commons.status'),
          value: 'status',
        },
        {
          label: this.$t('user.id'),
          value: 'userId',
        },
        {
          label: this.$t('commons.create_user'),
          value: 'createUser',
        },
        {
          label: this.$t('commons.trigger_mode.name'),
          value: 'triggerMode',
        },
        {
          label: this.$t('api_test.automation.scenario_name'),
          value: 'scenarioName',
        },
        {
          label: this.$t('api_test.scenario.id'),
          value: 'scenarioId',
        },
        {
          label: this.$t('commons.actuator'),
          value: 'actuator',
        },
        {
          label: this.$t('commons.description'),
          value: 'description',
        },
        {
          label: this.$t('report.test_end_time'),
          value: 'endTime',
        }
      ]
    };
  },
  activated() {
    this.initForm();
  },
  methods: {
    initForm() {
      this.loading = searchNoticeByType(TASK_TYPE).then(response => {
        this.defectTask = response.data;
        // 上报通知数
        this.$emit("noticeSize", {taskType: TASK_TYPE, module: 'api', data: this.defectTask});
        this.defectTask.forEach(task => {
          this.handleReceivers(task);
        });
      })
    },
    handleAddTaskModel() {
      let task = {};
      task.receiverOptions = this.receiverOptions;
      task.event = '';
      task.userIds = [];
      task.type = '';
      task.webhook = '';
      task.isSet = true;
      task.identification = '';
      task.taskType = TASK_TYPE;
      this.defectTask.unshift(task);
    },
    handleTemplate(index, row) {
      if (hasLicense()) {
        let robotTemplate = "";
        switch (row.event) {
          case 'DELETE':
            robotTemplate = this.robotTitle.replace('创建', '删除');
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, robotTemplate);
      }
    },
    handleReceivers(row) {
      let receiverOptions = JSON.parse(JSON.stringify(this.receiverOptions));
      let i2 = row.userIds.indexOf('CREATOR');

      switch (row.event) {
        case "DELETE":
          receiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (row.isSet) {
            if (i2 < 0) {
              row.userIds.unshift('CREATOR');
            }
          }
          break;
        default:
          break;
      }
      row.receiverOptions = receiverOptions;
    }
  },
  watch: {
    receiverOptions(value) {
      if (value && value.length > 0) {
        this.initForm();
      }
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
</style>
