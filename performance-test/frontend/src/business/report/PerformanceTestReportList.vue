<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card">
        <template v-slot:header>
          <ms-table-header :condition.sync="condition" @search="search"
                           :show-create="false"/>
        </template>
        <ms-table
          v-loading="loading"
          :data="tableData"
          :condition="condition"
          :page-size="pageSize"
          :total="total"
          :operators="operators"
          :screenHeight="screenHeight"
          :fields.sync="fields"
          :field-key="tableHeaderKey"
          :remember-order="true"
          row-key="id"
          :row-order-group-id="projectId"
          :batch-operators="buttons"
          operator-width="130px"
          :screen-height="screenHeight"
          @refresh="search"
          ref="table">
          <span v-for="(item, index) in fields" :key="index">
            <ms-table-column
              prop="name"
              sortable
              :label="$t('commons.name')"
              :show-overflow-tooltip="false"
              :field="item"
              :fields-width="fieldsWidth"
              :editable="true"
              :edit-content="$t('report.rename_report')"
              @editColumn="openReNameDialog"
              @click="handleView($event)"
              min-width="200px">
            </ms-table-column>
            <ms-table-column
              v-if="versionEnable"
              :label="$t('project.version.name')"
              :filters="versionFilters"
              min-width="100px"
              :field="item"
              :fields-width="fieldsWidth"
              prop="versionId">
              <template v-slot:default="scope">
                <span>{{ scope.row.versionName }}</span>
              </template>
            </ms-table-column>
            <ms-table-column
              prop="userName"
              :field="item"
              :filters="userFilters"
              :fields-width="fieldsWidth"
              :label="$t('report.user_name')"
              show-overflow-tooltip>
            </ms-table-column>
            <ms-table-column
              prop="maxUsers"
              sortable
              :field="item"
              :fields-width="fieldsWidth"
              min-width="100"
              :label="$t('report.max_users')">
              <template v-slot:default="scope">
                <span>{{ scope.row.maxUsers || '-'}}</span>
              </template>
            </ms-table-column>
            <ms-table-column
              min-width="100"
              :field="item"
              sortable
              :fields-width="fieldsWidth"
              prop="avgResponseTime"
              :label="$t('report.response_time')">
              <template v-slot:default="scope">
                <span>{{ scope.row.avgResponseTime || '-'}}</span>
              </template>
            </ms-table-column>
            <ms-table-column
              min-width="100"
              prop="tps"
              sortable
              :field="item"
              :fields-width="fieldsWidth"
              label="TPS">
              <template v-slot:default="scope">
                <span>{{ scope.row.tps || '-'}}</span>
              </template>
            </ms-table-column>
            <ms-table-column
              min-width="120"
              :field="item"
              sortable
              :fields-width="fieldsWidth"
              show-overflow-tooltip
              prop="testStartTime"
              :label="$t('report.test_start_time') ">
              <template v-slot:default="scope">
                <span v-if="scope.row.testStartTime > 0">{{ scope.row.testStartTime | datetimeFormat }}</span>
              </template>
            </ms-table-column>
            <ms-table-column
              min-width="120"
              show-overflow-tooltip
              :field="item"
              sortable
              :fields-width="fieldsWidth"
              prop="testEndTime"
              :label="$t('report.test_end_time')">
              <template v-slot:default="scope">
                <span v-if="scope.row.status === 'Completed'">{{ scope.row.testEndTime | datetimeFormat }}</span>
                <span v-else>-</span>
              </template>
            </ms-table-column>
            <ms-table-column
              min-width="120"
              prop="testDuration"
              :field="item"
              sortable
              :fields-width="fieldsWidth"
              :label="$t('report.test_execute_time')">
              <template v-slot:default="scope">
                <span>
                 {{ $t('performance_test.report.test_duration', [scope.row.hours, scope.row.minutes, scope.row.seconds]) }}
                </span>
              </template>
            </ms-table-column>
            <ms-table-column
              prop="triggerMode"
              :field="item"
              :fields-width="fieldsWidth"
              :label="$t('test_track.report.list.trigger_mode')"
              min-width="90"
              :filters="triggerFilters"
            >
              <template v-slot:default="scope">
                <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
              </template>
            </ms-table-column>
            <ms-table-column
              prop="status"
              :field="item"
              :fields-width="fieldsWidth"
              :filters="statusFilters"
              :label="$t('commons.status')">
              <template v-slot:default="{row}">
                <ms-performance-report-status :row="row"/>
              </template>
            </ms-table-column>
          </span>

        </ms-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
    <same-test-reports ref="compareReports"/>
    <ms-rename-report-dialog ref="renameDialog" @submit="rename" :max-length="255"></ms-rename-report-dialog>

  </ms-container>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsPerformanceReportStatus from "./PerformanceReportStatus";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import ReportTriggerModeItem from "metersphere-frontend/src/components/tableItem/ReportTriggerModeItem";
