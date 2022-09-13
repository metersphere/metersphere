<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('test_track.plan.test_plan') }}</h5>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel"
                   v-permission="['PROJECT_MESSAGE:READ+EDIT']">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table :table-data="testCasePlanTask"
                            :event-options="otherEventOptions"
                            :receive-type-options="receiveTypeOptions"
                            @handleReceivers="handleTestPlanReceivers"
                            @handleTemplate="handleTemplate"
                            @refresh="initForm"/>
      </el-col>
    </el-row>
    <notice-template v-xpack ref="noticeTemplate" :variables="variables"/>
  </div>

</template>

<script>
import {hasLicense} from "@/common/js/utils";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import NotificationTable from "@/business/components/project/notification/NotificationTable";

const TASK_TYPE = 'TEST_PLAN_TASK';
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "TestPlanTaskNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
  },
  props: {
    testPlanReceiverOptions: {
      type: Array
    },
    receiveTypeOptions: {
      type: Array
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle: "${operator} 创建了测试计划:${name} ",
      testCasePlanTask: [{
        taskType: "testPlanTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
      }],
      otherEventOptions: [
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')},
        {value: 'COMPLETE', label: this.$t('commons.run_completed')},
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('commons.run_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('commons.run_fail')},
      ],
      variables: [
        {
          label:this.$t('group.operator'),
          value:'operator',
        },
        {
          label:'id',
          value:'id',
        },
        {
          label:this.$t('workspace.id'),
          value:'workspaceId',
        },
        {
          label:this.$t('commons.name'),
          value:'name',
        },
        {
          label:this.$t('commons.description'),
          value:'description',
        },
        {
          label:this.$t('commons.status'),
          value:'status',
        },
        {
          label:this.$t('test_track.plan.plan_stage'),
          value:'stage',
        },
        {
          label:this.$t('commons.create_time'),
          value:'createTime',
        },
        {
          label:this.$t('commons.update_time'),
          value:'updateTime',
        },
        {
          label:this.$t('test_track.plan.actual_start_time'),
          value:'actualStartTime',
        },
        {
          label:this.$t('test_track.plan.actual_end_time'),
          value:'actualEndTime',
        },
        {
          label:this.$t('test_track.plan.planned_start_time'),
          value:'plannedStartTime',
        },
        {
          label:this.$t('test_track.plan.planned_end_time'),
          value:'plannedEndTime',
        },
        {
          label:this.$t('commons.create_user'),
          value:'creator',
        },
        {
          label:this.$t('project.id'),
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
        {
          label: this.$t('test_track.report.exacutive_rate'),
          value: 'executeRate'
        },
        {
          label: this.$t('test_track.report.total_number_tests'),
          value: 'caseCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_count'),
          value: 'functionAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_success_count'),
          value: 'functionSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_failed_count'),
          value: 'functionFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_blocked_count'),
          value: 'functionBlockedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_prepared_count'),
          value: 'functionPreparedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_test_case_skipped_count'),
          value: 'functionSkippedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_count'),
          value: 'apiCaseAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_success_count'),
          value: 'apiCaseSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_failed_count'),
          value: 'apiCaseFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_un_execute_count'),
          value: 'apiCaseUnExecuteCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_case_error_report_count'),
          value: 'apiCaseErrorReportCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_count'),
          value: 'apiScenarioAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_success_count'),
          value: 'apiScenarioSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_failed_count'),
          value: 'apiScenarioFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_un_execute_count'),
          value: 'apiScenarioUnExecuteCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_api_scenario_error_report_count'),
          value: 'apiScenarioErrorReportCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_count'),
          value: 'uiScenarioAllCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_success_count'),
          value: 'uiScenarioSuccessCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_failed_count'),
          value: 'uiScenarioFailedCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_ui_scenario_un_execute_count'),
          value: 'uiScenarioUnExecuteCount'
        },
        {
          label: this.$t('test_track.plan.test_plan_load_case_count'),
          value: 'loadCaseAllCount'
        },
      ],
    };
  },
  activated() {
    this.initForm();
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message/type/' + TASK_TYPE, response => {
        this.testCasePlanTask = response.data;
        // 上报通知数
        this.$emit("noticeSize", {module: 'track', data: this.testCasePlanTask, taskType: TASK_TYPE});
        this.testCasePlanTask.forEach(planTask => {
          this.handleTestPlanReceivers(planTask);
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
      this.testCasePlanTask.unshift(Task);
    },
    handleTestPlanReceivers(row) {
      let testPlanReceivers = JSON.parse(JSON.stringify(this.testPlanReceiverOptions));
      let i = row.userIds.indexOf('FOLLOW_PEOPLE');
      let i2 = row.userIds.indexOf('CREATOR');
      switch (row.event) {
        case  "CREATE":
          testPlanReceivers.unshift({id: 'EXECUTOR', name: this.$t('test_track.plan_view.executor')});
          if (i2 > -1) {
            row.userIds.splice(i2, 1);
          }
          if (i > -1) {
            row.userIds.splice(i, 1);
          }
          break;
        case "UPDATE":
        case "DELETE":
        case "COMMENT":
        case "COMPLETE":
          this.initExecuteReceivers(testPlanReceivers,row);
          break;
        case "EXECUTE_SUCCESSFUL":
          this.initExecuteReceivers(testPlanReceivers,row);
          break;
        case "EXECUTE_FAILED":
          this.initExecuteReceivers(testPlanReceivers,row);
          break;
        default:
          break;
      }
      row.receiverOptions = testPlanReceivers;
    },
    initExecuteReceivers(testPlanReceivers,row){
      let i = row.userIds.indexOf('FOLLOW_PEOPLE');
      let i2 = row.userIds.indexOf('CREATOR');
      let i3 = row.userIds.indexOf('EXECUTOR');
      testPlanReceivers.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
      testPlanReceivers.unshift({id: 'FOLLOW_PEOPLE', name: this.$t('api_test.automation.follow_people')});
      testPlanReceivers.unshift({id: 'EXECUTOR', name: this.$t('test_track.plan_view.executor')});

      if (row.isSet) {
        if (i2 < 0) {
          row.userIds.unshift('CREATOR');
        }
        if (i < 0) {
          row.userIds.unshift('FOLLOW_PEOPLE');
        }
        if (i3 < 0) {
          row.userIds.unshift('EXECUTOR');
        }
      }
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
          case 'COMMENT':
            robotTemplate = this.robotTitle.replace('创建', '评论');
            break;
          case "COMPLETE":
            robotTemplate = this.robotTitle.replace('创建', '完成测试计划');
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, robotTemplate);
      }
    }
  },
  watch: {
    testPlanReceiverOptions(value) {
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
