<template>
  <div style="margin-left: 40px">
    <el-form :model="form" ref="from">
      <el-row class="row">
        <el-col :span="20">
          <div class="grid-content bg-purple-dark">
            <el-row>
              <el-col :span="6">
                <span style="font-weight:bold;">{{ $t('organization.message.jenkins_task_notification') }}</span>
              </el-col>
              <el-col :span="14">
                <el-button type="text" icon="el-icon-plus" size="mini"
                           @click="handleAddTaskModel('jenkinsTask')">
                  {{ $t('organization.message.create_new_notification') }}
                </el-button>
              </el-col>
            </el-row>
          </div>

          <el-table
            :data="form.jenkinsTask"
            class="tb-edit"
            border
            size="mini"
            :header-cell-style="{background:'#ededed'}"
          >
            <el-table-column :label="$t('schedule.event')" min-width="20%" prop="events">
              <template slot-scope="scope">
                <el-select  v-model="scope.row.events" multiple
                            :placeholder="$t('organization.message.select_events')"
                            prop="events" :disabled="!scope.row.isSet">
                  <el-option
                    v-for="item in jenkinsEventOptions"
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
                           :placeholder="$t('commons.please_select')"
                           @click.native="userList()" style="width: 100%;" :disabled="!row.isSet">
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
                <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')" :disabled="!scope.row.isSet"  @change="handleEdit(scope.$index, scope.row)"
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址" :disabled="!scope.row.isSet||scope.row.events === 'EMAIL'"></el-input>
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
                  @click.native.prevent="removeRowTask(scope.$index,form.jenkinsTask)"
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
      <el-row class="row">
        <el-col :span="20">
          <div class="grid-content bg-purple-dark">
            <el-row>
              <el-col :span="6">
                <span style="font-weight:bold;">{{ $t('organization.message.test_plan_task_notification') }}</span>
              </el-col>
              <el-col :span="14">
                <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddTaskModel('testPlanTask')">
                  {{ $t('organization.message.create_new_notification') }}
                </el-button>
              </el-col>
            </el-row>
          </div>
          <el-table
            :data="form.testCasePlanTask"
            class="tb-edit"
            border
            size="mini"
            :header-cell-style="{background:'#EDEDED'}"
          >
            <el-table-column :label="$t('schedule.event')" min-width="20%" prop="events">
              <template slot-scope="scope">
                <el-select v-model="scope.row.events" multiple :placeholder="$t('organization.message.select_events')"
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
                           :placeholder="$t('commons.please_select')"
                           @click.native="testPlanUserList()" style="width: 100%;" :disabled="!row.isSet">
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
                <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')" :disabled="!scope.row.isSet">
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址" :disabled="!scope.row.isSet"></el-input>
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
      <el-row class="row">
        <el-col :span="20">
          <div class="grid-content bg-purple-dark">
            <el-row>
              <el-col :span="6">
                <span style="font-weight:bold;">{{ $t('organization.message.test_review_task_notice') }}</span>
              </el-col>
              <el-col :span="14">
                <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddTaskModel('reviewTask')">
                  {{ $t('organization.message.create_new_notification') }}
                </el-button>
              </el-col>
            </el-row>
          </div>
          <el-table
            :data="form.reviewTask"
            class="tb-edit"
            border
            size="mini"
            :header-cell-style="{background:'#EDEDED'}"
          >
            <el-table-column :label="$t('schedule.event')" min-width="20%" prop="events">
              <template slot-scope="scope">
                <el-select v-model="scope.row.events" multiple :placeholder="$t('organization.message.select_events')"
                           prop="event" :disabled="!scope.row.isSet">
                  <el-option
                    v-for="item in reviewTaskEventOptions"
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
                           @click.native="reviewUerList()" style="width: 100%;" :disabled="!row.isSet">
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
                <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')" :disabled="!scope.row.isSet">
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址" :disabled="!scope.row.isSet"></el-input>
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
                  @click.native.prevent="removeRowTask(scope.$index,form.reviewTask)"
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
      <el-row class="row">
        <el-col :span="20">
          <div class="grid-content bg-purple-dark">
            <el-row>
              <el-col :span="6">
                <span style="font-weight:bold;">{{ $t('organization.message.defect_task_notification') }}</span>
              </el-col>
              <el-col :span="14">
                <el-button type="text" icon="el-icon-plus" size="mini" @click="handleAddTaskModel('defectTask')">
                  {{ $t('organization.message.create_new_notification') }}
                </el-button>
              </el-col>
            </el-row>
          </div>
          <el-table
            :data="form.defectTask"
            class="tb-edit"
            border
            size="mini"
            :header-cell-style="{background:'#EDEDED'}"
          >
            <el-table-column :label="$t('schedule.event')" min-width="20%" prop="events">
              <template slot-scope="scope">
                <el-select v-model="scope.row.events" multiple :placeholder="$t('organization.message.select_events')"
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
                <el-select v-model="row.userIds" filterable multiple
                           :placeholder="$t('commons.please_select')"
                           @click.native="defectUserList()" style="width: 100%;" :disabled="!row.isSet">
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
                <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')" :disabled="!scope.row.isSet">
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址" :disabled="!scope.row.isSet"></el-input>
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
                  @click.native.prevent="removeRowTask(scope.$index,form.defectTask)"
                >{{ $t('commons.cancel') }}
                </el-button>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  v-show="!scope.row.isSet"
                  @click="deleteRowTask(scope.$index,scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>

