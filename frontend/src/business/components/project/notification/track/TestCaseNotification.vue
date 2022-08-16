<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('test_track.case.test_case') }}</h5>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel"
                   v-permission="['PROJECT_MESSAGE:READ+EDIT']">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table :table-data="defectTask"
                            :event-options="eventOptions"
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
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import MsTipButton from "@/business/components/common/components/MsTipButton";
import NotificationTable from "@/business/components/project/notification/NotificationTable";

const TASK_TYPE = 'TRACK_TEST_CASE_TASK';
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "TestCaseNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
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
      robotTitle: "${operator}创建了测试用例:${name}",
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
        {value: 'COMMENT', label: this.$t('commons.comment')}
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
          label:this.$t('test_track.case.node_id'),
          value:'nodeId',
        },
        {
          label:this.$t('load_test.id'),
          value:'testId',
        },
        {
          label:this.$t('test_track.case.node_path'),
          value:'nodePath',
        },
        {
          label:this.$t('project.id'),
          value:'projectId',
        },
        {
          label:this.$t('commons.name'),
          value:'name',
        },
        {
          label:this.$t('operating_log.type'),
          value:'type',
        },
        {
          label:this.$t('test_track.case.maintainer'),
          value:'maintainer',
        },
        {
          label:this.$t('test_track.case.priority'),
          value:'priority',
        },
        {
          label:this.$t('api_test.request.method'),
          value:'method',
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
          label:this.$t('test_track.sort'),
          value:'sort',
        },
        {
          label:this.$t('test_track.case.number'),
          value:'num',
        },
        {
          label:this.$t('test_track.review.review_status'),
          value:'reviewStatus',
        },
        {
          label:this.$t('commons.tag'),
          value:'tags',
        },
        {
          label:this.$t('test_track.demand.id'),
          value:'demandId',
        },
        {
          label:this.$t('test_track.demand.name'),
          value:'demandName',
        },
        {
          label:this.$t('commons.status'),
          value:'status',
        },
        {
          label:this.$t('test_track.step_model'),
          value:'stepModel',
        },
        {
          label:this.$t('commons.custom_num'),
          value:'customNum',
        },
        {
          label:this.$t('commons.create_user'),
          value:'createUser',
        },
        {
          label:this.$t('commons.original_state'),
          value:'originalStatus',
        },
        {
          label:this.$t('commons.delete_time'),
          value:'deleteTime',
        },
        {
          label:this.$t('commons.delete_user_id'),
          value:'deleteUserId',
        },
        {
          label:this.$t('api_test.definition.document.order'),
          value:'order',
        },
      ]
    };
  },
  activated() {
    this.initForm();
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message/type/' + TASK_TYPE, response => {
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
          case 'COMMENT':
            robotTemplate = this.robotTitle.replace('创建', '评论');
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
        case "UPDATE":
        case "DELETE":
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
        case "COMMENT":
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
