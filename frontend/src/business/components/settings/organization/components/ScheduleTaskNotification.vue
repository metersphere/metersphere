<template>
  <div>
    <el-row>
      <el-col :span="10">
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel('scheduleTask')">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table
          :data="form.scheduleTask"
          class="tb-edit"
          border
          size="mini"
          :cell-style="rowClass"
          :header-cell-style="headClass">
          <el-table-column :label="$t('schedule.event')" min-width="20%" prop="events">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event"
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
          <el-table-column :label="$t('schedule.receiver')" prop="userIds" min-width="20%">
            <template v-slot:default="{row}">
              <el-select v-model="row.userIds" filterable multiple
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
          <el-table-column :label="$t('schedule.receiving_mode')" min-width="20%" prop="type">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')"
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
                @click.native.prevent="removeRowTask(scope.$index,form.scheduleTask)"
              >{{ $t('commons.cancel') }}
              </el-button>
              <el-button
                type="primary"
                size="mini"
                v-show="!scope.row.isSet"
                @click="handleEditTask(scope.$index,scope.row)"
              >{{ $t('commons.edit') }}</el-button>
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
  name: "ScheduleTaskNotification",
  props: {
    testId:String,
    scheduleReceiverOptions:Array,
  },
  data() {
    return {
      form: {
        scheduleTask: [{
          taskType: "scheduleTask",
          event: "",
          userIds: [],
          type: [],
          webhook: "",
          isSet: true,
          identification: "",
          isReadOnly: false,
          testId:this.testId,

        }],
      },
      scheduleEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
    }
  },
  mounted(){
    this.initForm()
  },
  methods: {
    initForm(){
      this.result = this.$get('/notice/search/message/'+this.testId, response => {
        console.log(response.data);
        this.form.scheduleTask = response.data;
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
      if (type === 'scheduleTask') {
        Task.taskType = 'SCHEDULE_TASK'
        Task.testId=this.testId
        this.form.scheduleTask.push(Task)
      }
    },
    handleEditTask(index,data){
      data.isSet = true
      data.testId=this.testId
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
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
</style>