export default {
  name: "TaskNotification",
  data() {
    return {
      form: {
        jenkinsTask: [{
          taskType: "jenkinsTask",
          events: [],
          userIds: [],
          type: [],
          webhook: "",
          isSet: true,
          identification: "",
        }],
        testCasePlanTask: [{
          taskType: "testPlanTask",
          events: [],
          userIds: [],
          type: [],
          webhook: "",
          isSet: true,
          identification: "",
        }],
        reviewTask: [{
          taskType: "reviewTask",
          events: [],
          userIds: [],
          type: [],
          webhook: "",
          isSet: true,
          identification: "",
        }],
        defectTask: [{
          taskType: "defectTask",
          events: [],
          userIds: [],
          type: [],
          webhook: "",
          isSet: true,
          identification: "",
        }],
      },
      jenkinsEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
      jenkinsReceiverOptions: [],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
      otherEventOptions: [
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')}
      ],
      reviewTaskEventOptions:[
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')},
        {value: 'COMMENT', label: this.$t('commons.comment')}
      ],
      defectEventOptions:[
        {value: 'CREATE', label: this.$t('commons.create')},
/*
        {value: 'UPDATE', label: this.$t('commons.update')},
*/
      ],
      //测试计划
      testPlanReceiverOptions: [],
      //评审
      reviewReceiverOptions: [],
      //缺陷
      defectReceiverOptions: [],
    }
  },

  activated(){
    this.initForm()
    this. userList()
    this.testPlanUserList()
    this.defectUserList()
    this.reviewUerList()
  },
  methods: {
    handleEdit(index, data){

    },
    initForm() {
      this.result = this.$get('/notice/search/message', response => {
        this.form = response.data
      })
    },
    userList() {
      this.result = this.$get('user/list', response => {
        this.jenkinsReceiverOptions = response.data
      })
    },
    reviewUerList() {
      this.result = this.$get('user/list', response => {
        this.reviewReceiverOptions = response.data
        this.reviewReceiverOptions.unshift({id: 'EXECUTOR', name: this.$t('test_track.review.reviewer')},
          {id: 'FOUNDER', name: this.$t('test_track.review.review_creator')},
          {id: 'MAINTAINER', name: this.$t('test_track.case.maintainer')})
      })
    },
    defectUserList() {
      this.result = this.$get('user/list', response => {
        this.defectReceiverOptions = response.data
       /* this.defectReceiverOptions.unshift({id: 'FOUNDER', name: this.$t('api_test.creator')}, {
          id: 'EXECUTOR',
          name: this.$t('test_track.plan_view.executor')
        })*/
      })
    },
    testPlanUserList() {
      this.result = this.$get('user/list', response => {
        this.testPlanReceiverOptions = response.data
        this.testPlanReceiverOptions.unshift({id: 'FOUNDER', name: this.$t('api_test.creator')}, {
          id: 'EXECUTOR',
          name: this.$t('test_track.plan_view.executor')
        })
      })
    },
    handleAddTaskModel(type) {
      let Task = {};
      Task.events = [];
      Task.userIds = [];
      Task.type = "";
      Task.webhook = "";
      Task.isSet = true;
      Task.identification = "";
      if (type === 'jenkinsTask') {
        Task.taskType = 'JENKINS_TASK'
        this.form.jenkinsTask.unshift(Task)
      }
      if (type === 'testPlanTask') {
        Task.taskType = 'TEST_PLAN_TASK'
        this.form.testCasePlanTask.unshift(Task)
      }
      if (type === 'reviewTask') {
        Task.taskType = 'REVIEW_TASK'
        this.form.reviewTask.unshift(Task)
      }
      if (type === 'defectTask') {
        Task.taskType = 'DEFECT_TASK'
        this.form.defectTask.unshift(Task)
      }
    },

    handleAddTask(index, data) {
      let list = []
      if(data.events.length>0 && data.userIds.length>0 && data.type){
        data.isSet = false
        list.push(data)
        let param = {};
        param.messageDetail = list
        this.result = this.$post("/notice/save/message/task", param, () => {
          this.initForm()
          this.$success(this.$t('commons.save_success'));
        })
      }
    },
    removeRowTask(index, data) { //移除
      data.splice(index, 1)
    },
    deleteRowTask(index, data) { //删除
      this.result = this.$get("/notice/delete/message/" + data.identification, response => {
        this.$success(this.$t('commons.delete_success'));
        this.initForm()
      })
      /*data.splice(index, 1)*/
    },
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
