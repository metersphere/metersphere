<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>
        <test-plan-load-case-list-header
            :condition="condition"
            :plan-id="planId"
            @refresh="initTable"
            @relevanceCase="$emit('relevanceCase')"/>
      </template>

      <ms-table
        v-loading="result.loading"
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
        @refresh="initTable"
        ref="table">
        <span v-for="(item) in fields" :key="item.key">
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="num"
            sortable
            min-width="80"
            label="ID"/>
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="caseName"
            :label="$t('commons.name')"
            min-width="120"
            sortable>
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

    <load-case-report :report-id="reportId" ref="loadCaseReport" @refresh="initTable"/>
  </div>
</template>

<script>
import TestPlanLoadCaseListHeader
  from "@/business/components/track/plan/view/comonents/load/TestPlanLoadCaseListHeader";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";
import LoadCaseReport from "@/business/components/track/plan/view/comonents/load/LoadCaseReport";
import {
  buildBatchParam,
  checkTableRowIsSelect, getCustomTableHeader, getCustomTableWidth
} from "@/common/js/tableUtils";
import {TEST_PLAN_LOAD_CASE} from "@/common/js/constants";
import {getCurrentUser} from "@/common/js/utils";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsPlanRunMode from "../../../common/PlanRunMode";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsCreateTimeColumn from "@/business/components/common/components/table/MsCreateTimeColumn";
import MsUpdateTimeColumn from "@/business/components/common/components/table/MsUpdateTimeColumn";

