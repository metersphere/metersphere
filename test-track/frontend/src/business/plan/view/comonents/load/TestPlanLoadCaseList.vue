<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="loading">
      <template v-slot:header>
        <test-plan-load-case-list-header
          :condition="condition"
          :plan-id="planId"
          :plan-status="planStatus"
          :isShowVersion="false"
          @refresh="search"
          @relevanceCase="$emit('relevanceCase')"/>
      </template>

      <ms-table
        v-loading="loading"
        :data="tableData"
        :condition="condition"
        :total="total"
        :page-size.sync="pageSize"
        :operators="operators"
        :screen-height="screenHeight"
        :batch-operators="buttons"
        @handlePageChange="initTable"
        :fields.sync="fields"
        :field-key="tableHeaderKey"
        :enable-order-drag="enableOrderDrag"
        row-key="id"
        :row-order-group-id="planId"
        :row-order-func="editTestPlanLoadCaseOrder"
        @order="initTable"
        @filter="search"
        ref="table">
        <span v-for="(item) in fields" :key="item.key">
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="num"
            sortable
            min-width="80"
            label="ID">
             <template v-slot:default="scope">
               <el-link @click="openById(scope.row)">
                  <span>
                    {{ scope.row.num }}
                  </span>
               </el-link>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="caseName"
            :label="$t('commons.name')"
            min-width="120"
            sortable>
          </ms-table-column>
          <ms-table-column
            v-if="versionEnable"
            prop="versionId"
            :field="item"
            :filters="versionFilters"
            :fields-width="fieldsWidth"
            :label="$t('commons.version')"
            min-width="120px">
           <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
           </template>
          </ms-table-column>
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="projectName"
            min-width="120"
            :label="$t('load_test.project_name')">
          </ms-table-column>
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="userName"
            min-width="100"
            :label="$t('load_test.user_name')">
          </ms-table-column>
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="status"
            :filters="statusFilters"
            :label="$t('commons.status')"
            min-width="80">
            <template v-slot:default="{row}">
              <ms-performance-test-status :row="row"/>
            </template>
          </ms-table-column>
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100"
            prop="caseStatus"
            :label="$t('test_track.plan.load_case.execution_status')">
            <template v-slot:default="{row}">
              <el-tag size="mini" type="danger" v-if="row.caseStatus === 'error'">
                {{ row.caseStatus }}
              </el-tag>
              <el-tag size="mini" type="success" v-else-if="row.caseStatus === 'success'">
                {{ row.caseStatus }}
              </el-tag>
              <el-tag size="mini" v-else-if="row.caseStatus === 'run'">
                {{ row.caseStatus }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </ms-table-column>
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.plan.load_case.report')"
            prop="loadReportId"
            min-width="80">
            <template v-slot:default="scope">
              <div v-loading="loading === scope.row.id">
                <el-link type="info" @click="getReport(scope.row)" v-if="scope.row.loadReportId">
                  {{ $t('test_track.plan.load_case.view_report') }}
                </el-link>
                <span v-else> - </span>
              </div>
            </template>
          </ms-table-column>

          <ms-update-time-column :field="item" :fields-width="fieldsWidth"/>
          <ms-create-time-column :field="item" :fields-width="fieldsWidth"/>

        </span>
      </ms-table>

      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>
    <ms-plan-run-mode @handleRunBatch="runBatch" ref="runMode"/>
    <el-drawer class="load-case-report-drawer"
               :visible.sync="showResponse"
               direction="ltr"
               @close="handleClose"
               size="80%">
      <micro-app :to="`/performance/report/view/${reportId}`" service="performance"/>
    </el-drawer>
    <load-case-config ref="loadCaseConfig"/>
    <ms-task-center ref="taskCenter" :show-menu="false"/>
  </div>
</template>

<script>
import TestPlanLoadCaseListHeader from "@/business/plan/view/comonents/load/TestPlanLoadCaseListHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsPerformanceTestStatus from "@/business/performance/PerformanceTestStatus";
import {buildBatchParam, getCustomTableHeader, getCustomTableWidth} from "metersphere-frontend/src/utils/tableUtils";
import {TEST_PLAN_LOAD_CASE} from "metersphere-frontend/src/utils/constants";
import {getCurrentProjectID, getCurrentUser, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import MsPlanRunMode from "../../../common/PlanRunMode";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import LoadCaseConfig from "@/business/plan/view/comonents/load/LoadCaseConfig";
import MsTaskCenter from "metersphere-frontend/src/components/task/TaskCenter";
import {editTestPlanLoadCaseOrder, testPlanAutoCheck, testPlanLoadCaseEditStatus} from "@/api/remote/plan/test-plan";
import {hasLicense} from "@/business/utils/sdk-utils";
import {
  testPlanLoadCaseBatchDelete,
  testPlanLoadCaseDelete, testPlanLoadCaseReportExist, testPlanLoadCaseRun,
  testPlanLoadCaseRunBatch,
  testPlanLoadCaseSelectAllTableRows, testPlanLoadCaseUpdate, testPlanLoadList
} from "@/api/remote/plan/test-plan-load-case";
import {getProjectVersions} from "@/api/project";
import MicroApp from "metersphere-frontend/src/components/MicroApp";


export default {
  name: "TestPlanLoadCaseList",
  components: {
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
    MsTableColumn,
    MsTable,
    HeaderLabelOperate,
    TestPlanLoadCaseListHeader,
    MsTablePagination,
    MsPerformanceTestStatus,
    MsPlanRunMode,
    LoadCaseConfig,
    MicroApp,
    MsTaskCenter
  },
  data() {
    return {
      type: TEST_PLAN_LOAD_CASE,
      tableLabel: [],
      condition: {},
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      status: 'default',
      screenHeight: 'calc(100vh - 250px)',//屏幕高度
      tableHeaderKey: "TEST_PLAN_LOAD_CASE",
      enableOrderDrag: true,
      fields: getCustomTableHeader('TEST_PLAN_LOAD_CASE'),
      fieldsWidth: getCustomTableWidth('TEST_PLAN_LOAD_CASE'),
      operators: [
        {
          tip: this.$t('api_test.run'), icon: "el-icon-video-play",
          exec: this.run,
          class: (this.planStatus === 'Archived' || this.isReadOnly) ? 'disable-run' : 'run-button',
          isDisable: this.isReadOnly || this.planStatus === 'Archived',
          permissions: ['PROJECT_TRACK_PLAN:READ+RUN']
        },
        {
          tip: '修改配置',
          icon: "el-icon-setting",
          exec: this.changeLoadConfig,
          type: 'success',
          isDisable: this.isReadOnly || this.planStatus === 'Archived',
        },
        {
          tip: this.$t('test_track.plan_view.cancel_relevance'), icon: "el-icon-unlock",
          exec: this.handleDelete,
          type: 'danger',
          isDisable: this.isReadOnly || this.planStatus === 'Archived',
          permissions: ['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']
        },
      ],
      buttons: [
        {
          name: this.$t('test_track.plan.load_case.unlink_in_bulk'),
          handleClick: this.handleDeleteBatch,
          isDisable: this.planStatus === 'Archived',
          permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE']
        },
        {
          name: this.$t('test_track.plan.load_case.batch_exec_cases'),
          handleClick: this.handleRunBatch,
          isDisable: this.planStatus === 'Archived',
          permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_RUN']
        }
      ],
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      reportId: '',
      loading: false,
      statusScheduler: null,
      versionFilters: [],
      showResponse:false
    }
  },
  props: {
    selectProjectId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    planId: String,
    planStatus: String,
    clickType: String,
    versionEnable: Boolean,
  },
  computed: {
    editTestPlanLoadCaseOrder() {
      return editTestPlanLoadCaseOrder;
    }
  },
  created() {
    this.initTable();
    this.refreshStatus();
  },
  watch: {
    selectProjectId() {
      this.condition.selectAll = false;
      this.initTable();
    },
    planId() {
      this.initTable();
    }
  },
  mounted() {
    this.getVersionOptions();
  },
  methods: {
    orderBySelectRows(rows) {
      let selectIds = Array.from(rows).map(row => row.id);
      let array = [];
      for (let i in this.tableData) {
        if (selectIds.indexOf(this.tableData[i].id) !== -1) {
          array.push(this.tableData[i]);
        }
      }
      return array;
    },
    runBatch(config) {
      let rows = this.orderBySelectRows(this.$refs.table.selectRows);
      if (this.condition != null && this.condition.selectAll) {
        let selectAllRowParams = buildBatchParam(this);
        selectAllRowParams.ids = Array.from(rows).map(row => row.id);
        testPlanLoadCaseSelectAllTableRows(selectAllRowParams)
          .then(response => {
            let dataRows = response.data;
            let runArr = [];
            dataRows.forEach(loadCase => {
              runArr.push({
                id: loadCase.loadCaseId,
                testPlanLoadId: loadCase.id,
                userId: getCurrentUserId(),
                projectId: getCurrentProjectID(),
                triggerMode: 'BATCH'
              });
            });
            let obj = {config: config, requests: runArr, userId: getCurrentUser().id};
            this._runBatch(obj);
            this.initTable();
            this.refreshStatus();
          });
      } else {
        let runArr = [];
        rows.forEach(loadCase => {
          runArr.push({
            id: loadCase.loadCaseId,
            testPlanLoadId: loadCase.id,
            userId: getCurrentUserId(),
            projectId: getCurrentProjectID(),
            triggerMode: 'MANUAL'
          })
        });
        let obj = {config: config, requests: runArr, userId: getCurrentUser().id};
        this._runBatch(obj);
        this.initTable();
        this.refreshStatus();
      }
    },
    _runBatch(loadCases) {
      testPlanLoadCaseRunBatch(loadCases);
      this.$success(this.$t('test_track.plan.load_case.exec'));
      this.$message(this.$t('commons.run_message'));
      this.$refs.taskCenter.open();
      this.initTable();
      this.refreshStatus();
    },
    search() {
      this.currentPage = 1;
      this.initTable();
    },
    initTable() {
      this.autoCheckStatus();
      this.condition.testPlanId = this.planId;
      if (this.selectProjectId && this.selectProjectId !== 'root') {
        this.condition.projectId = this.selectProjectId;
      }
      if (this.selectProjectId === 'root') {
        this.condition.projectId = null;
      }
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = 'all';
      }
      this.enableOrderDrag = (this.condition.orders && this.condition.orders.length) > 0 ? false : true;
      this.loading = true;
      if (this.planId) {
        this.condition.testPlanId = this.planId;
        testPlanLoadList({pageNum: this.currentPage, pageSize: this.pageSize}, this.condition)
          .then(response => {
            this.loading = false;
            let data = response.data;
            let {itemCount, listObject} = data;
            this.total = itemCount;
            this.tableData = listObject;
          });
      }
    },
    autoCheckStatus() {
      if (!this.planId) {
        return;
      }
      testPlanAutoCheck(this.planId);
    },
    refreshStatus() {
      // 暂时保留，试运行后再看是否还需要
      // this.refreshScheduler = setInterval(() => {
      //   // 如果有状态不是最终状态则定时查询
      //   let arr = this.tableData.filter(data => data.status !== 'Completed' && data.status !== 'Error' && data.status !== 'Saved');
      //   arr.length > 0 ? this.initTable() : clearInterval(this.refreshScheduler);
      // }, 8000);
      this.initTable();
    },
    handleClose() {
      this.showResponse = false;
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = this.$refs.table.selectIds;
            if (this.planId) {
              let param = buildBatchParam(this);
              param.ids = ids;
              this.loading = true;
              testPlanLoadCaseBatchDelete(param)
                .then(() => {
                  this.loading = false;
                  this.clear();
                  this.initTable();
                  this.$success(this.$t('test_track.cancel_relevance_success'));
                });
            }
          }
        }
      })
    },
    clear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
    },
    handleRunBatch() {
      this.$refs.runMode.open();
    },
    run(loadCase) {
      this._run(loadCase);
      this.refreshStatus();
    },
    _run(loadCase) {
      testPlanLoadCaseRun({
        id: loadCase.loadCaseId,
        testPlanLoadId: loadCase.id,
        userId: getCurrentUserId(),
        projectId: getCurrentProjectID(),
        triggerMode: 'MANUAL'
      }).then(() => {
        this.$notify.success({
          title: loadCase.caseName,
          message: this.$t('test_track.plan.load_case.exec').toString()
        });
        this.updateStatus(loadCase, 'run');
      }).catch(() => {
        this.updateStatus(loadCase, 'error');
        this.$notify.error({
          title: loadCase.caseName,
          message: this.$t('test_track.plan.load_case.error').toString()
        });
      });
    },
    updateStatus(loadCase, status) {
      testPlanLoadCaseUpdate({id: loadCase.id, status: status})
        .then(() => {
          testPlanLoadCaseEditStatus(loadCase.testPlanId)
            .then(() => {
              this.initTable();
            });
        });
    },
    handleDelete(loadCase) {
      this.loading = true;
      testPlanLoadCaseDelete(loadCase.id)
        .then(() => {
          this.$success(this.$t('test_track.cancel_relevance_success'));
          this.$emit('refresh');
          this.initTable();
        });
    },
    changeLoadConfig(loadCase) {
      const {loadCaseId, id} = loadCase;
      this.$refs.loadCaseConfig.open(loadCaseId, id);
    },
    getReport(data) {
      const {loadReportId} = data;
      this.reportId = loadReportId;
      testPlanLoadCaseReportExist({
        testPlanLoadCaseId: data.id,
        reportId: loadReportId
      }).then(response => {
        let exist = response.data;
        if (exist) {
          this.showResponse = true;
        } else {
          this.$message.warning(this.$t('test_track.plan.load_case.report_not_found'));
        }
      });
    },
    cancelRefresh() {
      if (this.refreshScheduler) {
        clearInterval(this.refreshScheduler);
      }
    },
    getVersionOptions() {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID())
          .then(response => {
            this.versionOptions = response.data;
            this.versionFilters = response.data.map(u => {
              return {text: u.name, value: u.id};
            });
          });
      }
    },
    openById(row) {
      let performanceResolve = this.$router.resolve({
        path: '/performance/test/edit/' + row.loadCaseId,
        query: {projectId: row.projectId}
      });
      window.open(performanceResolve.href, '_blank');
    },
  },
  beforeDestroy() {
    this.cancelRefresh();
  },
}
</script>

<style scoped>
</style>
