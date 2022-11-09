<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result">
        <template v-slot:header>
          <ms-table-header :condition.sync="condition" v-if="loadIsOver" @search="search" :show-create="false">
            <template v-slot:button>
              <el-button-group>

                <el-tooltip class="item" effect="dark" content="left" :disabled="true" placement="left">
                  <el-button plain :class="{active: leftActive}" @click="changeTab('left')">
                    {{ $t('commons.scenario') }}
                  </el-button>
                </el-tooltip>

                <el-tooltip class="item" effect="dark" content="right" :disabled="true" placement="right">
                  <el-button plain :class="{active: rightActive}" @click="changeTab('right')">
                    {{ $t('api_test.definition.request.case') }}
                  </el-button>
                </el-tooltip>

              </el-button-group>
            </template>
          </ms-table-header>
        </template>


        <el-table ref="reportListTable" border :data="tableData" class="adjust-table table-content" @sort-change="sort"
                  @select-all="handleSelectAll"
                  @select="handleSelect"
                  :height="screenHeight"
                  @filter-change="filter" @row-click="handleView" v-if="loadIsOver">
          <el-table-column
            type="selection"/>
          <el-table-column width="40" :resizable="false" align="center">
            <el-dropdown slot="header" style="width: 14px">
              <span class="el-dropdown-link" style="width: 14px">
                <i class="el-icon-arrow-down el-icon--right" style="margin-left: 0px"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native.stop="isSelectDataAll(true)">
                  {{ $t('api_test.batch_menus.select_all_data', [total]) }}
                </el-dropdown-item>
                <el-dropdown-item @click.native.stop="isSelectDataAll(false)">
                  {{ $t('api_test.batch_menus.select_show_data', [tableData.length]) }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <template v-slot:default="scope">
              <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
            </template>
          </el-table-column>

          <ms-table-column
            prop="name"
            sortable
            :label="$t('commons.name')"
            :show-overflow-tooltip="false"
            :editable="true"
            :edit-content="$t('report.rename_report')"
            @editColumn="openReNameDialog"
            min-width="200px">
          </ms-table-column>

          <el-table-column prop="reportType" :label="$t('load_test.report_type')" width="150"
                           column-key="reportType"
                           :filters="reportTypeFilters">
            <template v-slot:default="scope">
              <div v-if="scope.row.reportType === 'SCENARIO_INTEGRATED'">
                <el-tag size="mini" type="primary">
                  {{ $t('api_test.scenario.integrated') }}
                </el-tag>
                {{ $t('commons.scenario') }}
              </div>
              <div v-else-if="scope.row.reportType === 'API_INDEPENDENT'">
                <el-tag size="mini" type="primary">
                  {{ $t('api_test.scenario.independent') }}
                </el-tag>
                case
              </div>
              <div v-else-if="scope.row.reportType === 'API_INTEGRATED'">
                <el-tag size="mini" type="primary">
                  {{ $t('api_test.scenario.integrated') }}
                </el-tag>
                case
              </div>
              <div v-else>
                <el-tag size="mini" type="primary">
                  {{ $t('api_test.scenario.independent') }}
                </el-tag>
                {{ $t('commons.scenario') }}
              </div>
            </template>
          </el-table-column>
          <ms-table-column prop="userName" :label="$t('api_test.creator')" width="150" show-overflow-tooltip
                           :filters="userFilters"/>
          <el-table-column prop="createTime" min-width="120" :label="$t('commons.create_time')" sortable>
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="endTime" min-width="120" :label="$t('report.test_end_time')" sortable>
            <template v-slot:default="scope">
              <span v-if="scope.row.endTime && scope.row.endTime > 0">
                {{ scope.row.endTime | datetimeFormat }}
              </span>
              <span v-else>
                {{ scope.row.updateTime | datetimeFormat }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="triggerMode" width="150" :label="$t('commons.trigger_mode.name')"
                           column-key="triggerMode" :filters="triggerFilters">
            <template v-slot:default="scope">
              <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('commons.status')"
            :filters="statusFilters"
            column-key="status"
            prop="status">
            <template v-slot:default="{row}">
              <ms-api-report-status :status="row.status"/>
            </template>
          </el-table-column>
          <el-table-column width="150" :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <div>
                <ms-table-operator-button
                  :tip="$t('api_report.detail')" icon="el-icon-s-data"
                  @exec="handleView(scope.row)" type="primary"/>
                <ms-table-operator-button
                  :tip="$t('api_report.delete')"
                  v-permission="['PROJECT_API_REPORT:READ+DELETE']"
                  icon="el-icon-delete" @exec="handleDelete(scope.row)" type="danger"/>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
      <ms-rename-report-dialog ref="renameDialog" @submit="rename($event)"></ms-rename-report-dialog>
      <el-dialog :close-on-click-modal="false" :title="$t('test_track.plan_view.test_result')" width="60%"
                 :visible.sync="resVisible" class="api-import" destroy-on-close @close="resVisible=false">
        <ms-request-result-tail :report-id="reportId" :response="response" ref="debugResult"/>
      </el-dialog>
    </ms-main-container>
  </ms-container>
</template>
<script>
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {REPORT_CASE_CONFIGS, REPORT_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import {_filter, _sort} from "metersphere-frontend/src/utils/tableUtils";
import MsRenameReportDialog from "metersphere-frontend/src/components/report/MsRenameReportDialog";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsRequestResultTail from "@/business/definition/components/response/RequestResultTail";
import MsTabButton from "@/business/commons/MsTabs";
import {getMaintainer} from "@/api/project";
import {delBatchReport, delReport, getReportPage, reportReName} from "@/api/scenario-report";
import {getApiReportPage} from "@/api/definition-report";
import {REPORT_STATUS} from "@/business/commons/js/commons";

export default {
  components: {
    ReportTriggerModeItem: () => import("metersphere-frontend/src/components/tableItem/ReportTriggerModeItem"),
    MsTableOperatorButton: () => import("metersphere-frontend/src/components/MsTableOperatorButton"),
    MsApiReportStatus: () => import("./ApiReportStatus"),
    MsMainContainer: () => import("metersphere-frontend/src/components/MsMainContainer"),
    MsContainer: () => import("metersphere-frontend/src/components/MsContainer"),
    MsTableHeader: () => import("metersphere-frontend/src/components/MsTableHeader"),
    MsTablePagination: () => import("metersphere-frontend/src/components/pagination/TablePagination"),
    ShowMoreBtn: () => import("@/business/commons/ShowMoreBtn"),
    MsRenameReportDialog,
    MsTableColumn,
    MsTabButton,
    MsRequestResultTail,
  },
  props: {
    reportType: String
  },
  data() {
    return {
      result: false,
      resVisible: false,
      response: {},
      reportId: "",
      debugVisible: false,
      condition: {
        components: REPORT_CONFIGS
      },
      tableData: [],
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      loadIsOver: true,
      total: 0,
      loading: false,
      currentProjectId: "",
      statusFilters: REPORT_STATUS,
      reportTypeFilters: [],
      reportScenarioFilters: [
        {text: this.$t('api_test.scenario.independent') + this.$t('commons.scenario'), value: 'SCENARIO_INDEPENDENT'},
        {text: this.$t('api_test.scenario.integrated') + this.$t('commons.scenario'), value: 'SCENARIO_INTEGRATED'}
      ],
      reportCaseFilters: [
        {text: this.$t('api_test.scenario.independent') + 'case', value: 'API_INDEPENDENT'},
        {text: this.$t('api_test.scenario.integrated') + 'case', value: 'API_INTEGRATED'},
      ],
      triggerFilters: [
        {text: this.$t('commons.trigger_mode.manual'), value: 'MANUAL'},
        {text: this.$t('commons.trigger_mode.schedule'), value: 'SCHEDULE'},
        {text: this.$t('commons.trigger_mode.api'), value: 'API'},
        {text: this.$t('api_test.automation.batch_execute'), value: 'BATCH'},
      ],
      buttons: [
        {
          name: this.$t('api_report.batch_delete'),
          handleClick: this.handleBatchDelete,
          permissions: ['PROJECT_API_REPORT:READ+DELETE']
        }
      ],
      selectRows: new Set(),
      selectAll: false,
      unSelection: [],
      selectDataCounts: 0,
      screenHeight: 'calc(100vh - 160px)',
      activeDom: 'left',
      userFilters: [],
    }
  },
  watch: {
    '$route'(to, from) {
      if (to.path.startsWith('/api/automation/report')) {
        this.init();
      }
    },
    activeDom() {
      this.condition.filters = {report_type: []};
      this.search();
    }
  },
  computed: {
    leftActive() {
      return this.activeDom === 'left';
    },
    rightActive() {
      return this.activeDom === 'right';
    },
  },
  methods: {
    getMaintainerOptions() {
      getMaintainer().then(response => {
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    search() {
      if (this.testId !== 'all') {
        this.condition.testId = this.testId;
      }
      this.condition.projectId = getCurrentProjectID();
      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;

      this.condition.reportType = this.reportType;
      if (this.condition.orders && this.condition.orders.length > 0) {
        let order = this.condition.orders[this.condition.orders.length - 1];
        this.condition.orders = [];
        this.condition.orders.push(order);
      }

      //解析并处理跳转的参数
      this.genRedirectParams(this.condition);
      if (this.activeDom === 'left') {
        this.reportTypeFilters = this.reportScenarioFilters;
        this.result = getReportPage(this.currentPage, this.pageSize, this.condition).then(res => {
          this.setData(res);
        })
      } else {
        this.reportTypeFilters = this.reportCaseFilters;
        this.result = getApiReportPage(this.currentPage, this.pageSize, this.condition).then(res => {
          this.setData(res);
        })
      }
    },
    genRedirectParams(condition) {
      let selectDataRange = "all";
      let selectDataType = "all";
      let routeParamObj = this.$route.params;
      if (routeParamObj) {
        selectDataRange = routeParamObj.dataSelectRange;
        selectDataType = routeParamObj.dataType;
      }

      if ((this.activeDom === 'left' && selectDataType === 'scenario') || (this.activeDom === 'right' && selectDataType === 'apiCase')) {
        condition.selectDataType = selectDataType;
        switch (selectDataRange) {
          case 'scheduleExecutionPassCount':
            this.condition.selectDataRange = 'scheduleExecutionPass';
            break;
          case 'scheduleExecutionFakeErrorCount':
            this.condition.selectDataRange = 'scheduleExecutionFakeError';
            break;
          case 'scheduleExecutionFailedCount':
            this.condition.selectDataRange = 'scheduleExecutionFailed';
            break;
        }
      }
    },
    setData(response) {
      let data = response.data;
      this.total = data.itemCount;
      this.tableData = data.listObject;
      this.selectRows.clear();
      this.unSelection = data.listObject.map(s => s.id);
    },
    handleView(report) {
      this.reportId = report.id;
      if (report.status === 'RUNNING' || report.status === 'RERUNNING') {
        this.$warning(this.$t('commons.run_warning'))
        return;
      }
      if (report.reportType.indexOf('SCENARIO') !== -1 || report.reportType === 'API_INTEGRATED') {
        this.currentProjectId = report.projectId;
        this.$router.push({
          path: 'report/view/' + report.id,
          query: {list: true}
        });
      } else {
        this.resVisible = true;
      }
    },
    handleDelete(report) {
      this.$alert(this.$t('api_report.delete_confirm') + report.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            delReport(report.id).then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.search();
            });
          }
        }
      });
    },
    init() {
      this.testId = this.$route.params.testId;
      this.search();
    },
    sort(column) {
      _sort(column, this.condition);
      this.init();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.init();
    },
    handleSelect(selection, row) {
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }
      this.selectRowsCount(this.selectRows)
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.tableData.forEach(item => {
          this.$set(item, "showMore", true);
          this.selectRows.add(item);
        });
      } else {
        this.selectRows.clear();
        this.tableData.forEach(row => {
          this.$set(row, "showMore", false);
        })
      }
      this.selectRowsCount(this.selectRows)
    },
    handleBatchDelete() {
      this.$alert(this.$t('api_report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            let sendParam = {};
            sendParam.ids = ids;
            sendParam.selectAllDate = this.isSelectAllDate;
            sendParam.unSelectIds = this.unSelection;
            sendParam = Object.assign(sendParam, this.condition);
            sendParam.caseType = this.activeDom === 'right' ? 'API' : 'SCENARIO';
            delBatchReport(sendParam).then(() => {
              this.selectRows.clear();
              this.$success(this.$t('commons.delete_success'));
              this.search();
            });
          }
        }
      });
    },
    selectRowsCount(selection) {
      let selectedIDs = this.getIds(selection);
      let allIDs = this.tableData.map(s => s.id);
      this.unSelection = allIDs.filter(function (val) {
        return selectedIDs.indexOf(val) === -1
      });
      if (this.isSelectAllDate) {
        this.selectDataCounts = this.total - this.unSelection.length;
      } else {
        this.selectDataCounts = selection.size;
      }
    },
    isSelectDataAll(dataType) {
      this.isSelectAllDate = dataType;
      this.selectRowsCount(this.selectRows)
      //如果已经全选，不需要再操作了
      if (this.selectRows.size != this.tableData.length) {
        this.$refs.reportListTable.toggleAllSelection(true);
      }
    },
    getIds(rowSets) {
      let rowArray = Array.from(rowSets)
      let ids = rowArray.map(s => s.id);
      return ids;
    },
    openReNameDialog($event) {
      this.$refs.renameDialog.open($event);
    },
    rename(data) {
      reportReName(data).then(() => {
        this.$success(this.$t("organization.integration.successful_operation"));
        this.init();
        this.$refs.renameDialog.close();
      });
    },
    changeTab(tabType) {
      this.activeDom = tabType;
      if (tabType === 'right') {
        this.condition.components = REPORT_CASE_CONFIGS;
      } else {
        this.condition.components = REPORT_CONFIGS;
      }
      this.loadIsOver = false;
      this.$nextTick(() => {
        this.loadIsOver = true;
      });
    },
  },

  created() {
    this.init();
    this.getMaintainerOptions();
  }
}
</script>

<style scoped>
.table-content {
  width: 100%;
}

.active {
  border: solid 1px #6d317c !important;
  background-color: var(--primary_color) !important;
  color: #FFFFFF !important;
}

.item {
  height: 32px;
  padding: 5px 8px;
  border: solid 1px var(--primary_color);
}
</style>
