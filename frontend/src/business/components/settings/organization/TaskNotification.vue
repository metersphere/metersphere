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
                           prop="events">
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址"></el-input>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.operating')" min-width="20%" prop="result">
              <template v-slot:default="scope">
                <el-button
                  type="primary"
                  size="mini"
                  v-show="scope.row.result.showSave"
                  @click="handleAddTask(scope.$index,scope.row)"
                >{{ $t('commons.add') }}
                </el-button>
                <el-button
                  size="mini"
                  v-show="scope.row.result.showCancel"
                  @click.native.prevent="removeRowTask(scope.$index,form.jenkinsTask)"
                >{{ $t('commons.cancel') }}
                </el-button>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  v-show="scope.row.result.showDelete"
                  @click.native.prevent="deleteRowTask(scope.$index,form.jenkinsTask)"
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
            :data="form.testPlanTask"
            class="tb-edit"
            border
            size="mini"
            :header-cell-style="{background:'#EDEDED'}"
          >
            <el-table-column :label="$t('schedule.event')" min-width="20%" prop="events">
              <template slot-scope="scope">
                <el-select v-model="scope.row.events" multiple :placeholder="$t('organization.message.select_events')"
                           prop="events">
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
                           @click.native="defectAndTestPlanUserList()" style="width: 100%;">
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址"></el-input>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.operating')" min-width="20%" prop="result">
              <template v-slot:default="scope">
                <el-button
                  type="primary"
                  size="mini"
                  v-show="scope.row.result.showSave"
                  @click="handleAddTask(scope.$index,scope.row)"
                >{{ $t('commons.add') }}
                </el-button>
                <el-button
                  size="mini"
                  v-show="scope.row.result.showCancel"
                  @click.native.prevent="removeRowTask(scope.$index,form.testPlanTask)"
                >{{ $t('commons.cancel') }}
                </el-button>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  v-show="scope.row.result.showDelete"
                  @click.native.prevent="deleteRowTask(scope.$index,form.testPlanTask)"
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
                           prop="event">
                  <el-option
                    v-for="item in otherEventOptions"
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
                           @click.native="reviewUerList()" style="width: 100%;">
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址"></el-input>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.operating')" min-width="20%" prop="result">
              <template v-slot:default="scope">
                <el-button
                  type="primary"
                  size="mini"
                  v-show="scope.row.result.showSave"
                  @click="handleAddTask(scope.$index,scope.row)"
                >{{ $t('commons.add') }}
                </el-button>
                <el-button
                  size="mini"
                  v-show="scope.row.result.showCancel"
                  @click.native.prevent="removeRowTask(scope.$index,form.reviewTask)"
                >{{ $t('commons.cancel') }}
                </el-button>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  v-show="scope.row.result.showDelete"
                  @click.native.prevent="deleteRowTask(scope.$index,form.reviewTask)"
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
                           prop="event">
                  <el-option
                    v-for="item in otherEventOptions"
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
                           @click.native="defectAndTestPlanUserList()" style="width: 100%;">
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
                <el-input v-model="scope.row.webhook" placeholder="webhook地址"></el-input>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.operating')" min-width="20%" prop="result">
              <template v-slot:default="scope">
                <el-button
                  type="primary"
                  size="mini"
                  v-show="scope.row.result.showSave"
                  @click="handleAddTask(scope.$index,scope.row)"
                >{{ $t('commons.add') }}
                </el-button>
                <el-button
                  size="mini"
                  v-show="scope.row.result.showCancel"
                  @click.native.prevent="removeRowTask(scope.$index,form.defectTask)"
                >{{ $t('commons.cancel') }}
                </el-button>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  v-show="scope.row.result.showDelete"
                  @click.native.prevent="deleteRowTask(scope.$index,form.defectTask)"
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
          type:[],
          webhook: "",
          result: {
            showSave: true,
            showCancel: true,
            showDelete: false
          },
          identification:"",
        }],
        testPlanTask: [{
          taskType: "testPlanTask",
          events: [],
          userIds: [],
          type:[],
          webhook: "",
          result: {
            showSave: true,
            showCancel: true,
            showDelete: false
          },
          identification:"",
        }],
        reviewTask: [{
          taskType: "reviewTask",
          events: [],
          userIds: [],
          type:[],
          webhook: "",
          result: {
            showSave: true,
            showCancel: true,
            showDelete: false
          },
          identification:"",
        }],
        defectTask: [{
          taskType: "defectTask",
          events: [],
          userIds: [],
          type:[],
          webhook: "",
          result: {
            showSave: true,
            showCancel: true,
            showDelete: false
          },
          identification:"",
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
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message', response => {
         /*this.form=response.data*/
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
        this.reviewReceiverOptions.unshift({id: 'Founder', name: this.$t('api_test.creator')},
          {id: 'Executor', name: this.$t('test_track.plan_view.executor')},
          {id: 'Maintainer', name: this.$t('test_track.case.maintainer')})
      })
    },
    defectAndTestPlanUserList() {
      this.result = this.$get('user/list', response => {
        this.testPlanReceiverOptions = response.data
        this.defectReceiverOptions = response.data
        this.testPlanReceiverOptions.unshift({id: 'Founder', name: this.$t('api_test.creator')}, {
          id: 'Executor',
          name: this.$t('test_track.plan_view.executor')
        })
      })
    },
    handleAddTaskModel(type) {
      let Task = {};
      Task.result = {
        showSave: true,
        showCancel: true,
        showDelete: false,
      }
      if (type === 'jenkinsTask') {
        Task.taskType = 'jenkinsTask'
        this.form.jenkinsTask.unshift(Task)
      } else if (type === 'testPlanTask') {
        Task.taskType = 'testPlanTask'
        this.form.testPlanTask.unshift(Task)
      } else if (type === 'reviewTask') {
        Task.taskType = 'reviewTask'
        this.form.reviewTask.unshift(Task)
      } else {
        Task.taskType = 'defectTask'
        this.form.defectTask.unshift(Task)
      }
    },

    handleAddTask(index, data) {
      let list = []
      list.push(data)
      let param = {};
      param.messageDetail = list
      this.result = this.$post("/notice/save/message/task", param, () => {
        data.result.showSave = false;
        data.result.showCancel = false;
        data.result.showDelete = true;
      })
    },
    removeRowTask(index, data) { //移除
      data.splice(index, 1)
    },
    deleteRowTask(index, data) { //删除
      this.result = this.$get("/delete/message" + index, response => {

      })
      data.splice(index, 1)
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
