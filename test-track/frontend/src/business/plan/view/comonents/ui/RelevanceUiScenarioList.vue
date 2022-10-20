<template>
  <div v-loading="loading">

    <el-input :placeholder="$t('api_test.definition.request.select_case')" @blur="filterSearch"
              @keyup.enter.native="filterSearch" class="search-input" size="small" v-model="condition.name"/>
    <ms-table-adv-search-bar :condition.sync="condition" class="adv-search-bar"
                             v-if="condition.components !== undefined && condition.components.length > 0"
                             @search="filterSearch"/>

    <ms-table ref="scenarioTable"
              v-loading="loading"
              :data="tableData"
              :condition="condition"
              :page-size="pageSize"
              :total="total"
              :remember-order="true"
              row-key="id"
              :row-order-group-id="projectId"
              @order="search"
              @filter="filterSearch"
              :disable-header-config="true"
              @selectCountChange="selectCountChange">

      <el-table-column v-if="!customNum" prop="num" label="ID" sortable="custom"
                       show-overflow-tooltip>
      </el-table-column>
      <el-table-column v-if="customNum" prop="customNum" label="ID" sortable="custom"
                       show-overflow-tooltip>
      </el-table-column>
      <el-table-column prop="name" :label="$t('api_test.automation.scenario_name')" sortable="custom" min-width="120px"
                       show-overflow-tooltip/>

      <el-table-column prop="level" :label="$t('api_test.automation.case_level')" sortable="custom" min-width="120px"
                       show-overflow-tooltip>
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.level" ref="level"/>
        </template>

      </el-table-column>
      <el-table-column prop="tagNames" :label="$t('api_test.automation.tag')" min-width="100">
        <template v-slot:default="scope">
          <ms-tag v-for="itemName in scope.row.tags" :key="itemName" type="success" effect="plain" :content="itemName"
                  style="margin-left: 0px; margin-right: 2px"/>
        </template>
      </el-table-column>
      <el-table-column prop="userId" :label="$t('api_test.automation.creator')" show-overflow-tooltip sortable="custom"
                       min-width="100px"/>
      <el-table-column prop="updateTime" :label="$t('commons.update_time')" width="180" sortable="custom">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stepTotal" :label="$t('api_test.automation.step')" show-overflow-tooltip/>
      <el-table-column prop="lastResult" :label="$t('api_test.automation.last_result')" sortable="custom"
                       min-width="120px">
        <template v-slot:default="{row}">
          <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">
            {{ $t('api_test.automation.success') }}
          </el-link>
          <el-link type="danger" @click="showReport(row)" v-if="row.lastResult === 'Fail'">
            {{ $t('api_test.automation.fail') }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="passRate" :label="$t('api_test.automation.passing_rate')"
                       show-overflow-tooltip/>
    </ms-table>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </div>
</template>

<script>
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import MsTag from "metersphere-frontend/src/components/MsTag";
import TestPlanScenarioListHeader from "../api/TestPlanScenarioListHeader";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import TEST_PLAN_RELEVANCE_API_SCENARIO_CONFIGS from "metersphere-frontend/src/components/search/search-components";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {getOwnerProjects, getVersionFilters} from "@/business/utils/sdk-utils";
import {getProjectApplicationConfig} from "@/api/project-application";
import {testPlanUiScenarioRelevanceList} from "@/api/remote/ui/test-plan-ui-scenario-case";

export default {
  name: "RelevanceUiScenarioList",
  components: {
    MsTable,
    PriorityTableItem,
    TestPlanScenarioListHeader,
    MsTablePagination,
    ShowMoreBtn,
    MsTableHeader,
    MsTag,
    MsTableAdvSearchBar,
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
  },
  data() {
    return {
      loading: false,
      showConfigButtonWithOutPermission: false,
      condition: {
        components: TEST_PLAN_RELEVANCE_API_SCENARIO_CONFIGS
      },
      currentScenario: {},
      schedule: {},
      selectAll: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      infoDb: false,
      selectRows: new Set(),
      projectEnvMap: new Map(),
      projectList: [],
      customNum: false,
      environmentType: ENV_TYPE.JSON,
      envGroupId: "",
      versionFilters: [],
    };
  },
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    }
  },
  watch: {
    selectNodeIds() {
      this.search();
    },
    projectId() {
      this.condition = {
        components: TEST_PLAN_RELEVANCE_API_SCENARIO_CONFIGS
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
    search() {
      this.projectEnvMap.clear();
      if (!this.projectId) {
        return;
      }
      this.getProject(this.projectId);
      this.selectRows = new Set();
      if (this.condition.filters) {
        this.condition.filters.status = ["Prepare", "Underway", "Completed"];
      } else {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
      }

      this.condition.moduleIds = this.selectNodeIds;

      if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

      if (this.planId != null) {
        this.condition.planId = this.planId;
      }
      this.condition.hasStep = true;
      this.loading = true;
      testPlanUiScenarioRelevanceList({pageNum: this.currentPage, pageSize: this.pageSize}, this.condition)
        .then(response => {
          this.loading = false;
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.clear();
        });

    },
    clear() {
      this.selectRows.clear();
    },
    setProjectEnvMap(projectEnvMap) {
      this.projectEnvMap = projectEnvMap;
    },
    setEnvGroup(id) {
      this.envGroupId = id;
    },
    getWsProjects() {
      getOwnerProjects()
        .then(res => {
          this.projectList = res.data;
        });
    },
    getProject(projectId) {
      if (projectId) {
        getProjectApplicationConfig('SCENARIO_CUSTOM_NUM')
          .then(result => {
            let data = result.data;
            if (data) {
              this.customNum = data.scenarioCustomNum;
            }
          });
      }
    },
    changeVersion(currentVersion) {
      this.condition.versionId = currentVersion || null;
      this.search();
    },
    getVersionOptions() {
      getVersionFilters(this.projectId)
        .then(r => this.versionFilters = r.data);
    },
    filter(field) {
      this.condition.filters = field || null;
      this.search();
    },
    selectCountChange(data) {
      this.selectRows = this.$refs.scenarioTable.selectRows;
      this.$emit("selectCountChange", data);
    },
    showReport() {

    }
  }
};
</script>

<style scoped>

:deep(.ms-select-all-fixed th:nth-child(2) .table-select-icon) {
  top: -3px !important;
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
</style>
