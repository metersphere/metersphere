<template>
  <div v-permission="['PROJECT_API_SCENARIO:READ']">
    <el-menu v-if="showMenu"
             :unique-opened="true"
             class="header-user-menu align-right header-top-menu"
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
               :withHeader="true" :modal="false" :title="$t('commons.task_center')" :size="size.toString()"
               custom-class="ms-drawer-task">
      <el-card style="float: left;width: 850px" v-if="size > 550 ">

        <div class="ms-task-opt-btn" @click="packUp">收起</div>
        <!-- 接口用例结果 -->
        <ms-request-result-tail :response="response" ref="debugResult" v-if="reportType === 'API'"/>

        <ms-api-report-detail :reportId="reportId" v-if="reportType === 'SCENARIO'"/>

        <performance-report-view :perReportId="reportId" v-if="reportType === 'PERFORMANCE'"/>

      </el-card>
      <el-card style="width: 550px;float: right">
        <div style="color: #2B415C;margin: 0px 20px 0px;">
          <el-form label-width="68px" class="ms-el-form-item">
            <el-row>
              <el-col :span="12">
                <el-form-item :label="$t('test_track.report.list.trigger_mode')" prop="runMode">
                  <el-select size="small" style="margin-right: 10px" v-model="condition.triggerMode" @change="init">
                    <el-option v-for="item in runMode" :key="item.id" :value="item.id" :label="item.label"/>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('commons.status')" prop="status">
                  <el-select size="small" style="margin-right: 10px" v-model="condition.executionStatus" @change="init">
                    <el-option v-for="item in runStatus" :key="item.id" :value="item.id" :label="item.label"/>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
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
              <el-col :span="12">
                <el-button size="small" class="ms-task-stop" @click="stop(null)">
                  {{ $t('report.stop_btn_all') }}
                </el-button>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div class="report-container">
          <div v-for="item in taskData" :key="item.id" style="margin-bottom: 5px">
            <el-card class="ms-card-task" @click.native="showReport(item)">
            <span class="ms-task-name-width"><el-link type="primary">
              {{ getModeName(item.executionModule) }} </el-link>: {{ item.name }} </span>
              <el-button size="mini" class="ms-task-stop" @click.stop @click="stop(item)"
                         v-if="showStop(item.executionStatus)">
                {{ $t('report.stop_btn') }}
              </el-button>
              <br/>
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
                  <span v-if="item.executionStatus && item.executionStatus.toLowerCase() === 'error'"
                        class="ms-task-error">
                     error
                  </span>
                  <span v-else-if="item.executionStatus && item.executionStatus.toLowerCase() === 'success'"
                        class="ms-task-success">
                     success
                </span>
                  <span v-else-if="item.executionStatus && item.executionStatus.toLowerCase() === 'stop'">
                    stopped
                  </span>
                  <span v-else>{{
                      item.executionStatus ? item.executionStatus.toLowerCase() : item.executionStatus
                    }}</span>
                </el-col>
              </el-row>
            </el-card>
          </div>
        </div>
      </el-card>
    </el-drawer>
  </div>
</template>

<script>
import MsDrawer from "../common/components/MsDrawer";
import {getCurrentProjectID, getCurrentUser, hasPermissions} from "@/common/js/utils";

