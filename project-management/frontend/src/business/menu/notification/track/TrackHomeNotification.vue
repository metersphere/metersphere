<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('i18n.home') }}</h5>
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

const TASK_TYPE = 'TRACK_HOME_TASK';


export default {
  name: "TrackHomeNotification",
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
      robotTitle: "${operator}关闭了定时任务: ${name}",
      defectTask: [],
      eventOptions: [
        {value: 'CLOSE_SCHEDULE', label: this.$t('commons.close_schedule')},
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
          label: this.$t('api_test.key'),
          value: 'key',
        },
        {
          label: this.$t('operating_log.type'),
          value: 'type',
        },
        {
          label: this.$t('api_test.value'),
          value: 'value',
        },
        {
          label: this.$t('commons.group'),
          value: 'group',
        },
        {
          label: this.$t('schedule.job'),
          value: 'job',
        },
        {
          label: this.$t('api_test.scenario.enable'),
          value: 'enable',
        },
        {
          label: this.$t('commons.resourceId'),
          value: 'resourceId',
        },
        {
          label: this.$t('user.id'),
          value: 'userId',
        },
        {
          label: this.$t('workspace.id'),
          value: 'workspaceId',
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
          label: this.$t('project.id'),
          value: 'projectId',
        },
        {
          label: this.$t('commons.name'),
          value: 'name',
        },
        {
          label: this.$t('variables.config'),
          value: 'config',
        }
      ],
      loading: false
    };
  },
  activated() {
    this.initForm();
  },
  methods: {
    hasLicense,
    initForm() {
      this.loading = searchNoticeByType(TASK_TYPE).then(response => {
        this.defectTask = response.data;
        // 上报通知数
        this.$emit("noticeSize", {module: 'track', data: this.defectTask, taskType: TASK_TYPE});
        this.defectTask.forEach(planTask => {
          this.handleReceivers(planTask);
        });
      });
    },
    handleAddTaskModel() {
      let task = {};
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
          case 'CLOSE_SCHEDULE':
            robotTemplate = this.robotTitle;
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
        case "CLOSE_SCHEDULE":
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
    receiverOptions() {
      this.initForm();
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
