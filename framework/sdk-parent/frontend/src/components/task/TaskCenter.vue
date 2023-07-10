<template>
  <div v-permission="['PROJECT_API_SCENARIO:READ', 'WORKSPACE_USER:READ']">
    <div class="ms-header-menu align-right">
      <el-tooltip effect="light" v-if="showMenu">
        <template v-slot:content>
          <span>{{ $t("commons.task_center") }}</span>
        </template>
        <div @click="showTaskCenter" v-if="runningTotal > 0">
          <el-badge :value="runningTotal" class="item" type="primary">
            <font-awesome-icon
              class="icon global focusing"
              :icon="['fas', 'tasks']"
            />
          </el-badge>
        </div>
        <font-awesome-icon
          @click="open('API')"
          class="icon global focusing"
          :icon="['fas', 'tasks']"
          v-else
        />
      </el-tooltip>
    </div>
    <el-drawer
      :visible.sync="taskVisible"
      :destroy-on-close="true"
      direction="rtl"
      :withHeader="true"
      :modal="false"
      :title="$t('commons.task_center')"
      :size="size.toString()"
      custom-class="ms-drawer-task"
    >
      <el-card
        style="float: left; margin-top: 0px"
        :style="{ width: size - 600 + 'px' }"
        v-if="size > 600"
        class="ms-task-container"
      >
        <div class="ms-task-opt-btn" @click="packUp">
          {{ $t("commons.task_close") }}
        </div>
        <micro-app
          v-if="isMicroAppInited"
          :to="microAppConfig.url"
          :service="microAppConfig.service"
        />
      </el-card>
      <el-card style="width: 550px; float: right">
        <div style="color: #2b415c; margin: 0px">
          <el-form label-width="95px" class="ms-el-form-item">
            <el-row>
              <el-col :span="12">
                <el-form-item
                  :label="$t('test_track.report.list.trigger_mode')"
                  prop="runMode"
                >
                  <el-select
                    size="mini"
                    style="margin-right: 10px"
                    v-model="condition.triggerMode"
                    @change="changeInit"
                    :disabled="isDebugHistory"
                  >
                    <el-option
                      v-for="item in runMode"
                      :key="item.id"
                      :value="item.id"
                      :label="item.label"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('commons.status')" prop="status">
                  <el-select
                    size="mini"
                    style="margin-right: 10px"
                    v-model="condition.executionStatus"
                    @change="init(true)"
                    :disabled="isDebugHistory"
                  >
                    <el-option
                      v-for="item in runStatus"
                      :key="item.id"
                      :value="item.id"
                      :label="item.label"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <el-form-item :label="$t('commons.executor')" prop="status">
                  <el-select
                    v-model="condition.executor"
                    :placeholder="$t('commons.executor')"
                    filterable
                    size="mini"
                    style="margin-right: 10px"
                    @change="changeInit"
                    :disabled="isDebugHistory"
                  >
                    <el-option
                      v-for="item in maintainerOptions"
                      :key="item.id"
                      :label="item.id + ' (' + item.name + ')'"
                      :value="item.id"
                    >
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-button
                  size="mini"
                  class="ms-task-stop"
                  @click="stop(null)"
                  :disabled="isDebugHistory"
                >
                  {{ $t("report.stop_btn_all") }}
                </el-button>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <el-divider direction="horizontal" style="width: 100%" />
        <el-tabs
          v-model="activeName"
          @tab-click="init(true)"
          v-loading="loading"
        >
          <el-tab-pane
            :key="tab.id"
            :name="tab.id"
            :label="tab.label"
            v-for="tab in tabs"
            :disabled="isDebugHistory"
          >
            <span slot="label">
              <el-badge
                class="ms-badge-item"
                v-if="showBadge(tab.id) > 0"
                :value="showBadge(tab.id)"
              >
                {{ tab.label }}
              </el-badge>
              <span style="font-size: 13px" v-else>{{ tab.label }}</span>
            </span>
            <task-center-item
              :task-data="taskData"
              :is-debug-history="isDebugHistory"
              :show-type="showType"
              :total="total"
              :maintainer-options="maintainerOptions"
              @packUp="packUp"
              @nextPage="nextPage"
              @showReport="showReport"
            />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-drawer>
  </div>
</template>

<script>
import MsDrawer from "../MsDrawer";
import { getCurrentProjectID, getCurrentUser } from "../../utils/token";
import { hasLicense, hasPermissions } from "../../utils/permission";
import { getProjectUsers } from "../../api/user";
import {
  getCaseData,
  getScenarioData,
  getTaskList,
  getTaskSocket,
  stopBatchTask,
  stopTask,
} from "../../api/task";
import MicroApp from "../../components/MicroApp";
import { prefetchApps } from "qiankun";
import TaskCenterItem from "./TaskCenterItem";
import { getUUID } from "../../utils";

