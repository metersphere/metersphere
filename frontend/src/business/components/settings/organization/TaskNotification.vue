<template>
  <div style="margin-left: 40px">
    <el-row class="row">
      <el-col :span="20">
        <div class="grid-content bg-purple-dark">
          <el-row>
            <el-col :span="6">
              <span style="font-weight:bold;">{{$t('organization.message.jenkins_task_notification')}}</span>
            </el-col>
            <el-col :span="14">
              <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddStep('jenkinsTask')">
                {{$t('organization.message.create_new_notification')}}
              </el-button>
            </el-col>
          </el-row>
        </div>
        <el-table
          :data="jenkinsTask"
          class="tb-edit"
          border
          size="mini"
          :header-cell-style="{background:'#EDEDED'}"
        >
          <el-table-column :label="$t('schedule.event')" min-width="20%" prop="event">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" multiple :placeholder="$t('organization.message.select_events')" prop="event">
                <el-option
                  v-for="item in jenkinsEventOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiver')" prop="receiver" min-width="20%">
            <template v-slot:default="{row}">
              <el-select v-model="row.userIds" filterable multiple
                         :placeholder="$t('commons.please_select')"
                         @click.native="userList()" style="width: 100%;">
                <el-option
                  v-for="item in jenkinsReceiverOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiving_mode')" min-width="20%" prop="type">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')">
                <el-option
                  v-for="item in receiveOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="webhook" min-width="20%" prop="webhook">
            <template v-slot:default="scope">
              <el-input v-model="scope.row.webhook" placeholder="webhook地址"></el-input>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.operating')" min-width="20%" prop="result">
            <template v-slot:default="scope">
              <el-button
                type="primary"
                size="mini"
                v-show="!scope.row.showAdd"
                @click="handleAddTask(scope.$index,scope.row)"
              >{{$t('commons.add')}}
              </el-button>
              <el-button
                size="mini"
                v-show="!scope.row.showCancel"
                @click="removeRow(scope.$index,scope.row)"
              >{{$t('commons.cancel')}}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="scope.row.showDelete"
                @click="removeRow(scope.$index,scope.row)"
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
  name: "TaskNotification",
  data() {
    return {
      jenkinsTask: [{}],
      planCaseTask: [{}],
      jenkinsEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
      jenkinsReceiverOptions: [],
      receiveOptions: [
        {value: 'email', label: this.$t('organization.message.mail')},
        {value: 'nailRobot', label: this.$t('organization.message.nail_robot')},
        {value: 'wechatRobot', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
      webhook: "",
      showAdd: true,
      showDelete: false,
      showCancel: true,

    }
  },
  methods: {
    userList() {
      this.result = this.$get('user/list', response => {
        this.jenkinsReceiverOptions = response.data
      })
    },
    handleAddStep(index, data) {
      this.showAdd = true;
      this.showCancel = true;
      this.showDelete = false;
      let jenkinsTask = {};
      this.jenkinsTask.unshift(jenkinsTask)
    },
    handleAddTask(index,row) {
      this.result = this.$post('/notice/save', this.jenkinsTask, () => {

      })

    },
    removeRow(index, rows) { //删除
      this.jenkinsTask.splice(index, 1)
    }
  }
}
</script>

<style scoped>
/deep/ .el-select__tags {
  flex-wrap: unset;
  overflow: auto;
}

.row {
  margin-bottom: 30px;

}


</style>
