<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('test_track.issue.issue') }}</h5>
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
          :event-options="defectEventOptions"
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

const TASK_TYPE = 'DEFECT_TASK';


export default {
  name: "DefectTaskNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    MxNoticeTemplate
  },
  props: {
    defectReceiverOptions: {
      type: Array
    },
    receiveTypeOptions: {
      type: Array
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle: "${operator}创建了缺陷:${title}",
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
      defectEventOptions: [
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')},
        {value: 'COMMENT', label: this.$t('commons.comment')},
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
          label: this.$t('commons.title'),
          value: 'title',
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
          label: this.$t('test_track.reporter'),
          value: 'reporter',
        },
        {
          label: this.$t('test_track.lastmodify'),
          value: 'lastmodify',
        },
        {
          label: this.$t('test_track.issue.platform'),
          value: 'platform',
        },
        {
          label: this.$t('project.id'),
          value: 'projectId',
        },
        {
          label: this.$t('commons.create_user'),
          value: 'creator',
        },
        {
          label: this.$t('commons.resourceId'),
          value: 'resourceId',
        },
        {
          label: this.$t('test_track.case.number'),
          value: 'num',
        },
        {
          label: this.$t('test_track.issue.platform_status'),
          value: 'platformStatus',
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
        this.$emit("noticeSize", {module: 'track', data: this.defectTask, taskType: TASK_TYPE});
        this.defectTask.forEach(planTask => {
          this.handleReceivers(planTask);
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
      Task.taskType = TASK_TYPE;
      this.defectTask.unshift(Task);
    },
    handleTemplate(index, row) {
      if (hasLicense()) {
        let robotTemplate = "";
        switch (row.event) {
          case 'CREATE':
            robotTemplate = this.robotTitle;
            break;
          case 'UPDATE':
            robotTemplate = this.robotTitle.replace('创建', '更新');
            break;
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
      let testPlanReceivers = JSON.parse(JSON.stringify(this.defectReceiverOptions));
      let i = row.userIds.indexOf('PROCESSOR');
      let i2 = row.userIds.indexOf('CREATOR');
      switch (row.event) {
        case "CREATE":
          testPlanReceivers.unshift({id: 'PROCESSOR', name: '处理人'});
          if (row.isSet) {
            if (i < 0) {
              row.userIds.unshift('PROCESSOR');
            }
          }
          if (i2 > -1) {
            row.userIds.splice(i2, 1);
          }
          break;
        case "UPDATE":
          testPlanReceivers.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          testPlanReceivers.unshift({id: 'PROCESSOR', name: '处理人'});
          if (row.isSet) {
            if (i2 < 0) {
              row.userIds.unshift('CREATOR');
            }
            if (i < 0) {
              row.userIds.unshift('PROCESSOR');
            }
          }
          break;
        case "DELETE":
          testPlanReceivers.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (row.isSet) {
            if (i2 < 0) {
              row.userIds.unshift('CREATOR');
            }
          }
          break;
        case "COMMENT":
          testPlanReceivers.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (row.isSet) {
            if (i2 < 0) {
              row.userIds.unshift('CREATOR');
            }
          }
          break;
        default:
          break;
      }
      row.receiverOptions = testPlanReceivers;
    },
  },
  watch: {
    defectReceiverOptions(value) {
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

:deep(.el-select .el-input.is-disabled .el-input__inner ) {
  background-color: #F5F7FA;
  border-color: #E4E7ED;
  color: #0a0a0a;
  cursor: not-allowed;
}

:deep(.el-input.is-disabled .el-input__inner ) {
  background-color: #F5F7FA;
  border-color: #E4E7ED;
  color: #0a0a0a;
  cursor: not-allowed;
}
</style>
