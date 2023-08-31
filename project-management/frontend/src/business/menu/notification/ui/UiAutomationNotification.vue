<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('i18n.ui_automation') }}</h5>
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

const TASK_TYPE = 'UI_AUTOMATION_TASK';

export default {
  name: "UiAutomationNotification",
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
      robotTitle: "${operator}创建了UI自动化: ${name}",
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
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')},
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('commons.run_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('commons.run_fail')},
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
          label: this.$t('ui.automation.scenario.module_id'),
          value: 'moduleId',
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
          label: this.$t('ui.automation.scenario.principal'),
          value: 'principal',
        },
        {
          label: this.$t('report.plan_share_url'),
          value: 'scenarioShareUrl',
        },
        {
          label: this.$t('ui.automation.scenario.step_total'),
          value: 'stepTotal',
        },
        {
          label: this.$t('ui.automation.scenario.schedule'),
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
          label: this.$t('ui.automation.scenario.last_result'),
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
          label: this.$t('ui.automation.scenario.order'),
          value: 'order',
        },
        {
          label: this.$t('ui.automation.scenario.environment_type'),
          value: 'environmentType',
        }, {
          label: this.$t('ui.automation.scenario.environment_json'),
          value: 'environmentJson',
        }, {
          label: this.$t('ui.automation.scenario.environment_group_id'),
          value: 'environmentGroupId',
        },

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
        this.$emit("noticeSize", {module: 'ui', data: this.defectTask, taskType: TASK_TYPE});
        this.defectTask.forEach(planTask => {
          this.handleReceivers(planTask);
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
          case 'CREATE':
            robotTemplate = this.robotTitle;
            break;
          case 'UPDATE':
            robotTemplate = this.robotTitle.replace('创建', '更新');
            break;
          case 'DELETE':
            robotTemplate = this.robotTitle.replace('创建', '删除');
            break;
          case 'EXECUTE_SUCCESSFUL':
            robotTemplate = this.robotTitle.replace('创建', '执行')
              .replace('UI自动化', 'UI自动化成功');
            break;
          case 'EXECUTE_FAILED':
            robotTemplate = this.robotTitle.replace('创建', '执行')
              .replace('UI自动化', 'UI自动化失败');
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
        case "CREATE":
          receiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (i2 < 0) {
            row.userIds.unshift('CREATOR');
          }
          break;
        case "UPDATE":
          receiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (row.isSet) {
            if (i2 < 0) {
              row.userIds.unshift('CREATOR');
            }
          }
          break;
        case "DELETE":
        case "EXECUTE_SUCCESSFUL":
        case "EXECUTE_FAILED":
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
