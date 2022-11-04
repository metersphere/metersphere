<template>
  <div>
    <div class="ms-body-container">
      <div v-for="item in taskData" :key="item.id" style="margin-bottom: 5px;">
        <el-card class="ms-card-task" @click.native="showReport(item)">
          <span>
            {{ getModeName(item.executionModule) }} :
            <el-link type="primary" class="ms-task-name-width"> {{ item.name }} </el-link>
          </span>
          <el-button size="mini" class="ms-task-stop"
                     @click.stop @click="stop(item)"
                     v-if="showStop(item.executionStatus)">
            {{ $t('report.stop_btn') }}
          </el-button>
          <br/>
          <span>
            {{ $t('commons.actuator') }}：
            {{ item.actuator }}
            {{ $t('commons.from') }}
            {{ item.executor }}
            {{ item.executionTime | datetimeFormat }}
            {{ getMode(item.triggerMode) }}
          </span>
          <br/>
          <el-row>
            <el-col :span="20">
              <el-progress :percentage="getPercentage(item.executionStatus)" :format="format"/>
            </el-col>
            <el-col :span="4">
              <ms-task-report-status :status="item.executionStatus"/>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </div>
    <div class="report-bottom">
      <ms-table-pagination
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"
        :change="init"
        v-if="showType !== 'SCENARIO' && showType !== 'CASE'" small/>
      <span v-else> {{ $t('commons.task_center_remark') }}</span>
    </div>
  </div>
</template>

<script>
import {getCurrentProjectID, getCurrentUser} from "../../utils/token";
import {stopBatchTask, stopTask} from "../../api/task";

export default {
  name: "TaskCenterItem",
  components: {
    MsTaskReportStatus: () => import("./TaskReportStatus"),
    MsTablePagination: () => import("./TaskPagination"),
  },
  inject: [
    'reload'
  ],
  data() {
    return {
      pageSize: 10,
      currentPage: 1,
      runMode: [
        {id: '', label: this.$t('api_test.definition.document.data_set.all')},
        {id: 'BATCH', label: this.$t('api_test.automation.batch_execute')},
        {id: 'SCHEDULE', label: this.$t('commons.trigger_mode.schedule')},
        {id: 'MANUAL', label: this.$t('commons.trigger_mode.manual')},
        {id: 'API', label: this.$t('commons.trigger_mode.api')}
      ],
      runStatus: [
        {id: '', label: this.$t('api_test.definition.document.data_set.all')},
        {id: 'STARTING', label: 'Starting'},
        {id: 'PENDING', label: 'Pending'},
        {id: 'RUNNING', label: 'Running'},
        {id: 'RERUNNING', label: 'Rerunning'},
        {id: 'REPORTING', label: 'Reporting'},
        {id: 'SUCCESS', label: 'Success'},
        {id: "FAKE_ERROR", label: 'FakeError'},
        {id: 'ERROR', label: 'Error'},
        {id: 'STOPPED', label: 'Stopped'},
        {id: 'COMPLETED', label: 'Completed'},
      ],
    }
  },
  props: {
    total: Number,
    taskData: Array,
    isDebugHistory: Boolean,
    showType: String,
    maintainerOptions: Array
  },
  methods: {
    format(item) {
      return '';
    },
    stop(row) {
      if (row) {
        let request = {type: row.executionModule, reportId: row.id};
        stopTask(request).then(response => {
          this.$success(this.$t('report.test_stop_success'));
          this.init();
        });
      } else {
        let array = [];
        array.push({type: 'API', projectId: getCurrentProjectID(), userId: getCurrentUser().id});
        array.push({type: 'SCENARIO', projectId: getCurrentProjectID(), userId: getCurrentUser().id});
        array.push({type: 'PERFORMANCE', projectId: getCurrentProjectID(), userId: getCurrentUser().id});
        array.push({type: 'UI_SCENARIO', projectId: getCurrentProjectID(), userId: getCurrentUser().id});
        stopBatchTask(array).then(response => {
          this.$success(this.$t('report.test_stop_success'));
          this.init();
        });
      }
    },
    getPercentage(status) {
      if (status) {
        status = status.toLowerCase();
        if (status === "pending" || status === 'stopped') {
          return 0;
        }
        if (status === 'saved' || status === 'completed' || status === 'success' || status === 'error' || status ===
          'pending' || status === 'fake_error') {
          return 100;
        }
      }
      return 60;
    },
    showStop(status) {
      if (status) {
        status = status.toLowerCase();
        if (status === "stopped" || status === 'saved' || status === 'completed' || status === 'success' || status ===
          'error' || status === 'pending' || status === 'fake_error') {
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
        case "UI_SCENARIO":
          return this.$t("test_track.ui_scenario_test_case");
      }
    },
    packUp() {
      this.$emit("packUp")
    },
    showReport(row) {
      this.$emit("showReport", row)
    },
    getMode(mode) {
      if (mode) {
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
        if (mode.startsWith('JENKINS')) {
          return this.$t('commons.trigger_mode.api');
        }
      }
      return mode;
    },
    init() {
      if (this.showType === "CASE" || this.showType === "SCENARIO") {
        return;
      }
      this.$emit("nextPage", this.currentPage, this.pageSize);
    },
  }
};
</script>


<style scoped>
.ms-body-container {
  height: calc(100vh - 290px);
  overflow-y: auto;
}

.ms-card-task :deep(.el-card__body) {
  padding: 10px;
}

.ms-card-task:hover {
  cursor: pointer;
  border-color: #783887;
}

:deep(.el-progress-bar) {
  padding-right: 20px;
}

.ms-task-stop {
  color: #F56C6C;
  float: right;
  margin-right: 20px;
}


.ms-task-stop {
  color: #909399;
}

.ms-task-name-width {
  display: inline-block;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 300px;
}

.ms-el-form-item :deep(.el-form-item) {
  margin-bottom: 6px;
}

.ms-task-opt-btn i {
  margin-left: -2px;
}

.ms-task-opt-btn:hover i {
  margin-left: 0;
  color: white;
}

.report-bottom {
  margin-top: 10px;
}
</style>
