<template>
  <div v-loading="result.loading">
    <env-group-popover
      :env-map="projectEnvMap"
      :project-ids="projectIds"
      @setProjectEnvMap="setProjectEnvMap"
      :environment-type.sync="environmentType"
      :group-id="envGroupId"
      :is-scenario="false"
      @setEnvGroup="setEnvGroup"
      :show-config-button-with-out-permission="
        showConfigButtonWithOutPermission
      "
      :project-list="projectList"
      ref="envPopover"
      class="env-popover"
    />

    <mx-version-select
      v-xpack
      :project-id="projectId"
      @changeVersion="changeVersion"
      style="float: left"
      class="search-input"
    />
    <ms-search
      :base-search-tip="$t('api_test.definition.request.select_case')"
      :condition.sync="condition"
      v-if="clearOver"
      style="margin-top: 10px"
      @search="filterSearch"
    >
    </ms-search>
    <ms-table
      ref="scenarioTable"
      :data="tableData"
      :condition="condition"
      :page-size="pageSize"
      :total="total"
      :remember-order="true"
      row-key="id"
      :reserve-option="true"
      :page-refresh="pageRefresh"
      :row-order-group-id="projectId"
      @order="search"
      @filter="filterSearch"
      :disable-header-config="true"
      @selectCountChange="selectCountChange"
    >
      <el-table-column
        v-if="!customNum"
        prop="num"
        label="ID"
        sortable="custom"
        show-overflow-tooltip
      >
      </el-table-column>
      <el-table-column
        v-if="customNum"
        prop="customNum"
        label="ID"
        sortable="custom"
        show-overflow-tooltip
      >
      </el-table-column>
      <el-table-column
        prop="name"
        :label="$t('api_test.automation.scenario_name')"
        sortable="custom"
        min-width="120px"
        show-overflow-tooltip
      />
      <el-table-column
        v-if="versionEnable"
        column-key="version_id"
        :filters="versionFilters"
        :label="$t('commons.version')"
        min-width="100px"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="level"
        :label="$t('api_test.automation.case_level')"
        sortable="custom"
        min-width="120px"
        show-overflow-tooltip
      >
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.level" ref="level" />
        </template>
      </el-table-column>
      <el-table-column
        prop="tagNames"
        :label="$t('api_test.automation.tag')"
        min-width="100"
        :showOverflowTooltip="false"
      >
        <template v-slot:default="scope">
          <el-tooltip class="item" effect="dark" placement="top">
            <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
            <div class="oneLine">
              <ms-tag
                v-for="(itemName, index) in scope.row.tags"
                :key="index"
                type="success"
                effect="plain"
                :show-tooltip="
                  scope.row.tags.length === 1 && itemName.length * 12 <= 100
                "
                :content="itemName"
                style="margin-left: 0px; margin-right: 2px"
              />
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column
        prop="userName"
        :label="$t('api_test.automation.creator')"
        show-overflow-tooltip
        sortable="custom"
        min-width="100px"
      />
      <el-table-column
        prop="updateTime"
        :label="$t('commons.update_time')"
        width="180"
        sortable="custom"
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="stepTotal"
        :label="$t('api_test.automation.step')"
        show-overflow-tooltip
      />
      <el-table-column
        prop="lastResult"
        :label="$t('api_test.automation.last_result')"
        sortable="custom"
        min-width="120px"
      >
        <template v-slot:default="{ row }">
          <ms-api-report-status :status="row.lastResult" />
        </template>
      </el-table-column>
      <el-table-column
        prop="passRate"
        :label="$t('api_test.automation.passing_rate')"
        show-overflow-tooltip
      />
    </ms-table>
    <ms-table-pagination
      :change="pageChange"
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"
    />
  </div>
</template>

<script>
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag"; // import MsApiReportDetail from "../../../../../api/automation/report/ApiReportDetail";
import TestPlanScenarioListHeader from "./TestPlanScenarioListHeader";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import { TEST_PLAN_RELEVANCE_API_SCENARIO_CONFIGS } from "metersphere-frontend/src/components/search/search-components";
import { ENV_TYPE } from "metersphere-frontend/src/utils/constants";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";
import {
  getOwnerProjects,
  getVersionFilters,
} from "@/business/utils/sdk-utils";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import { getProjectApplicationConfig } from "@/api/project-application";
import { getApiScenarioEnvByProjectId } from "@/api/remote/api/api-automation";
import {
  scenarioRelevanceList,
  scenarioRelevanceProjectIds,
} from "@/api/remote/plan/test-plan-scenario";
import EnvGroupPopover from "@/business/plan/env/EnvGroupPopover";
import ApiReportStatus from "@/business/plan/view/comonents/report/detail/api/ApiReportStatus";
import MsApiReportStatus from "@/business/plan/view/comonents/report/detail/api/ApiReportStatus";

