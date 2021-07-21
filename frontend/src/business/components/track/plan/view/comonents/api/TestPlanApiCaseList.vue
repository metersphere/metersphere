<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">
      <template v-slot:header>
        <test-plan-case-list-header
          :project-id="getProjectId()"
          :condition="condition"
          :plan-id="planId"
          @refresh="initTable"
          @relevanceCase="$emit('relevanceCase')"
          @setEnvironment="setEnvironment"
          v-if="isPlanModel"/>
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

          <ms-table-column :field="item" prop="num"
                           :fields-width="fieldsWidth"
                           sortable label="ID" min-width="80"/>

          <ms-table-column :field="item" :fields-width="fieldsWidth" prop="name" sortable min-width="120"
                           :label="$t('test_track.case.name')"/>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="priority"
            :filters="priorityFilters"
            sortable
            :label="$t('test_track.case.priority')"
            min-width="120px">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority"/>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            min-width="100"
            prop="path"
            :label="$t('api_test.definition.api_path')"/>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="createUser"
            sortable
            min-width="100"
            :filters="userFilters"
            :label="'创建人'">
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

          <ms-update-time-column :field="item" :fields-width="fieldsWidth"/>
          <ms-create-time-column :field="item" :fields-width="fieldsWidth"/>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="tags"
            min-width="100"
            :label="$t('commons.tag')">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                      :content="itemName" style="margin-left: 0px; margin-right: 2px"/>
            </template>
          </ms-table-column>

          <ms-table-column :field="item"
                           prop="execResult"
                           :fields-width="fieldsWidth"
                           :label="'执行状态'" min-width="150" align="center">
            <template v-slot:default="scope">
              <div v-loading="rowLoading === scope.row.id">
                <el-link type="danger"
                         v-if="scope.row.execResult && scope.row.execResult === 'error'"
                         @click="getReportResult(scope.row)" v-text="getResult(scope.row.execResult)"/>
                <el-link v-else-if="scope.row.execResult && scope.row.execResult === 'success'"
                         type="primary"
                         @click="getReportResult(scope.row)" v-text="getResult(scope.row.execResult)">

                </el-link>
                <div v-else v-text="getResult(scope.row.execResult)"/>

                <div v-if="scope.row.id" style="color: #999999;font-size: 12px">
                  <span> {{ scope.row.updateTime | timestampFormatDate }}</span>
                  {{ scope.row.updateUser }}
                </div>
              </div>
            </template>
          </ms-table-column>
        </span>
      </ms-table>

      <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

      <test-plan-api-case-result :response="response" ref="apiCaseResult"/>

      <!-- 执行组件 -->
      <ms-run :debug="false" :type="'API_PLAN'" :reportId="reportId" :run-data="runData"
              @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest" @autoCheckStatus="autoCheckStatus"/>

      <!-- 批量编辑 -->
      <batch-edit :dialog-title="$t('test_track.case.batch_edit_case')" :type-arr="typeArr" :value-arr="valueArr"
                  :select-row="$refs.table ? $refs.table.selectRows : new Set()" ref="batchEdit" @batchEdit="batchEdit"/>

      <ms-plan-run-mode @handleRunBatch="handleRunBatch" ref="runMode"/>
    </el-card>
    <ms-task-center ref="taskCenter"/>
  </div>

</template>

<script>

