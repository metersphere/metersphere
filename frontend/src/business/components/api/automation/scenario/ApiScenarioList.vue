<template>
  <div>
    <el-card class="table-card-nopadding" v-loading="result.loading">
      <ms-table-header :condition.sync="condition" @search="selectByParam" title=""
                       :show-create="false" :tip="$t('commons.search_by_id_name_tag')"/>

      <el-table ref="scenarioTable" border :data="tableData" class="adjust-table ms-select-all-fixed"
                @sort-change="sort"
                @filter-change="filter"
                @select-all="handleSelectAll"
                @select="handleSelect"
                @header-dragend="headerDragend"
                :height="screenHeight">

        <el-table-column type="selection" width="50"/>

        <ms-table-header-select-popover v-show="total>0"
                                        :page-size="pageSize>total?total:pageSize"
                                        :total="total"
                                        :select-data-counts="selectDataCounts"
                                        @selectPageAll="isSelectDataAll(false)"
                                        @selectAll="isSelectDataAll(true)"/>

        <el-table-column v-if="!referenced" width="30" min-width="30" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="trashEnable ? trashButtons: buttons"
                           :size="selectDataCounts"/>
          </template>
        </el-table-column>
        <template v-for="(item, index) in tableLabel">
          <el-table-column v-if="item.id == 'num' && !customNum" prop="num" label="ID"
                           sortable="custom"
                           min-width="120px"
                           show-overflow-tooltip :key="index">
            <template slot-scope="scope">
              <!--<span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.num }} </span>-->
              <el-tooltip content="编辑">
                <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'num' && customNum" prop="customNum" label="ID"
                           sortable="custom"
                           min-width="120px"
                           show-overflow-tooltip :key="index">
            <template slot-scope="scope">
              <!--<span style="cursor:pointer" v-if="isReadOnly"> {{ scope.row.customNum }} </span>-->
              <el-tooltip content="编辑">
                <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.customNum }} </a>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'name'" prop="name"
                           sortable="custom"
                           :label="$t('api_test.automation.scenario_name')"
                           show-overflow-tooltip
                           min-width="150px"
                           :key="index"
          />
          <el-table-column v-if="item.id == 'level'" prop="level"
                           sortable="custom"
                           column-key="level"
                           :filters="LEVEL_FILTERS"
                           min-width="130px"
                           :label="$t('api_test.automation.case_level')"
                           show-overflow-tooltip :key="index">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level"/>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'status'" prop="status" :label="$t('test_track.plan.plan_status')"
                           sortable="custom"
                           column-key="status"
                           :filters="STATUS_FILTERS"
                           show-overflow-tooltip min-width="120px" :key="index">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'tags'" prop="tags" min-width="120px"
                           :label="$t('api_test.automation.tag')" :key="index">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                      :content="itemName" :show-tooltip="true"
                      tooltip style="margin-left: 0px; margin-right: 2px"/>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'principal'" prop="principal" min-width="120px"
                           :label="$t('api_test.definition.api_principal')"
                           :filters="userFilters"
                           column-key="principal"
                           sortable="custom"
                           show-overflow-tooltip
                           :key="index"/>
          <el-table-column v-if="item.id == 'userId'" prop="userId" min-width="120px"
                           :label="$t('api_test.automation.creator')"
                           :filters="userFilters"
                           column-key="userId"
                           sortable="custom"
                           show-overflow-tooltip
                           :key="index"/>
          <el-table-column v-if="item.id == 'updateTime'" prop="updateTime"
                           :label="$t('api_test.automation.update_time')" sortable="custom" min-width="180px"
                           :key="index">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'stepTotal'" prop="stepTotal" :label="$t('api_test.automation.step')"
                           min-width="80px"
                           show-overflow-tooltip :key="index"/>
          <el-table-column v-if="item.id == 'lastResult'" prop="lastResult"
                           :label="$t('api_test.automation.last_result')"
                           :filters="RESULT_FILTERS"

                           sortable="custom" column-key="last_result" min-width="130px" :key="index">
            <template v-slot:default="{row}">
              <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">
                {{ $t('api_test.automation.success') }}
              </el-link>
              <el-link type="danger" @click="showReport(row)" v-if="row.lastResult === 'Fail'">
                {{ $t('api_test.automation.fail') }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'passRate'" prop="passRate"
                           :label="$t('api_test.automation.passing_rate')"
                           min-width="120px"
                           show-overflow-tooltip :key="index"/>
        </template>
        <el-table-column fixed="right" :label="$t('commons.operating')" width="190px" v-if="!referenced && !isReadOnly">
          <template slot="header">
            <header-label-operate @exec="customHeader"/>
          </template>
          <template v-slot:default="{row}">
            <div v-if="trashEnable">
              <ms-table-operator-button :tip="$t('commons.reduction')" icon="el-icon-refresh-left"
                                        @exec="reductionApi(row)"/>
              <ms-table-operator-button :tip="$t('api_test.automation.remove')" icon="el-icon-delete"
                                        @exec="remove(row)" type="danger"/>
            </div>
            <div v-else>
              <ms-table-operator-button :tip="$t('api_test.automation.edit')" icon="el-icon-edit" @exec="edit(row)"
                                        v-permission="['PROJECT_API_SCENARIO:READ+EDIT']"/>
              <ms-table-operator-button class="run-button"
                                        :tip="$t('api_test.automation.execute')"
                                        icon="el-icon-video-play"
                                        v-permission="['PROJECT_API_SCENARIO:READ+RUN']"
                                        @exec="execute(row)"/>
              <ms-table-operator-button :tip="$t('api_test.automation.copy')" icon="el-icon-document-copy" type=""
                                        v-permission="['PROJECT_API_SCENARIO:READ+COPY']"
                                        @exec="copy(row)"/>
              <ms-table-operator-button :tip="$t('api_test.automation.remove')"
                                        v-permission="['PROJECT_API_SCENARIO:READ+DELETE']"
                                        icon="el-icon-delete" @exec="remove(row)" type="danger"/>
              <ms-scenario-extend-buttons style="display: contents" @openScenario="openScenario" :row="row"/>
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
        <!--测试计划-->
        <el-drawer :visible.sync="planVisible" :destroy-on-close="true" direction="ltr" :withHeader="false"
                   :title="$t('test_track.plan_view.test_result')" :modal="false" size="90%">
          <ms-test-plan-list @addTestPlan="addTestPlan(arguments)" @cancel="cancel" ref="testPlanList"
                             :scenario-condition="condition" :row="selectRows"/>
        </el-drawer>
      </div>
    </el-card>

    <batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr"
                :dialog-title="$t('test_track.case.batch_edit_case')"/>
    <batch-move @refresh="search" @moveSave="moveSave" ref="testBatchMove"/>
    <ms-run-mode @handleRunBatch="handleRunBatch" ref="runMode"/>
  </div>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import MsTag from "../../../common/components/MsTag";
import {downloadFile, getUUID, strMapToObj} from "@/common/js/utils";
import MsApiReportDetail from "../report/ApiReportDetail";
import MsTableMoreBtn from "./TableMoreBtn";
import MsScenarioExtendButtons from "@/business/components/api/automation/scenario/ScenarioExtendBtns";
import MsTestPlanList from "./testplan/TestPlanList";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import {API_SCENARIO_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import PriorityTableItem from "../../../track/common/tableItems/planview/PriorityTableItem";
import PlanStatusTableItem from "../../../track/common/tableItems/plan/PlanStatusTableItem";
import BatchEdit from "../../../track/case/components/BatchEdit";
import {API_SCENARIO_LIST, PROJECT_NAME, WORKSPACE_ID} from "../../../../../common/js/constants";
import EnvironmentSelect from "../../definition/components/environment/EnvironmentSelect";
import BatchMove from "../../../track/case/components/BatchMove";
import MsRunMode from "./common/RunMode";

import {
  _filter,
  _handleSelect,
  _handleSelectAll,
  _sort,
  getLabel,
  getSelectDataCounts,
  setUnSelectIds,
  toggleAllSelection
} from "@/common/js/tableUtils";
import {Api_Scenario_List} from "@/business/components/common/model/JsonData";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import {API_SCENARIO_FILTERS} from "@/common/js/table-constants";

export default {
  name: "MsApiScenarioList",
  components: {
    HeaderLabelOperate,
    HeaderCustom,
    BatchMove,
    EnvironmentSelect,
    BatchEdit,
    PlanStatusTableItem,
    PriorityTableItem,
    MsTableHeaderSelectPopover,
    MsTablePagination,
    MsTableMoreBtn,
    ShowMoreBtn,
    MsTableHeader,
    MsTag,
    MsApiReportDetail,
    MsScenarioExtendButtons,
    MsTestPlanList,
    MsTableOperatorButton,
    MsRunMode
  },
  props: {
    referenced: {
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
    }
  },
  data() {
    return {
      result: {},
      type: API_SCENARIO_LIST,
      headerItems: Api_Scenario_List,
      tableLabel: [],
      screenHeight: document.documentElement.clientHeight - 280,//屏幕高度,
      condition: {
        components: API_SCENARIO_CONFIGS
      },
      currentScenario: {},
      schedule: {},
      selection: [],
      tableData: [],
      selectDataRange: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      batchReportId: "",
      content: {},
      infoDb: false,
      runVisible: false,
      planVisible: false,
      runData: [],
      report: {},
      selectDataSize: 0,
      selectAll: false,
      userFilters: [],
      buttons: [
        {
          name: this.$t('api_test.automation.batch_add_plan'),
          handleClick: this.handleBatchAddCase,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH']
        },
        {
          name: this.$t('api_test.automation.batch_execute'),
          handleClick: this.handleBatchExecute,
          permissions: ['PROJECT_API_SCENARIO:READ+RUN']
        },
        {
          name: this.$t('test_track.case.batch_edit_case'),
          handleClick: this.handleBatchEdit,
          permissions: ['PROJECT_API_SCENARIO:READ+EDIT']
        },
        {
          name: this.$t('test_track.case.batch_move_case'),
          handleClick: this.handleBatchMove,
          permissions: ['PROJECT_API_SCENARIO:READ+MOVE_BATCH']
        },
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },

      ],
      trashButtons: [
        {
          name: this.$t('api_test.definition.request.batch_delete'),
          handleClick: this.handleDeleteBatch,
          permissions: ['PROJECT_API_SCENARIO:READ+DELETE']
        },
        {
          name: "批量恢复", handleClick: this.handleBatchRestore
        },
      ],
      isSelectAllDate: false,
      selectRows: new Set(),
      selectDataCounts: 0,
      ...API_SCENARIO_FILTERS,
      typeArr: [
        {id: 'level', name: this.$t('test_track.case.priority')},
        {id: 'status', name: this.$t('test_track.plan.plan_status')},
        {
          id: 'principal',
          name: this.$t('api_test.definition.request.responsible'),
          optionMethod: this.getPrincipalOptions
        },
        // {id: 'environmentId', name: this.$t('api_test.definition.request.run_env'), optionMethod: this.getEnvsOptions},
        {id: 'projectEnv', name: this.$t('api_test.definition.request.run_env')},
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
      },
    };
  },
  created() {
    this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
    this.search();
    this.getPrincipalOptions([]);
  },
  watch: {
    selectNodeIds() {
      this.currentPage = 1;
      this.condition.selectAll = false;
      this.condition.unSelectIds = [];
      this.selectDataCounts = 0;
      this.selectProjectId ? this.search(this.selectProjectId) : this.search();
    },
    trashEnable() {
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
      } else {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
      }
      this.condition.selectAll = false;
      this.condition.unSelectIds = [];
      this.selectDataCounts = 0;

      this.search();
    },
    batchReportId() {
      this.result.loading = true;
      this.getReport();
    }
  },
  computed: {
    isNotRunning() {
      return "Running" !== this.report.status;
    },
    projectId() {
      return this.$store.state.projectId;
    },
  },
  methods: {
    customHeader() {
      this.$refs.headerCustom.open(this.tableLabel);
    },
    selectByParam() {
      this.changeSelectDataRangeAll();
      this.search();
    },
    search(projectId) {
      this.selectRows = new Set();
      this.condition.moduleIds = this.selectNodeIds;
      if (this.trashEnable) {
        this.condition.filters = {status: ["Trash"]};
        this.condition.moduleIds = [];
      }

      // todo
      if (projectId != null && typeof projectId === 'string') {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

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
      this.selection = [];

      if (!this.condition.selectAll) {
        this.condition.selectAll = false;
        this.condition.unSelectIds = [];
        this.selectDataCounts = 0;
      }

      let url = "/api/automation/list/" + this.currentPage + "/" + this.pageSize;
      if (this.condition.projectId) {
        this.result.loading = true;
        this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });

          if (!this.condition.selectAll) {
            this.condition.unSelectIds = response.data.listObject.map(s => s.id);
          }

          this.$nextTick(function () {
            if (this.$refs.scenarioTable) {
              setTimeout(this.$refs.scenarioTable.doLayout, 200);
            }
            this.checkTableRowIsSelect();
          });
        });
      }
      getLabel(this, API_SCENARIO_LIST);
    },
    checkTableRowIsSelect() {
      //如果默认全选的话，则选中应该选中的行
      if (this.condition.selectAll) {
        let unSelectIds = this.condition.unSelectIds;
        this.tableData.forEach(row => {
          if (unSelectIds.indexOf(row.id) < 0) {
            this.$refs.scenarioTable.toggleRowSelection(row, true);

            //默认全选，需要把选中对行添加到selectRows中。不然会影响到勾选函数统计
            if (!this.selectRows.has(row)) {
              this.$set(row, "showMore", true);
              this.selectRows.add(row);
            }
          } else {
            //不勾选的行，也要判断是否被加入了selectRow中。加入了的话就去除。
            if (this.selectRows.has(row)) {
              this.$set(row, "showMore", false);
              this.selectRows.delete(row);
            }
          }
        });
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
      this.planVisible = true;
    },
    handleBatchEdit() {
      this.$refs.batchEdit.setScenarioSelectRows(this.selectRows, "scenario");
      if (this.condition.selectAll) {
        this.condition.ids = [];
        let param = {};
        this.buildBatchParam(param);
        this.$post('/api/automation/listWithIds/all', param, response => {
          let dataRows = response.data;
          this.$refs.batchEdit.open(dataRows.size);
          this.$refs.batchEdit.setAllDataRows(dataRows);
          this.$refs.batchEdit.open(this.selectDataCounts);
        });
      } else {
        this.$refs.batchEdit.setAllDataRows(new Set());
        this.$refs.batchEdit.open(this.selectDataCounts);
      }
    },
    handleBatchMove() {
      this.$refs.testBatchMove.open(this.moduleTree, [], this.moduleOptions);
    },
    moveSave(param) {
      this.buildBatchParam(param);
      param.apiScenarioModuleId = param.nodeId;
      param.modulePath = param.nodePath;
      this.$post('/api/automation/batch/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.testBatchMove.close();
        this.search();
      });
    },
    batchEdit(form) {
      // 批量修改环境
      if (form.type === 'projectEnv') {
        let param = {};
        param.mapping = strMapToObj(form.map);
        param.envMap = strMapToObj(form.projectEnvMap);
        this.$post('/api/automation/batch/update/env', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      } else {
        // 批量修改其它
        let param = {};
        param[form.type] = form.value;
        this.buildBatchParam(param);
        this.$post('/api/automation/batch/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      }


    },
    getPrincipalOptions(option) {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        option.push(...response.data);
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    getEnvsOptions(option) {
      this.$get('/api/environment/list/' + this.projectId, response => {
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
      let obj = {planIds: params[0], scenarioIds: this.selection};

      // todo 选取全部数据
      if (this.isSelectAllDate) {
        this.$warning("暂不支持批量添加所有场景到测试计划！");
      }

      this.planVisible = false;

      obj.mapping = strMapToObj(params[2]);
      obj.envMap = strMapToObj(params[1]);

      this.$post("/api/automation/scenario/plan", obj, response => {
        this.$success(this.$t("commons.save_success"));
        this.search();
      });
    },
    getReport() {
      if (this.batchReportId) {
        this.result.loading = false;
        this.$success("批量执行成功，请到报告页面查看详情！");
      }
    },
    buildBatchParam(param) {
      param.ids = Array.from(this.selectRows).map(row => row.id);
      param.projectId = this.projectId;
      param.condition = this.condition;
    },
    handleBatchExecute() {
      this.$refs.runMode.open();

    },
    orderBySelectRows(rows) {
      let selectIds = Array.from(rows).map(row => row.id);
      let array = [];
      for (let i in this.tableData) {
        if (selectIds.indexOf(this.tableData[i].id) !== -1) {
          array.push(this.tableData[i].id);
        }
      }
      return array;
    },

    handleRunBatch(config) {
      this.infoDb = false;
      let url = "/api/automation/run/batch";
      let run = {config: config};
      run.id = getUUID();
      //按照列表排序
      let ids = this.orderBySelectRows(this.selectRows);
      run.ids = ids;
      run.projectId = this.projectId;
      run.condition = this.condition;
      this.$post(url, run, response => {
        let data = response.data;
        this.runVisible = false;
        this.batchReportId = run.id;
      });
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows, this.condition);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.$emit('selection', selection);
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.$emit('selection', selection);
    },

    isSelectDataAll(data) {
      // this.condition.selectAll = data;
      // setUnSelectIds(this.tableData, this.condition, this.selectRows);
      // this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      // toggleAllSelection(this.$refs.scenarioTable, this.tableData, this.selectRows);
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
    edit(row) {
      let data = JSON.parse(JSON.stringify(row));
      this.$emit('edit', data);
    },
    reductionApi(row) {
      this.$post("/api/automation/reduction", [row.id], response => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleBatchRestore() {
      let ids = Array.from(this.selectRows).map(row => row.id);
      this.$post("/api/automation/reduction", ids, response => {
        this.$success(this.$t('commons.save_success'));
        this.search();
      });
    },
    handleDeleteBatch(row) {
      if (this.trashEnable) {
        //let ids = Array.from(this.selectRows).map(row => row.id);
        let param = {};
        this.buildBatchParam(param);
        this.$post('/api/automation/deleteBatchByCondition/', param, () => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
        return;
      }
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            //let ids = Array.from(this.selectRows).map(row => row.id);
            let param = {};
            this.buildBatchParam(param);
            this.$post('/api/automation/removeToGcByBatch/', param, () => {
              this.$success(this.$t('commons.delete_success'));
              this.search();
            });
          }
        }
      });
    },

    execute(row) {
      this.infoDb = false;
      let url = "/api/automation/run";
      let run = {};
      let scenarioIds = [];
      scenarioIds.push(row.id);
      run.id = getUUID();
      run.projectId = this.projectId;
      run.ids = scenarioIds;
      this.$post(url, run, response => {
        let data = response.data;
        this.runVisible = true;
        this.reportId = run.id;
      });
    },
    copy(row) {
      let rowParam = JSON.parse(JSON.stringify(row));
      rowParam.copy = true;
      rowParam.name = 'copy_' + rowParam.name;
      rowParam.customNum = '';
      this.$emit('edit', rowParam);
    },
    showReport(row) {
      this.runVisible = true;
      this.infoDb = true;
      this.reportId = row.reportId;
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isSelectThissWeekData() {
      let dataRange = this.$route.params.dataSelectRange;
      this.selectDataRange = dataRange;
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll");
    },
    remove(row) {
      if (this.trashEnable) {
        this.$get('/api/automation/delete/' + row.id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
        return;
      }
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            // let ids = [row.id];
            let param = {};
            this.buildBatchParam(param);
            param.ids = [row.id];
            this.$post('/api/automation/removeToGcByBatch/', param, () => {
              // this.$post('/api/automation/removeToGc/', ids, () => {
              this.$success(this.$t('commons.delete_success'));
              this.search();
            });
          }
        }
      });
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
    headerDragend(newWidth, oldWidth, column, event) {
      let finalWidth = newWidth;
      if (column.minWidth > finalWidth) {
        finalWidth = column.minWidth;
      }
      column.width = finalWidth;
      column.realWidth = finalWidth;
    },
    openScenario(item) {
      this.$emit('openScenario', item);
    },
    exportApi() {
      let param = {};
      this.buildBatchParam(param);
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t("api_test.automation.scenario.check_case"));
        return;
      }
      this.result.loading = true;
      this.result = this.$post("/api/automation/export", param, response => {
        this.result.loading = false;
        let obj = response.data;
        this.buildApiPath(obj.data);
        downloadFile("Metersphere_Scenario_" + localStorage.getItem(PROJECT_NAME) + ".json", JSON.stringify(obj));
      });
    },
    exportJmx() {
      let param = {};
      this.buildBatchParam(param);
      if (param.ids === undefined || param.ids.length < 1) {
        this.$warning(this.$t("api_test.automation.scenario.check_case"));
        return;
      }
      this.result.loading = true;
      this.result = this.$post("/api/automation/export/jmx", param, response => {
        this.result.loading = false;
        let obj = response.data;
        if (obj && obj.length > 0) {
          obj.forEach(item => {
            downloadFile(item.name + ".jmx", item.jmx);
          });
        }
      });
    },
    buildApiPath(scenarios) {
      scenarios.forEach((scenario) => {
        this.moduleOptions.forEach(item => {
          if (scenario.moduleId === item.id) {
            scenario.modulePath = item.path;
          }
        });
      });
    },
    getConditions() {
      return this.condition;
    }
  }
};
</script>

<style scoped>
/deep/ .el-drawer__header {
  margin-bottom: 0px;
}

/deep/ .run-button {
  background-color: #409EFF;
  border-color: #409EFF;
}

/deep/ .el-table__fixed-body-wrapper {
  z-index: auto !important;
}

/deep/ .el-table__fixed-right {
  height: 100% !important;
}

/deep/ .el-table__fixed {
  height: 110px !important;
}

/deep/ .el-card__header {
  padding: 10px;
}

/deep/ .el-table__fixed-body-wrapper {
  top: 60px !important;
}
</style>
