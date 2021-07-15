<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card">
        <template v-slot:header>
          <ms-table-header :condition.sync="condition" @search="search"
                           :show-create="false"/>
        </template>

        <el-table v-loading="result.loading"
                  border :data="tableData" class="adjust-table test-content"
                  @select-all="handleSelectAll"
                  @select="handleSelect"
                  @sort-change="sort"
                  @filter-change="filter"
                  :height="screenHeight"
        >
          <el-table-column
            type="selection"/>
          <el-table-column width="40" :resizable="false" align="center">
            <template v-slot:default="scope">
              <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
            </template>
          </el-table-column>
          <el-table-column
            prop="testName"
            :label="$t('report.test_name')"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <span @click="handleView(scope.row)" style="cursor: pointer;">{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="userName"
            :label="$t('report.user_name')"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="maxUsers"
            width="65"
            :label="$t('report.max_users')">
          </el-table-column>
          <el-table-column
            width="100"
            prop="avgResponseTime"
            :label="$t('report.response_time')">
          </el-table-column>
          <el-table-column
            prop="tps"
            label="TPS">
          </el-table-column>
          <el-table-column
            width="100"
            prop="testStartTime"
            :label="$t('report.test_start_time') ">
            <template v-slot:default="scope">
              <span v-if="scope.row.testStartTime > 0">{{ scope.row.testStartTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="100"
            prop="testEndTime"
            :label="$t('report.test_end_time')">
            <template v-slot:default="scope">
              <span v-if="scope.row.status === 'Completed'">{{ scope.row.testEndTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="90"
            prop="testDuration"
            :label="$t('report.test_execute_time')">
            <template v-slot:default="scope">
              <span v-if="scope.row.status === 'Completed'">
                {{ scope.row.minutes }}{{ $t('schedule.cron.minutes') }}
                {{ scope.row.seconds }}{{ $t('schedule.cron.seconds') }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="triggerMode" :label="$t('test_track.report.list.trigger_mode')"
                           column-key="triggerMode"
                           :filters="triggerFilters">
            <template v-slot:default="scope">
              <report-trigger-mode-item :trigger-mode="scope.row.triggerMode"/>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            column-key="status"
            :filters="statusFilters"
            :label="$t('commons.status')">
            <template v-slot:default="{row}">
              <ms-performance-report-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column
            width="180"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operator-button :tip="$t('test_track.module.rename')" icon="el-icon-edit"
                                        v-permission="['PROJECT_PERFORMANCE_REPORT:READ+DELETE']"
                                        @exec="handleRename(scope.row)" type="success"/>
              <ms-table-operator-button :tip="$t('api_report.detail')" icon="el-icon-s-data"
                                        v-permission="['PROJECT_PERFORMANCE_REPORT:READ']"
                                        @exec="handleView(scope.row)" type="primary"/>
              <ms-table-operator-button :tip="$t('load_test.report.diff')" icon="el-icon-s-operation"
                                        v-permission="['PROJECT_PERFORMANCE_REPORT:READ']"
                                        @exec="handleDiff(scope.row)" type="warning"/>
              <ms-table-operator-button :tip="$t('api_report.delete')"
                                        v-permission="['PROJECT_PERFORMANCE_REPORT:READ+DELETE']"
                                        icon="el-icon-delete" @exec="handleDelete(scope.row)" type="danger"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
    <same-test-reports ref="compareReports"/>
  </ms-container>
</template>

<script>
import MsTablePagination from "../../common/pagination/TablePagination";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsPerformanceReportStatus from "./PerformanceReportStatus";
import {getCurrentProjectID, getCurrentWorkspaceId} from "@/common/js/utils";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import ReportTriggerModeItem from "../../common/tableItem/ReportTriggerModeItem";
import {REPORT_CONFIGS} from "../../common/components/search/search-components";
import MsTableHeader from "../../common/components/MsTableHeader";
import ShowMoreBtn from "../../track/case/components/ShowMoreBtn";
import {_filter, _sort,saveLastTableSortField,getLastTableSortField} from "@/common/js/tableUtils";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import SameTestReports from "@/business/components/performance/report/components/SameTestReports";

export default {
  name: "PerformanceTestReportList",
  components: {
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
  },
  created: function () {
    this.testId = this.$route.path.split('/')[3];
    this.initTableData();
  },
  data() {
    return {
      tableHeaderKey:"PERFORMANCE_REPORT_TABLE",
      result: {},
      deletePath: "/performance/report/delete/",
      condition: {
        components: REPORT_CONFIGS
      },
      projectId: null,
      tableData: [],
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      testId: null,
      screenHeight: 'calc(100vh - 200px)',
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
        {text: this.$t('commons.trigger_mode.api'), value: 'API'},
      ],
      buttons: [
        {
          name: this.$t('report.batch_delete'), handleClick: this.handleBatchDelete
        }
      ],
      selectRows: new Set(),
    };
  },
  watch: {
    '$route'(to) {
      if (to.name !== 'perReport') {
        return;
      }
      this.projectId = to.params.projectId;
      this.testId = this.$route.path.split('/')[3];
      this.initTableData();
    }
  },
  methods: {
    handleTimeInfo(report) {
      if (report.testStartTime) {
        let duration = report.testDuration;
        let minutes = Math.floor(duration / 60);
        let seconds = duration % 60;
        this.$set(report, 'minutes', minutes);
        this.$set(report, 'seconds', seconds);
      }
      if (report.status === 'Completed' && !report.testStartTime) {
        this.result = this.$get("/performance/report/content/report_time/" + report.id)
          .then(res => {
            let data = res.data.data;
            if (data) {
              let duration = data.duration;
              let minutes = Math.floor(duration / 60);
              let seconds = duration % 60;
              this.$set(report, 'testStartTime', data.startTime);
              this.$set(report, 'testEndTime', data.endTime);
              this.$set(report, 'minutes', minutes);
              this.$set(report, 'seconds', seconds);
            }
          }).catch(() => {
          });
      }
    },
    handleOverview(report) {
      if (report.status === 'Completed' && !report.maxUsers) {
        this.result = this.$get('/performance/report/content/testoverview/' + report.id)
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
      let orderArr = this.getSortField();
      if(orderArr){
        this.condition.orders = orderArr;
      }
      if (!getCurrentProjectID()) {
        return;
      }
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.condition.projectId = getCurrentProjectID();
      this.result = this.$post(this.buildPagePath('/performance/report/list/all'), this.condition, response => {
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
    handleSelectionChange(val) {
      this.multipleSelection = val;
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
        this.$post('/performance/report/rename', {id: report.id, name: value}, response => {
          this.initTableData();
        });
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
      this.result = this.$post(this.deletePath + report.id, {}, () => {
        this.initTableData();
      });
    },
    _handleDelete(report) {
      this.result = this.$post(this.deletePath + report.id, {}, () => {
        this.$success(this.$t('commons.delete_success'));
        this.initTableData();
      });
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.saveSortField(this.tableHeaderKey,this.condition.orders);
      this.initTableData();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    handleSelect(selection, row) {
      if (this.selectRows.has(row)) {
        this.$set(row, "showMore", false);
        this.selectRows.delete(row);
      } else {
        this.$set(row, "showMore", true);
        this.selectRows.add(row);
      }
    },
    saveSortField(key,orders){
      saveLastTableSortField(key,JSON.stringify(orders));
    },
    getSortField(){
      let orderJsonStr = getLastTableSortField(this.tableHeaderKey);
      let returnObj = null;
      if(orderJsonStr){
        try {
          returnObj = JSON.parse(orderJsonStr);
        }catch (e){
          return null;
        }
      }
      return returnObj;
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
        });
      }
    },
    handleBatchDelete() {
      this.$alert(this.$t('report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = [];
            this.selectRows.forEach(row => {
              ids.push(row.id);
            });
            this.result = this.$post("/performance/report/batch/delete", {ids: ids}, () => {
              this.initTableData();
            });

            this.$success(this.$t('commons.delete_success'));
          }
        },
      });
    }
  }
};
</script>

<style scoped>

.test-content {
  width: 100%;
}
</style>
