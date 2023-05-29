<template>
  <div>
    <el-card class="table-card-nopadding" v-loading="result.loading">
      <slot name="version"></slot>
      <slot name="tabChange"></slot>
      <ms-search
        :condition.sync="condition"
        :base-search-tip="$t('commons.search_by_id_name_tag')"
        @search="search">
      </ms-search>

      <ms-table
        :data="tableData"
        :screen-height="isRelate ? 'calc(100vh - 200px)' :  screenHeight"
        :condition="condition"
        :page-size="pageSize"
        :operators="isRelate ? [] : operators"
        :batch-operators="buttons"
        :total="total"
        :fields.sync="fields"
        :field-key=tableHeaderKey
        :remember-order="true"
        operator-width="200"
        :enable-order-drag="enableOrderDrag && showDrag"
        row-key="id"
        :row-order-group-id="condition.projectId"
        :row-order-func="editApiScenarioCaseOrder"
        @refresh="search(projectId)"
        @callBackSelectAll="callBackSelectAll"
        @callBackSelect="callBackSelect"
        ref="scenarioTable">
        <ms-table-column
          prop="deleteTime"
          sortable
          v-if="this.trashEnable"
          :fields-width="fieldsWidth"
          :label="$t('commons.delete_time')"
          min-width="150px">
          <template v-slot:default="scope">
            <span>{{ scope.row.deleteTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="deleteUser"
          :fields-width="fieldsWidth"
          v-if="this.trashEnable"
          :label="$t('commons.delete_user')"
          min-width="120"/>

        <span v-for="(item) in fields" :key="item.key">

          <ms-table-column v-if="item.id == 'num' && !customNum"
                           prop="num"
                           label="ID"
                           sortable
                           :fields-width="fieldsWidth"
                           min-width="120px">
            <template slot-scope="scope">
              <el-tooltip :content="$t('commons.edit')">
                <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            v-if="item.id == 'num' && customNum" prop="customNum"
            label="ID"
            sortable
            :fields-width="fieldsWidth"
            min-width="120px">
            <template slot-scope="scope">
              <el-tooltip :content="$t('commons.edit')">
                <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.customNum }} </a>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column prop="name"
                           sortable
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.scenario_name')"
                           min-width="150px"/>

          <ms-table-column
            prop="level"
            sortable
            :field="item"
            :fields-width="fieldsWidth"
            :filters="apiscenariofilters.LEVEL_FILTERS"
            min-width="150px"
            :label="$t('api_test.automation.case_level')">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level"/>
            </template>
          </ms-table-column>

          <ms-table-column prop="status"
                           :label="$t('test_track.plan.plan_status')"
                           :sortable="trashEnable ? false : true"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :filters="!trashEnable ? apiscenariofilters.STATUS_FILTERS : null"
                           min-width="120px">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.status"/>
            </template>
          </ms-table-column>

          <ms-table-column prop="principalName"
                           min-width="120px"
                           :label="$t('api_test.definition.request.responsible')"
                           :filters="userFilters"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable/>
          <ms-table-column prop="creatorName" min-width="120px"
                           :label="$t('api_test.automation.creator')"
                           :filters="userFilters"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable="custom"/>

          <ms-table-column prop="updateTime"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.update_time')"
                           sortable
                           min-width="180px">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>
          <ms-table-column prop="createTime"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('commons.create_time')"
                           sortable
                           min-width="180px">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>

          <ms-table-column prop="stepTotal"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.step')"
                           min-width="80px"/>
          <ms-table-column prop="lastResult"
                           :label="$t('api_test.automation.last_result')"
                           :filters="uiResultFilters"
                           :field="item"
                           :fields-width="fieldsWidth"
                           sortable
                           min-width="150px">
            <template v-slot:default="{row}">
              <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'SUCCESS'">
                {{ $t('Success') }}
              </el-link>
              <el-link type="danger" @click="showReport(row)" v-else-if="row.lastResult === 'ERROR'">
                {{ $t('Error') }}
              </el-link>
              <el-link type="info" v-else-if="row.lastResult === 'PENDING'">
                {{ $t('Pending') }}
              </el-link>
              <el-link type="info" v-else-if="row.lastResult">
                {{ row.lastResult }}
              </el-link>
              <el-link type="info" v-else>
                {{ $t('Pending') }}
              </el-link>
            </template>
          </ms-table-column>

          <ms-table-column prop="passRate"
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="$t('api_test.automation.passing_rate')"
                           min-width="120px"/>
        </span>

        <template v-slot:opt-before="scope">
          <ms-table-operator-button v-permission=" ['PROJECT_UI_SCENARIO:READ+RUN']"
                                    :tip="$t('api_test.automation.execute')" icon="el-icon-video-play"
                                    class="run-button"
                                    @exec="run(scope.row)" v-if="!scope.row.isStop && !trashEnable"
                                    style="margin-right: 10px;"/>
          <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
            <el-button v-if="!trashEnable" @click.once="stop(scope.row)" size="mini"
                       style="color:white;padding: 0;width: 28px;height: 28px;margin-right: 10px;" class="stop-btn"
                       circle>
              <div style="transform: scale(0.72)">
                <span style="margin-left: -3.5px;font-weight: bold">STOP</span>
              </div>
            </el-button>
          </el-tooltip>

        </template>

      </ms-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
      <div>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="runVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false"
                   size="90%" :before-close="resetRunBtn">
          <sysn-api-report-detail v-loading="reportLoading" :element-loading-text="$t('ui.executing')"
                                  @finish="reportLoading = false" @refresh="search"
                                  :debug="true" :scenario="currentScenario" :scenarioId="scenarioId"
                                  :infoDb="infoDb" :report-id="reportId" :is-ui="true" ref="uiReport"/>
        </el-drawer>

        <!-- 执行结果 -->
        <el-drawer :visible.sync="showReportVisible" :destroy-on-close="true" direction="ltr" :withHeader="true"
                   :modal="false"
                   size="90%">
          <ms-api-report-detail @invisible="showReportVisible = false" @refresh="search" :infoDb="infoDb"
                                :show-cancel-button="false"
                                :report-id="showReportId"/>
        </el-drawer>

      </div>
    </el-card>
  </div>
