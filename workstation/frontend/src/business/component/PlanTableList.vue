<template>
  <el-card class="table-card" v-loading="cardResult.loading">

    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      @sort-change="sort"
      :height="screenHeight"
      :table-is-loading="this.result"
      @row-click="intoPlan">
      <template v-for="(item, index) in tableLabel">
        <el-table-column
          v-if="item.id === 'name'"
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id === 'userName'"
          prop="principalName"
          :label="$t('test_track.plan.plan_principal')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id === 'createUser'"
          prop="createUser"
          :label="$t('commons.create_user')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id === 'status'"
          prop="status"
          column-key="status"
          :filters="statusFilters"
          :filtered-value="['Prepare', 'Underway']"
          :label="$t('test_track.plan.plan_status')"
          show-overflow-tooltip
          :min-width="100"
          :key="index">
          <template v-slot:default="scope">
          <span @click.stop="clickt = 'stop'">
            <el-dropdown class="test-case-status" @command="statusChange">
                <span class="el-dropdown-link">
                  <plan-status-table-item :value="scope.row.status"/>
                </span>
                <el-dropdown-menu slot="dropdown" chang>
                  <el-dropdown-item :disabled="!hasEditPermission" :command="{item: scope.row, status: 'Prepare'}">
                    {{ $t('test_track.plan.plan_status_prepare') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{item: scope.row, status: 'Underway'}">
                    {{ $t('test_track.plan.plan_status_running') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{item: scope.row, status: 'Finished'}">
                    {{ $t('test_track.plan.plan_status_finished') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{item: scope.row, status: 'Completed'}">
                    {{ $t('test_track.plan.plan_status_completed') }}
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!hasEditPermission"
                                    :command="{item: scope.row, status: 'Archived'}">
                    {{ $t('test_track.plan.plan_status_archived') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
          </span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id === 'stage'&& isShowAllColumn"
          prop="stage"
          column-key="stage"
          :filters="stageFilters"
          :label="$t('test_track.plan.plan_stage')"
          show-overflow-tooltip
          :min-width="110"
          :key="index">
          <template v-slot:default="scope">
            <plan-stage-table-item :option="stageOption" :stage="scope.row.stage"/>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id === 'testRate'&& isShowAllColumn"
          prop="testRate"
          :label="$t('test_track.home.test_rate')"
          min-width="100"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <el-progress :percentage="scope.row.testRate"></el-progress>
          </template>
        </el-table-column>
        <el-table-column v-if="item.id === 'tags'&& isShowAllColumn" prop="tags"
                         :label="$t('api_test.automation.tag')" :key="index">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"></ms-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id === 'projectName'"
          prop="projectName"
          :label="$t('test_track.plan.plan_project')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id === 'passRate'&& isShowAllColumn"
          prop="passRate"
          :label="$t('commons.pass_rate')"
          show-overflow-tooltip
          :min-width="110"
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id === 'plannedStartTime'"
          sortable
          prop="plannedStartTime"
          :label="$t('test_track.plan.planned_start_time')"
          show-overflow-tooltip
          :min-width="150"
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.plannedStartTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id === 'plannedEndTime'"
          sortable
          prop="plannedEndTime"
          :label="$t('test_track.plan.planned_end_time')"
          show-overflow-tooltip
          :min-width="150"
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.plannedEndTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id === 'actualStartTime' && isShowAllColumn"
          sortable
          prop="actualStartTime"
          :min-width="170"
          :label="$t('test_track.plan.actual_start_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.actualStartTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id === 'actualEndTime' && isShowAllColumn"
          sortable
          :min-width="170"
          prop="actualEndTime"
          :label="$t('test_track.plan.actual_end_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.actualEndTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
      </template>

    </el-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </el-card>
</template>

<script>

import {_filter, _sort, saveLastTableSortField} from "metersphere-frontend/src/utils/tableUtils";
import {TEST_PLAN_LIST} from "metersphere-frontend/src/utils/constants";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import PlanStageTableItem from "@/business/module/plan/PlanStageTableItem";
import PlanStatusTableItem from "@/business/module/plan/PlanStatusTableItem";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import {TEST_PLAN_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {getCustomTableHeaderByXpack} from "@/business/component/js/table-head-util";
import {editPlan, getDashboardPlanList, getPlanList, getPlanStageOption, getPrincipalById} from "@/api/test-plan";

export default {
  name: "PlanTableList",
  components: {
    PlanStageTableItem,
    PlanStatusTableItem,
    MsDialogFooter,
    MsTablePagination,
    MsTag
  },
  props: {
    isFocus: {
      type: Boolean,
      default: false,
    },
    isCreation: {
      type: Boolean,
      default: false,
    },
    isShowAllColumn: {
      type: Boolean,
      default: true,
    },
    isSelectAll: {
      type: Boolean,
      default: false,
    },
    isDashboard: {
      type: Boolean,
      default: false,
    },
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 160px)';
      }
    }, //屏幕高度
  },
  data() {
    return {
      createUser: "",
      type: TEST_PLAN_LIST,
      tableHeaderKey: "TEST_PLAN_LIST",
      tableLabel: [],
      result: false,
      cardResult: {},
      enableDeleteTip: false,
      condition: {
        components: TEST_PLAN_CONFIGS,
        executorOrPrincipal: "current",
      },
      currentPage: 1,
      pageSize: 10,
      hasEditPermission: false,
      total: 0,
      tableData: [],
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_finished'), value: 'Finished'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'},
        {text: this.$t('test_track.plan.plan_status_archived'), value: 'Archived'}
      ],
      stageFilters: [
        {text: this.$t('test_track.plan.smoke_test'), value: 'smoke'},
        {text: this.$t('test_track.plan.system_test'), value: 'system'},
        {text: this.$t('test_track.plan.regression_test'), value: 'regression'},
      ],
      currentPlanId: "",
      stageOption: []
    };
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
    if (!this.projectId) {
      this.projectId = getCurrentProjectID();
    }
    this.hasEditPermission = hasPermission('PROJECT_TRACK_PLAN:READ+EDIT');
    getPlanStageOption(this.projectId).then(r => {
      this.stageOption = r.data;
    })
    this.initTableData();
  },
  methods: {
    init() {
      this.initTableData();
    },

    initTableData() {
      if (this.planId) {
        this.condition.planId = this.planId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      }
      if (!this.projectId) {
        return;
      }
      if (this.isFocus) {
        if (this.condition.filters) {
          delete this.condition.filters['user_id']
        }
        this.condition.combine = {followPeople: {operator: "current user", value: "current user",}}
        if (this.condition.executorOrPrincipal) {
          delete this.condition.executorOrPrincipal
        }
      } else if (this.isCreation) {
        if (this.condition.filters) {
          delete this.condition.filters['user_id']
        }
        if (this.condition.executorOrPrincipal) {
          delete this.condition.executorOrPrincipal
        }
        this.condition.combine = {creator: {operator: "current user", value: "current user",}}
      } else {
        if (!this.condition.filters) {
          this.condition.filters = {status: ["Prepare", "Underway"]}
        } else if (this.condition.filters.status == null) {
          this.condition.filters.status = ["Prepare", "Underway"];
        }
      }
      if (this.isSelectAll === false) {
        this.condition.projectId = getCurrentProjectID();
      }
      this.condition.workspaceId = getCurrentWorkspaceId();
      if (this.isDashboard) {
        getDashboardPlanList(this.currentPage,this.pageSize,this.condition).then(response => {
          let data = response.data;
          this.dealResponseData(data)
        });
      } else {
        getPlanList(this.currentPage,this.pageSize,this.condition).then(response => {
          let data = response.data;
          this.dealResponseData(data)
        });
      }
      this.tableLabel = getCustomTableHeaderByXpack('TEST_PLAN_LIST_HEAD');
    },
    dealResponseData(data){
      this.total = data.itemCount;
      this.tableData = data.listObject;
      this.tableData.forEach(item => {
        if (item.tags && item.tags.length > 0) {
          item.tags = JSON.parse(item.tags);
        }
        item.passRate = item.passRate + '%';
        item.testRate = item.testRate ? item.testRate : 0;
        getPrincipalById(item.id).then(res => {
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
        })
      });
    },
    statusChange(data) {
      if (!hasPermission('PROJECT_TRACK_PLAN:READ+EDIT')) {
        return;
      }
      let oldStatus = data.item.status;
      let newStatus = data.status;
      let param = {};
      param.id = data.item.id;
      param.status = newStatus;
      editPlan(param).then(()=>{
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id === param.id) { //  手动修改当前状态后，前端结束时间先用当前时间，等刷新后变成后台数据（相等）
            if (oldStatus !== "Completed" && newStatus === "Completed") {
              this.tableData[i].actualEndTime = Date.now();
            } //  非完成->已完成，结束时间=null
            else if (oldStatus !== "Underway" && newStatus === "Underway") {
              this.tableData[i].actualStartTime = Date.now();
              this.tableData[i].actualEndTime = "";
            } //  非进行中->进行中，结束时间=null
            else if (oldStatus !== "Prepare" && newStatus === "Prepare") {
              this.tableData[i].actualStartTime = this.tableData[i].actualEndTime = "";
            } //  非未开始->未开始，结束时间=null
            this.tableData[i].status = newStatus;
            break;
          }
        }
      });
    },
    intoPlan(row, column, event) {
      if (column.label !== this.$t('commons.operating')) {
        let testPlanResolve = this.$router.resolve({
          path: '/track/plan/view/' + row.id,
          query: {projectId: row.projectId}
        });
        window.open(testPlanResolve.href, '_blank');
      }
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData()
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.saveSortField(this.tableHeaderKey, this.condition.orders);
      this.initTableData();
    },
    saveSortField(key, orders) {
      saveLastTableSortField(key, JSON.stringify(orders));
    },
  }
};
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

.schedule-btn >>> .el-button {
  margin-left: 10px;
  color: #85888E;
  border-color: #85888E;
  border-width: thin;
}

.scenario-ext-btn {
  margin-left: 10px;
}
</style>