export default {
  name: "MsTaskCenter",
  components: {
    MsDrawer,
    MsRequestResultTail: () => import("../../components/api/definition/components/response/RequestResultTail"),
    MsApiReportDetail: () => import("../../components/api/automation/report/ApiReportDetail"),
    PerformanceReportView: () => import("../../components/performance/report/PerformanceReportView")
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
      showType: "",
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
      websocket: Object,
      size: 550,
      reportId: "",
      reportType: "",
    };
  },
  props: {
    color: String,
    showMenu: {
      type: Boolean,
      default: true
    }
  },
  created() {
    if (hasPermissions('PROJECT_API_SCENARIO:READ')) {
      this.condition.executor = getCurrentUser().id;
    }
  },
  watch: {
    taskVisible(v) {
      if (!v) {
        this.close();
      }
    }
  },
  methods: {
    format(item) {
      return '';
    },
    packUp() {
      this.size = 550;
    },
    stop(row) {
      let array = [];
      if (row) {
        let request = {type: row.executionModule, reportId: row.id};
        array = [request];
      } else {
        this.taskData.forEach(item => {
          if (this.showStop(item.executionStatus)) {
            let request = {type: item.executionModule, reportId: item.id};
            array.push(request);
          }
        });
      }
      if (array.length === 0) {
        this.$warning("没有需要停止的任务");
        return;
      }
      this.$post('/api/automation/stop/batch', array, response => {
        this.$success(this.$t('report.test_stop_success'));
        this.init();
      });
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
      if (this.taskVisible && this.initEnd) {
        setTimeout(() => {
          this.initEnd = false;
          this.init();
        }, 3000);
      }
    },
    onClose(e) {
    },
    showTaskCenter() {
      this.getTaskRunning();
      this.getMaintainerOptions();
      this.init();
      this.taskVisible = true;
    },
    close() {
      this.visible = false;
      this.size = 550;
      this.showType = "";
      if (this.websocket && this.websocket.close instanceof Function) {
        this.websocket.close();
      }
    },
    open() {
      this.showTaskCenter();
      this.initIndex = 0;
    },
    getPercentage(status) {
      if (status) {
        status = status.toLowerCase();
        if (status === "waiting" || status === 'stop') {
          return 0;
        }
        if (status === 'saved' || status === 'completed' || status === 'success' || status === 'error') {
          return 100;
        }
      }
      return 60;
    },
    showStop(status) {
      if (status) {
        status = status.toLowerCase();
        if (status === "stop" || status === 'saved' || status === 'completed' || status === 'success' || status === 'error') {
          return false;
        }
      }
      return true;
    },
    getModeName(executionModule) {
      switch (executionModule) {
        case "SCENARIO":
          return this.$t('test_track.scenario_test_case');
        case "PERFORMANCE":
          return this.$t('test_track.performance_test_case');
        case "API":
          return this.$t('test_track.api_test_case');
      }
    },
    showReport(row) {
      if (this.size > 550 && this.reportId === row.id) {
        this.packUp();
        return;
      }
      let status = row.executionStatus;
      if (status) {
        status = row.executionStatus.toLowerCase();
        if (status === 'saved' || status === 'completed' || status === 'success' || status === 'error') {
          this.size = 1400;
          this.reportId = row.id;
          this.reportType = row.executionModule;
          if (row.executionModule === "API") {
            this.getExecResult(row.id);
          }
        } else if (status === 'stop') {
          this.$warning("当前任务已停止，无法查看报告");
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
    calculationRunningTotal() {
      if (this.taskData) {
        let total = 0;
        this.taskData.forEach(item => {
          if (this.getPercentage(item.executionStatus) !== 100 && this.getPercentage(item.executionStatus) !== 0) {
            total++;
          }
        });
        this.runningTotal = total;
      }
    },
    init() {
      if (this.showType === "CASE" || this.showType === "SCENARIO") {
        return;
      }
      this.result.loading = true;
      this.condition.projectId = getCurrentProjectID();
      this.result = this.$post('/task/center/list', this.condition, response => {
        this.taskData = response.data;
        this.calculationRunningTotal();
        this.initEnd = true;
      });
    },
    initCaseHistory(id) {
      this.result = this.$get('/task/center/case/' + id, response => {
        this.taskData = response.data;
      });
    },
    openHistory(id) {
      this.initCaseHistory(id);
      this.taskVisible = true;
      this.showType = "CASE";
    },
    openScenarioHistory(id) {
      this.result = this.$get('/task/center/scenario/' + id, response => {
        this.taskData = response.data;
      });
      this.showType = "SCENARIO";
      this.taskVisible = true;
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
  min-height: 550px;
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

.ms-task-stop {
  color: #F56C6C;
  float: right;
  margin-right: 20px;
}

.ms-task-success {
  color: #67C23A;
}

.ms-task-name-width {
  display: inline-block;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 360px;
}

.ms-el-form-item >>> .el-form-item {
  margin-bottom: 6px;
}

.ms-task-opt-btn {
  position: fixed;
  right: 1372px;
  top: 50%;
  z-index: 1;
  width: 20px;
  height: 60px;
  padding: 3px;
  line-height: 30px;
  border-radius: 0 15px 15px 0;
  background-color: #783887;
  color: white;
  display: inline-block;
  cursor: pointer;
  opacity: 0.5;
  font-size: 10px;
  font-weight: bold;
  margin-left: 1px;
}

.ms-task-opt-btn i {
  margin-left: -2px;
}

.ms-task-opt-btn:hover {
  opacity: 0.8;
}

.ms-task-opt-btn:hover i {
  margin-left: 0;
  color: white;
}
</style>