</template>

<script>
import {getUUID, strMapToObj} from 'metersphere-frontend/src/utils';
import {getCurrentProjectID} from 'metersphere-frontend/src/utils/token';
import {hasLicense} from "metersphere-frontend/src/utils/permission";

import {
  UI_SCENARIO_CONFIGS,
  UI_SCENARIO_CONFIGS_TRASH
} from "metersphere-frontend/src/components/search/search-components";

import {
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField
} from "metersphere-frontend/src/utils/tableUtils";
import {API_SCENARIO_FILTERS} from "metersphere-frontend/src/utils/table-constants";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import {TYPE_TO_C} from "metersphere-frontend/src/model/Setting";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import ListItemDeleteConfirm from "metersphere-frontend/src/components/ListItemDeleteConfirm";
import {set} from "lodash-es";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination"
import {getUiAutomationList} from "@/business/menu/environment/components/ui-related/ui-scenario";
import PlanStatusTableItem from "@/business/menu/environment/components/ui-related/PlanStatusTableItem";
import PriorityTableItem from "@/business/menu/common/PriorityTableItem";

export default {
  name: "MsUiScenarioList",
  components: {
    ListItemDeleteConfirm,
    MsTableAdvSearchBar,
    MsTableSearchBar,
    MsTable,
    MsTableColumn,
    HeaderLabelOperate,
    MsTablePagination,
    PlanStatusTableItem,
    PriorityTableItem,
    MsSearch
  },
  props: {
    referenced: {
      type: Boolean,
      default: false,
    },
    showDrag: {
      type: Boolean,
      default: true,
    },
    isReferenceTable: {
      type: Boolean,
      default: false,
    },
    selectNodeIds: Array,
    selectProjectId: {
      type: String,
      default: ""
    },
    trashEnable: {
      type: Boolean,
      default: false,
    },
    moduleTree: {
      type: Array,
      default() {
        return [];
      },
    },
    moduleOptions: {
      type: Array,
      default() {
        return [];
      },
    },
    //用于判断是否是只读用户
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    customNum: {
      type: Boolean,
      default: false
    },
    batchOperators: {
      type: Array,
      default: null
    },
    initApiTableOpretion: String,
    isRelate: Boolean,
    mode: {
      type: String,
      default: 'view'
    },
  },
  data() {
    return {
      projectName: "",
      result: {},
      tableHeaderKey: "UI_SCENARIO",
      fields: getCustomTableHeader('UI_SCENARIO'),
      fieldsWidth: getCustomTableWidth('UI_SCENARIO'),
      screenHeight: 'calc(100vh - 185px)',//屏幕高度,
      scenarioId: "",
      isMoveBatch: true,
      currentScenario: {},
      schedule: {},
      tableData: [],
      selectDataRange: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      showReportId: "",
      projectEnvMap: new Map(),
      batchReportId: "",
      content: {},
      infoDb: false,
      runVisible: false,
      showReportVisible: false,
      planVisible: false,
      runData: [],
      report: {},
      selectDataSize: 0,
      selectAll: false,
      userFilters: [],
      versionFilters: [],
      operators: [],
      selectRows: new Set(),
      runRows: new Set(),
      isStop: false,
      enableOrderDrag: true,
      debugData: {},
      trashOperators: [
        {
          tip: this.$t('commons.reduction'),
          icon: "el-icon-refresh-left",
          exec: this.reductionApi,
          permissions: ['PROJECT_UI_SCENARIO:READ+EDIT']
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.remove,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_UI_SCENARIO:READ+DELETE']
        },
      ],
      unTrashOperators: [
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.edit,
          permissions: ['PROJECT_UI_SCENARIO:READ+EDIT']
        },
        {
          tip: this.$t('api_test.automation.copy'),
          icon: "el-icon-document-copy",
          exec: this.copy,
          permissions: ['PROJECT_UI_SCENARIO:READ+COPY']
        },
        {
          tip: this.$t('commons.delete'),
          exec: this.remove,
          icon: "el-icon-delete",
          type: "danger",
          permissions: ['PROJECT_UI_SCENARIO:READ+DELETE']
        },
      ],
      buttons: [],
      trashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_UI_SCENARIO:READ+DELETE']
        },
        {
          name: this.$t('commons.batch_restore'),
          handleClick: this.handleBatchRestore
        },
      ],
      unTrashButtons: [
        {
          name: this.$t('api_test.automation.batch_execute'),
          handleClick: this.handleBatchExecute,
          permissions: ['PROJECT_UI_SCENARIO:READ+RUN']
        },
        {
          name: this.$t('test_track.case.batch_edit_case'),
          handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_UI_SCENARIO:READ+EDIT']
        },
        {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_UI_SCENARIO:READ+MOVE_BATCH']
        },
        {
          name: this.$t('api_test.batch_copy'),
          handleClick: this.handleBatchCopy,
          permissions: ['PROJECT_UI_SCENARIO:READ+BATCH_COPY']
        },
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_UI_SCENARIO:READ+DELETE']
        },
        // {
        //   name: this.$t('test_track.case.generate_dependencies'),
        //   handleClick: this.generateGraph,
        //   isXPack: true,
        //   permissions: ['PROJECT_UI_SCENARIO:READ+EDIT']
        // },
        // {
        //   name: this.$t('api_test.automation.batch_add_plan'),
        //   handleClick: this.handleBatchAddCase,
        //   permissions: ['PROJECT_UI_SCENARIO:READ+MOVE_BATCH']
        // },
        // {
        //   name: this.$t('api_test.create_performance_test_batch'),
        //   handleClick: this.batchCreatePerformance,
        //   permissions: ['PROJECT_UI_SCENARIO:READ+CREATE_PERFORMANCE_BATCH']
        // },
      ],
      typeArr: [
        {id: 'level', name: this.$t('test_track.case.priority')},
        {id: 'status', name: this.$t('test_track.plan.plan_status')},
        {
          id: 'principal',
          name: this.$t('api_test.definition.request.responsible'),
          optionMethod: this.getPrincipalOptions
        },
        // {id: 'environmentId', name: this.$t('api_test.definition.request.run_env'), optionMethod: this.getEnvsOptions},
        // 当前不展示运行环境 此处暂时注释
        // {id: 'projectEnv', name: this.$t('api_test.definition.request.run_env')},
      ],
      valueArr: {
        level: [
          {name: 'P0', id: 'P0'},
          {name: 'P1', id: 'P1'},
          {name: 'P2', id: 'P2'},
          {name: 'P3', id: 'P3'}
        ],
        status: [
          {name: this.$t('test_track.plan.plan_status_prepare'), id: 'Prepare'},
          {name: this.$t('test_track.plan.plan_status_running'), id: 'Underway'},
          {name: this.$t('test_track.plan.plan_status_completed'), id: 'Completed'}
        ],
        principal: [],
        environmentId: [],
        projectEnv: [],
        projectId: ''
      },
      graphData: {},
      environmentType: "",
      envGroupId: "",
      uiResultFilters: [
        {text: "Running", value: "RUNNING"},
        {text: "Error", value: "ERROR"},
        {text: "Success", value: "SUCCESS"},
        {text: 'Stopped', value: 'STOPPED'},
        {text: "Pending", value: "PENDING"},
        {text: "Timeout", value: "TIMEOUT"},
      ],
      runRequest: {},
      versionEnable: false,
      reportLoading: false,
      apiscenariofilters: {},
      condition: {
        components: this.trashEnable ? UI_SCENARIO_CONFIGS_TRASH : UI_SCENARIO_CONFIGS
      },
    };
  },
  created() {
    this.apiscenariofilters = API_SCENARIO_FILTERS();
    this.projectId = getCurrentProjectID();
    this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};

    if (!this.batchOperators) {
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
        this.operators = this.trashOperators;
        this.buttons = this.trashButtons;
      } else {
        if (!this.isReferenceTable) {
          this.operators = this.unTrashOperators;
          this.buttons = this.unTrashButtons;
        } else {
          this.operators = this.unTrashOperators;
          this.buttons = this.unTrashButtons;
        }
      }
    }

    if (this.trashEnable) {
      this.condition.orders = [{"name": "delete_time", "type": "desc"}];
    } else {
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
      // 默认创建时间降序
      if (!this.condition.orders) {
        this.condition.orders = [];
      }
      // 会引起列表自定义排序失败
      // this.condition.orders.push({"name": "create_time", "type": "desc"})
    }
    this.search();
    this.getPrincipalOptions([]);

    if (this.isRelate) {
      this.checkVersionEnable(this.selectProjectId);
    } else {
      this.checkVersionEnable(this.projectId);
    }

    // 通知过来的数据跳转到编辑
    if (this.$route.query.resourceId) {
      getUiAutomation(this.$route.query.resourceId).then(response => {
        this.edit(response.data.data);
      })
    }
  },
  watch: {
    selectNodeIds() {
      this.currentPage = 1;
      this.$refs.scenarioTable.clear();
      this.selectProjectId ? this.search(this.selectProjectId) : this.search();
    },
    selectProjectId(v) {
      if (v) {
        this.getPrincipalOptions([]);
      }
    },
    trashEnable() {
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.operators = this.trashOperators;
        this.buttons = this.trashButtons;
      } else {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        this.operators = this.unTrashOperators;
        this.buttons = this.unTrashButtons;
      }
      this.$refs.scenarioTable.clear();
      this.search();
    },
    batchReportId() {
      this.result.loading = true;
      this.getReport();
    },
  },
  computed: {
    isNotRunning() {
      return "Running" !== this.report.status;
    },
    editApiScenarioCaseOrder() {
      return this.editUiScenarioCaseOrder;
    },
  },
  methods: {
    editUiScenarioCaseOrder(request, callback) {
      editOrder(request).then(response => {
        return response.data;
      })
    },
    selectByParam() {
      this.changeSelectDataRangeAll();
      this.search();
    },
    search(projectId) {
      if (this.needRefreshModule()) {
        this.$emit('refreshTree');
      }
      if (this.selectProjectId) {
        projectId = this.selectProjectId;
      }
      this.projectId = getCurrentProjectID();
      this.selectRows = new Set();
      this.condition.moduleIds = this.selectNodeIds;
      if(!this.condition.filters){
        this.condition.filters = {};
      }
      if (this.trashEnable) {
        if (this.condition.filters.status) {
          let arr = ["Trash", ...this.condition.filters.status];
          this.condition.filters.status = arr.filter((item, index) => {
            return arr.indexOf(item) == index;
          });
        } else {
          this.condition.filters.status = ["Trash"];
        }
      } else {
        if (!this.condition.filters.status) {
          this.condition.filters.status = ["Prepare", "Underway", "Completed"];
        }
      }

      if (projectId != null && typeof projectId === 'string') {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

      this.enableOrderDrag = this.condition.orders && this.condition.orders.length > 0 ? false : true;

      //检查是否只查询本周数据
      this.condition.selectThisWeedData = false;
      this.condition.executeStatus = null;
      this.isSelectThissWeekData();
      switch (this.selectDataRange) {
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'unExecute':
          this.condition.executeStatus = 'unExecute';
          break;
        case 'executeFailed':
          this.condition.executeStatus = 'executeFailed';
          break;
        case 'executePass':
          this.condition.executeStatus = 'executePass';
          break;
      }
      if (this.selectDataRange != null) {
        let selectParamArr = this.selectDataRange.split(":");
        if (selectParamArr.length === 2) {
          if (selectParamArr[0] === "list") {
            let ids = selectParamArr[1].split(",");
            this.condition.ids = ids;
          }
        }
      }
      if (this.condition.projectId) {
        this.result.loading = true;
        getUiAutomationList(this.currentPage, this.pageSize, this.condition).then(response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.result.loading = false;
          this.$emit('getTrashCase');
        })
      }
    },
    handleCommand(cmd) {
      let table = this.$refs.scenarioTable;
      switch (cmd) {
        case "table":
          this.condition.selectAll = false;
          table.toggleAllSelection();
          break;
        case "all":
          this.condition.selectAll = true;
          break;
      }
    },
    handleBatchAddCase() {
      this.selectRows = this.$refs.scenarioTable.selectRows;
      this.planVisible = true;
    },
    handleBatchEdit() {
      this.$refs.batchEdit.setScenarioSelectRows(this.$refs.scenarioTable.selectRows, "scenario");
      if (this.condition.selectAll) {
        this.condition.ids = [];
        let param = {};
        this.buildBatchParam(param);
        getUiAutomationListWithIdsAll(param).then(response => {
          let dataRows = response.data;
          this.$refs.batchEdit.open(dataRows.size);
          this.$refs.batchEdit.setAllDataRows(dataRows);
          this.$refs.batchEdit.open(this.$refs.scenarioTable.selectDataCounts);
        })
      } else {
        this.$refs.batchEdit.setAllDataRows(new Set());
        this.$refs.batchEdit.open(this.$refs.scenarioTable.selectDataCounts);
      }
    },
    handleBatchMove() {
      this.isMoveBatch = true;
      this.$refs.testBatchMove.open(this.moduleTree, [], this.moduleOptions);
    },
    handleBatchCopy() {
      this.isMoveBatch = false;
      this.$refs.testBatchMove.open(this.moduleTree, this.$refs.scenarioTable.selectIds, this.moduleOptions);
    },
    moveSave(param) {
      this.buildBatchParam(param);
      param.moduleId = param.nodeId;
      param.modulePath = param.nodePath;
      let url = '/ui/automation/batch/edit';
      if (!this.isMoveBatch)
        url = '/ui/automation/batch/copy';
      this.$refs.testBatchMove.load(true);
      editAutomationBatch(url, param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.load(false);
        this.$refs.testBatchMove.close();
        this.search();
      })
    },
    batchEdit(form) {
      // 批量修改环境
      if (form.type === 'projectEnv') {
        let param = {};
        param.mapping = strMapToObj(form.map);
        param.envMap = strMapToObj(form.projectEnvMap);
        param.environmentType = form.environmentType;
        param.environmentGroupId = form.envGroupId;
        this.$post('/ui/automation/batch/update/env', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      } else {
        // 批量修改其它
        let param = {};
        param[form.type] = form.value;
        this.buildBatchParam(param);
        editAutomationBatchEdit(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        })
      }


    },
    getPrincipalOptions(option) {
      if (this.selectProjectId) {
        //场景导入 切换项目后 更新表头筛选 创建人
        this.$get('/user/project/member/' + this.selectProjectId).then(response => {
          option.push(...response.data);
          this.userFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
        return;
      }
      this.$get('/user/project/member/list').then(response => {
        option.push(...response.data);
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then(response => {
          if (currentVersion) {
            this.versionFilters = response.data.filter(u => u.id === currentVersion).map(u => {
              return {text: u.name, value: u.id};
            });
          } else {
            this.versionFilters = response.data.map(u => {
              return {text: u.name, value: u.id};
            });
          }
        });
      }
    },
    checkVersionEnable(projectId) {
      if (!projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + projectId).then(response => {
          this.versionEnable = response.data;
          if (!response.data) {
            this.fields = this.fields.filter(f => f.id !== 'versionId');
          }
        });
      }
    },
    getEnvsOptions(option) {
      this.$get('/api/environment/list/' + this.projectId).then(response => {
        option.push(...response.data);
        option.forEach(environment => {
          if (!(environment.config instanceof Object)) {
            environment.config = JSON.parse(environment.config);
          }
        });
      });
    },
    cancel() {
      this.planVisible = false;
    },
    addTestPlan(params) {
      let obj = {planIds: params[0], scenarioIds: this.$refs.scenarioTable.selectIds};

      // todo 选取全部数据
      if (this.condition.selectAll) {
        this.$warning(this.$t('api_test.scenario.warning_context'));
      }

      this.planVisible = false;

      obj.mapping = strMapToObj(params[2]);
      obj.envMap = strMapToObj(params[1]);
      obj.environmentType = params[3];
      obj.envGroupId = params[4];

      this.$post("/ui/automation/scenario/plan", obj).then(response => {
        this.$success(this.$t("commons.save_success"));
        this.search();
      });
    },
    getReport() {
      if (this.batchReportId) {
        this.result.loading = false;
        this.$success(this.$t('commons.run_message'));
        this.$refs.taskCenter.open();
      }
    },
    buildBatchParam(param) {
      param.ids = this.$refs.scenarioTable ? this.$refs.scenarioTable.selectIds : [];
      param.projectId = this.projectId;
      param.condition = this.condition;
    },
    handleBatchExecute() {
      let run = {};
      run.id = getUUID();
      //按照列表排序
      let ids = this.orderBySelectRows("id");
      run.ids = ids;
      run.projectId = this.projectId;
      run.condition = this.condition;
      this.runRequest = run;
      this.$refs.runMode.open();
      this.runRows = new Set();
      this.runRows.add(set(this.orderBySelectRows("row")));
    },
    orderBySelectRows(type) {
      let selectIds = this.$refs.scenarioTable.selectIds;
      let array = [];
      for (let i in this.tableData) {
        if (selectIds.indexOf(this.tableData[i].id) !== -1) {
          if (type == "id") {
            array.push(this.tableData[i].id);
          } else if (type == "row") {
            array.push(this.tableData[i]);
          }
        }
      }
      return array;
    },

    handleRunBatch(config) {
      this.infoDb = false;
      let run = {uiConfig: config};
      run.id = getUUID();
      //按照列表排序
      let ids = this.orderBySelectRows("id");
      run.ids = ids;
      run.projectId = this.projectId;
      run.condition = this.condition;
      runBatch(run).then(() => {
        this.runVisible = false;
        this.batchReportId = run.id;
      })
    },
    edit(row) {
      let data = JSON.parse(JSON.stringify(row));
      this.$emit('edit', data);
    },
    reductionApi(row) {
      this.$post("/ui/automation/reduction", [row.id]).then(response => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleBatchRestore() {
      let ids = this.$refs.scenarioTable.selectIds;

      let params = {};
      this.buildBatchParam(params);
      params.ids = ids;

      this.$post("/ui/automation/id/all", params).then(response => {
        let idParams = response.data;
        this.$post("/ui/automation/reduction", idParams).then(response => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      });
    },
    handleDeleteBatch(row) {
      if (this.trashEnable) {
        //let ids = Array.from(this.selectRows).map(row => row.id);
        let param = {};
        this.buildBatchParam(param);
        this.result.loading = true;
        this.$post('/ui/automation/deleteBatchByCondition/', param).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.result.loading = false;
          this.search();
        }).catch(() => {
          this.search();
        });
        return;
      } else {
        let param = {};
        this.buildBatchParam(param);
        this.calcRefMessage(
          {batchOpt: true, name: "", id: param.ids[0]},
          param.ids
        );
      }
    },
    getUiScenario(scenarioId) {
      return new Promise((resolve) => {
        getUiAutomation(scenarioId).then(response => {
          if (response.data) {
            this.currentScenario = response.data;
            this.currentScenario.clazzName = TYPE_TO_C.get("scenario");
            if (response.data.scenarioDefinition != null) {
              let obj = JSON.parse(response.data.scenarioDefinition);
              this.currentScenario.scenarioDefinition = obj;
              this.currentScenario.name = response.data.name;
              if (this.currentScenario.scenarioDefinition && this.currentScenario.scenarioDefinition.hashTree) {
                this.sort(this.currentScenario.scenarioDefinition.hashTree);
              }
              resolve();
            }
          }
        })
      });
    },
    sort(stepArray) {
      for (let i in stepArray) {
        stepArray[i].index = Number(i) + 1;
        if (!stepArray[i].resourceId) {
          stepArray[i].resourceId = getUUID();
        }
        if (!stepArray[i].clazzName) {
          stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
        }
        if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
          stepArray[i].document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
        }
        if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
          stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
        }
        if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
          this.sort(stepArray[i].hashTree);
        }
      }
    },
    execute(row) {
    },
    runRefresh(row) {
      this.$set(row, "isStop", false);
    },
    onDebugMessage(e) {
      if (e.data && e.data.indexOf("MS_TEST_END") !== -1) {
        this.reportLoading = false;
      }
    },
    run(row) {
      this.scenarioId = row.id;
      this.runRows = new Set();
      this.runRows.add(row);
      this.getUiScenario(row.id).then(() => {
        this.doRun(row);
      });
    },
    async doRun(row) {
      this.reportLoading = true;
      let hashTree = this.currentScenario.scenarioDefinition.hashTree;
      validateFirstCommand(hashTree);
      validateProgramController(hashTree);
      //校验selenium配置
      let serverResult = await this.validateSeleniumSetting();
      if (!serverResult) {
        return;
      }
      this.debugData = getUiRunDebugData(this.currentScenario);
      this.reportId = getUUID().substring(0, 8);
      this.runVisible = true;
      this.$set(row, "isStop", true);
    },
    async validateSeleniumSetting() {
      //选择本地调试默认取消性能模式
      const h = this.$createElement;
      //检测是否配置了 个人地址, 检测配置的ip端口 是否正常
      let result = await this.$get("/ui/automation/verify/seleniumServer/sys", null);
      if (result.data) {
        let res = result.data;
        if (res === "ok") {
          return true;
        } else if (res === "connectionErr") {
          this.showServerMessageBox(
            h("p", null, [
              h(
                "span",
                null,
                this.$t("ui.check_grid")
              ),
              h(
                "p",
                {
                  style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                  on: {
                    click: (value) => {
                      this.redirectSetting();
                    },
                  },
                },
                this.$t("ui.view_config")
              ),
            ])
          );
          return false;
        } else {
          this.showServerMessageBox(
            h("p", null, [
              h("span", null, this.$t("ui.check_grid_ip")),
              h(
                "p",
                {
                  style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                  on: {
                    click: (value) => {
                      this.redirectSetting();
                    },
                  },
                },
                this.$t("ui.view_config")
              ),
            ])
          );
          return false;
        }
      }
      return true;
    },
    redirectSetting() {
      window.open("/#/setting/systemparametersetting");
    },
    showServerMessageBox(msg) {
      this.$msgbox({
        title: "",
        message: msg,
        confirmButtonText: this.$t("commons.confirm"),
        cancelButtonText: this.$t("commons.cancel"),
      })
        .then((action) => {
        })
        .catch(() => {
        });
    },
    copy(row) {
    },
    showReport(row) {
      if (this.mode === 'view') {
        this.showReportVisible = true;
        this.infoDb = true;
        this.showReportId = row.reportId;
      }
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isSelectThissWeekData() {
      let dataRange = this.$route.params.dataSelectRange;
      this.selectDataRange = dataRange;
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll");
    },
    calcRefMessage(row, scenarioIds) {
      let param = {scenarioIds};
      this.$post("/ui/automation/check/ref", param).then((response) => {
        let checkResult = response.data;
        let isRef = checkResult.result ? true : false;
        let data = checkResult.data;
        let alertMsg = isRef
          ? this.$t("ui.scenario_is_referenced") + checkResult.result
          : this.$t("ui.confirm_del_scen") + "[" + row.name + "";

        let isSub = alertMsg.length >= 69;
        if (isSub) {
          alertMsg = alertMsg.substring(0, 68) + "... ";
        }
        alertMsg = isRef ? alertMsg + ";" + this.$t("ui.continue_or_not") : alertMsg + "]？";
        if (row.batchOpt && !isRef) {
          let batchParam = {};
          this.buildBatchParam(batchParam);
          let rows = this.$refs.scenarioTable.getSelectRows();
          let tempName = rows.entries().next().value[0].name;
          let name = !tempName ? "" : tempName.length > 15 ? tempName.slice(0, 15) + "..." : tempName;
          if (!batchParam.condition.selectAll && batchParam.ids && batchParam.ids.length) {
            if (batchParam.ids.length == 1) {
              alertMsg = this.$t("ui.confirm_del_scen") + " " + name + " ？";
            } else {
              alertMsg = this.$t("ui.confirm_del") + name + this.$t("ui.deng") + (batchParam.condition.selectAll ? this.total : batchParam.ids.length) + this.$t("ui.ge_scenario");
            }
          } else {
            alertMsg = this.$t("ui.confirm_del") + name + this.$t("ui.deng") + (batchParam.condition.selectAll ? this.total : batchParam.ids.length) + this.$t("ui.ge_scenario");
          }
        }
        const h = this.$createElement;
        let arr = [h("span", null, alertMsg)];
        if (isRef) {
          arr.push(
            h(
              "p",
              {
                style: "color: #6c327a;cursor:pointer;font-size: 10px;",
                on: {
                  click: (value) => {
                    this.showRef(value, {...row, id: data[0].scenarioId});
                  },
                },
              },
              this.$t("ui.view_ref")
            )
          );
        }
        this.showMessageBox(h("p", null, arr), row);
      });
    },
    showRef(value, row) {
      this.$refs.uiCustomShowRef.open(row, "scenario");
    },
    showMessageBox(msg, row) {
      this.$msgbox({
        title: this.$t("ui.delete_scenario_lable"),
        message: msg,
        confirmButtonText: this.$t("commons.confirm"),
        cancelButtonText: this.$t("commons.cancel"),
      })
        .then((action) => {
          if (action === "confirm") {
            this._handleDelete(row, false);
          }
        })
        .catch(() => {
        });
    },
    remove(row) {
      if (this.trashEnable) {
        this.$get('/ui/automation/delete/' + row.id).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
        return;
      }
      this.calcRefMessage(row, [row.id]);
    },
    _handleDelete(api, deleteCurrentVersion) {
      // 删除指定版本
      if (deleteCurrentVersion) {
        this.$get('/ui/automation/delete/' + api.versionId + '/' + api.refId).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirm.close();
          this.search();
        });
      }
      // 删除全部版本
      else {
        let param = {};
        this.buildBatchParam(param);
        if (!api.batchOpt) {
          param.ids = [api.id];
        }
        removeToGcByBatch(param).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.$refs.apiDeleteConfirm.close();
          this.search();
        })
      }
    },
    openScenario(item) {
      this.$emit('openScenario', item);
    },
    exportSide() {
      let param = {};
      this.buildBatchParam(param);
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t("api_test.automation.scenario.check_case"));
        return;
      }
      // this.result.loading = true;
      this.projectId = getCurrentProjectID();
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t("api_test.automation.scenario.check_case"));
        return;
      }
      let fileName = "Metersphere_Ui_" + this.projectName + ".side";
      downloadAutomationExport(param, fileName)
      // this.result.loading = false;
    },
    getConditions() {
      return this.condition;
    },
    needRefreshModule() {
      if (this.initApiTableOpretion === '0') {
        return true;
      } else {
        this.$emit('updateInitApiTableOpretion', '0');
        return false;
      }
    },
    callBackSelectAll(selection) {
      this.$emit('selection', selection);
    },
    callBackSelect(selection) {
      this.$emit('selection', selection);
    },
    batchCreatePerformance() {
      this.$alert(this.$t('api_test.definition.request.batch_to_performance_confirm') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.infoDb = false;
            let param = {};
            this.buildBatchParam(param);
            this.$post('/ui/automation/batchGenPerformanceTestJmx/', param, response => {
              let returnDataList = response.data;
              let jmxObjList = [];
              returnDataList.forEach(item => {
                let jmxObj = {};
                jmxObj.name = item.name;
                jmxObj.xml = item.xml;
                jmxObj.attachFiles = item.attachFiles;
                jmxObj.attachByteFiles = item.attachByteFiles;
                jmxObj.scenarioId = item.id;
                jmxObj.version = item.version;
                jmxObjList.push(jmxObj);
              });
              commandStore.scenarioJmxs = {
                name: 'Scenarios',
                jmxs: jmxObjList
              };
              this.$router.push({
                path: "/performance/test/create"
              });
            });
          }
        }
      });
    },
    stop(row) {
      let url = "/ui/automation/stop/" + this.reportId;
      this.$get(url).then(() => {
        this.$set(row, "isStop", false);
      });
    },
    hideStopBtn(scenarioId) {
      for (let data of this.tableData) {
        if (scenarioId && scenarioId === data.id) {
          this.$set(data, "isStop", false);
        }
      }
    },
    resetRunBtn() {
      if (this.runRows.size > 0) {
        for (var r of this.runRows) {
          this.$set(r, "isStop", false);
        }
      }
      this.runVisible = false;
      this.$refs.uiReport.cleanHeartBeat();
    },
    getSelectRows(){
      return this.$refs.scenarioTable.getSelectRows();
    }
  }
};
</script>

<style scoped>
:deep(.el-drawer__header) {
  margin-bottom: 0px;
}

:deep(.el-table__fixed-body-wrapper) {
  z-index: auto !important;
}

:deep(.el-table__fixed-right) {
  height: 100% !important;
}

:deep(.el-card__header) {
  padding: 10px;
}

.stop-btn {
  background-color: #E62424;
  border-color: #dd3636;
  color: white;
}

.plan-case-env {
  display: inline-block;
  padding: 0 0;
  max-width: 350px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
  margin-left: 5px;
}

.project-name {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 80px;
  vertical-align: middle;
}

.project-env {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  width: 50px;
  text-overflow: ellipsis;
  vertical-align: middle;
}


.search-input {
  float: right;
  width: 250px;
}

.adv-search-bar {
  float: right;
  margin-top: 5px;
  margin-right: 10px;
}
</style>