import MsTablePagination from "../../../../../common/pagination/TablePagination";
import MsTag from "../../../../../common/components/MsTag";
import MsApiCaseList from "../../../../../api/definition/components/case/ApiCaseList";
import ApiCaseList from "../../../../../api/definition/components/case/ApiCaseList";
import MsContainer from "../../../../../common/components/MsContainer";
import MsBottomContainer from "../../../../../api/definition/components/BottomContainer";
import BatchEdit from "@/business/components/track/case/components/BatchEdit";
import {API_METHOD_COLOUR, CASE_PRIORITY, RESULT_MAP} from "../../../../../api/definition/model/JsonData";
import {getCurrentProjectID, strMapToObj} from "@/common/js/utils";
import ApiListContainer from "../../../../../api/definition/components/list/ApiListContainer";
import PriorityTableItem from "../../../../common/tableItems/planview/PriorityTableItem";
import {getUUID} from "../../../../../../../common/js/utils";
import TestPlanCaseListHeader from "./TestPlanCaseListHeader";
import MsRun from "../../../../../api/definition/components/Run";
import TestPlanApiCaseResult from "./TestPlanApiCaseResult";
import {TEST_PLAN_API_CASE} from "@/common/js/constants";
import {
  buildBatchParam,
  checkTableRowIsSelect, deepClone, getCustomTableHeader, getCustomTableWidth,
} from "@/common/js/tableUtils";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsTaskCenter from "../../../../../task/TaskCenter";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsPlanRunMode from "@/business/components/track/plan/common/PlanRunMode";
import MsUpdateTimeColumn from "@/business/components/common/components/table/MsUpdateTimeColumn";
import MsCreateTimeColumn from "@/business/components/common/components/table/MsCreateTimeColumn";