export default {
  name: "MsTaskCenter",
  components: {
    MicroApp,
    MsDrawer,
    TaskCenterItem,
    MsTaskReportStatus: () => import("./TaskReportStatus"),
    MsTablePagination: () => import("./TaskPagination"),
  },
  inject: ["reload"],
  data() {
    return {
      runningData: {},
      pageSize: 10,
      currentPage: 1,
      total: 0,
      activeName: "API",
      runningTotal: 0,
      taskVisible: false,
      result: {},
      loading: false,
      taskData: [],
      response: {},
      visible: false,
      showType: "",
      runMode: [
        { id: "", label: this.$t("api_test.definition.document.data_set.all") },
        { id: "BATCH", label: this.$t("api_test.automation.batch_execute") },
        { id: "SCHEDULE", label: this.$t("commons.trigger_mode.schedule") },
        { id: "MANUAL", label: this.$t("commons.trigger_mode.manual") },
        { id: "API", label: this.$t("commons.trigger_mode.api") },
      ],
      tabs: [
        { id: "API", label: this.$t("task.api_title") },
        { id: "SCENARIO", label: this.$t("task.scenario_title") },
        { id: "PERF", label: this.$t("task.perf_title") },
      ],
      runStatus: [
        { id: "", label: this.$t("api_test.definition.document.data_set.all") },
        { id: "STARTING", label: "Starting" },
        { id: "PENDING", label: "Pending" },
        { id: "RUNNING", label: "Running" },
        { id: "RERUNNING", label: "Rerunning" },
        { id: "REPORTING", label: "Reporting" },
        { id: "SUCCESS", label: "Success" },
        { id: "FAKE_ERROR", label: "FakeError" },
        { id: "ERROR", label: "Error" },
        { id: "STOPPED", label: "Stopped" },
        { id: "COMPLETED", label: "Completed" },
      ],
      condition: { triggerMode: "", executionStatus: "" },
      maintainerOptions: [],
      websocket: Object,
      size: 600,
      reportId: "",
      executionModule: "",
      reportType: "",
      isDebugHistory: false,
      isMicroAppInited: false,
      microAppConfig: {
        url: "",
        service: "",
      },
    };
  },
  props: {
    showMenu: {
      type: Boolean,
      default: true,
    },
  },
  computed: {
    disabled() {
      return this.loading;
    },
  },

  created() {
    if (hasPermissions("PROJECT_API_SCENARIO:READ")) {
      this.condition.executor = getCurrentUser().id;
    }
    if (hasLicense()) {
      this.tabs.push({ id: "UI", label: this.$t("task.ui_title") });
    }
    this.prefetchApps();
  },
  watch: {
    taskVisible(v) {
      if (!v) {
        this.close();
      }
    },
    reportId(v) {
      // 接口用例结果
      if (
        this.executionModule === "API" &&
        this.reportType !== "API_INTEGRATED"
      ) {
        this.microAppConfig = {
          url: `/definition/report/view/${this.reportId}`,
          service: "api",
        };
      } else if (
        // 接口场景报告
        this.executionModule === "SCENARIO" ||
        this.reportType === "API_INTEGRATED"
      ) {
        this.microAppConfig = {
          url: `/automation/report/view/${this.reportId}`,
          service: "api",
        };
      } else if (this.executionModule === "PERFORMANCE") {
        // 性能测试报告
        this.microAppConfig = {
          url: `/performance/report/view/${this.reportId}`,
          service: "performance",
        };
      } else if (this.executionModule === "UI_SCENARIO") {
        // UI测试报告
        this.microAppConfig = {
          url: `/ui/report/view/${this.reportId}?showCancelButton=false`,
          service: "ui",
        };
      }
      this.isMicroAppInited = true;
    },
  },
  methods: {
    showBadge(executionModule) {
      switch (executionModule) {
        case "SCENARIO":
          return this.runningData.scenarioTotal;
        case "PERF":
          return this.runningData.perfTotal;
        case "API":
          return this.runningData.apiTotal;
        case "UI":
          return this.runningData.uiTotal;
      }
    },
    format(item) {
      return "";
    },
    packUp() {
      this.size = 600;
    },
    stop(row) {
      if (row) {
        let request = { type: row.executionModule, reportId: row.id };
        stopTask(request).then((response) => {
          this.$success(this.$t("report.test_stop_success"));
          this.init(true);
        });
      } else {
        let array = [];
        array.push({
          type: "API",
          projectId: getCurrentProjectID(),
          userId: getCurrentUser().id,
        });
        array.push({
          type: "SCENARIO",
          projectId: getCurrentProjectID(),
          userId: getCurrentUser().id,
        });
        array.push({
          type: "PERFORMANCE",
          projectId: getCurrentProjectID(),
          userId: getCurrentUser().id,
        });
        array.push({
          type: "UI_SCENARIO",
          projectId: getCurrentProjectID(),
          userId: getCurrentUser().id,
        });
        stopBatchTask(array).then((response) => {
          this.$success(this.$t("report.test_stop_success"));
          this.init(true);
        });
      }
    },
    getMaintainerOptions() {
      getProjectUsers().then((response) => {
        this.maintainerOptions = response.data;
        this.condition.executor = getCurrentUser().id;
      });
    },
    initWebSocket() {
      let isLicense = hasLicense();
      this.websocket = getTaskSocket(
        this.condition.executor,
        this.condition.triggerMode,
        isLicense || false
      );
      this.websocket.onmessage = this.onMessage;
      this.websocket.onopen = this.onOpen;
      this.websocket.onerror = this.onError;
      this.websocket.onclose = this.onClose;
    },
    onOpen() {},
    onError(e) {},
    onMessage(e) {
      this.loading = false;
      this.runningData = JSON.parse(e.data);
      this.runningTotal = this.runningData.total;
      this.init(false);
    },
    onClose(e) {},
    listenScreenChange() {
      if (this.size != 600) {
        this.size = document.body.clientWidth;
      }
    },
    showTaskCenter() {
      this.getTaskRunning();
      this.getMaintainerOptions();
      window.addEventListener("resize", this.listenScreenChange, false);
      this.taskVisible = true;
    },
    close() {
      this.visible = false;
      this.size = 600;
      window.removeEventListener("resize", this.listenScreenChange);
      this.showType = "";
      if (this.websocket && this.websocket.close instanceof Function) {
        this.websocket.close();
      }
    },
    open(activeName) {
      if (activeName) {
        this.activeName = activeName;
      }
      this.init(true);
      this.taskVisible = true;
      setTimeout(this.showTaskCenter, 2000);
    },
    getPercentage(status) {
      if (status) {
        status = status.toLowerCase();
        if (status === "pending" || status === "stopped") {
          return 0;
        }
        let statusArray = [
          "saved",
          "completed",
          "success",
          "error",
          "pending",
          "fake_error",
        ];
        if (statusArray.includes(status)) {
          return 100;
        }
      }
      return 60;
    },
    showStop(status) {
      if (status) {
        status = status.toLowerCase();
        let statusArray = [
          "saved",
          "completed",
          "success",
          "error",
          "pending",
          "fake_error",
          "stopped",
        ];
        if (statusArray.includes(status)) {
          return false;
        }
      }
      return true;
    },
    getModeName(executionModule) {
      switch (executionModule) {
        case "SCENARIO":
          return this.$t("test_track.scenario_test_case");
        case "PERFORMANCE":
          return this.$t("test_track.performance_test_case");
        case "API":
          return this.$t("test_track.api_test_case");
        case "UI_SCENARIO":
          return this.$t("test_track.ui_scenario_test_case");
      }
    },
    showReport(row) {
      if (this.size > 600 && this.reportId === row.id) {
        this.packUp();
        return;
      }
      let status = row.executionStatus;
      if (status) {
        status = row.executionStatus.toLowerCase();
        let statusArray = [
          "saved",
          "completed",
          "success",
          "error",
          "pending",
          "fake_error",
          "stopped",
        ];
        if (statusArray.includes(status)) {
          this.executionModule = null;
          this.$nextTick(() => {
            this.size = window.innerWidth;
            this.executionModule = row.executionModule;
            this.reportType = row.reportType;
            if (
              row.executionModule === "SCENARIO" ||
              row.reportType === "API_INTEGRATED"
            ) {
              this.reportId = getUUID() + "[TEST-PLAN-REDIRECT]" + row.id;
            } else {
              this.reportId = row.id;
            }
          });
        } else {
          this.$warning(this.$t("commons.run_warning"));
        }
      }
    },
    getMode(mode) {
      if (mode === "MANUAL") {
        return this.$t("commons.trigger_mode.manual");
      }
      if (mode === "SCHEDULE") {
        return this.$t("commons.trigger_mode.schedule");
      }
      if (mode === "TEST_PLAN_SCHEDULE") {
        return this.$t("commons.trigger_mode.schedule");
      }
      if (mode === "API") {
        return this.$t("commons.trigger_mode.api");
      }
      if (mode === "BATCH") {
        return this.$t("api_test.automation.batch_execute");
      }
      if (mode.startsWith("JENKINS")) {
        return this.$t("commons.trigger_mode.api");
      }
      return mode;
    },
    getTaskRunning() {
      this.initWebSocket();
    },
    nextPage(currentPage, pageSize) {
      this.currentPage = currentPage;
      this.pageSize = pageSize;
      this.init(true);
    },
    changeInit() {
      if (this.websocket && this.websocket.close instanceof Function) {
        this.websocket.close();
      }
      this.getTaskRunning();
      this.init(true);
    },
    init(loading) {
      if (this.showType === "CASE" || this.showType === "SCENARIO") {
        return;
      }
      this.condition.projectId = getCurrentProjectID();
      this.condition.userId = getCurrentUser().id;
      this.condition.activeName = this.activeName;
      this.loading = loading;
      this.result = getTaskList(
        this.condition,
        this.currentPage,
        this.pageSize
      ).then((response) => {
        this.total = response.data.itemCount;
        this.taskData = response.data.listObject;
        this.loading = false;
      });
    },
    setActiveName() {
      if (this.runningData.apiTotal > 0) {
        this.activeName = "API";
      } else if (this.runningData.scenarioTotal > 0) {
        this.activeName = "SCENARIO";
      } else if (this.runningData.perfTotal > 0) {
        this.activeName = "PERF";
      } else if (this.runningData.uiTotal > 0) {
        this.activeName = "UI";
      }
    },
    initCaseHistory(id) {
      getCaseData(id).then((response) => {
        this.taskData = response.data;
      });
    },
    openHistory(id) {
      window.addEventListener("resize", this.listenScreenChange, false);
      this.activeName = "API";
      this.initCaseHistory(id);
      this.taskVisible = true;
      this.isDebugHistory = true;
      this.condition.triggerMode = "MANUAL";
      this.showType = "CASE";
    },
    openScenarioHistory(id) {
      window.addEventListener("resize", this.listenScreenChange, false);
      this.activeName = "SCENARIO";
      getScenarioData(id).then((response) => {
        this.taskData = response.data;
      });
      this.showType = "SCENARIO";
      this.isDebugHistory = true;
      this.condition.triggerMode = "MANUAL";
      this.taskVisible = true;
    },
    prefetchApps() {
      const microPorts = JSON.parse(sessionStorage.getItem("micro_ports"));
      let apps = [];
      if (microPorts.api) {
        apps.push({
          name: "api",
          entry: "//127.0.0.1:" + (microPorts.api - 4000),
        });
      }
      if (microPorts.performance) {
        apps.push({
          name: "performance",
          entry: "//127.0.0.1:" + (microPorts.performance - 4000),
        });
      }
      if (microPorts.ui) {
        apps.push({
          name: "ui",
          entry: "//127.0.0.1:" + (microPorts.ui - 4000),
        });
      }

      if (process.env.NODE_ENV !== "development") {
        // 替换成后端的端口
        apps.forEach((app) => {
          app.entry = app.entry.replace(
            /127\.0\.0\.1:\d+/g,
            window.location.host + "/" + app.name
          );
        });
      }
      prefetchApps(apps);
    },
  },
};
</script>

