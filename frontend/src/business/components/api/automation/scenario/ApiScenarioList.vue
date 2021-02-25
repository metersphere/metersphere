<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="selectByParam" title=""
                         :show-create="false" :tip="$t('commons.search_by_id_name_tag')"/>
      </template>

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
                                        @selectPageAll="isSelectDataAll(false)"
                                        @selectAll="isSelectDataAll(true)"/>

        <el-table-column v-if="!referenced" width="30" min-width="30" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
          </template>
        </el-table-column>
        <template v-for="(item, index) in tableLabel">
          <el-table-column v-if="item.id == 'num'" prop="num" label="ID"
                           sortable="custom"
                           min-width="120px"
                           show-overflow-tooltip :key="index">
            <template slot-scope="scope">
              <el-tooltip content="编辑">
                <a style="cursor:pointer" @click="edit(scope.row)"> {{ scope.row.num }} </a>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'name'" prop="name"
                           sortable="custom"
                           :label="$t('api_test.automation.scenario_name')"
                           show-overflow-tooltip
                           min-width="120px"
                           :key="index"
          />
          <el-table-column v-if="item.id == 'level'" prop="level"
                           sortable="custom"
                           column-key="level"
                           :filters="levelFilters"
                           min-width="120px"
                           :label="$t('api_test.automation.case_level')"
                           show-overflow-tooltip :key="index">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level"/>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'status'" prop="status" :label="$t('test_track.plan.plan_status')"
                           sortable="custom"
                           column-key="status"
                           :filters="statusFilters"
                           show-overflow-tooltip min-width="120px" :key="index">
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'tags'" prop="tags" min-width="120px"
                           :label="$t('api_test.automation.tag')" :key="index">
            <template v-slot:default="scope">
              <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain" :content="itemName" style="margin-left: 5px"/>
            </template>
          </el-table-column>
          <el-table-column v-if="item.id == 'userId'" prop="userId" min-width="120px"
                           :label="$t('api_test.automation.creator')"
                           :filters="userFilters"
                           column-key="user_id"
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
                           :filters="resultFilters"

                           sortable="custom" column-key="last_result" min-width="120px" :key="index">
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
        <el-table-column fixed="right" :label="$t('commons.operating')" width="190px" v-if="!referenced">
          <template slot="header">
            <header-label-operate @exec="customHeader"/>
          </template>
          <template v-slot:default="{row}">
            <div v-if="trashEnable">
              <ms-table-operator-button :tip="$t('commons.reduction')" icon="el-icon-refresh-left"
                                        @exec="reductionApi(row)" v-tester/>
              <ms-table-operator-button :tip="$t('api_test.automation.remove')" icon="el-icon-delete"
                                        @exec="remove(row)" type="danger" v-tester/>
            </div>
            <div v-else>
              <ms-table-operator-button :tip="$t('api_test.automation.edit')" icon="el-icon-edit" @exec="edit(row)"
                                        v-tester/>
              <ms-table-operator-button class="run-button" :is-tester-permission="true"
                                        :tip="$t('api_test.automation.execute')"
                                        icon="el-icon-video-play"
                                        @exec="execute(row)" v-tester/>
              <ms-table-operator-button :tip="$t('api_test.automation.copy')" icon="el-icon-document-copy" type=""
                                        @exec="copy(row)"/>
              <ms-table-operator-button :tip="$t('api_test.automation.remove')" icon="el-icon-delete" @exec="remove(row)" type="danger" v-tester/>
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
          <ms-test-plan-list @addTestPlan="addTestPlan" @cancel="cancel"/>
        </el-drawer>
      </div>
    </el-card>

    <batch-edit ref="batchEdit" @batchEdit="batchEdit" :typeArr="typeArr" :value-arr="valueArr" :dialog-title="$t('test_track.case.batch_edit_case')">
      <template v-slot:value>
        <environment-select :current-data="{}" :project-id="projectId"/>
      </template>
    </batch-edit>

    <batch-move @refresh="search" @moveSave="moveSave" ref="testBatchMove"/>

  </div>
</template>

