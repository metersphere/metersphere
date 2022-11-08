<template>
  <el-card class="table-card" v-loading="result">
    <template v-slot:header>
      <ms-table-header :condition.sync="condition"
                       @search="initTableData"
                       :title="$t('test_track.plan.test_plan')"
                       :show-create="false"
      >
      </ms-table-header>
    </template>
    <env-popover :env-map="projectEnvMap"
                 :project-ids="projectIds"
                 @setProjectEnvMap="setProjectEnvMap"
                 :environment-type.sync="environmentType"
                 :group-id="envGroupId"
                 :project-list="projectList"
                 :is-scenario="false"
                 @setEnvGroup="setEnvGroup"
                 ref="envPopover"
                 class="env-popover" style="float: right; margin-top: 4px;"/>
    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      @sort-change="sort"
      @select-all="select"
      @select="select">
      <el-table-column type="selection"/>

      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="principalName"
        :label="$t('test_track.plan.plan_principal')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="status"
        column-key="status"
        :filters="statusFilters"
        :label="$t('test_track.plan.plan_status')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
            <el-dropdown class="test-case-status" @command="statusChange">
              <span class="el-dropdown-link">
                <plan-status-table-item :value="scope.row.status"/>
              </span>
              <el-dropdown-menu slot="dropdown" chang>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser" :command="{id: scope.row.id, status: 'Prepare'}">
                  {{ $t('test_track.plan.plan_status_prepare') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{id: scope.row.id, status: 'Underway'}">
                  {{ $t('test_track.plan.plan_status_running') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="!isTestManagerOrTestUser"
                                  :command="{id: scope.row.id, status: 'Completed'}">
                  {{ $t('test_track.plan.plan_status_completed') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
        </template>
      </el-table-column>
      <el-table-column
        prop="projectName"
        :label="$t('test_track.plan.plan_project')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        sortable
        prop="plannedStartTime"
        :label="$t('test_track.plan.planned_start_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.plannedStartTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="plannedEndTime"
        :label="$t('test_track.plan.planned_end_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.plannedEndTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="actualStartTime"
        :label="$t('test_track.plan.actual_start_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.actualStartTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="actualEndTime"
        :label="$t('test_track.plan.actual_end_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.actualEndTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <ms-delete-confirm :title="$t('test_track.plan.plan_delete')" @delete="_handleDelete" ref="deleteConfirm"
                       :with-tip="enableDeleteTip">
      {{ $t('test_track.plan.plan_delete_tip') }}
    </ms-delete-confirm>

    <ms-dialog-footer style="float: right;margin: 20px"
                      @confirm="confirm" @cancel="cancel">
    </ms-dialog-footer>

  </el-card>
</template>

<script>
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import PlanStatusTableItem from "@/business/commons/PlanStatusTableItem";
import PlanStageTableItem from "./PlanStageTableItem";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import {TEST_PLAN_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {_filter, _sort} from "metersphere-frontend/src/utils/tableUtils";
import EnvPopover from "@/business/automation/scenario/EnvPopover";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import {getPlanStageOption, planPage, testPlanGetPrincipal} from "@/api/test-plan";
import {getApiScenarioProjectIdByConditions, getScenarioByProjectId} from "@/api/scenario";
import {getOwnerProjects} from "@/api/project";

export default {
  name: "TestPlanList",
  components: {
    MsDeleteConfirm,
    PlanStageTableItem,
    PlanStatusTableItem,
    MsTableOperator, MsTableOperatorButton, MsDialogFooter, MsTableHeader, MsTablePagination, EnvPopover
  },
  props: {
    row: Set,
    scenarioCondition: {},
  },
  data() {
    return {
      dialogVisible: false,
      result: false,
      selection: [],
      enableDeleteTip: false,
      condition: {
        components: TEST_PLAN_CONFIGS
      },
      currentPage: 1,
      pageSize: 10,
      isTestManagerOrTestUser: false,
      total: 0,
      tableData: [],
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'}
      ],
      stageFilters: [
        {text: this.$t('test_track.plan.smoke_test'), value: 'smoke'},
        {text: this.$t('test_track.plan.system_test'), value: 'system'},
        {text: this.$t('test_track.plan.regression_test'), value: 'regression'},
      ],
      projectEnvMap: new Map(),
      projectList: [],
      projectIds: new Set(),
      map: new Map(),
      environmentType: ENV_TYPE.JSON,
      envGroupId: "",
      stageOption: []
    }
  },
  watch: {
    '$route'(to, from) {
      if (to.path.indexOf("/track/plan/all") >= 0) {
        this.initTableData();
      }
    }
  },
  created() {
    this.projectId = this.$route.params.projectId;
    this.isTestManagerOrTestUser = true;
    getPlanStageOption()
      .then((r) => {
        this.stageOption = r.data;
      });
    this.initTableData();
    this.setScenarioSelectRows(this.row);
    this.getWsProjects();
  },
  methods: {
    confirm() {
      if (this.selection.length == 0) {
        this.$warning(this.$t("api_test.definition.request.test_plan_select"));
      } else {
        const sign = this.checkEnv();
        if (!sign) {
          return false;
        }
        if (this.environmentType === ENV_TYPE.JSON && (!this.projectEnvMap || this.projectEnvMap.size < 1)) {
          this.$warning(this.$t("api_test.environment.select_environment"));
          return false;
        } else if (this.environmentType === ENV_TYPE.GROUP && !this.envGroupId) {
          this.$warning(this.$t("api_test.environment.select_environment"));
          return false;
        }
        this.$emit('addTestPlan', this.selection, this.projectEnvMap, this.map, this.environmentType, this.envGroupId);
      }
    },
    cancel() {
      this.$emit('cancel');
    },
    setProjectEnvMap(projectEnvMap) {
      this.projectEnvMap = projectEnvMap;
    },
    setEnvGroup(id) {
      this.envGroupId = id;
    },
    select(selection) {
      this.selection = selection.map(s => s.id);
      this.$emit('selection', selection);
    },
    setScenarioSelectRows(rows) {
      this.projectIds.clear();
      this.map.clear();
      if (this.scenarioCondition != null) {
        let params = {};
        params.ids = [];
        rows.forEach(row => {
          params.ids.push(row.id);
        });
        params.condition = this.scenarioCondition;

        getApiScenarioProjectIdByConditions(params).then(res => {
          let data = res.data;
          data.forEach(scenario => {
            scenario.projectIds.forEach(d => this.projectIds.add(d));
            this.map.set(scenario.id, scenario.projectIds);
          });
        });
      } else {
        rows.forEach(row => {
          this.result = getScenarioByProjectId(row.id).then(res => {
            let data = res.data;
            data.projectIds.forEach(d => this.projectIds.add(d));
            this.map.set(row.id, data.projectIds);
          });
        });
      }
    },
    initTableData() {
      if (this.planId) {
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      }
      if (!getCurrentProjectID()) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.condition.projectId = getCurrentProjectID();
      this.result = planPage(this.currentPage, this.pageSize, this.condition).then(response => {
        let data = response.data;
        this.total = data.itemCount;
        data.listObject.forEach(item => {
          testPlanGetPrincipal(item.id)
            .then(res => {
              let data = res.data;
              let principal = "";
              let principalIds = data.map(d => d.id);
              if (data) {
                data.forEach(d => {
                  if (principal !== "") {
                    principal = principal + "、" + d.name;
                  } else {
                    principal = principal + d.name;
                  }
                })
              }
              this.$set(item, "principalName", principal);
              // 编辑时初始化id
              this.$set(item, "principals", principalIds);
            });
        });
        this.tableData = data.listObject;
      });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    testPlanCreate() {
      this.$refs.testPlanEditDialog.openTestPlanEditDialog();
    },
    handleEdit(testPlan) {
      this.$emit('testPlanEdit', testPlan);
    },
    statusChange(param) {

    },
    handleDelete(testPlan) {
      this.enableDeleteTip = testPlan.status === 'Underway' ? true : false;
      this.$refs.deleteConfirm.open(testPlan);
    },
    _handleDelete(testPlan) {

    },
    intoPlan(row, event, column) {
      this.$router.push('/track/plan/view/' + row.id);
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      _sort(column, this.condition);
      this.initTableData();
    },
    openTestReportTemplate(data) {
      this.$refs.testReportTemplateList.open(data.id);
    },
    openReport(planId, reportId) {
      if (reportId) {
        this.$refs.testCaseReportView.open(planId, reportId);
      }
    },
    checkEnv() {
      return this.$refs.envPopover.checkEnv();
    },
    getWsProjects() {
      getOwnerProjects().then(res => {
        this.projectList = res.data;
      })
    },
  }
}
</script>

<style scoped>

.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

.el-table {
  cursor: pointer;
}
</style>
