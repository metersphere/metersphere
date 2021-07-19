<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="loading">
      <template v-slot:header>
        <test-plan-scenario-list-header
          :condition="condition"
          @refresh="search"
          @relevanceCase="$emit('relevanceCase', 'scenario')"/>
      </template>

      <ms-table
        :data="tableData"
        :condition="condition"
        :total="total"
        :page-size.sync="pageSize"
        :operators="operators"
        :screen-height="screenHeight"
        :batch-operators="buttons"
        @handlePageChange="search"
        :fields.sync="fields"
        :field-key="tableHeaderKey"
        @refresh="search"
        ref="table">
        <span v-for="(item) in fields" :key="item.key">
          <ms-table-column
            v-if="item.id == 'num'"
            :fields-width="fieldsWidth"
            sortable
            prop="customNum"
            min-width="80px"
            label="ID"/>

          <ms-table-column :field="item"
                           :fields-width="fieldsWidth"
                           prop="name"
                           :label="$t('api_test.automation.scenario_name')" min-width="120px"
                           sortable/>

          <ms-table-column :field="item"
                           :fields-width="fieldsWidth"
                           prop="level" :label="$t('api_test.automation.case_level')" min-width="120px"
                           column-key="level"
                           sortable
                           :filters="LEVEL_FILTERS">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level" ref="level"/>
            </template>
          </ms-table-column>

          <ms-table-column :field="item"
                           :fields-width="fieldsWidth"
                           prop="tagNames" :label="$t('api_test.automation.tag')"
                           min-width="100px">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index) in scope.row.tags" :key="index" type="success" effect="plain"
                      :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            </template>
          </ms-table-column>

          <ms-table-column :field="item"
                           :fields-width="fieldsWidth"
                           prop="createUser"
                           :label="$t('api_test.automation.creator')"
                           min-width="100px">
            <template v-slot:default="scope">
              {{scope.row.creatorName}}
            </template>
          </ms-table-column>

          <ms-table-column
            v-if="item.id == 'maintainer'"
            prop="userId"
            :fields-width="fieldsWidth"
            :label="$t('custom_field.case_maintainer')"
            min-width="120">
             <template v-slot:default="scope">
              {{scope.row.principalName}}
            </template>
          </ms-table-column>


          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="principal"
            :label="$t('custom_field.case_maintainer')"
            min-width="120"/>

          <ms-update-time-column :field="item" :fields-width="fieldsWidth"/>
          <ms-create-time-column :field="item" :fields-width="fieldsWidth"/>


          <ms-table-column :field="item"
                           :fields-width="fieldsWidth"
                           prop="stepTotal" :label="$t('api_test.automation.step')"
                           min-width="80px"/>
          <ms-table-column :field="item"
                           :fields-width="fieldsWidth"
                           prop="lastResult" min-width="100px"
                           :filters="RESULT_FILTERS"
                           :label="$t('api_test.automation.last_result')">
            <template v-slot:default="{row}">
              <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">
                {{ $t('api_test.automation.success') }}
              </el-link>
              <el-link type="danger" @click="showReport(row)" v-if="row.lastResult === 'Fail'">
                {{ $t('api_test.automation.fail') }}
              </el-link>
            </template>
          </ms-table-column>
          <ms-table-column :field="item"
                           :fields-width="fieldsWidth"
                           prop="passRate" min-width="80px"
                           :label="$t('api_test.automation.passing_rate')"/>
        </span>
      </ms-table>

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
                :select-row="this.$refs.table ? this.$refs.table.selectRows : new Set()" ref="batchEdit" @batchEdit="batchEdit"/>
    <ms-plan-run-mode @handleRunBatch="handleRunBatch" ref="runMode"/>
    <ms-task-center ref="taskCenter"/>
  </div>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTag from "../../../../../common/components/MsTag";
