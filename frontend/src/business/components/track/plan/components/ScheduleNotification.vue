<template>
  <div>
    <el-row>
      <el-col :span="10">
        <el-button icon="el-icon-circle-plus-outline" plain size="mini"
                   @click="handleAddTaskModel">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table
          :data="scheduleTask"
          class="tb-edit"
          border
          :cell-style="rowClass"
          :header-cell-style="headClass">
          <el-table-column :label="$t('schedule.event')" prop="events" min-width="13%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" size="mini"
                         :placeholder="$t('organization.message.select_events')"
                         prop="events" :disabled="!scope.row.isSet">
                <el-option
                  v-for="item in scheduleEventOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiver')" prop="userIds" min-width="18%">
            <template v-slot:default="{row}">
              <el-select v-model="row.userIds" filterable multiple size="mini"
                         :placeholder="$t('commons.please_select')" style="width: 100%;" :disabled="!row.isSet">
                <el-option
                  v-for="item in scheduleReceiverOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiving_mode')" prop="type" min-width="15%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')"
                         size="mini"
                         :disabled="!scope.row.isSet" @change="handleEdit(scope.$index, scope.row)"
              >
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
              <el-input v-model="scope.row.webhook" size="mini"
                        :disabled="!scope.row.isSet||!scope.row.isReadOnly"></el-input>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.operating')" prop="result" min-width="25%">
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
                @click="removeRowTask(scope.$index,scheduleTask)"
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
import MsCodeEdit from "@/business/components/api/definition/components/MsCodeEdit";
import MsTipButton from "@/business/components/common/components/MsTipButton";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "ScheduleNotification",
  components: {
    MsTipButton,
    MsCodeEdit,
    "NoticeTemplate": noticeTemplate.default
  },
  props: {
    testId: String,
    scheduleReceiverOptions: Array,
    isTesterPermission: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      modes: ['text', 'html'],
      title: '<!DOCTYPE html>\n' +
        '<html lang="en">\n' +
        '<head>\n' +
        '    <meta charset="UTF-8">\n' +
        '    <title>MeterSphere</title>\n' +
        '</head>\n' +
        '<body>\n' +
        '<div>\n' +
        '    <div style="margin-left: 100px">\n' +
        '        ${operator}执行接口测试成功: ${name}, 报告: ${planShareUrl}' +
        '    </div>\n' +
        '\n' +
        '</div>\n' +
        '</body>\n' +
        '</html>',
      robotTitle: "${operator}执行接口测试成功: ${name}, 报告: ${planShareUrl}",
      scheduleTask: [{
        taskType: "scheduleTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
        testId: this.testId,
      }],
      scheduleEventOptions: [
        {value: 'COMPLETE', label: this.$t('commons.run_completed')},
      ],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')},
        {value: 'LARK', label: this.$t('organization.message.lark')}
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
          label:this.$t('test_track.case.match_rule'),
          value:'testCaseMatchRule',
        },
        {
          label:this.$t('test_track.plan.plan_stage'),
          value:'executorMatchRule',
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
          label: this.$t('commons.execution_times'),
          value: 'executionTimes',
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
      ],
    };
  },
  mounted() {
    this.initForm();
  },
  activated() {
    this.initForm();
  },
  watch: {
    testId() {
      this.initForm();
    }
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message/' + this.testId, response => {
        // console.log(response.data);
        this.scheduleTask = response.data;
      });
    },
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL') {
        data.isReadOnly = !data.isReadOnly;
        data.webhook = '';
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
      Task.taskType = 'SCHEDULE_TASK';
      Task.testId = this.testId;
      this.scheduleTask.unshift(Task);
    },
    handleEditTask(index, data) {
      data.isSet = true;
      data.testId = this.testId;
      if (data.type === 'EMAIL') {
        data.isReadOnly = false;
        data.webhook = '';
      } else {
        data.isReadOnly = true;
      }
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
          case 'EXECUTE_SUCCESSFUL':
            htmlTemplate = this.title;
            robotTemplate = this.robotTitle;
            break;
          case 'EXECUTE_FAILED':
            htmlTemplate = this.title.replace('成功', '失败');
            robotTemplate = this.robotTitle.replace('成功', '失败');
            break;
          case 'COMPLETE':
            htmlTemplate = this.title.replace('成功', '完成');
            robotTemplate = this.robotTitle.replace('成功', '完成');
            break;
          default:
            break;
        }
        this.$refs.noticeTemplate.open(row, htmlTemplate, robotTemplate);
      }
    }
  }
};
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
</style>