export default {
  name: "TestPlanLoadCaseList",
  components: {
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
    MsTableColumn,
    MsTable,
    HeaderLabelOperate,
    LoadCaseReport,
    TestPlanLoadCaseListHeader,
    MsTablePagination,
    MsPerformanceTestStatus,
    MsPlanRunMode
  },
  data() {
    return {
      type: TEST_PLAN_LOAD_CASE,
      tableLabel: [],
      condition: {},
      result: {},
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      status: 'default',
      screenHeight: 'calc(100vh - 250px)',//屏幕高度
      tableHeaderKey:"TEST_PLAN_LOAD_CASE",
      fields: getCustomTableHeader('TEST_PLAN_LOAD_CASE'),
      fieldsWidth: getCustomTableWidth('TEST_PLAN_LOAD_CASE'),
      operators: [
        {
          tip: this.$t('api_test.run'), icon: "el-icon-video-play",
          exec: this.run,
          class: 'run-button',
          isDisable: this.isReadOnly,
          permissions: ['PROJECT_TRACK_PLAN:READ+RUN']
        },
        {
          tip: this.$t('test_track.plan_view.cancel_relevance'), icon: "el-icon-unlock",
          exec: this.handleDelete,
          type: 'danger',
          isDisable: this.isReadOnly,
          permissions: ['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']
        }
      ],
      buttons: [
        {
          name: this.$t('test_track.plan.load_case.unlink_in_bulk'), handleClick: this.handleDeleteBatch, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE']
        },
        {
          name: this.$t('test_track.plan.load_case.batch_exec_cases'), handleClick: this.handleRunBatch, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_RUN']
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
      statusScheduler: null
    }
  },
  props: {
    selectProjectId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    planId: String,
    reviewId: String,
    clickType: String,
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
  methods: {
    orderBySelectRows(rows){
      let selectIds = Array.from(rows).map(row => row.id);
      let array = [];
      for(let i in this.tableData){
        if(selectIds.indexOf(this.tableData[i].id)!==-1){
          array.push(this.tableData[i]);
        }
      }
      return array;
    },
    runBatch(config){
      let rows = this.orderBySelectRows(this.$refs.table.selectRows);
      if(this.condition != null && this.condition.selectAll){
        let selectAllRowParams = buildBatchParam(this);
        selectAllRowParams.ids = Array.from(rows).map(row => row.id);
        this.$post('/test/plan/load/case/selectAllTableRows', selectAllRowParams, response => {
          let dataRows = response.data;
          let runArr = [];
          dataRows.forEach(loadCase => {
            runArr.push({
              id: loadCase.loadCaseId,
              testPlanLoadId: loadCase.id,
              triggerMode: 'MANUAL'
            });
          });
          let obj = {config: config, requests: runArr, userId: getCurrentUser().id};
          this._runBatch(obj);
          this.initTable();
          this.refreshStatus();
        });
      }else {
        let runArr = [];
        rows.forEach(loadCase => {
          runArr.push( {
            id: loadCase.loadCaseId,
            testPlanLoadId: loadCase.id,
            triggerMode: 'MANUAL'
          })
        });
        let obj = {config:config,requests:runArr,userId:getCurrentUser().id};
        this._runBatch(obj);
        this.initTable();
        this.refreshStatus();
      }
    },
    _runBatch(loadCases) {
      this.$post('/test/plan/load/case/run/batch',loadCases, response => {
      });
      this.$success(this.$t('test_track.plan.load_case.exec'));
      this.initTable();
      this.refreshStatus();
    },
    initTable() {
      this.autoCheckStatus();
      this.condition.testPlanId = this.planId;
      if (this.selectProjectId && this.selectProjectId !== 'root') {
        this.condition.projectId = this.selectProjectId;
      }
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = 'all';
      }
      if (this.planId) {
        this.condition.testPlanId = this.planId;
        this.$post("/test/plan/load/case/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          let data = response.data;
          let {itemCount, listObject} = data;
          this.total = itemCount;
          this.tableData = listObject;
          if (this.$refs.table) {
            setTimeout(this.$refs.table.doLayout, 200);
            this.$nextTick(() => {
              checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.table, this.$refs.table.selectRows);
            });
          }
        })
      }
      if (this.reviewId) {
        this.condition.testCaseReviewId = this.reviewId;
        this.$post("/test/review/load/case/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          let data = response.data;
          let {itemCount, listObject} = data;
          this.total = itemCount;
          this.tableData = listObject;
          if (this.$refs.table) {
            setTimeout(this.$refs.table.doLayout, 200);
            this.$nextTick(() => {
              checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.table, this.$refs.table.selectRows);
            });
          }
        })
      }
    },
    autoCheckStatus() {
      if (!this.planId) {
        return;
      }
      this.$post('/test/plan/autoCheck/' + this.planId, (response) => {
      });
    },
    refreshStatus() {
      this.refreshScheduler = setInterval(() => {
        // 如果有状态不是最终状态则定时查询
        let arr = this.tableData.filter(data => data.status !== 'Completed' && data.status !== 'Error' && data.status !== 'Saved');
        arr.length > 0 ? this.initTable() : clearInterval(this.refreshScheduler);
      }, 8000);
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
              this.result = this.$post('/test/plan/load/case/batch/delete', param, () => {
                this.clear();
                this.initTable();
                this.$success(this.$t('test_track.cancel_relevance_success'));
              });
            }
            if (this.reviewId) {
              this.result = this.$post('/test/review/load/case/batch/delete', ids, () => {
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
      this.$post('/test/plan/load/case/run', {
        id: loadCase.loadCaseId,
        testPlanLoadId: loadCase.id,
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
      })
    },
    updateStatus(loadCase, status) {
      this.$post('/test/plan/load/case/update', {id: loadCase.id, status: status}, () => {
        this.$post('/test/plan/edit/status/' + loadCase.testPlanId, {}, () => {
          this.initTable();
        });
      });
    },
    handleDelete(loadCase) {
      this.result = this.$get('/test/plan/load/case/delete/' + loadCase.id, () => {
        this.$success(this.$t('test_track.cancel_relevance_success'));
        this.$emit('refresh');
        this.initTable();
      });
    },
    getReport(data) {
      const {loadReportId} = data;
      this.reportId = loadReportId;
      this.loading = data.id;
      this.$post('/test/plan/load/case/report/exist', {
        testPlanLoadCaseId: data.id,
        reportId: loadReportId
      }, response => {
        let exist = response.data;
        this.loading = "";
        if (exist) {
          this.$refs.loadCaseReport.drawer = true;
        } else {
          this.$warning(this.$t('test_track.plan.load_case.report_not_found'));
        }
      })
    },
    cancelRefresh() {
      if (this.refreshScheduler) {
        clearInterval(this.refreshScheduler);
      }
    },
  },
  beforeDestroy() {
    this.cancelRefresh();
  },
}
</script>

<style scoped>
</style>
