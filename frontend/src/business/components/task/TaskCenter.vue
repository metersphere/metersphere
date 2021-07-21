<template>
  <div v-permission="['PROJECT_API_SCENARIO:READ']">
    <el-menu :unique-opened="true" class="header-user-menu align-right header-top-menu"
             mode="horizontal"
             :background-color="color"
             text-color="#fff"
             active-text-color="#fff">
      <el-menu-item onselectstart="return false">
        <el-tooltip effect="light">
          <template v-slot:content>
            <span>{{ $t('commons.task_center') }}</span>
          </template>
          <div @click="showTaskCenter" v-if="runningTotal > 0">
            <el-badge :value="runningTotal" class="item" type="primary">
              <font-awesome-icon class="icon global focusing" :icon="['fas', 'tasks']"
                                 style="font-size: 18px"/>
            </el-badge>
          </div>
          <font-awesome-icon @click="showTaskCenter" class="icon global focusing" :icon="['fas', 'tasks']" v-else/>
        </el-tooltip>
      </el-menu-item>
    </el-menu>

    <el-drawer :visible.sync="taskVisible" :destroy-on-close="true" direction="rtl"
               :withHeader="true" :modal="false" :title="$t('commons.task_center')" size="600px"
               custom-class="ms-drawer-task">
      <div style="color: #2B415C;margin: 0px 20px 0px">
        <el-form label-width="68px">
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('test_track.report.list.trigger_mode')" prop="runMode">
                <el-select size="small" style="margin-right: 10px" v-model="condition.triggerMode" @change="init">
                  <el-option v-for="item in runMode" :key="item.id" :value="item.id" :label="item.label"/>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select size="small" style="margin-right: 10px" v-model="condition.executionStatus" @change="init">
                  <el-option v-for="item in runStatus" :key="item.id" :value="item.id" :label="item.label"/>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('commons.executor')" prop="status">
                <el-select v-model="condition.executor" :placeholder="$t('commons.executor')" filterable size="small"
                           style="margin-right: 10px" @change="init">
                  <el-option
                    v-for="item in maintainerOptions"
                    :key="item.id"
                    :label="item.id + ' (' + item.name + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <div class="report-container">
        <div v-for="item in taskData" :key="item.id" style="margin-bottom: 5px">
          <el-card class="ms-card-task" @click.native="showReport(item,$event)">
            <span><el-link type="primary">{{getModeName(item.executionModule)}} </el-link>: {{ item.name }} </span><br/>
            <span>
              执行器：{{ item.actuator }} 由 {{ item.executor }}
              {{ item.executionTime | timestampFormatDate }}
              {{ getMode(item.triggerMode) }}
            </span>
            <br/>
            <el-row>
              <el-col :span="20">
                <el-progress :percentage="getPercentage(item.executionStatus)" :format="format"/>
              </el-col>
              <el-col :span="4">
                  <span v-if="item.executionStatus && item.executionStatus.toLowerCase() === 'error'" class="ms-task-error">
                     error
                  </span>
                <span v-else-if="item.executionStatus && item.executionStatus.toLowerCase() === 'success'" class="ms-task-success">
                     success
                </span>
                <span v-else>{{ item.executionStatus ? item.executionStatus.toLowerCase() : item.executionStatus }}</span>
              </el-col>
            </el-row>
          </el-card>
        </div>
      </div>
    </el-drawer>


    <el-dialog :close-on-click-modal="false" :title="$t('test_track.plan_view.test_result')" width="60%"
               :visible.sync="visible" class="api-import" destroy-on-close @close="close">
      <ms-request-result-tail :response="response" ref="debugResult"/>
    </el-dialog>
  </div>
</template>

<script>
import MsDrawer from "../common/components/MsDrawer";
import {getCurrentProjectID, getCurrentUser, hasPermissions} from "@/common/js/utils";
import MsRequestResultTail from "../../components/api/definition/components/response/RequestResultTail";

