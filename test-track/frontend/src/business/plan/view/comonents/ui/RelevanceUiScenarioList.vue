<template>
  <div v-loading="loading">
    <ms-table-adv-search-bar :condition.sync="condition" class="adv-search-bar"
                             v-if="condition.components !== undefined && condition.components.length > 0"
                             @search="filterSearch"/>

    <el-input :placeholder="$t('api_test.definition.request.select_case')" @blur="filterSearch"
              @keyup.enter.native="filterSearch" class="search-input" size="small" v-model="condition.name"/>

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

      <ms-table-column v-if="!customNum" prop="num" label="ID" sortable="custom"
                       show-overflow-tooltip>
      </ms-table-column>
      <ms-table-column v-if="customNum" prop="customNum" label="ID" sortable="custom"
                       show-overflow-tooltip>
      </ms-table-column>

      <ms-table-column :fields-width="fieldsWidth" prop="name" :label="$t('api_test.automation.scenario_name')" sortable="custom" min-width="120px"
                       show-overflow-tooltip/>

      <ms-table-column prop="level" :label="$t('api_test.automation.case_level')" sortable="custom" min-width="120px"
                       show-overflow-tooltip>
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.level" ref="level"/>
        </template>

      </ms-table-column>
      <ms-table-column prop="tagNames" :label="$t('commons.tag')" width="120px" :show-overflow-tooltip="false">
        <template v-slot:default="scope">
          <el-tooltip class="item" effect="dark" placement="top">
            <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
            <div class="oneLine">
              <ms-tag
                v-for="(itemName, index) in scope.row.tags"
                :key="index"
                type="success"
                effect="plain"
                :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 100"
                :content="itemName"
                style="margin-left: 0px; margin-right: 2px"/>
            </div>
          </el-tooltip>
        </template>
      </ms-table-column>
      <ms-table-column prop="userId" :label="$t('api_test.automation.creator')" show-overflow-tooltip sortable="custom"
                       min-width="100px"/>
      <ms-table-column prop="updateTime" :label="$t('commons.update_time')" width="180" sortable="custom">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>
      <ms-table-column prop="stepTotal" :label="$t('api_test.automation.step')" show-overflow-tooltip/>
      <ms-table-column prop="lastResult" :label="$t('api_test.automation.last_result')" sortable="custom"
                       min-width="120px">
        <template v-slot:default="{row}">
          <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success' || row.lastResult === 'SUCCESS'">
            {{ $t('Success') }}
          </el-link>
          <el-link type="danger" @click="showReport(row)" v-else-if="row.lastResult === 'Fail' || row.lastResult === 'FAIL' || row.lastResult === 'ERROR'">
            {{ $t('Error') }}
          </el-link>
          <el-link type="info" v-else-if="row.lastResult === 'PENDING' || row.lastResult === 'UnExecute'">
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
      <ms-table-column prop="passRate" :label="$t('api_test.automation.passing_rate')"
                       show-overflow-tooltip/>
    </ms-table>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <div>
      <el-radio-group v-model="envType" style="float: left;margin-top: 18px;">
        <el-radio label="default">{{ $t("api_test.environment.default_environment") }}</el-radio>
        <el-radio label="newEnv">{{ $t("api_test.environment.choose_new_environment") }}</el-radio>
      </el-radio-group>
      <env-group-popover
        :env-map="projectEnvMap"
        :project-ids="projectIds"
        :show-env-group="false"
        @setProjectEnvMap="setProjectEnvMap"
        :environment-type.sync="environmentType"
        :group-id="envGroupId"
        :is-scenario="false"
        @setEnvGroup="setEnvGroup"
        :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
        :project-list="projectList"
        ref="envPopover"
        class="env-popover"
        style="float: left;margin-left: 16px"
        v-if="envType==='newEnv'"
      />
    </div>
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
import {TEST_PLAN_RELEVANCE_UI_SCENARIO_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {getOwnerProjects, getVersionFilters} from "@/business/utils/sdk-utils";
import {testPlanUiScenarioRelevanceList} from "@/api/remote/ui/test-plan-ui-scenario-case";
import {getCustomTableWidth} from "metersphere-frontend/src/utils/tableUtils";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import EnvGroupPopover from "@/business/plan/env/EnvGroupPopover";
import {getUiScenarioEnvByProjectId} from "@/api/remote/ui/ui-automation";

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
    MsTableColumn,
    EnvGroupPopover,
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
        components: TEST_PLAN_RELEVANCE_UI_SCENARIO_CONFIGS
      },
      currentScenario: {},
      schedule: {},
      selectAll: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      envType: "default",
      reportId: "",
      infoDb: false,
      selectRows: new Set(),
      map: new Map(),
      projectEnvMap: new Map(),
      projectList: [],
      customNum: false,
      environmentType: ENV_TYPE.JSON,
      envGroupId: "",
      versionFilters: [],
      fieldsWidth: getCustomTableWidth('TEST_PLAN_UI_SCENARIO_CASE'),
      projectIds: new Set()
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
        components: TEST_PLAN_RELEVANCE_UI_SCENARIO_CONFIGS
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
    getTagToolTips(tags) {
      try {
        let showTips = '';
        tags.forEach((item) => {
          showTips += item + ',';
        });
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return '';
      }
    },
    filterSearch() {
      this.currentPage = 1;
      this.search();
    },
    search() {
      this.projectEnvMap.clear();
      if (!this.projectId) {
        return;
      }
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
      this.initProjectIds();
    },
    showReport() {

    },
    initProjectIds() {
      this.projectIds.clear();
      this.map.clear();
      this.selectRows.forEach((row) => {
        getUiScenarioEnvByProjectId(row.id).then((res) => {
          let data = res.data;
          data.projectIds.forEach((d) => this.projectIds.add(d));
          this.map.set(row.id, data.projectIds);
        });
      });
    },
    closeEnv(){
      this.$refs.envPopover.close();
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

.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
