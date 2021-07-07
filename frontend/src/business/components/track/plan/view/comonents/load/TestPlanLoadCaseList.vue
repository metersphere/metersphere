<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>
        <test-plan-load-case-list-header
            :condition="condition"
            :plan-id="planId"
            @refresh="initTable"
            @relevanceCase="$emit('relevanceCase')"
        />
      </template>

      <el-table v-loading="result.loading" ref="table"
                border
                :data="tableData" row-key="id" class="test-content adjust-table ms-select-all-fixed"
                @select-all="handleSelectAll"
                @filter-change="filter"
                @sort-change="sort"
                @select="handleSelectionChange" :height="screenHeight">
        <el-table-column width="50" type="selection"/>
        <ms-table-header-select-popover v-show="total>0"
                                        :page-size="pageSize > total ? total : pageSize"
                                        :total="total"
                                        :table-data-count-in-page="tableData.length"
                                        @selectPageAll="isSelectDataAll(false)"
                                        @selectAll="isSelectDataAll(true)"/>
        <el-table-column min-width="40" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show-tool="scope.row.showTool" :is-show="scope.row.showMore && !isReadOnly"
                           :buttons="buttons" :size="selectDataCounts"/>
          </template>
        </el-table-column>
        <template v-for="(item, index) in tableLabel">
          <el-table-column v-if="item.id == 'num'" prop="num" sortable min-width="80" label="ID" show-overflow-tooltip :key="index"/>
          <el-table-column
              v-if="item.id == 'caseName'"
              prop="caseName"
              :label="$t('commons.name')"
              min-width="120"
              sortable
              show-overflow-tooltip
              :key="index">
          </el-table-column>
          <el-table-column
              v-if="item.id == 'projectName'"
              prop="projectName"
              min-width="120"
              :label="$t('load_test.project_name')"
              show-overflow-tooltip
              :key="index">
          </el-table-column>
          <el-table-column
              v-if="item.id == 'userName'"
              prop="userName"
              min-width="100"
              :label="$t('load_test.user_name')"
              show-overflow-tooltip
              :key="index">
          </el-table-column>
          <el-table-column
              v-if="item.id == 'createTime'"
              sortable
              prop="createTime"
              min-width="160"
              :label="$t('commons.create_time')"
              :key="index">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
              v-if="item.id == 'status'"
              prop="status"
              column-key="status"
              :filters="statusFilters"
              :label="$t('commons.status')"
              min-width="80"
              :key="index">
            <template v-slot:default="{row}">
              <ms-performance-test-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column
              v-if="item.id == 'caseStatus'"
              min-width="100"
              prop="caseStatus"
              :label="$t('test_track.plan.load_case.execution_status')"
              :key="index">
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
          </el-table-column>
          <el-table-column
              v-if="item.id == 'loadReportId'"
              :label="$t('test_track.plan.load_case.report')"
              min-width="80"
              show-overflow-tooltip
              :key="index">
            <template v-slot:default="scope">
              <div v-loading="loading === scope.row.id">
                <el-link type="info" @click="getReport(scope.row)" v-if="scope.row.loadReportId">
                  {{ $t('test_track.plan.load_case.view_report') }}
                </el-link>
                <span v-else> - </span>
              </div>
            </template>
          </el-table-column>
        </template>
        <el-table-column v-if="!isReadOnly" fixed="right" min-width="100" :label="$t('commons.operating')" >
          <template slot="header">
            <header-label-operate @exec="customHeader"/>
          </template>
          <template v-slot:default="scope">
            <div>

              <ms-table-operator-button class="run-button"
                                        v-permission="['PROJECT_TRACK_PLAN:READ+RUN']"
                                        :tip="$t('api_test.run')"
                                        icon="el-icon-video-play"
                                        @exec="run(scope.row)"/>
              <ms-table-operator-button v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"
                                        :tip="$t('test_track.plan_view.cancel_relevance')"
                                        icon="el-icon-unlock" type="danger" @exec="handleDelete(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <header-custom ref="headerCustom" :initTableData="initTable" :optionalFields=headerItems
                     :type=type></header-custom>
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
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import LoadCaseReport from "@/business/components/track/plan/view/comonents/load/LoadCaseReport";
import {
  _filter,
  _handleSelect,
  _handleSelectAll,
  _sort,
  getLabel,
  getSelectDataCounts,
  setUnSelectIds,
  buildBatchParam,
  initCondition,
  toggleAllSelection,
  checkTableRowIsSelect, deepClone
} from "@/common/js/tableUtils";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import {TAPD, TEST_CASE_LIST, TEST_PLAN_LOAD_CASE} from "@/common/js/constants";
import {Test_Plan_Load_Case, Track_Test_Case} from "@/business/components/common/model/JsonData";
import {getCurrentUser} from "@/common/js/utils";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import MsPlanRunMode from "../../../common/PlanRunMode";

