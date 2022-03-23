<template>
  <ms-container>
    <ms-main-container v-if="loading">
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :condition.sync="condition" @search="search"
                           :create-permission="['PROJECT_PERFORMANCE_TEST:READ+CREATE']"
                           :version-options="versionOptions"
                           :current-version.sync="currentVersion"
                           :is-show-version=true
                           @changeVersion="changeVersion"
                           @create="create" :createTip="$t('load_test.create')"/>
        </template>
        <ms-table
          :data="tableData"
          :condition="condition"
          :page-size="pageSize"
          :total="total"
          :operators="operators"
          :screenHeight="screenHeight"
          :field-key="tableHeaderKey"
          :remember-order="true"
          :enable-order-drag="enableOrderDrag"
          row-key="id"
          :row-order-group-id="projectId"
          :row-order-func="editLoadTestCaseOrder"
          :batch-operators="batchButtons"
          operator-width="120px"
          :screen-height="screenHeight"
          @refresh="search"
          :disable-header-config="true"
          ref="table">
          <el-table-column
            prop="num"
            label="ID"
            width="100">
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
            v-if="versionEnable"
            :label="$t('project.version.name')"
            :filters="versionFilters"
            column-key="versionId"
            prop="versionId">
            <template v-slot:default="scope">
              <span>{{ scope.row.versionName }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="userName"
            sortable="custom"
            :filters="userFilters"
            column-key="user_id"
            :label="$t('load_test.user_name')"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            width="200"
            sortable
            prop="createTime"
            :label="$t('commons.create_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="200"
            sortable
            prop="updateTime"
            :label="$t('commons.update_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="reportCount"
            :label="$t('commons.report')">
            <template v-slot:default="scope">
              <el-link v-if="scope.row.reportCount > 0" @click="reports(scope.row)">
                {{ scope.row.reportCount }}
              </el-link>
              <span v-else> {{ scope.row.reportCount }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            column-key="status"
            :filters="statusFilters"
            :label="$t('commons.status')">
            <template v-slot:default="{row}">
              <ms-performance-test-status :row="row"/>
            </template>
          </el-table-column>
        </ms-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
    <!--  删除接口提示  -->
    <list-item-delete-confirm ref="apiDeleteConfirm" @handleDelete="_handleDelete"/>
  </ms-container>
</template>

<script>
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsPerformanceTestStatus from "./PerformanceTestStatus";
import MsTableOperators from "../../common/components/MsTableOperators";
import {getCurrentProjectID, getCurrentWorkspaceId, hasLicense} from "@/common/js/utils";
import MsTableHeader from "../../common/components/MsTableHeader";
import {TEST_CONFIGS} from "../../common/components/search/search-components";
import {buildBatchParam, getLastTableSortField} from "@/common/js/tableUtils";
import MsTable from "@/business/components/common/components/table/MsTable";
import {editLoadTestCaseOrder} from "@/network/load-test";
import ListItemDeleteConfirm from "@/business/components/common/components/ListItemDeleteConfirm";

export default {
  components: {
    ListItemDeleteConfirm,
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
      result: {},
      deletePath: "/performance/delete",
      condition: {
        components: TEST_CONFIGS
      },
      projectId: null,
      tableData: [],
      multipleSelection: [],
      versionFilters: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: true,
      testId: null,
      enableOrderDrag: true,
      batchButtons: [
        {
          name: this.$t('commons.delete_batch'),
          handleClick: this.handleBatchDelete,
          permissions: ['PROJECT_PERFORMANCE_TEST:READ+DELETE']
        }
      ],
      operators: [
        {
          tip: this.$t('commons.run'), icon: "el-icon-video-play",
          class: 'run-button',
          exec: this.handleRun,
          permissions: ['PROJECT_PERFORMANCE_TEST:READ+RUN']
        },
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_PERFORMANCE_TEST:READ+EDIT']
        },
        {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "primary",
          exec: this.handleCopy,
          permissions: ['PROJECT_PERFORMANCE_TEST:READ+COPY']
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_PERFORMANCE_TEST:READ+DELETE']
        }
      ],
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      userFilters: [],
      screenHeight: 'calc(100vh - 200px)',
      versionOptions: [],
      currentVersion: '',
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
    }
  },
  computed: {
    editLoadTestCaseOrder() {
      return editLoadTestCaseOrder;
    }
  },
  created() {
    this.projectId = getCurrentProjectID();
    this.initTableData();
    this.getMaintainerOptions();
    this.getVersionOptions();
    this.checkVersionEnable();
  },
  methods: {
    getMaintainerOptions() {
      let workspaceId = getCurrentWorkspaceId();
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    initTableData() {
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);

      this.enableOrderDrag = this.condition.orders.length > 0 ? false : true;

      this.condition.projectId = getCurrentProjectID();
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.result = this.$post(this.buildPagePath('/performance/list'), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        // 查询报告数量
        this.tableData.forEach(test => {
          this.result = this.$get('/performance/test/report-count/' + test.id, response => {
            this.$set(test, 'reportCount', response.data);
          });
        });
      });
    },
    search(combine) {
      this.initTableData(combine);
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    handleEdit(test) {
      this.$router.push({
        path: '/performance/test/edit/' + test.id,
      });
    },
    handleCopy(test) {
      this.result = this.$post("/performance/copy", {id: test.id}, () => {
        this.$success(this.$t('commons.copy_success'));
        this.search();
      });
    },
    handleRun(test) {
      this.result = this.$post("/performance/run", {id: test.id, triggerMode: 'MANUAL'}, (response) => {
        let reportId = response.data;
        this.$router.push({path: '/performance/report/view/' + reportId});
      });
    },
    handleBatchDelete() {
      this.$alert(this.$t('load_test.batch_delete_confirm') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this, this.$refs.table.selectIds);
            this.result = this.$post("/performance/delete/batch", param, () => {
              this.$success(this.$t('commons.delete_success'));
              this.initTableData();
            })
          }
        }
      });
    },
    handleDelete(test) {
      this.$get('/performance/versions/' + test.id, response => {
        if (hasLicense() && this.versionEnable && response.data.length > 1) {
          // 删除提供列表删除和全部版本删除
          this.$refs.apiDeleteConfirm.open(test, this.$t('load_test.delete_confirm'));
        } else {
          this.$alert(this.$t('load_test.delete_confirm') + test.name + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                this._handleDelete(test, false);
              }
            }
          });
        }
      });
    },
    _handleDelete(test, deleteCurrentVersion) {
      if (deleteCurrentVersion) {
        this.$get('performance/delete/' + test.versionId + '/' + test.refId, () => {
          this.$success(this.$t('commons.delete_success'));
          this.initTableData();
          this.$refs.apiDeleteConfirm.close();
        });
      } else {
        let data = {
          id: test.id
        };
        this.result = this.$post(this.deletePath, data, () => {
          this.$success(this.$t('commons.delete_success'));
          this.initTableData();
          this.$refs.apiDeleteConfirm.close();
        });
      }
    },
    link(row) {
      this.$router.push({
        path: '/performance/test/edit/' + row.id,
      });
    },
    reports(row) {
      this.$router.push({
        path: '/performance/report/' + row.id,
      });
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
        this.$get('/project/version/get-project-versions/' + getCurrentProjectID(), response => {
          this.versionOptions = response.data;
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
    changeVersion(value) {
      this.currentVersion = value || null;
      this.condition.versionId = value || null;
      this.refresh();
      this.getVersionOptions(value);
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + this.projectId, response => {
          this.versionEnable = response.data;
          this.loading = false;
          this.$nextTick(() => {
            this.loading = true;
          });
        });
      }
    },
    refresh(data) {
      this.initTableData();
      //this.$refs.nodeTree.list();
    },
  }
};
</script>

<style scoped>

.test-content {
  width: 100%;
}

.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

</style>
