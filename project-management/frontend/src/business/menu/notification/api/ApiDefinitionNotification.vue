<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('i18n.definition') }}</h5>
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

const TASK_TYPE = 'API_DEFINITION_TASK';

export default {
  name: "ApiDefinitionNotification",
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
      robotTitle: "${operator}创建了接口定义: ${name}",
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
        {value: 'CREATE', label: 'API ' + this.$t('commons.create')},
        {value: 'UPDATE', label: 'API ' + this.$t('commons.update')},
        {value: 'DELETE', label: 'API ' + this.$t('commons.delete')},
        {value: 'CASE_CREATE', label: 'CASE ' + this.$t('commons.create')},
        {value: 'CASE_UPDATE', label: 'CASE ' + this.$t('commons.update')},
        {value: 'CASE_DELETE', label: 'CASE ' + this.$t('commons.delete')},
        {value: 'MOCK_CREATE', label: 'MOCK ' + this.$t('commons.create')},
        {value: 'MOCK_UPDATE', label: 'MOCK ' + this.$t('commons.update')},
        {value: 'MOCK_DELETE', label: 'MOCK ' + this.$t('commons.delete')},
        {value: 'EXECUTE_SUCCESSFUL', label: 'CASE ' + this.$t('commons.run_success')},
        {value: 'EXECUTE_FAILED', label: 'CASE ' + this.$t('commons.run_fail')},
      ],
      variables: [],
      apiVariables: [
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
          label: this.$t('api_test.request.method'),
          value: 'method',
        },
        {
          label: this.$t('api_test.request.dubbo.protocol'),
          value: 'protocol',
        },
        {
          label: this.$t('api_test.request.path'),
          value: 'path',
        },
        {
          label: this.$t('module.path'),
          value: 'modulePath',
        },
        {
          label: this.$t('api_test.environment.id'),
          value: 'environmentId',
        },
        {
          label: this.$t('api_test.automation.schedule'),
          value: 'schedule',
        },
        {
          label: this.$t('commons.status'),
          value: 'status',
        },
        {
          label: this.$t('test_track.module.id'),
          value: 'moduleId',
        },
        {
          label: this.$t('user.id'),
          value: 'userId',
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
          label: this.$t('test_track.case.number'),
          value: 'num',
        },
        {
          label: this.$t('commons.tag'),
          value: 'tags',
        },
        {
          label: this.$t('commons.original_state'),
          value: 'originalState',
        },
        {
          label: this.$t('commons.create_user'),
          value: 'createUser',
        },
        {
          label: this.$t('test_track.case.total'),
          value: 'caseTotal',
        },
        {
          label: this.$t('test_track.case.status'),
          value: 'caseStatus',
        },
        {
          label: this.$t('test_track.case.passing_rate'),
          value: 'casePassingRate',
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
          label: this.$t('api_test.definition.document.order'),
          value: 'order',
        },

      ],
      caseVariables: [
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
          label: this.$t('test_track.case.priority'),
          value: 'priority',
        },
        {
          label: this.$t('api_test.definition.id'),
          value: 'apiDefinitionId',
        },
        {
          label: this.$t('commons.create_user_id'),
          value: 'createUserId',
        },
        {
          label: this.$t('commons.update_user_id'),
          value: 'updateUserId',
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
          label: this.$t('test_track.case.number'),
          value: 'num',
        },
        {
          label: this.$t('commons.tag'),
          value: 'tags',
        },
        {
          label: this.$t('api_test.automation.last_result_id'),
          value: 'lastResultId',
        },
        {
          label: this.$t('commons.status'),
          value: 'status',
        },
        {
          label: this.$t('commons.original_state'),
          value: 'originalStatus',
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
          label: this.$t('commons.version'),
          value: 'version',
        },
        {
          label: this.$t('api_test.definition.document.order'),
          value: 'order',
        },
      ],
      mockVariables: [
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
          label: this.$t('api_test.definition.id'),
          value: 'apiDefinitionId',
        },
        {
          label: this.$t('commons.create_user_id'),
          value: 'createUserId',
        },
        {
          label: this.$t('commons.update_user_id'),
          value: 'updateUserId',
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
          label: this.$t('commons.tag'),
          value: 'tags',
        },
        {
          label: this.$t('commons.status'),
          value: 'status',
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
          label: this.$t('commons.version'),
          value: 'version',
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
        this.$emit("noticeSize", {module: 'api', data: this.defectTask, taskType: TASK_TYPE});
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
            this.variables = this.apiVariables;
            break;
          case 'UPDATE':
            robotTemplate = this.robotTitle.replace('创建', '更新');
            this.variables = this.apiVariables;
            break;
          case 'DELETE':
            robotTemplate = this.robotTitle.replace('创建', '删除');
            this.variables = this.apiVariables;
            break;
          case 'CASE_CREATE':
            robotTemplate = this.robotTitle.replace('接口定义', '接口用例');
            this.variables = this.caseVariables;
            break;
          case 'CASE_UPDATE':
            robotTemplate = this.robotTitle.replace('创建', '更新')
              .replace('接口定义', '接口用例');
            this.variables = this.caseVariables;
            break;
          case 'CASE_DELETE':
            robotTemplate = this.robotTitle.replace('创建', '删除')
              .replace('接口定义', '接口用例');
            this.variables = this.caseVariables;
            break;
          case 'MOCK_CREATE':
            robotTemplate = this.robotTitle.replace('接口定义', '接口用例');
            this.variables = this.mockVariables;
            break;
          case 'MOCK_UPDATE':
            robotTemplate = this.robotTitle.replace('创建', '更新')
              .replace('接口定义', '接口用例');
            this.variables = this.mockVariables;
            break;
          case 'MOCK_DELETE':
            robotTemplate = this.robotTitle.replace('创建', '删除')
              .replace('接口定义', '接口用例');
            this.variables = this.mockVariables;
            break;
          case 'EXECUTE_SUCCESSFUL':
            robotTemplate = this.robotTitle.replace('创建', '执行')
              .replace('接口定义', '接口用例成功');
            this.variables = this.caseVariables;
            break;
          case 'EXECUTE_FAILED':
            robotTemplate = this.robotTitle.replace('创建', '执行')
              .replace('接口定义', '接口用例失败');
            this.variables = this.caseVariables;
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, robotTemplate);
      }
    },
    handleReceivers(row) {
      let receiverOptions = JSON.parse(JSON.stringify(this.receiverOptions));
      let i = row.userIds.indexOf('FOLLOW_PEOPLE');
      let i2 = row.userIds.indexOf('CREATOR');

      switch (row.event) {
        case "CREATE":
          if (i2 > -1) {
            row.userIds.splice(i2, 1);
          }
          if (i > -1) {
            row.userIds.splice(i, 1);
          }
          break;
        case "MOCK_CREATE":
          receiverOptions.unshift({id: 'FOLLOW_PEOPLE', name: this.$t('api_test.automation.follow_people')});
          receiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (row.isSet) {
            if (i < 0) {
              row.userIds.unshift('FOLLOW_PEOPLE');
            }
          }
          break;
        case "UPDATE":
        case "CASE_UPDATE":
        case "MOCK_UPDATE":
        case "MOCK_DELETE":
          receiverOptions.unshift({id: 'FOLLOW_PEOPLE', name: this.$t('api_test.automation.follow_people')});
          receiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (row.isSet) {
            if (i2 < 0) {
              row.userIds.unshift('CREATOR');
            }
            if (i < 0) {
              row.userIds.unshift('FOLLOW_PEOPLE');
            }
          }
          break;
        case "DELETE":
        case "CASE_DELETE":
        case "EXECUTE_SUCCESSFUL":
        case "EXECUTE_FAILED":
          receiverOptions.unshift({id: 'FOLLOW_PEOPLE', name: this.$t('api_test.automation.follow_people')});
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