import {REPORT_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import {
  _filter,
  _sort,
  buildBatchParam,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  saveLastTableSortField
} from "metersphere-frontend/src/utils/tableUtils";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import SameTestReports from "@/business/report/components/SameTestReports";
import MsRenameReportDialog from "metersphere-frontend/src/components/report/MsRenameReportDialog";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {deleteReport, deleteReportBatch, getOverview, getReportTime, renameReport, searchReports} from "@/api/report";
import {getProjectVersions, isProjectVersionEnable} from "metersphere-frontend/src/api/version";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import {getProjectUsers} from "metersphere-frontend/src/api/user";

export default {
  name: "PerformanceTestReportList",
  components: {
    MsTable,
    SameTestReports,
    MsDialogFooter,
    MsTableHeader,
    ReportTriggerModeItem,
    MsTableOperatorButton,
    MsPerformanceReportStatus,
    MsTablePagination,
    MsContainer,
    MsMainContainer,
    ShowMoreBtn,
    MsRenameReportDialog,
    MsTableColumn,
  },
  created() {
    this.testId = this.$route.path.split('/')[3];
    this.getMaintainerOptions();
    this.initTableData();
    this.getVersionOptions();
    this.checkVersionEnable();
  },
  data() {
    return {
      tableHeaderKey: "PERFORMANCE_REPORT_TABLE",
      deletePath: "/performance/report/delete/",
      condition: {
        components: REPORT_CONFIGS
      },
      projectId: null,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      testId: null,
      screenHeight: 'calc(100vh - 160px)',
      statusFilters: [
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      triggerFilters: [
        {text: this.$t('commons.trigger_mode.manual'), value: 'MANUAL'},
        {text: this.$t('commons.trigger_mode.schedule'), value: 'SCHEDULE'},
        {text: this.$t('commons.trigger_mode.test_plan_schedule'), value: 'TEST_PLAN_SCHEDULE'},
        {text: this.$t('commons.trigger_mode.test_plan_api'), value: 'TEST_PLAN_API'},
        {text: this.$t('commons.trigger_mode.api'), value: 'API'},
        {text: this.$t('commons.trigger_mode.case'), value: 'CASE'},
        {text: this.$t('api_test.automation.batch_execute'), value: 'BATCH'},
      ],
      buttons: [
        {
          name: this.$t('report.batch_delete'), handleClick: this.handleBatchDelete
        }
      ],
      operators: [
        {
          tip: this.$t('api_report.detail'),
          icon: "el-icon-s-data",
          exec: this.handleView,
          type: 'primary',
          permissions: ['PROJECT_PERFORMANCE_REPORT:READ']
        },
        {
          tip: this.$t('load_test.report.diff'),
          icon: "el-icon-s-operation",
          type: "warning",
          exec: this.handleDiff,
          permissions: ['PROJECT_PERFORMANCE_REPORT:READ+COMPARE']
        }, {
          tip: this.$t('commons.delete'),
          icon: "el-icon-delete",
          type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_PERFORMANCE_REPORT:READ+DELETE']
        }
      ],
      selectRows: new Set(),
      versionFilters: [],
      versionOptions: [],
      currentVersion: '',
      versionEnable: false,
      fields: getCustomTableHeader('PERFORMANCE_REPORT_TABLE'),
      fieldsWidth: getCustomTableWidth('PERFORMANCE_REPORT_TABLE'),
      userFilters: [],
    };
  },
  watch: {
    '$route'(to) {
      if (!to.path.startsWith('/performance/report/')) {
        return;
      }
      this.projectId = to.params.projectId;
      this.testId = this.$route.path.split('/')[3];
      this.initTableData();
    }
  },
  methods: {
    getMaintainerOptions() {
      let workspaceId = getCurrentWorkspaceId();
      getProjectUsers()
        .then(response => {
          this.userFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
    },
    handleTimeInfo(report) {
      if (report.testStartTime) {
        let duration = report.testDuration;
        let hours = Math.floor(duration / 60 / 60);
        let minutes = Math.floor(duration / 60 % 60);
        let seconds = duration % 60;
        this.$set(report, 'hours', hours);
        this.$set(report, 'minutes', minutes);
        this.$set(report, 'seconds', seconds);
      }
      if (report.status === 'Completed' && !report.testStartTime) {
        this.loading = getReportTime(report.id)
          .then(res => {
            let data = res.data.data;
            if (data) {
              let duration = data.duration;
              let hours = Math.floor(duration / 60 / 60);
              let minutes = Math.floor(duration / 60 % 60);
              let seconds = duration % 60;
              this.$set(report, 'testStartTime', data.startTime);
              this.$set(report, 'testEndTime', data.endTime);
              this.$set(report, 'hours', hours);
              this.$set(report, 'minutes', minutes);
              this.$set(report, 'seconds', seconds);
            }
          })
          .catch(() => {
          });
      }
    },
    handleOverview(report) {
      this.$set(report, 'maxUsers', parseInt(report.maxUsers));
      this.$set(report, 'avgResponseTime', parseFloat(report.avgResponseTime));
      this.$set(report, 'tps', parseFloat(report.tps));
      if (report.status === 'Completed' && !report.maxUsers) {
        this.loading = getOverview(report.id)
          .then(response => {
            let data = response.data.data;
            this.$set(report, 'maxUsers', data.maxUsers);
            this.$set(report, 'avgResponseTime', data.avgResponseTime);
            this.$set(report, 'tps', data.avgTransactions);
          })
          .catch(() => {
          });
      }
    },
    initTableData() {
      if (this.testId !== 'all') {
        this.condition.testId = this.testId;
      } else {
        this.condition.testId = null;
      }
      this.condition.orders = getLastTableSortField(this.tableHeaderKey);

      if (!getCurrentProjectID()) {
        return;
      }
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.condition.projectId = getCurrentProjectID();
      this.loading = searchReports(this.currentPage, this.pageSize, this.condition)
        .then(response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.selectRows = new Set();

          this.tableData.forEach(report => {
            this.handleOverview(report);
            this.handleTimeInfo(report);
          });
        });
    },
    search(combine) {
      this.initTableData(combine);
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleRename(report) {
      this.$prompt(this.$t('commons.input_name'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        inputValue: report.name,
        inputValidator: function (value) {
          if (value.length > 63) {
            return false;
          }
        }
      }).then(({value}) => {
        renameReport({id: report.id, name: value})
          .then(() => {
            this.initTableData();
          })
      }).catch(() => {

      });
    },
    handleView(report) {
      this.$router.push({
        path: '/performance/report/view/' + report.id
      });
    },
    handleDelete(report) {
      this.$alert(this.$t('report.delete_confirm') + report.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(report);
          }
        }
      });
    },
    handleDiff(report) {
      this.$refs.compareReports.open(report);
    },
    _handleDeleteNoMsg(report) {
      deleteReport(report.id)
        .then(() => {
          this.initTableData();
        })
    },
    _handleDelete(report) {
      this.loading = deleteReport(report.id)
        .then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.initTableData();
        })
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
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    saveSortField(key, orders) {
      saveLastTableSortField(key, JSON.stringify(orders));
    },
    handleBatchDelete() {
      this.$alert(this.$t('report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = buildBatchParam(this, this.$refs.table.selectIds);
            this.loading = deleteReportBatch(param)
              .then(() => {
                this.$success(this.$t('commons.delete_success'));
                this.initTableData();
              })
          }
        },
      });
    },
    getVersionOptions() {
      if (hasLicense()) {
        getProjectVersions(getCurrentProjectID())
          .then(response => {
            this.versionOptions = response.data;
            this.versionFilters = response.data.map(u => {
              return {text: u.name, value: u.id};
            });
          });
      }
    },
    checkVersionEnable() {
      if (!getCurrentProjectID()) {
        return;
      }
      if (hasLicense()) {
        isProjectVersionEnable(getCurrentProjectID())
          .then(response => {
            this.versionEnable = response.data;
          });
      }
    },
    openReNameDialog($event) {
      this.$refs.renameDialog.open($event);
    },
    rename(data) {
      this.loading = renameReport(data)
        .then(() => {
          this.$success(this.$t("organization.integration.successful_operation"));
          this.initTableData();
          this.$refs.renameDialog.close();
        });
    },
  }
};
</script>

<style scoped>

.test-content {
  width: 100%;
}


.table-card :deep( .el-input__icon ) {
  line-height: 23px;
}

</style>