export default {
  name: "TestPlanApiCaseList",
  components: {
    MsPlanRunMode,
    MsCreateTimeColumn,
    MsUpdateTimeColumn,
    MsTableColumn,
    MsTable,
    BatchEdit,
    HeaderLabelOperate,
    HeaderCustom,
    TestPlanApiCaseResult,
    MsRun,
    TestPlanCaseListHeader,
    ApiCaseList,
    PriorityTableItem,
    ApiListContainer,
    MsTablePagination,
    MsTag,
    MsApiCaseList,
    MsContainer,
    MsBottomContainer,
    MsTaskCenter
  },
  data() {
    return {
      type: TEST_PLAN_API_CASE,
      tableHeaderKey:"TEST_PLAN_API_CASE",
      fields: getCustomTableHeader('TEST_PLAN_API_CASE'),
      fieldsWidth: getCustomTableWidth('TEST_PLAN_API_CASE'),
      tableLabel: [],
      condition: {},
      selectCase: {},
      result: {},
      moduleId: "",
      status: 'default',
      deletePath: "/test/case/delete",
      operators: [
        {
          tip: this.$t('api_test.run'), icon: "el-icon-video-play",
          exec: this.singleRun,
          class: 'run-button',
          permissions: ['PROJECT_TRACK_PLAN:READ+RUN']
        },
        {
          tip: this.$t('test_track.plan_view.cancel_relevance'), icon: "el-icon-unlock",
          exec: this.handleDelete,
          type: 'danger',
          permissions: ['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']
        }
      ],
      buttons: [
        {name: this.$t('test_track.case.batch_unlink'), handleClick: this.handleDeleteBatch, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE']},
        {name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleBatchExecute, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_RUN']},
        {name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleBatchEdit, permissions: ['PROJECT_TRACK_PLAN:READ+CASE_BATCH_EDIT']}
      ],
      typeArr: [
        {id: 'projectEnv', name: this.$t('api_test.definition.request.run_env')},
      ],
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      valueArr: {
        priority: CASE_PRIORITY,
        userId: [],
        projectEnv: []
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectDataCounts: 0,
      screenHeight: 'calc(100vh - 250px)',//屏幕高度
      // environmentId: undefined,
      currentCaseProjectId: "",
      runData: [],
      testPlanCaseIds: [],
      reportId: "",
      response: {},
      rowLoading: "",
      userFilters: [],
      projectIds: [],
      projectList: []
    };
  },
  props: {
    currentProtocol: String,
    selectNodeIds: Array,
    visible: {
      type: Boolean,
      default: false,
    },
    isApiListEnable: {
      type: Boolean,
      default: false,
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    isCaseRelevance: {
      type: Boolean,
      default: false,
    },
    model: {
      type: String,
      default() {
        'api';
      }
    },
    planId: String,
    reviewId: String,
    clickType: String
  },
  created: function () {
    this.getMaintainerOptions();
    this.initTable();

  },
  activated() {
    this.status = 'default';
  },
  watch: {
    selectNodeIds() {
      this.condition.selectAll = false;
      this.initTable();
    },
    currentProtocol() {
      this.initTable();
    },
    planId() {
      this.initTable();
    },
    reviewId() {
      this.initTable();
    }
  },
  computed: {
    // 测试计划关联测试列表
    isRelevanceModel() {
      return this.model === 'relevance';
    },
    // 测试计划接口用例列表
    isPlanModel() {
      return this.model === 'plan';
    },
    // 接口定义用例列表
    isApiModel() {
      return this.model === 'api';
    },
  },
  methods: {
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    getMaintainerOptions() {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.valueArr.userId = response.data;
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    isApiListEnableChange(data) {
      this.$emit('isApiListEnableChange', data);
    },
    initTable() {
      this.autoCheckStatus();
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      }
      if (this.clickType) {
        if (this.status == 'default') {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = 'all';
      }
      if (this.reviewId) {
        this.condition.reviewId = this.reviewId;
        this.result = this.$post('/test/case/review/api/case/list/' + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          if (this.$refs.table) {
            this.$refs.table.clear();
            setTimeout(this.$refs.table.doLayout, 200);
            this.$nextTick(() => {
              checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.table, this.$refs.table.selectRows);
            });
          }
        });
      }
      if (this.planId) {
        this.condition.planId = this.planId;
        this.result = this.$post('/test/plan/api/case/list/' + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          if (this.$refs.table) {
            this.$refs.table.clear();
            setTimeout(this.$refs.table.doLayout, 200);
            this.$nextTick(() => {
              checkTableRowIsSelect(this, this.condition, this.tableData, this.$refs.table, this.$refs.table.selectRows);
            });
          }
        });
      }
    },
    search() {
      this.initTable();
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    reductionApi(row) {
      let ids = [row.id];
      this.$post('/api/testcase/reduction/', ids, () => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleDeleteBatch() {
      this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this);
            param.ids = this.$refs.table.selectIds;
            if (this.reviewId) {
              param.testCaseReviewId = this.reviewId;
              this.$post('/test/case/review/api/case/batch/delete', param, () => {
                if (this.$refs.table) {
                  this.$refs.table.clear();
                }
                this.initTable();
                this.$emit('refresh');
                this.$success(this.$t('test_track.cancel_relevance_success'));
              });
            }
            if (this.planId) {
              param.planId = this.planId;
              this.$post('/test/plan/api/case/batch/delete', param, () => {
                if (this.$refs.table) {
                  this.$refs.table.clear();
                }                this.initTable();
                this.$emit('refresh');
                this.$success(this.$t('test_track.cancel_relevance_success'));
              });
            }
          }
        }
      });
    },
    getResult(data) {
      if (RESULT_MAP.get(data)) {
        return RESULT_MAP.get(data);
      } else {
        return RESULT_MAP.get("default");
      }
    },
    runRefresh(data) {
      this.rowLoading = "";
      this.$success(this.$t('schedule.event_success'));
      this.initTable();
    },
    singleRun(row) {
      this.runData = [];
      this.rowLoading = row.id;
      this.$get('/api/testcase/get/' + row.caseId, (response) => {
        let apiCase = response.data;
        let request = JSON.parse(apiCase.request);
        request.name = row.id;
        request.useEnvironment = row.environmentId;
        this.runData.push(request);
        /*触发执行操作*/
        this.reportId = getUUID().substring(0, 8);
      });
    },
    errorRefresh() {
      this.rowLoading = "";
    },
    handleBatchEdit() {
      this.$refs.batchEdit.open(this.$refs.table.selectRows.size);
      this.$refs.batchEdit.setSelectRows(this.$refs.table.selectRows);
    },
    getData() {
      return new Promise((resolve) => {
        let index = 1;
        this.runData = [];
        this.testPlanCaseIds = [];
        if (this.condition != null && this.condition.selectAll) {
          let selectAllRowParams = buildBatchParam(this);
          selectAllRowParams.ids = this.$refs.table.selectIds;
          this.$post('/test/plan/api/case/selectAllTableRows', selectAllRowParams, response => {
            let dataRows = response.data;
            // 按照列表顺序排序
            this.orderBySelectRows(dataRows);
            dataRows.forEach(row => {
              this.$get('/api/testcase/get/' + row.caseId, (response) => {
                let apiCase = response.data;
                let request = JSON.parse(apiCase.request);
                request.name = row.id;
                request.id = row.id;
                request.useEnvironment = row.environmentId;
                this.runData.unshift(request);
                this.testPlanCaseIds.unshift(row.id);
                if (dataRows.length === index) {
                  resolve();
                }
                index++;
              });
            });
          });
        } else {
          // 按照列表顺序排序
          let dataRows = this.orderBySelectRows(this.$refs.table.selectRows);
          dataRows.forEach(row => {
            this.$get('/api/testcase/get/' + row.caseId, (response) => {
              let apiCase = response.data;
              let request = JSON.parse(apiCase.request);
              request.name = row.id;
              request.id = row.id;
              request.useEnvironment = row.environmentId;
              this.runData.unshift(request);
              this.testPlanCaseIds.unshift(row.id);
              if (dataRows.length === index) {
                resolve();
              }
              index++;
            });
          });
        }
      });
    },
    batchEdit(form) {
      let param = {};
      // 批量修改环境
      if (form.type === 'projectEnv') {
        let selectAllRowParams = buildBatchParam(this);
        selectAllRowParams.ids = this.$refs.table.selectIds;
        this.$post('/test/plan/api/case/selectAllTableRows', selectAllRowParams, response => {
          let dataRows = response.data;
          let map = new Map();
          param.projectEnvMap = strMapToObj(form.projectEnvMap);
          dataRows.forEach(row => {
            map[row.id] = row.projectId;
          });
          param.selectRows = map;
          this.$post('/test/plan/api/case/batch/update/env', param, () => {
            this.$success(this.$t('commons.save_success'));
            this.initTable();
          });
        });
      } else {
        // 批量修改其它
      }
    },
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
    handleBatchExecute() {
      this.getData().then(() => {
        if (this.runData && this.runData.length > 0) {
          this.$refs.runMode.open('API');
        }
      });
    },
    handleRunBatch(config) {
      let obj = {planIds: this.testPlanCaseIds, config: config};
      this.$post("/test/plan/api/case/run", obj, response => {
        this.$message(this.$t('commons.run_message'));
        this.$refs.taskCenter.open();
      });
      this.search();
    },
    autoCheckStatus() { //  检查执行结果，自动更新计划状态
      if (!this.planId) {
        return;
      }
      this.$post('/test/plan/autoCheck/' + this.planId, (response) => {
      });
    },
    handleDelete(apiCase) {
      if (this.planId) {
        this.$get('/test/plan/api/case/delete/' + apiCase.id, () => {
          this.$success(this.$t('test_track.cancel_relevance_success'));
          this.$emit('refresh');
          this.initTable();
        });
      }
      if (this.reviewId) {
        this.$get('/test/case/review/api/case/delete/' + apiCase.id, () => {
          this.$success(this.$t('test_track.cancel_relevance_success'));
          this.$emit('refresh');
          this.initTable();
        });
      }
      return;
    },
    getProjectId() {
      if (!this.isRelevanceModel) {
        return getCurrentProjectID();
      } else {
        return this.currentCaseProjectId;
      }
    },
    setEnvironment(data) {
      //   this.environmentId = data.id;
    },
    getReportResult(apiCase) {
      let url = "/api/definition/report/getReport/" + apiCase.id + '/' + 'API_PLAN';
      this.$get(url, response => {
        if (response.data) {
          this.response = JSON.parse(response.data.content);
          this.$refs.apiCaseResult.open();
        }
      });
    },
  },
};
</script>

<style scoped>
.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.request-method {
  padding: 0 5px;
  color: #1E90FF;
}

.api-el-tag {
  color: white;
}

.search-input {
  float: right;
  width: 300px;
  /*margin-bottom: 20px;*/
  margin-right: 20px;
}

</style>
