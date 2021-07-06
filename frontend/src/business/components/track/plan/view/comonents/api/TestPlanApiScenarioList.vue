<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="loading">
      <template v-slot:header>
        <test-plan-scenario-list-header
          :condition="condition"
          @refresh="search"
          @relevanceCase="$emit('relevanceCase', 'scenario')"/>
      </template>

      <el-table ref="scenarioTable"
                border :data="tableData" class="test-content adjust-table ms-select-all-fixed"
                @select-all="handleSelectAll"
                @sort-change="sort"
                @header-dragend="tableHeaderDragend"
                @filter-change="filter"
                :height="screenHeight"
                @select="handleSelect">
        <el-table-column width="50" type="selection"/>
        <ms-table-header-select-popover v-show="total>0"
                                        :page-size="pageSize > total ? total : pageSize"
                                        :total="total"
                                        :table-data-count-in-page="tableData.length"
                                        @selectPageAll="isSelectDataAll(false)"
                                        @selectAll="isSelectDataAll(true)"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="{row}">
            <show-more-btn :is-show-tool="row.showTool" :is-show="isSelect(row)" :buttons="buttons"
                           :size="selectDataCounts"/>
          </template>
        </el-table-column>
        <template v-for="(item, index) in tableLabel">
          <el-table-column
            v-if="item.id == 'num'"
            sortable="custom"
            prop="customNum"
            min-width="80px"
            label="ID"
            :key="index"/>
          <el-table-column v-if="item.id == 'name'" prop="name" :label="$t('api_test.automation.scenario_name')" min-width="120px"
                           sortable
                           show-overflow-tooltip :key="index"/>
          <el-table-column v-if="item.id == 'level'" prop="level" :label="$t('api_test.automation.case_level')" min-width="120px"
                           column-key="level"
                           sortable="custom"
                           :filters="LEVEL_FILTERS"
                           show-overflow-tooltip :key="index">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level" ref="level"/>
            </template>

          </el-table-column>
          <el-table-column v-if="item.id == 'tagNames'" prop="tagNames" :label="$t('api_test.automation.tag')"
                           min-width="100px" :key="index">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index) in scope.row.tags" :key="index" type="success" effect="plain"
                      :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'userId'" prop="userId" :label="$t('api_test.automation.creator')"
                           min-width="100px"
                           show-overflow-tooltip :key="index"/>
          <el-table-column
            v-if="item.id == 'maintainer'"
            prop="principal"
            :label="$t('custom_field.case_maintainer')"
            show-overflow-tooltip
            :key="index"
            min-width="120"
          >
          </el-table-column>
          <el-table-column v-if="item.id == 'updateTime'"
                           prop="updateTime"
                           min-width="160px"
                           sortable="custom"
                           :label="$t('api_test.automation.update_time')" width="180" :key="index">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'createTime'"
                           prop="createTime"
                           min-width="120px"
                           sortable="custom"
                           :label="$t('commons.create_time')" width="180" :key="index">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'stepTotal'" prop="stepTotal" :label="$t('api_test.automation.step')"
                           min-width="80px"
                           show-overflow-tooltip :key="index"/>
          <el-table-column v-if="item.id == 'lastResult'" prop="lastResult" min-width="100px"
                           column-key="lastResult"
                           :filters="RESULT_FILTERS"
                           :label="$t('api_test.automation.last_result')" :key="index">
            <template v-slot:default="{row}">
              <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">
                {{ $t('api_test.automation.success') }}
              </el-link>
              <el-link type="danger" @click="showReport(row)" v-if="row.lastResult === 'Fail'">
                {{ $t('api_test.automation.fail') }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'passRate'" prop="passRate" min-width="80px"
                           :label="$t('api_test.automation.passing_rate')"
                           show-overflow-tooltip :key="index"/>
        </template>
        <el-table-column :label="$t('commons.operating')" fixed="right" min-width="100px" v-if="!referenced">
          <template slot="header">
            <header-label-operate @exec="customHeader"/>
          </template>
          <template v-slot:default="{row}">
            <div>
              <ms-table-operator-button class="run-button"
                                        v-permission="['PROJECT_TRACK_PLAN:READ+RUN']"
                                        :tip="$t('api_test.run')"
                                        icon="el-icon-video-play"
                                        @exec="execute(row)"/>
              <ms-table-operator-button v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"
                                        :tip="$t('test_track.plan_view.cancel_relevance')"
                                        icon="el-icon-unlock" type="danger" @exec="remove(row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <header-custom ref="headerCustom" :initTableData="search" :optionalFields=headerItems :type=type></header-custom>
      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
      <div>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="runVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false"
                   size="90%">
          <ms-api-report-detail @refresh="search" :infoDb="infoDb" :report-id="reportId" :currentProjectId="projectId"/>
        </el-drawer>
      </div>
    </el-card>

    <!-- 批量编辑 -->
    <batch-edit :dialog-title="$t('test_track.case.batch_edit_case')" :type-arr="typeArr" :value-arr="valueArr"
                :select-row="selectRows" ref="batchEdit" @batchEdit="batchEdit"/>
    <ms-plan-run-mode @handleRunBatch="handleRunBatch" ref="runMode"/>
    <ms-task-center ref="taskCenter"/>
  </div>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import MsTag from "../../../../../common/components/MsTag";
import {getCurrentProjectID, getUUID, strMapToObj} from "@/common/js/utils";
import MsApiReportDetail from "../../../../../api/automation/report/ApiReportDetail";
import MsTableMoreBtn from "../../../../../api/automation/scenario/TableMoreBtn";
import MsScenarioExtendButtons from "@/business/components/api/automation/scenario/ScenarioExtendBtns";
import MsTestPlanList from "../../../../../api/automation/scenario/testplan/TestPlanList";
import TestPlanScenarioListHeader from "./TestPlanScenarioListHeader";
import {
  _handleSelect,
  _handleSelectAll,
  getLabel,
  getSelectDataCounts,
  setUnSelectIds,
  _filter,
  _sort,
  initCondition,
  buildBatchParam,
  toggleAllSelection,
  checkTableRowIsSelect, deepClone
} from "../../../../../../../common/js/tableUtils";
import MsTableOperatorButton from "../../../../../common/components/MsTableOperatorButton";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import {TEST_CASE_LIST, TEST_PLAN_SCENARIO_CASE} from "@/common/js/constants";
import {Test_Plan_Scenario_Case, Track_Test_Case} from "@/business/components/common/model/JsonData";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import BatchEdit from "@/business/components/track/case/components/BatchEdit";
import MsPlanRunMode from "../../../common/PlanRunMode";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import {API_SCENARIO_FILTERS} from "@/common/js/table-constants";
import MsTaskCenter from "../../../../../task/TaskCenter";

export default {
  name: "MsTestPlanApiScenarioList",
  components: {
    PriorityTableItem,
    HeaderLabelOperate,
    HeaderCustom,
    MsTableOperatorButton,
    TestPlanScenarioListHeader,
    MsTablePagination,
    MsTableMoreBtn,
    ShowMoreBtn,
    MsTableHeader,
    MsTag,
    MsApiReportDetail,
    MsScenarioExtendButtons,
    MsTestPlanList,
    BatchEdit,
    MsPlanRunMode,
    MsTableHeaderSelectPopover,
    MsTaskCenter
  },
  props: {
    referenced: {
      type: Boolean,
      default: false,
    },
    selectNodeIds: Array,
    reviewId: String,
    planId: String,
    clickType: String
  },
  data() {
    return {
      type: TEST_PLAN_SCENARIO_CASE,
      headerItems: Test_Plan_Scenario_Case,
      screenHeight: 'calc(100vh - 250px)',//屏幕高度
      tableLabel: [],
      loading: false,
      condition: {},
      currentScenario: {},
      schedule: {},
      selectAll: false,
      tableData: [],
      currentPage: 1,
      selectDataCounts: 0,
      pageSize: 10,
      total: 0,
      reportId: "",
      status: 'default',
      infoDb: false,
      runVisible: false,
      runData: [],
      ...API_SCENARIO_FILTERS,
      buttons: [
        {
          name: this.$t('test_track.case.batch_unlink'), handleClick: this.handleDeleteBatch, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE']
        },
        {
          name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleBatchExecute, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_RUN']
        },
        {
          name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleBatchEdit, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_EDIT']
        }
      ],
      selectRows: new Set(),
      typeArr: [
        {id: 'projectEnv', name: this.$t('api_test.definition.request.run_env')},
      ],
      valueArr: {
        projectEnv: []
      },
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  created() {
    this.search();

  },
  watch: {
    selectNodeIds() {
      this.condition.selectAll = false;
      this.search();
    },
    planId() {
      this.search();
    }
  },
  methods: {
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    search() {
      initCondition(this.condition,this.condition.selectAll);
      this.selectRows = new Set();
      this.loading = true;
      this.condition.moduleIds = this.selectNodeIds;
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = 'all';
      }
      if (this.planId) {
        this.condition.planId = this.planId;
        let url = "/test/plan/scenario/case/list/" + this.currentPage + "/" + this.pageSize;
        this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.loading = false;
          if (this.$refs.scenarioTable) {
            setTimeout(this.$refs.scenarioTable.doLayout, 200);
          }
          this.$nextTick(() => {
            checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.scenarioTable, this.selectRows);
            this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
          })
        });
      }
      if (this.reviewId) {
        this.condition.reviewId = this.reviewId;
        let url = "/test/case/review/scenario/case/list/" + this.currentPage + "/" + this.pageSize;
        this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.loading = false;
          if (this.$refs.scenarioTable) {
            setTimeout(this.$refs.scenarioTable.doLayout, 200);
          }
          this.$nextTick(() => {
            checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.scenarioTable, this.selectRows);
            this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
          })
        });
      }
      getLabel(this, TEST_PLAN_SCENARIO_CASE);

    },
    reductionApi(row) {
      row.scenarioDefinition = null;
      let rows = [row];
      this.$post("/api/automation/reduction", rows, response => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      })
    },
    handleBatchExecute() {
      this.$refs.runMode.open('API');
    },
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
    handleRunBatch(config){
      this.orderBySelectRows(this.selectRows);
      if (this.reviewId) {
        let param = {config : config,planCaseIds:[]};
        this.selectRows.forEach(row => {
          this.buildExecuteParam(param,row);
        });
        this.$post("/test/case/review/scenario/case/run", param, response => {
          this.$message(this.$t('commons.run_message'));
          this.$refs.taskCenter.open();
        });
      }
      if (this.planId) {
        let selectParam = buildBatchParam(this);
        let param = {config: config, planCaseIds: []};
        this.selectRows.forEach(row => {
          this.buildExecuteParam(param, row);
        });
        param.condition = selectParam.condition;
        param.triggerMode = "BATCH";
        this.$post("/test/plan/scenario/case/run", param, response => {
          this.$message(this.$t('commons.run_message'));
          this.$refs.taskCenter.open();
        });
      }
      this.search();
    },
    execute(row) {
      this.infoDb = false;
      let param ={planCaseIds: []};
      this.reportId = "";
      this.buildExecuteParam(param,row);
      param.triggerMode = "MANUAL";
      if (this.planId) {
        this.$post("/test/plan/scenario/case/run", param, response => {
          this.runVisible = true;
          this.reportId = response.data;
        });
      }
      if (this.reviewId) {
        this.$post("/test/case/review/scenario/case/run", param, response => {
          this.runVisible = true;
          this.reportId = response.data;
        });
      }
    },
    buildExecuteParam(param,row) {
      // param.id = row.id;
      param.id = getUUID();
      param.planScenarioId = row.id;
      param.projectId = row.projectId;
      param.planCaseIds.push(row.id);
      return param;
    },
    showReport(row) {
      this.runVisible = true;
      this.infoDb = true;
      this.reportId = row.reportId;
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.search();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.search();
    },
    remove(row) {
      if (this.planId) {
        this.$get('/test/plan/scenario/case/delete/' + row.id, () => {
          this.$success(this.$t('test_track.cancel_relevance_success'));
          this.$emit('refresh');
          this.search();
        });
      }
      if (this.reviewId) {
        this.$get('/test/case/review/scenario/case/delete/' + row.id, () => {
          this.$success(this.$t('test_track.cancel_relevance_success'));
          this.$emit('refresh');
          this.search();
        });
      }
      return;
    },
    isSelect(row) {
      return this.selectRows.has(row);
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this);
            param.ids = Array.from(this.selectRows).map(row => row.id);
            if (this.planId) {
              param.planId = this.planId;
              this.$post('/test/plan/scenario/case/batch/delete', param, () => {
                this.selectRows.clear();
                this.search();
                this.$success(this.$t('test_track.cancel_relevance_success'));
                this.$emit('refresh');
              });
            }
            if (this.reviewId) {
              param.reviewId = this.reviewId;
              this.$post('/test/case/review/scenario/case/batch/delete', param, () => {
                this.selectRows.clear();
                this.search();
                this.$success(this.$t('test_track.cancel_relevance_success'));
                this.$emit('refresh');
              });
            }
          }
        }
      });
    },
    handleBatchEdit() {
      if (this.condition != null && this.condition.selectAll) {
        let selectAllRowParams = buildBatchParam(this);
        selectAllRowParams.ids = Array.from(this.selectRows).map(row => row.id);
        this.$post('/test/plan/scenario/case/selectAllTableRows', selectAllRowParams, response => {
          let dataRows = response.data;
          this.$refs.batchEdit.open(dataRows.size);
          this.$refs.batchEdit.setScenarioSelectRows(dataRows, "planScenario");
        });
      } else {
        this.$refs.batchEdit.open(this.selectRows.size);
        this.$refs.batchEdit.setScenarioSelectRows(this.selectRows, "planScenario");
      }
    },
    batchEdit(form) {
      let param = {};
      param.mapping = strMapToObj(form.map);
      param.envMap = strMapToObj(form.projectEnvMap);

      if (this.planId) {
        this.$post('/test/plan/scenario/case/batch/update/env', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      }
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      //设置勾选
      toggleAllSelection(this.$refs.scenarioTable, this.tableData, this.selectRows);
      //显示隐藏菜单
      _handleSelectAll(this, this.tableData, this.tableData, this.selectRows);
      //设置未选择ID(更新)
      this.condition.unSelectIds = [];
      //更新统计信息
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    tableHeaderDragend(newWidth, oldWidth, column, event){
      if(column){
        if(column.minWidth){
          let minWidth = column.minWidth;
          if(minWidth > newWidth){
            column.width = minWidth;
          }
        }
      }
    },
  }
}
</script>

<style scoped>
/deep/ .el-drawer__header {
  margin-bottom: 0px;
}

.ms-select-all-fixed >>> th:nth-child(2) .el-icon-arrow-down {
  top: -4px;
}

/deep/ .el-table__fixed-body-wrapper {
 top: 48px !important;
}
</style>
