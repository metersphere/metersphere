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
              <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddJenkins('jenkinsTask')">
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
    <el-row class="row">
      <el-col :span="20">
        <div class="grid-content bg-purple-dark">
          <el-row>
            <el-col :span="6">
              <span style="font-weight:bold;">{{$t('organization.message.test_plan_task_notification')}}</span>
            </el-col>
            <el-col :span="14">
              <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddTestPlan('testPlanTask')">
                {{$t('organization.message.create_new_notification')}}
              </el-button>
            </el-col>
          </el-row>
        </div>
        <el-table
          :data="testPlanTask"
          class="tb-edit"
          border
          size="mini"
          :header-cell-style="{background:'#EDEDED'}"
        >
          <el-table-column :label="$t('schedule.event')" min-width="20%" prop="event">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" multiple :placeholder="$t('organization.message.select_events')" prop="event">
                <el-option
                  v-for="item in testPlanEventOptions"
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
                  v-for="item in testPlanReceiverOptions"
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
                @click="removeRowTestPlan(scope.$index,scope.row)"
              >{{$t('commons.cancel')}}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="scope.row.showDelete"
                @click="removeRowTestPlan(scope.$index,scope.row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <el-row class="row">
      <el-col :span="20">
        <div class="grid-content bg-purple-dark">
          <el-row>
            <el-col :span="6">
              <span style="font-weight:bold;">{{$t('organization.message.test_review_task_notice')}}</span>
            </el-col>
            <el-col :span="14">
              <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddReview('reviewTask')">
                {{$t('organization.message.create_new_notification')}}
              </el-button>
            </el-col>
          </el-row>
        </div>
        <el-table
          :data="reviewTask"
          class="tb-edit"
          border
          size="mini"
          :header-cell-style="{background:'#EDEDED'}"
        >
          <el-table-column :label="$t('schedule.event')" min-width="20%" prop="event">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" multiple :placeholder="$t('organization.message.select_events')" prop="event">
                <el-option
                  v-for="item in reviewEventOptions"
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
                  v-for="item in reviewReceiverOptions"
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
                @click="removeRowReview(scope.$index,scope.row)"
              >{{$t('commons.cancel')}}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="scope.row.showDelete"
                @click="removeRowReview(scope.$index,scope.row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <el-row class="row">
      <el-col :span="20">
        <div class="grid-content bg-purple-dark">
          <el-row>
            <el-col :span="6">
              <span style="font-weight:bold;">{{$t('organization.message.defect_task_notification')}}</span>
            </el-col>
            <el-col :span="14">
              <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddDefect('defectTask')">
                {{$t('organization.message.create_new_notification')}}
              </el-button>
            </el-col>
          </el-row>
        </div>
        <el-table
          :data="defectTask"
          class="tb-edit"
          border
          size="mini"
          :header-cell-style="{background:'#EDEDED'}"
        >
          <el-table-column :label="$t('schedule.event')" min-width="20%" prop="event">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" multiple :placeholder="$t('organization.message.select_events')" prop="event">
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
              <el-select v-model="row.userIds" filterable multiple
                         :placeholder="$t('commons.please_select')"
                         @click.native="userList()" style="width: 100%;">
                <el-option
                  v-for="item in defectReceiverOptions"
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
                  v-for="item in defectOptions"
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
                @click="removeRowDefect(scope.$index,scope.row)"
              >{{$t('commons.cancel')}}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="scope.row.showDelete"
                @click="removeRowDefect(scope.$index,scope.row)"
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
      jenkinsEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
      jenkinsReceiverOptions: [],

      testPlanTask:[{}],
      testPlanEventOptions: [
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')}
      ],
      testPlanReceiverOption:[],
      testPlanReceiverOptions:[],
      receiveOptions: [
        {value: 'email', label: this.$t('organization.message.mail')},
        {value: 'nailRobot', label: this.$t('organization.message.nail_robot')},
        {value: 'wechatRobot', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
      reviewTask:[{}],
      reviewEventOptions:[
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')}
      ],
      reviewReceiverOptions:[],
      defectTask:[{}],
      defectEventOptions:[
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')}
      ],
      defectOptions:[
        {value: 'email', label: this.$t('organization.message.mail')},
        {value: 'nailRobot', label: this.$t('organization.message.nail_robot')},
        {value: 'wechatRobot', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
      defectReceiverOptions:[{}],
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
    handleAddJenkins(index, data) {
      this.showAdd = true;
      this.showCancel = true;
      this.showDelete = false;
      let jenkinsTask = {};
      this.jenkinsTask.unshift(jenkinsTask)
    },
    handleAddTestPlan(index, data) {
      this.showAdd = true;
      this.showCancel = true;
      this.showDelete = false;
      let testPlanTask = {};
      this.testPlanTask.unshift(testPlanTask)
    },
    handleAddReview(index, data) {
      this.showAdd = true;
      this.showCancel = true;
      this.showDelete = false;
      let reviewTask = {};
      this.reviewTask.unshift(reviewTask)
    },
    handleAddDefect(index, data) {
      this.showAdd = true;
      this.showCancel = true;
      this.showDelete = false;
      let defectTask = {};
      this.defectTask.unshift(defectTask)
    },
    handleAddTask(index,row) {
      this.result = this.$post('/notice/save/message', this.jenkinsTask, () => {

      })

    },
    removeRow(index, rows) { //删除
      this.jenkinsTask.splice(index, 1)
    },
    removeRowTestPlan(index, rows) { //删除
      this.testPlanTask.splice(index, 1)
    },
    removeRowReview(index, rows) { //删除
      this.reviewTask.splice(index, 1)
    },
    removeRowDefect(index, rows) { //删除
      this.defectTask.splice(index, 1)
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