<script>
  import MsTableHeader from "@/business/components/common/components/MsTableHeader";
  import MsTablePagination from "@/business/components/common/pagination/TablePagination";
  import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
  import MsTag from "../../../common/components/MsTag";
  import {downloadFile, getCurrentProjectID, getCurrentUser, getUUID} from "@/common/js/utils";
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
  import {API_SCENARIO_LIST, TEST_CASE_LIST, TEST_PLAN_LIST, WORKSPACE_ID} from "../../../../../common/js/constants";
  import {PROJECT_NAME} from "../../../../../common/js/constants";
  import EnvironmentSelect from "../../definition/components/environment/EnvironmentSelect";
  import BatchMove from "../../../track/case/components/BatchMove";
  import {_sort, getLabel} from "@/common/js/tableUtils";
  import {Api_Scenario_List} from "@/business/components/common/model/JsonData";
  import HeaderCustom from "@/business/components/common/head/HeaderCustom";
  import {
    _filter,
    _handleSelect,
    _handleSelectAll,
    getSelectDataCounts,
    setUnSelectIds, toggleAllSelection
  } from "@/common/js/tableUtils";
  import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";

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
      MsTableOperatorButton
    },
    props: {
      referenced: {
        type: Boolean,
        default: false,
      },
      selectNodeIds: Array,
      trashEnable: {
        type: Boolean,
        default: false,
      },
      moduleTree: {
        type: Array,
        default() {
          return []
        },
      },
      moduleOptions: {
        type: Array,
        default() {
          return []
        },
      }
    },
    data() {
      return {
        type: API_SCENARIO_LIST,
        headerItems: Api_Scenario_List,
        tableLabel: Api_Scenario_List,
        loading: false,
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
        projectId: "",
        runData: [],
        report: {},
        selectDataSize: 0,
        selectAll: false,
        userFilters: [],
        buttons: [
          {
            name: this.$t('api_test.automation.batch_add_plan'), handleClick: this.handleBatchAddCase
          },
          {
            name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleBatchExecute
          },
          {
            name: this.$t('test_track.case.batch_edit_case'), handleClick: this.handleBatchEdit
          },
          {
            name: this.$t('test_track.case.batch_move_case'), handleClick: this.handleBatchMove
          }
        ],
        isSelectAllDate: false,
        selectRows: new Set(),
        selectDataCounts: 0,
        typeArr: [
          {id: 'level', name: this.$t('test_track.case.priority')},
          {id: 'status', name: this.$t('test_track.plan.plan_status')},
          {id: 'principal', name: this.$t('api_test.definition.request.responsible'), optionMethod: this.getPrincipalOptions},
          {id: 'environmentId', name: this.$t('api_test.definition.request.run_env'), optionMethod: this.getEnvsOptions},
        ],
        statusFilters: [
          {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
          {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
          {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'},
          {text: this.$t('test_track.plan.plan_status_trash'), value: 'Trash'},
        ],
        levelFilters: [
          {text: 'P0', value: 'P0'},
          {text: 'P1', value: 'P1'},
          {text: 'P2', value: 'P2'},
          {text: 'P3', value: 'P3'}
        ],
        resultFilters: [
          {text: 'Fail', value: 'Fail'},
          {text: 'Success', value: 'Success'}
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
          environmentId: []
        },
      }
    },
    created() {
      this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
      this.projectId = getCurrentProjectID();
      this.search();
      this.getPrincipalOptions([]);
    },
    watch: {
      selectNodeIds() {
        this.search();
      },
      trashEnable() {
        if (this.trashEnable) {
          this.condition.filters = {status: ["Trash"]};
          this.condition.moduleIds = [];
        } else {
          this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        }
        this.search();
      },
      batchReportId() {
        this.loading = true;
        this.getReport();
      }
    },
    computed: {
      isNotRunning() {
        return "Running" !== this.report.status;
      }
    },
    methods: {
      customHeader() {
        this.$refs.headerCustom.open(this.tableLabel)
      },
      selectByParam() {
        this.changeSelectDataRangeAll();
        this.search();
      },
      search(projectId) {
        this.selectRows = new Set();
        getLabel(this, API_SCENARIO_LIST);
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
        this.selectAll = false;
        this.unSelection = [];
        this.selectDataCounts = 0;
        let url = "/api/automation/list/" + this.currentPage + "/" + this.pageSize;
        if (this.condition.projectId) {
          this.loading = true;
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
            this.unSelection = data.listObject.map(s => s.id);
          });
        }
      },
      handleCommand(cmd) {
        let table = this.$refs.scenarioTable;
        switch (cmd) {
          case "table":
            this.selectAll = false;
            table.toggleAllSelection();
            break;
          case "all":
            this.selectAll = true;
            break
        }
      },
      handleBatchAddCase() {
        this.planVisible = true;
      },
      handleBatchEdit() {
        this.$refs.batchEdit.open(this.selectDataCounts);
      },
      handleBatchMove() {
        this.$refs.testBatchMove.open(this.moduleTree, [], this.moduleOptions);
      },
      moveSave(param) {
        this.buildBatchParam(param);
        param.apiScenarioModuleId = param.nodeId;
        this.$post('/api/automation/batch/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.$refs.testBatchMove.close();
          this.search();
        });
      },
      batchEdit(form) {
        let param = {};
        param[form.type] = form.value;
        this.buildBatchParam(param);
        this.$post('/api/automation/batch/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      },
      getPrincipalOptions(option) {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          option.push(...response.data);
          this.userFilters = response.data.map(u => {
            return {text: u.name, value: u.id}
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
            environment.name = environment.name + (environment.config.httpConfig.socket ?
              (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '');
          });
        });
      },
      cancel() {
        this.planVisible = false;
      },
      addTestPlan(plans) {
        let obj = {planIds: plans, scenarioIds: this.selection};

        obj.projectId = getCurrentProjectID();
        obj.selectAllDate = this.isSelectAllDate;
        obj.unSelectIds = this.unSelection;
        obj = Object.assign(obj, this.condition);

        this.planVisible = false;
        this.$post("/api/automation/scenario/plan", obj, response => {
          this.$success(this.$t("commons.save_success"));
        });
      },
      getReport() {
        if (this.batchReportId) {
          let url = "/api/scenario/report/get/" + this.batchReportId;
          this.$get(url, response => {
            this.report = response.data || {};
            if (response.data) {
              if (this.isNotRunning) {
                try {
                  this.content = JSON.parse(this.report.content);
                } catch (e) {
                  throw e;
                }
                this.loading = false;
                this.$success("批量执行成功，请到报告页面查看详情！");
              } else {
                setTimeout(this.getReport, 2000)
              }
            } else {
              this.loading = false;
              this.$error(this.$t('api_report.not_exist'));
            }
          });
        }
      },
      buildBatchParam(param) {
        param.ids = Array.from(this.selectRows).map(row => row.id);
        param.projectId = getCurrentProjectID();
        param.condition = this.condition;
      },
      handleBatchExecute() {
        this.infoDb = false;
        let url = "/api/automation/run/batch";
        let run = {};
        run.id = getUUID();
        this.buildBatchParam(run);
        this.$post(url, run, response => {
          let data = response.data;
          this.runVisible = false;
          this.batchReportId = run.id;
        });
      },
      handleSelectAll(selection) {
        _handleSelectAll(this, selection, this.tableData, this.selectRows);
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
        this.condition.selectAll = data;
        setUnSelectIds(this.tableData, this.condition, this.selectRows);
        this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
        toggleAllSelection(this.$refs.scenarioTable, this.tableData, this.selectRows);
      },
      edit(row) {
        let data = JSON.parse(JSON.stringify(row));
        this.$emit('edit', data);
      },
      reductionApi(row) {
        row.scenarioDefinition = null;
        row.tags = null;
        let rows = [row];
        this.$post("/api/automation/reduction", rows, response => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        })
      },
      execute(row) {
        this.infoDb = false;
        let url = "/api/automation/run";
        let run = {};
        let scenarioIds = [];
        scenarioIds.push(row.id);
        run.id = getUUID();
        run.projectId = getCurrentProjectID();
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
              let ids = [row.id];
              this.$post('/api/automation/removeToGc/', ids, () => {
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
        this.$emit('openScenario', item)
      },
      exportApi() {
        let param = {};
        this.buildBatchParam(param);
        if (param.ids === undefined || param.ids.length < 1) {
          this.$warning(this.$t("api_test.automation.scenario.check_case"));
          return;
        }
        this.loading = true;
        this.result = this.$post("/api/automation/export", param, response => {
          this.loading = false;
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
        this.loading = true;
        this.result = this.$post("/api/automation/export/jmx", param, response => {
          this.loading = false;
          let obj = response.data;
          if (obj && obj.length > 0) {
            obj.forEach(item => {
              downloadFile(item.name + ".jmx", item.jmx);
            })
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
    }
  }
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

  /deep/ el-table__fixed-right {

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
</style>