export default {
  name: "MsTaskCenter",
  components: {
    MsDrawer,
    MsRequestResultTail
  },
  inject: [
    'reload'
  ],
  data() {
    return {
      runningTotal: 0,
      taskVisible: false,
      result: {},
      taskData: [],
      response: {},
      initEnd: false,
      visible: false,
      runMode: [
        {id: '', label: this.$t('api_test.definition.document.data_set.all')},
        {id: 'BATCH', label: this.$t('api_test.automation.batch_execute')},
        {id: 'SCHEDULE', label: this.$t('commons.trigger_mode.schedule')},
        {id: 'MANUAL', label: this.$t('commons.trigger_mode.manual')},
        {id: 'API', label: 'API'}
      ],
      runStatus: [
        {id: '', label: this.$t('api_test.definition.document.data_set.all')},
        {id: 'Saved', label: 'Saved'},
        {id: 'Starting', label: 'Starting'},
        {id: 'Running', label: 'Running'},
        {id: 'Reporting', label: 'Reporting'},
        {id: 'Completed', label: 'Completed'},
        {id: 'error', label: 'Error'},
        {id: 'success', label: 'Success'}
      ],
      condition: {triggerMode: "", executionStatus: ""},
      maintainerOptions: [],
    };
  },
  props: {
    color: String
  },
  created() {
    if (hasPermissions('PROJECT_API_SCENARIO:READ')) {
      this.getTaskRunning();
      this.condition.executor = getCurrentUser().id;
    }
  },
  methods: {
    format(item) {
      return '';
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.maintainerOptions = response.data;
        this.condition.executor = getCurrentUser().id;
      });
    },
    initWebSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      const uri = protocol + window.location.host + "/task/center/count/running/" + getCurrentProjectID();
      this.websocket = new WebSocket(uri);
      this.websocket.onmessage = this.onMessage;
      this.websocket.onopen = this.onOpen;
      this.websocket.onerror = this.onError;
      this.websocket.onclose = this.onClose;
    },
    onOpen() {
    },
    onError(e) {
    },
    onMessage(e) {
      let taskTotal = e.data;
      this.runningTotal = taskTotal;
      this.initIndex++;
      if (this.taskVisible && taskTotal > 0 && this.initEnd) {
        setTimeout(() => {
          this.initEnd = false;
          this.init();
        }, 3000);
      }
    },
    onClose(e) {
      if (e.code === 1005) {
        // 强制删除之后关闭socket，不用刷新report
        return;
      }
    },
    showTaskCenter() {
      this.getTaskRunning();
      this.getMaintainerOptions();
      this.init();
      this.taskVisible = true;
    },
    close() {
      this.visible = false;
      this.taskVisible = false;
    },
    open() {
      this.showTaskCenter();
      this.initIndex = 0;
    },
    getPercentage(status) {
      if (status) {
        status = status.toLowerCase();
        if (status === "waiting") {
          return 0;
        }
        if (status === 'saved' || status === 'completed' || status === 'success' || status === 'error') {
          return 100;
        }
      }
      return 60;
    },
    getModeName(executionModule){
      switch (executionModule) {
        case "SCENARIO":
          return this.$t('test_track.scenario_test_case');
        case "PERFORMANCE":
          return this.$t('test_track.performance_test_case');
        case "API":
          return this.$t('test_track.api_test_case');
      }
    },
    showReport(row, env) {
      let status = row.executionStatus;
      if (status) {
        status = row.executionStatus.toLowerCase();
        if (status === 'saved' || status === 'completed' || status === 'success' || status === 'error') {
          this.taskVisible = false;
          switch (row.executionModule) {
            case "SCENARIO":
              this.$router.push({
                path: '/api/automation/report/view/' + row.id,
              });
              break;
            case "PERFORMANCE":
              this.$router.push({
                path: '/performance/report/view/' + row.id,
              });
              break;
            case "API":
              this.getExecResult(row.id);
              break;
          }
        } else {
          this.$warning("正在运行中，请稍后查看");
        }
      }
    },

    getExecResult(reportId) {
      if (reportId) {
        let url = "/api/definition/report/get/" + reportId;
        this.$get(url, response => {
          if (response.data) {
            let data = JSON.parse(response.data.content);
            this.response = data;
            this.visible = true;
          }
        });
      }
    },
    getMode(mode) {
      if (mode === 'MANUAL') {
        return this.$t('commons.trigger_mode.manual');
      }
      if (mode === 'SCHEDULE') {
        return this.$t('commons.trigger_mode.schedule');
      }
      if (mode === 'TEST_PLAN_SCHEDULE') {
        return this.$t('commons.trigger_mode.schedule');
      }
      if (mode === 'API') {
        return this.$t('commons.trigger_mode.api');
      }
      if (mode === 'BATCH') {
        return this.$t('api_test.automation.batch_execute');
      }
      return mode;
    },
    getTaskRunning() {
      this.initWebSocket();
    },
    init() {
      this.result.loading = true;
      this.condition.projectId = getCurrentProjectID();
      this.result = this.$post('/task/center/list', this.condition, response => {
        this.taskData = response.data;
        this.initEnd = true;
      });
    }
  }
};
</script>

<style>
.ms-drawer-task {
  top: 42px !important;
}
</style>

<style scoped>
.el-icon-check {
  color: #44b349;
  margin-left: 10px;
}

.report-container {
  height: calc(100vh - 180px);
  min-height: 600px;
  overflow-y: auto;
}

.align-right {
  float: right;
}

.icon {
  width: 24px;
}

/deep/ .el-drawer__header {
  font-size: 18px;
  color: #0a0a0a;
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
  margin-bottom: 10px;
  padding: 10px;
}

.ms-card-task >>> .el-card__body {
  padding: 10px;
}

.global {
  color: #fff;
}

.header-top-menu {
  height: 40px;
  line-height: 40px;
  color: inherit;
}

.header-top-menu.el-menu--horizontal > li {
  height: 40px;
  line-height: 40px;
  color: inherit;
}

.header-top-menu.el-menu--horizontal > li.el-submenu > * {
  height: 39px;
  line-height: 40px;
  color: inherit;
}

.header-top-menu.el-menu--horizontal > li.is-active {
  background: var(--color_shallow) !important;
}

.ms-card-task:hover {
  cursor: pointer;
  border-color: #783887;
}

/deep/ .el-progress-bar {
  padding-right: 20px;
}

/deep/ .el-menu-item {
  padding-left: 0;
  padding-right: 0;
}

/deep/ .el-badge__content.is-fixed {
  top: 25px;
}

/deep/ .el-badge__content {
  border-radius: 10px;
  height: 10px;
  line-height: 10px;
}

.item {
  margin-right: 10px;
}

.ms-task-error {
  color: #F56C6C;
}

.ms-task-success {
  color: #67C23A;
}
</style>