export default {
  name: "TestPlanLoadCaseList",
  components: {
    HeaderLabelOperate,
    HeaderCustom,
    LoadCaseReport,
    TestPlanLoadCaseListHeader,
    ShowMoreBtn,
    MsTablePagination,
    MsPerformanceTestStatus,
    MsTableOperatorButton,
    MsTableHeaderSelectPopover,
    MsPlanRunMode
  },
  data() {
    return {
      type: TEST_PLAN_LOAD_CASE,
      headerItems: Test_Plan_Load_Case,
      tableLabel: [],
      condition: {},
      result: {},
      tableData: [],
      selectRows: new Set(),
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectDataCounts: 0,
      status: 'default',
      screenHeight: 'calc(100vh - 250px)',//屏幕高度
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
      this.selectRows = array;
    },
    runBatch(config){
      this.orderBySelectRows(this.selectRows);
      if(this.condition != null && this.condition.selectAll){
        let selectAllRowParams = buildBatchParam(this);
        selectAllRowParams.ids = Array.from(this.selectRows).map(row => row.id);
        this.$post('/test/plan/load/case/selectAllTableRows', selectAllRowParams, response => {
          let dataRows = response.data;
          let runArr = [];
          dataRows.forEach(loadCase => {
            runArr.push({
              id: loadCase.loadCaseId,
              testPlanLoadId: loadCase.id,
              triggerMode: 'CASE'
            });
          });
          let obj = {config: config, requests: runArr, userId: getCurrentUser().id};
          this._runBatch(obj);
          this.initTable();
          this.refreshStatus();
        });
      }else {
        let runArr = [];
        this.selectRows.forEach(loadCase => {
          runArr.push( {
            id: loadCase.loadCaseId,
            testPlanLoadId: loadCase.id,
            triggerMode: 'CASE'
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

    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    initTable() {
      this.autoCheckStatus();
      this.selectRows = new Set();
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
          }
          this.$nextTick(() => {
            checkTableRowIsSelect(this,this.condition,this.tableData,this.$refs.table,this.selectRows);
          })
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
          }
          this.$nextTick(() => {
            checkTableRowIsSelect(this,this.condition,this.tableData,this.$refs.table,this.selectRows);
          })
        })
      }
      getLabel(this, TEST_PLAN_LOAD_CASE);

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
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleSelectionChange(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            if (this.planId) {
              let param = buildBatchParam(this);
              this.result = this.$post('/test/plan/load/case/batch/delete', param, () => {
                this.selectRows.clear();
                this.initTable();
                this.$success(this.$t('test_track.cancel_relevance_success'));
              });
            }
            if (this.reviewId) {
              this.result = this.$post('/test/review/load/case/batch/delete', ids, () => {
                this.selectRows.clear();
                this.initTable();
                this.$success(this.$t('test_track.cancel_relevance_success'));
              });
            }
          }
        }
      })
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
        triggerMode: 'CASE'
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
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initTable();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTable();
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
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      //设置勾选
      toggleAllSelection(this.$refs.table, this.tableData, this.selectRows);
      //显示隐藏菜单
      _handleSelectAll(this, this.tableData, this.tableData, this.selectRows);
      //设置未选择ID(更新)
      this.condition.unSelectIds = [];
      //更新统计信息
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
  },
  beforeDestroy() {
    console.log('beforeDestroy')
    this.cancelRefresh();
  },
}
</script>

<style scoped>
/deep/ .el-table__fixed-body-wrapper {
  top: 47px !important;
}
</style>
