<template>
  <div>
    <ms-search
      :base-search-tip="$t('api_test.definition.request.select_case')"
      :condition.sync="condition"
      @search="search">
    </ms-search>
    <mx-version-select
      v-xpack
      :project-id="projectId"
      @changeVersion="changeVersion"
      style="float: left"
      class="search-input" />

    <ms-table
      ref="scenarioTable"
      v-loading="result"
      :data="tableData"
      :condition="condition"
      :page-size="pageSize"
      :total="total"
      :remember-order="true"
      row-key="id"
      :row-order-group-id="projectId"
      @refresh="search"
      :disable-header-config="true"
      @selectCountChange="selectCountChange">
      <el-table-column v-if="!customNum" prop="num" label="ID" show-overflow-tooltip> </el-table-column>
      <el-table-column v-if="customNum" prop="customNum" label="ID" show-overflow-tooltip> </el-table-column>
      <el-table-column prop="name" :label="$t('api_test.automation.scenario_name')" show-overflow-tooltip />
      <el-table-column
        v-if="versionEnable"
        column-key="version_id"
        :filters="versionFilters"
        :label="$t('commons.version')"
        min-width="120px">
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="level" :label="$t('api_test.automation.case_level')" show-overflow-tooltip>
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.level" ref="level" />
        </template>
      </el-table-column>
      <el-table-column prop="tagNames" :label="$t('api_test.automation.tag')" min-width="120">
        <template v-slot:default="scope">
          <ms-tag
            v-for="itemName in scope.row.tags"
            :key="itemName"
            type="success"
            effect="plain"
            :content="itemName"
            style="margin-left: 0px; margin-right: 2px" />
        </template>
      </el-table-column>
      <el-table-column prop="userId" :label="$t('api_test.automation.creator')" show-overflow-tooltip />
      <el-table-column prop="updateTime" :label="$t('api_test.automation.update_time')" width="180">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stepTotal" :label="$t('api_test.automation.step')" show-overflow-tooltip />
      <el-table-column prop="lastResult" :label="$t('api_test.automation.last_result')">
        <template v-slot:default="{ row }">
          <el-tag type="success" size="mini" v-if="row.lastResult === 'SUCCESS'">
            {{ $t('api_test.automation.success') }}
          </el-tag>
          <el-tag type="danger" size="mini" v-if="row.lastResult === 'ERROR'">
            {{ $t('api_test.automation.fail') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="passRate" :label="$t('api_test.automation.passing_rate')" show-overflow-tooltip />
    </ms-table>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total" />
  </div>
</template>

<script>
import { getProjectVersions } from '@/api/xpack';
import { getScenarioList } from '@/api/scenario';
import MsTableHeader from 'metersphere-frontend/src/components/MsTableHeader';
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import ShowMoreBtn from '@/business/commons/ShowMoreBtn';
import MsApiReportDetail from '@/business/automation/report/ApiReportDetail';
import MsTableMoreBtn from '@/business/automation/scenario/TableMoreBtn';
import PriorityTableItem from '@/business/commons/PriorityTableItem';
import MsTableAdvSearchBar from 'metersphere-frontend/src/components/search/MsTableAdvSearchBar';
import { API_SCENARIO_CONFIGS } from 'metersphere-frontend/src/components/search/search-components';
import { ENV_TYPE } from 'metersphere-frontend/src/utils/constants';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import { hasLicense } from 'metersphere-frontend/src/utils/permission';
import MsTable from 'metersphere-frontend/src/components/table/MsTable';
import MsTag from 'metersphere-frontend/src/components/MsTag';
import MsSearch from 'metersphere-frontend/src/components/search/MsSearch';
import { getOwnerProjects, getProjectConfig } from '@/api/project';

export default {
  name: 'RelevanceScenarioList',
  components: {
    MsTable,
    PriorityTableItem,
    MsTablePagination,
    MsTableMoreBtn,
    ShowMoreBtn,
    MsTableHeader,
    MsTag,
    MsApiReportDetail,
    MsTableAdvSearchBar,
    MsSearch,
    MxVersionSelect: () => import('metersphere-frontend/src/components/version/MxVersionSelect'),
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
      result: false,
      showConfigButtonWithOutPermission: false,
      condition: {
        components: API_SCENARIO_CONFIGS,
      },
      currentScenario: {},
      schedule: {},
      selectAll: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: '',
      infoDb: false,
      selectRows: new Set(),
      projectEnvMap: new Map(),
      projectList: [],
      projectIds: new Set(),
      map: new Map(),
      customNum: false,
      environmentType: ENV_TYPE.JSON,
      envGroupId: '',
      versionFilters: [],
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
        components: API_SCENARIO_CONFIGS,
      };
      this.selectNodeIds.length = 0;
      this.search();
      this.getProject();
    },
  },
  created() {
    this.getProject();
    this.getWsProjects();
    this.getVersionOptions();
  },
  methods: {
    search(currentProjectId) {
      this.selectRows = new Set();
      this.condition.moduleIds = this.selectNodeIds;
      if (this.trashEnable) {
        this.condition.filters = { status: ['Trash'] };
        this.condition.moduleIds = [];
      }
      if (typeof currentProjectId === 'string') {
        this.condition.projectId = currentProjectId;
      } else if (this.projectId) {
        this.condition.projectId = this.projectId;
      }

      if (this.condition.projectId) {
        this.result = getScenarioList(this.currentPage, this.pageSize, this.condition).then((response) => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach((item) => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
        });
      }
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
      getOwnerProjects().then((res) => {
        this.projectList = res.data;
      });
    },
    getProject() {
      getProjectConfig(this.projectId, '/SCENARIO_CUSTOM_NUM').then((result) => {
        if (result.data) {
          this.customNum = result.data.scenarioCustomNum;
        }
      });
    },
    getConditions() {
      this.condition.tableDataIds = Array.from(this.tableData).map((row) => row.id);
      return this.condition;
    },
    checkEnv() {
      return this.$refs.envPopover.checkEnv();
    },
    changeVersion(currentVersion) {
      this.condition.versionId = currentVersion || null;
      this.search();
    },
    getVersionOptions() {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID()).then((response) => {
          this.versionFilters = response.data.map((u) => {
            return { text: u.name, value: u.id };
          });
        });
      }
    },
    filter(field) {
      this.condition.filters = field || null;
      this.search();
    },
    selectCountChange(data) {
      this.selectRows = this.$refs.scenarioTable.selectRows;
      this.$emit('selectCountChange', data);
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
</style>