import {getCurrentProjectID, getUUID, strMapToObj} from "@/common/js/utils";
import MsApiReportDetail from "../../../../../api/automation/report/ApiReportDetail";
import MsTableMoreBtn from "../../../../../api/automation/scenario/TableMoreBtn";
import MsScenarioExtendButtons from "@/business/components/api/automation/scenario/ScenarioExtendBtns";
import MsTestPlanList from "../../../../../api/automation/scenario/testplan/TestPlanList";
import TestPlanScenarioListHeader from "./TestPlanScenarioListHeader";
import {
  initCondition,
  buildBatchParam,
  checkTableRowIsSelect, getCustomTableHeader, getCustomTableWidth
} from "../../../../../../../common/js/tableUtils";
import {TEST_PLAN_SCENARIO_CASE} from "@/common/js/constants";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import BatchEdit from "@/business/components/track/case/components/BatchEdit";
import MsPlanRunMode from "../../../common/PlanRunMode";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import {API_SCENARIO_FILTERS} from "@/common/js/table-constants";
import MsTaskCenter from "../../../../../task/TaskCenter";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsUpdateTimeColumn from "@/business/components/common/components/table/MsUpdateTimeColumn";
import MsCreateTimeColumn from "@/business/components/common/components/table/MsCreateTimeColumn";

export default {
  name: "MsTestPlanApiScenarioList",
  components: {
    MsCreateTimeColumn,
    MsUpdateTimeColumn,
    MsTableColumn,
    MsTable,
    PriorityTableItem,
    HeaderLabelOperate,
    TestPlanScenarioListHeader,
    MsTablePagination,
    MsTableMoreBtn,
    MsTableHeader,
    MsTag,
    MsApiReportDetail,
    MsScenarioExtendButtons,
    MsTestPlanList,
    BatchEdit,
    MsPlanRunMode,
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
      tableHeaderKey:"TEST_PLAN_SCENARIO_CASE",
      fields: getCustomTableHeader('TEST_PLAN_SCENARIO_CASE'),
      fieldsWidth: getCustomTableWidth('TEST_PLAN_SCENARIO_CASE'),
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
      operators: [
        {
          tip: this.$t('api_test.run'), icon: "el-icon-video-play",
          exec: this.execute,
          class: 'run-button',
          permissions: ['PROJECT_TRACK_PLAN:READ+RUN']
        },
        {
          tip: this.$t('test_track.plan_view.cancel_relevance'), icon: "el-icon-unlock",
          exec: this.remove,
          type: 'danger',
          permissions: ['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']
        }
      ],
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
    search() {
      initCondition(this.condition,this.condition.selectAll);
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
          if (this.$refs.table) {
            this.$refs.table.selectRows.clear();
            setTimeout(this.$refs.table.doLayout, 200);
            this.$nextTick(() => {
              checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.table, this.$refs.table.selectRows);
            });
          }
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
          if (this.$refs.table) {
            setTimeout(this.$refs.table.doLayout, 200);
            this.$nextTick(() => {
              checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.table, this.$refs.table.selectRows);
            });
          }
        });
      }
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
      return array;
    },
    handleRunBatch(config){
      let rows = this.orderBySelectRows(this.$refs.table.selectRows);
      if (this.reviewId) {
        let param = {config : config,planCaseIds:[]};
        rows.forEach(row => {
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
        param.ids = rows.map(r => r.id);
        rows.forEach(row => {
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
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this);
            param.ids = this.$refs.table.selectIds;
            if (this.planId) {
              param.planId = this.planId;
              this.$post('/test/plan/scenario/case/batch/delete', param, () => {
                this.$refs.table.selectRows.clear();
                this.search();
                this.$success(this.$t('test_track.cancel_relevance_success'));
                this.$emit('refresh');
              });
            }
            if (this.reviewId) {
              param.reviewId = this.reviewId;
              this.$post('/test/case/review/scenario/case/batch/delete', param, () => {
                this.$refs.table.selectRows.clear();
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
        selectAllRowParams.ids = this.$refs.table.selectIds;
        this.$post('/test/plan/scenario/case/selectAllTableRows', selectAllRowParams, response => {
          let dataRows = response.data;
          this.$refs.batchEdit.open(dataRows.size);
          this.$refs.batchEdit.setScenarioSelectRows(dataRows, "planScenario");
        });
      } else {
        this.$refs.batchEdit.open(this.$refs.table.selectRows.size);
        this.$refs.batchEdit.setScenarioSelectRows(this.$refs.table.selectRows, "planScenario");
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
  }
}
</script>

<style scoped>
/*/deep/ .el-drawer__header {*/
/*  margin-bottom: 0px;*/
/*}*/

</style>
