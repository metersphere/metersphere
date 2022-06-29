<template>
  <div>
    <el-row>
      <el-col :span="24">
        <h5>{{ $t('test_track.review.test_review') }}</h5>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel"
                   v-permission="['PROJECT_MESSAGE:READ+EDIT']">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <notification-table :table-data="reviewTask"
                            :event-options="reviewTaskEventOptions"
                            :receive-type-options="receiveTypeOptions"
                            @handleReceivers="handleReviewReceivers"
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

const TASK_TYPE = 'REVIEW_TASK';
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "TestReviewNotification",
  components: {
    NotificationTable,
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
  },
  props: {
    reviewReceiverOptions: {
      type: Array
    },
    receiveTypeOptions: {
      type: Array
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      robotTitle: "${operator}创建了用例评审:${name}",
      reviewTask: [{
        taskType: "reviewTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
      }],
      reviewTaskEventOptions: [
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')},
        {value: 'COMMENT', label: this.$t('commons.comment')},
        {value: 'COMPLETE', label:this.$t('commons.review_complete')}
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
          label:this.$t('commons.name'),
          value:'name',
        },
        {
          label:this.$t('commons.status'),
          value:'status',
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
          label:this.$t('test_track.review.end_time'),
          value:'endTime',
        },
        {
          label:this.$t('project.id'),
          value:'projectId',
        },
        {
          label:this.$t('commons.tag'),
          value:'tags',
        },
        {
          label:this.$t('commons.create_user'),
          value:'createUser',
        },
        {
          label:this.$t('commons.description'),
          value:'description',
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
        this.reviewTask = response.data;
        // 上报通知数
        this.$emit("noticeSize", {module: 'track', data: this.reviewTask, taskType: TASK_TYPE});
        this.reviewTask.forEach(planTask => {
          this.handleReviewReceivers(planTask);
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
      this.reviewTask.unshift(Task);
    },
    handleReviewReceivers(row) {
      let reviewReceiverOptions = JSON.parse(JSON.stringify(this.reviewReceiverOptions));
      let i = row.userIds.indexOf('FOLLOW_PEOPLE');
      let i2 = row.userIds.indexOf('CREATOR');
      switch (row.event) {
        case  "CREATE":
          reviewReceiverOptions.unshift({id: 'EXECUTOR', name: this.$t('test_track.review.reviewer')});
          if (i2 > -1) {
            row.userIds.splice(i2, 1);
          }
          if (i > -1) {
            row.userIds.splice(i, 1);
          }
          break;
        case "UPDATE":
          reviewReceiverOptions.unshift({id: 'FOLLOW_PEOPLE', name: this.$t('api_test.automation.follow_people')});
          reviewReceiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
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
          reviewReceiverOptions.unshift({id: 'FOLLOW_PEOPLE', name: this.$t('api_test.automation.follow_people')});
          reviewReceiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
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
          reviewReceiverOptions.unshift({id: 'MAINTAINER', name: this.$t('test_track.case.maintainer')});
          if (i2 > -1) {
            row.userIds.splice(i2, 1);
          }
          if (i > -1) {
            row.userIds.splice(i, 1);
          }
          break;
        case "COMPLETE":
          reviewReceiverOptions.unshift({id: 'FOLLOW_PEOPLE', name: this.$t('api_test.automation.follow_people')});
          reviewReceiverOptions.unshift({id: 'CREATOR', name: this.$t('commons.create_user')});
          if (row.isSet) {
            if (i2 < 0) {
              row.userIds.unshift('CREATOR');
            }
            if (i < 0) {
              row.userIds.unshift('FOLLOW_PEOPLE');
            }
          }
          break;
        default:
          break;
      }
      row.receiverOptions = reviewReceiverOptions;
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
            robotTemplate = this.robotTitle.replace('创建', '完成评审');
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, robotTemplate);
      }
    }
  },
  watch: {
    reviewReceiverOptions(value) {
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
