<template>
  <div>
    <el-row>
      <el-col :span="10">
        <h3>{{ $t('organization.message.test_plan_task_notification') }}</h3>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel('testPlanTask')">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table
          :data="form.testCasePlanTask"
          class="tb-edit"
          border
          size="mini"
          :cell-style="rowClass"
          :header-cell-style="headClass"
        >
          <el-table-column :label="$t('schedule.event')" min-width="20%" prop="events">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" :placeholder="$t('organization.message.select_events')"
                         @change="handleTestPlanReceivers(scope.row)"
                         prop="events" :disabled="!scope.row.isSet">
                <el-option
                  v-for="item in otherEventOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiver')" prop="userIds" min-width="20%">
            <template v-slot:default="{row}">
              <el-select v-model="row.userIds" filterable multiple
                         :placeholder="$t('commons.please_select')" style="width: 100%;" :disabled="!row.isSet">
                <el-option
                  v-for="item in row.testPlanReceiverOptions"
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
              <el-input v-model="scope.row.webhook" placeholder="webhook地址"
                        :disabled="!scope.row.isSet||!scope.row.isReadOnly"></el-input>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.operating')" min-width="20%" prop="result">
            <template v-slot:default="scope">
              <el-button
                type="primary"
                size="mini"
                v-show="scope.row.isSet"
                @click="handleAddTask(scope.$index,scope.row)"
              >{{ $t('commons.add') }}
              </el-button>
              <el-button
                size="mini"
                v-show="scope.row.isSet"
                @click.native.prevent="removeRowTask(scope.$index,form.testCasePlanTask)"
              >{{ $t('commons.cancel') }}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="!scope.row.isSet"
                @click.native.prevent="deleteRowTask(scope.$index,scope.row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>

</template>

<script>
export default {
  name: "TestPlanTaskNotification",
  props: {
    testPlanReceiverOptions: {
      type: Array
    }
  },
  data() {
    return {
      form: {
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
      },
      otherEventOptions: [
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')}
      ],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
    };
  },
  activated() {
    this.initForm()
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message', response => {
        this.form.testCasePlanTask = response.data.testCasePlanTask;
        this.form.testCasePlanTask.forEach(planTask => {
          this.handleTestPlanReceivers(planTask);
        });
      })
    },
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL') {
        data.isReadOnly = !data.isReadOnly
      }
    },
    handleAddTaskModel(type) {
      let Task = {};
      Task.event = [];
      Task.userIds = [];
      Task.type = "";
      Task.webhook = "";
      Task.isSet = true;
      Task.identification = "";
      if (type === 'jenkinsTask') {
        Task.taskType = 'JENKINS_TASK'
        this.form.jenkinsTask.push(Task)
      }
      if (type === 'testPlanTask') {
        Task.taskType = 'TEST_PLAN_TASK'
        this.form.testCasePlanTask.push(Task)
      }
      if (type === 'reviewTask') {
        Task.taskType = 'REVIEW_TASK'
        this.form.reviewTask.push(Task)
      }
      if (type === 'defectTask') {
        Task.taskType = 'DEFECT_TASK'
        this.form.defectTask.push(Task)
      }
    },
    handleAddTask(index, data) {

      if (data.event && data.userIds.length > 0 && data.type) {
        console.log(data.type)
        if (data.type === 'NAIL_ROBOT' || data.type === 'WECHAT_ROBOT') {
          if (!data.webhook) {
            this.$warning(this.$t('organization.message.message_webhook'));
          } else {
            this.addTask(data)
          }
        } else {
          this.addTask(data)
        }
      } else {
        this.$warning(this.$t('organization.message.message'));
      }
    },
    addTask(data) {
      let list = []
      data.isSet = false
      list.push(data)
      let param = {};
      param.messageDetail = list
      this.result = this.$post("/notice/save/message/task", param, () => {
        this.initForm()
        this.$success(this.$t('commons.save_success'));
      })
    },
    removeRowTask(index, data) { //移除
      data.splice(index, 1)
    },
    deleteRowTask(index, data) { //删除
      this.result = this.$get("/notice/delete/message/" + data.identification, response => {
        this.$success(this.$t('commons.delete_success'));
        this.initForm()
      })
    },
    rowClass() {
      return "text-align:center"
    },
    headClass() {
      return "text-align:center;background:'#ededed'"
    },
    handleTestPlanReceivers(row) {
      let testPlanReceivers = JSON.parse(JSON.stringify(this.testPlanReceiverOptions));
      switch (row.event) {
        case  "CREATE":
          testPlanReceivers.unshift({id: 'EXECUTOR', name: this.$t('test_track.plan_view.executor')})
          break;
        case "UPDATE":
        case "DELETE":
        case "COMMENT":
          testPlanReceivers.unshift({id: 'FOUNDER', name: this.$t('api_test.creator')});
          break;
        default:
          break;
      }
      row.testPlanReceiverOptions = testPlanReceivers;
    },
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
</style>
