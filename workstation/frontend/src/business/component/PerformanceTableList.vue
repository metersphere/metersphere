<template>
  <div class="card-container">
    <ms-table :table-is-loading="this.result"
              :data="tableData"
              :condition="condition"
              :page-size="pageSize"
              :total="total"
              :screen-height="screenHeight"
              :field-key="tableHeaderKey"
              :remember-order="true"
              row-key="id"
              :row-order-group-id="projectId"
              :row-order-func="editLoadTestCaseOrder"
              :enable-selection="false"
              @refresh="search"
              :disable-header-config="true"
              ref="table">

      <el-table-column
        prop="num"
        label="ID"
        width="80"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span @click="link(scope.row)" style="cursor: pointer;">{{ scope.row.num }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span @click="link(scope.row)" style="cursor: pointer;">{{ scope.row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="status"
        column-key="status"
        :filters="statusFilters"
        :label="$t('commons.status')">
        <template v-slot:default="scope">
          <ms-performance-test-status :row="scope.row"/>
        </template>
      </el-table-column>

      <el-table-column
        v-if="versionEnable"
        :label="$t('project.version.name')"
        column-key="versionId"
        min-width="100px"
        prop="versionId">
        <template v-slot:default="scope">
          <span>{{ scope.row.versionName }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="userName"
        sortable="custom"
        width="110"
        :filters="userFilters"
        column-key="user_id"
        :label="$t('load_test.user_name')"
        show-overflow-tooltip>
      </el-table-column>


      <el-table-column
        width="160"
        sortable
        prop="createTime"
        :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | datetimeFormat }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="projectName"
        width="120"
        :label="$t('load_test.project_name')"
      >
      </el-table-column>

      <el-table-column
        width="200"
        sortable
        v-if="isShowAllColumn"
        prop="updateTime"
        :label="$t('commons.update_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="reportCount"
        v-if="isShowAllColumn"
        :label="$t('report.load_test_report')"
        width="150">
        <template v-slot:default="scope">
          <el-link v-if="scope.row.reportCount > 0" @click="reports(scope.row)">
            {{ scope.row.reportCount }}
          </el-link>
          <span v-else> {{ scope.row.reportCount }}</span>
        </template>
      </el-table-column>

    </ms-table>
    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

  </div>
</template>

<script>

import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getLastTableSortField} from "metersphere-frontend/src/utils/tableUtils";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {editLoadTestCaseOrder} from "@/api/load-test";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsPerformanceTestStatus from "@/business/module/performance/PerformanceTestStatus";
import {TEST_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {getProjectMember} from "@/api/user";
import {getProjectVersions} from "metersphere-frontend/src/api/version";
import {searchTests} from "@/api/performance";
import {versionEnableByProjectId} from "@/api/project";

export default {
  name: "PerformanceTableList",
  components: {
    MsTable,
    MsTableHeader,
    MsPerformanceTestStatus,
    MsTablePagination,
    MsTableOperator,
    MsContainer,
    MsMainContainer,
    MsTableOperators
  },
  data() {
    return {
      tableHeaderKey: "PERFORMANCE_TEST_TABLE",
      result: false,
      condition: {
        components: TEST_CONFIGS,
        combine: {
          creator: {
            operator: "current user",
            value: "current user",
          }
        }
      },
      projectId: null,
      tableData: [],
      versionFilters: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      testId: null,
      statusFiltersSelect: [
        {text: 'Error', value: 'Error'}
      ],
      statusFiltersAll: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      statusFilters: [],
      userFilters: [],
      versionEnable: false,
    };
  },
  watch: {
    '$route'(to) {
      if (to.name !== 'perPlan') {
        return;
      }
      this.projectId = to.params.projectId;
      this.initTableData();
    },
    currentVersion() {
      this.condition.versionId = this.currentVersion;
      this.initTableData();
      // 选择了版本过滤，版本列上的checkbox也进行过滤
      this.getVersionOptions(this.currentVersion);
    },
  },
  computed: {
    editLoadTestCaseOrder() {
      return editLoadTestCaseOrder;
    }
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
    currentVersion: String,
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 200px)';
      }
    }, //屏幕高度
  },
  created: function () {
    this.projectId = getCurrentProjectID();
    if (this.isShowAllColumn) {
      if (this.isCreation || this.isFocus) {
        this.statusFilters = this.statusFiltersAll
      } else {
        this.statusFilters = this.statusFiltersSelect
      }
    } else {
      if (this.isFocus) {
        this.statusFilters = this.statusFiltersAll
      } else {
        this.statusFilters = this.statusFiltersSelect
      }
    }
    this.condition.versionId = this.currentVersion;
    this.initTableData();
    this.getMaintainerOptions();
    this.getVersionOptions();
    this.checkVersionEnable();
  },
  methods: {
    getMaintainerOptions() {
      getProjectMember((data) => {
        if (this.isCreation) {
          data.map(u => {
            if (u.id === getCurrentUserId()) {
              let a = {text: u.name, value: u.id};
              this.userFilters.push(a);
            }
          });
        } else {
          this.userFilters = data.map(u => {
            return {text: u.name, value: u.id};
          });
        }
      });
    },
    initTableData() {
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);
      if (this.isFocus) {
        if (this.condition.filters) {
          delete this.condition.filters['user_id']
        }
        this.condition.combine = {followPeople: {operator: "current user", value: "current user",}}
      } else if (this.isCreation) {
        if (this.condition.filters) {
          delete this.condition.filters['user_id']
        }
        this.condition.combine = {creator: {operator: "current user", value: "current user",}}
      } else {
        if (this.condition.filters) {
          this.condition.filters.status = ["Error"];
        } else {
          this.condition.filters = {status: ["Error"]};
        }
      }
      if (this.isSelectAll === false) {
        this.condition.projectId = getCurrentProjectID();
      }
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.result = searchTests(this.currentPage, this.pageSize, this.condition)
        .then(response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      });
    },
    search(combine) {
      this.initTableData(combine);
    },

    link(row) {
      let performanceResolve = this.$router.resolve({
        path: '/performance/test/edit/' + row.id,
        query: {projectId: row.projectId}
      });
      window.open(performanceResolve.href, '_blank');

    },

    create() {
      if (!getCurrentProjectID()) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$router.push('/performance/test/create');

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
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        versionEnableByProjectId(this.projectId).then(response => {
          this.versionEnable = response.data;
          this.loading = false;
          this.$nextTick(() => {
            this.loading = true;
          });
        });
      }
    },
  }
};
</script>

<style scoped>

</style>
