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
          <el-badge :value="runningTotal" class="item" type="primary" v-if="runningTotal > 0">
            <font-awesome-icon @click="showTaskCenter" class="icon global focusing" :icon="['fas', 'tasks']"
                               style="font-size: 18px"/>
          </el-badge>
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
        </el-form>
      </div>

      <div class="report-container" v-loading="result.loading">
        <div v-for="item in taskData" :key="item.id" style="margin-bottom: 5px">
          <el-card class="ms-card-task" @click.native="showReport(item,$event)">
            <span>{{ item.name }} </span><br/>
            <span>执行器：{{ item.actuator }} 由 {{ item.executor }} {{
                item.executionTime | timestampFormatDate
              }} {{ getMode(item.triggerMode) }}</span><br/>
            <el-row>
              <el-col :span="20">
                <el-progress :percentage="getPercentage(item.executionStatus)" :format="format"/>
              </el-col>
              <el-col :span="4">
                <span>{{ item.executionStatus }}</span>
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
import {getCurrentProjectID, hasPermissions} from "@/common/js/utils";
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
      visible: false,
      runMode: [
        {id: '', label: this.$t('api_test.definition.document.data_set.all')},
        {id: 'BATCH', label: this.$t('api_test.automation.batch_execute')},
        {id: 'SCHEDULE', label: this.$t('commons.trigger_mode.schedule')},
        {id: 'MANUAL', label: this.$t('commons.trigger_mode.manual')},
        {id: 'CASE', label: this.$t('commons.trigger_mode.case')},
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
    };
  },
  props: {
    color: String
  },
  created() {
    if (hasPermissions('PROJECT_API_SCENARIO:READ')) {
      this.getTaskRunning();
    }
  },
  methods: {
    format(item) {
      return '';
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
    },
    onClose(e) {
      if (e.code === 1005) {
        // 强制删除之后关闭socket，不用刷新report
        return;
      }
    },
    showTaskCenter() {
      this.getTaskRunning();
      this.init();
      this.taskVisible = true;
    },
    close() {
      this.visible = false;
    },
    open() {
      this.showTaskCenter();
    },
    getPercentage(status) {
      if (status) {
        status = status.toLowerCase();
        if (status === 'saved' || status === 'completed' || status === 'success' || status === 'error') {
          return 100;
        }
      }
      return 60;
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
      if (mode === 'CASE') {
        return this.$t('commons.trigger_mode.case');
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
</style>
