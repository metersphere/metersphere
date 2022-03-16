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
        <el-table
          :data="defectTask"
          class="tb-edit"
          border
          :cell-style="rowClass"
          :header-cell-style="headClass"
        >
          <el-table-column :label="$t('schedule.event')" min-width="15%" prop="events">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" :placeholder="$t('organization.message.select_events')" size="mini"
                         @change="handleReceivers(scope.row)"
                         prop="event" :disabled="!scope.row.isSet">
                <el-option
                  v-for="item in defectEventOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiver')" prop="receiver" min-width="20%">
            <template v-slot:default="{row}">
              <el-select v-model="row.userIds" filterable multiple size="mini"
                         :placeholder="$t('commons.please_select')"
                         style="width: 100%;" :disabled="!row.isSet">
                <el-option
                  v-for="item in row.defectReceiverOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiving_mode')" min-width="20%" prop="type">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')"
                         size="mini"
                         :disabled="!scope.row.isSet" @change="handleEdit(scope.$index, scope.row)">
                <el-option
                  v-for="item in receiveTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="webhook" min-width="20%" prop="webhook">
            <template v-slot:default="scope">
              <el-input v-model="scope.row.webhook"  size="mini"
                        :disabled="!scope.row.isSet||!scope.row.isReadOnly"></el-input>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.operating')" min-width="25%" prop="result">
            <template v-slot:default="scope">
              <ms-tip-button
                circle
                type="success"
                size="mini"
                v-if="scope.row.isSet"
                v-xpack
                @click="handleTemplate(scope.$index,scope.row)"
                :tip="$t('organization.message.template')"
                icon="el-icon-tickets"/>
              <ms-tip-button
                circle
                type="primary"
                size="mini"
                v-show="scope.row.isSet"
                @click="handleAddTask(scope.$index,scope.row)"
                :tip="$t('commons.add')"
                icon="el-icon-check"/>
              <ms-tip-button
                circle
                size="mini"
                v-show="scope.row.isSet"
                @click="removeRowTask(scope.$index,defectTask)"
                :tip="$t('commons.cancel')"
                icon="el-icon-refresh-left"/>
              <ms-tip-button
                el-button
                circle
                type="primary"
                size="mini"
                icon="el-icon-edit"
                v-show="!scope.row.isSet"
                :tip="$t('commons.edit')"
                @click="handleEditTask(scope.$index,scope.row)"
                v-permission="['PROJECT_MESSAGE:READ+EDIT']"/>
              <ms-tip-button
                circle
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="!scope.row.isSet"
                @click="deleteRowTask(scope.$index,scope.row)"
                :tip="$t('commons.delete')"
                v-permission="['PROJECT_MESSAGE:READ+EDIT']"/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <notice-template v-xpack ref="noticeTemplate" :variables="variables"/>
  </div>
</template>

<script>
import {hasLicense} from "@/common/js/utils";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import MsTipButton from "@/business/components/common/components/MsTipButton";

const TASK_TYPE = 'DEFECT_TASK';
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "DefectTaskNotification",
  components: {
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
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
      title: "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>MeterSphere</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div>\n" +
        "    <p>${operator}创建了缺陷:${title}</p>\n" +
        "</div>\n" +
        "</body>\n" +
        "</html>",
      robotTitle: "${operator}创建了缺陷:${title}",
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
          label:this.$t('group.operator'),
          value:'operator',
        },
        {
          label:'id',
          value:'id',
        },
        {
          label:this.$t('commons.title'),
          value:'title',
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
          label:this.$t('test_track.reporter'),
          value:'reporter',
        },
        {
          label:this.$t('test_track.lastmodify'),
          value:'lastmodify',
        },
        {
          label:this.$t('test_track.issue.platform'),
          value:'platform',
        },
        {
          label:this.$t('project.id'),
          value:'projectId',
        },
        {
          label:this.$t('commons.create_user'),
          value:'creator',
        },
        {
          label:this.$t('commons.resourceId'),
          value:'resourceId',
        },
        {
          label:this.$t('test_track.case.number'),
          value:'num',
        },
        {
          label:this.$t('test_track.issue.platform_status'),
          value:'platformStatus',
        },
        {
          label:this.$t('custom_field.issue_processor'),
          value:'processor',
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
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL' || data.type === 'IN_SITE') {
        data.isReadOnly = !data.isReadOnly;
        data.webhook = '';
      }
    },
    handleEditTask(index, data) {
      data.isSet = true;
      if (data.type === 'EMAIL' || data.type === 'IN_SITE') {
        data.isReadOnly = false;
        data.webhook = '';
      } else {
        data.isReadOnly = true;
      }
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
    handleAddTask(index, data) {

      if (data.event && data.userIds.length > 0 && data.type) {
        // console.log(data.type)
        if (data.type === 'NAIL_ROBOT' || data.type === 'WECHAT_ROBOT' || data.type === 'LARK') {
          if (!data.webhook) {
            this.$warning(this.$t('organization.message.message_webhook'));
          } else {
            this.addTask(data);
          }
        } else {
          this.addTask(data);
        }
      } else {
        this.$warning(this.$t('organization.message.message'));
      }
    },
    addTask(data) {
      this.result = this.$post("/notice/save/message/task", data, () => {
        data.isSet = false;
        this.initForm();
        this.$success(this.$t('commons.save_success'));
      });
    },
    removeRowTask(index, data) { //移除
      if (!data[index].identification) {
        data.splice(index, 1);
      } else {
        data[index].isSet = false;
      }
    },
    deleteRowTask(index, data) { //删除
      this.result = this.$get("/notice/delete/message/" + data.identification, response => {
        this.$success(this.$t('commons.delete_success'));
        this.initForm();
      });
    },
    rowClass() {
      return "text-align:center";
    },
    headClass() {
      return "text-align:center;background:'#ededed'";
    },
    handleTemplate(index, row) {
      if (hasLicense()) {
        let htmlTemplate = "";
        let robotTemplate = "";
        switch (row.event) {
          case 'CREATE':
            htmlTemplate = this.title;
            robotTemplate = this.robotTitle;
            break;
          case 'UPDATE':
            htmlTemplate = this.title.replace('创建', '更新');
            robotTemplate = this.robotTitle.replace('创建', '更新');
            break;
          case 'DELETE':
            htmlTemplate = this.title.replace('创建', '删除');
            robotTemplate = this.robotTitle.replace('创建', '删除');
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, htmlTemplate, robotTemplate);
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
      row.defectReceiverOptions = testPlanReceivers;
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