<style scoped>
.align-right {
  float: right;
}

.icon {
  width: 24px;
}

:deep(.el-drawer__header) {
  font-size: 14px;
  color: #0a0a0a;
  border-bottom: 1px solid #e6e6e6;
  background-color: #fff;
  margin-bottom: 0px;
  padding-top: 6px;
  padding-bottom: 6px;
}

.global {
  color: rgb(96, 98, 102);
  font-size: 14px;
}

.ms-header-menu {
  padding-top: 12px;
  width: 24px;
}

.ms-header-menu:hover {
  cursor: pointer;
  border-color: var(--color);
}

:deep(.el-progress-bar) {
  padding-right: 20px;
}

.item {
  margin-right: 10px;
}

.ms-task-stop {
  color: #f56c6c;
  float: right;
  margin-right: 20px;
}

.ms-task-stop {
  color: #909399;
}

.ms-el-form-item :deep(.el-form-item) {
  margin-bottom: 0px;
}

.ms-task-opt-btn {
  position: fixed;
  right: calc(98% - var(--asideWidth));
  top: 50%;
  z-index: 5;
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

:deep(.report-container) {
  height: calc(100vh - 155px) !important;
  min-height: 600px;
  overflow-y: auto;
}

.ms-task-container :deep(.el-container) {
  height: calc(100vh - 100px) !important;
}

.ms-badge-item {
  margin-top: 0px;
  margin-right: 0px;
  font-size: 13px;
}

:deep(.el-tabs__nav-wrap) {
  width: calc(100%);
}

:deep(.el-badge__content.is-fixed) {
  transform: translateY(-10%) translateX(100%);
}
</style>