export default {
  name: "RelevanceScenarioList",
  components: {
    MsApiReportStatus,
    EnvGroupPopover,
    MsTable,
    PriorityTableItem,
    TestPlanScenarioListHeader,
    MsTablePagination,
    MsTableHeader,
    MsTag,
    // MsApiReportDetail,
    MsTableAdvSearchBar,
    MxVersionSelect,
    MsSearch,
  },
  props: {
    referenced: {
      type: Boolean,
      default: false,
    },
    selectNodeIds: Array,
    projectId: String,
    planId: String,
    versionEnable: Boolean,
    ApiReportStatus,
  },
  data() {
    return {
      result: { loading: false },
      showConfigButtonWithOutPermission: false,
      condition: {
        components: TEST_PLAN_RELEVANCE_API_SCENARIO_CONFIGS,
      },
      currentScenario: {},
      schedule: {},
      tableData: [],
      clearOver: true,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      infoDb: false,
      selectRows: new Set(),
      projectEnvMap: new Map(),
      projectList: [],
      projectIds: new Set(),
      map: new Map(),
      customNum: false,
      environmentType: ENV_TYPE.JSON,
      envGroupId: "",
      versionFilters: [],
      pageRefresh: false,
    };
  },
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    },
  },
  watch: {
    selectNodeIds() {
      this.search();
    },
    projectId() {
      this.condition = {
        components: TEST_PLAN_RELEVANCE_API_SCENARIO_CONFIGS,
      };
      this.selectNodeIds.length = 0;
      this.search();
      this.getVersionOptions();
    },
  },
  created() {
    this.getWsProjects();
    this.getVersionOptions();
  },
  methods: {
    filterSearch() {
      this.currentPage = 1;
      this.search();
    },
    pageChange() {
      this.search("page");
    },
    search(data) {
      this.pageRefresh = data === "page";
      this.projectEnvMap.clear();
      this.projectIds.clear();
      if (!this.projectId) {
        return;
      }
      this.getProject(this.projectId);
      this.selectRows = new Set();
      this.loading = true;
      if (this.condition.filters) {
        this.condition.filters.status = ["Prepare", "Underway", "Completed"];
      } else {
        this.condition.filters = {
          status: ["Prepare", "Underway", "Completed"],
        };
      }

      this.condition.moduleIds = this.selectNodeIds;

      if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

      if (this.planId != null) {
        this.condition.planId = this.planId;
      }
      this.condition.stepTotal = "testPlan";
      scenarioRelevanceList(
        { pageNum: this.currentPage, pageSize: this.pageSize },
        this.condition
      ).then((response) => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach((item) => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
        this.clear();
      });
    },
    clear() {
      this.selectRows.clear();
      this.clearOver = false;
      this.$nextTick(() => {
        this.clearOver = true;
      });
    },
    setProjectEnvMap(projectEnvMap) {
      this.projectEnvMap = projectEnvMap;
    },
    setEnvGroup(id) {
      this.envGroupId = id;
    },
    getWsProjects() {
      getOwnerProjects().then((res) => {
        this.projectList = res.data;
      });
    },
    getProject(projectId) {
      if (projectId) {
        getProjectApplicationConfig(projectId, "SCENARIO_CUSTOM_NUM").then(
          (result) => {
            let data = result.data;
            if (data && data.typeValue === "true") {
              this.customNum = true;
            } else {
              this.customNum = false;
            }
          }
        );
      }
    },
    initProjectIds() {
      this.projectIds.clear();
      this.map.clear();
      if (this.condition && this.condition.selectAll) {
        this.result.loading = true;
        scenarioRelevanceProjectIds(this.condition)
          .then((rsp) => {
            this.result.loading = false;
            if (rsp.data) {
              let projectIds = rsp.data.projectIdList;
              projectIds.forEach((d) => this.projectIds.add(d));
              let scenarioProjectIdMap = rsp.data.scenarioProjectIdMap;
              let scenarioIds = Object.keys(scenarioProjectIdMap);
              scenarioIds.forEach((scenarioId) => {
                this.map.set(scenarioId, scenarioProjectIdMap[scenarioId]);
              });
            }
          })
          .catch(() => {
            this.result.loading = false;
          });
      } else {
        this.selectRows.forEach((row) => {
          getApiScenarioEnvByProjectId(row.id).then((res) => {
            let data = res.data;
            data.projectIds.forEach((d) => this.projectIds.add(d));
            this.map.set(row.id, data.projectIds);
          });
        });
      }
    },
    checkEnv() {
      return this.$refs.envPopover.checkEnv();
    },
    changeVersion(currentVersion) {
      this.condition.versionId = currentVersion || null;
      this.search();
    },
    getVersionOptions() {
      getVersionFilters(this.projectId).then(
        (r) => (this.versionFilters = r.data)
      );
    },
    filter(field) {
      this.condition.filters = field || null;
      this.search();
    },
    selectCountChange(data) {
      this.selectRows = this.$refs.scenarioTable.selectRows;
      this.initProjectIds();
      this.$emit("selectCountChange", data);
    },
    getTagToolTips(tags) {
      try {
        let showTips = "";
        tags.forEach((item) => {
          showTips += item + ",";
        });
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return "";
      }
    },
  },
};
</script>

<style scoped>
:deep(.el-drawer__header) {
  margin-bottom: 0px;
}

.env-popover {
  float: right;
  margin-top: 10px;
}

.search-input {
  float: right;
  width: 250px;
  margin-top: 10px;
  margin-bottom: 20px;
  margin-right: 20px;
}

.adv-search-bar {
  float: right;
  margin-top: 15px;
  margin-right: 10px;
}

.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
