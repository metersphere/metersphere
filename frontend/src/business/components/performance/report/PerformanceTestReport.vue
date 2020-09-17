<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="search"
                           :title="$t('commons.report')"
                           :show-create="false"/>
        </template>

        <el-table border :data="tableData" class="adjust-table test-content"
                  @select-all="handleSelectAll"
                  @select="handleSelect"
                  @sort-change="sort"
                  @filter-change="filter"
        >
          <el-table-column
            type="selection"/>
          <el-table-column width="40" :resizable="false" align="center">
            <template v-slot:default="scope">
              <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectRows.size"/>
            </template>
          </el-table-column>
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="testName"
            :label="$t('report.test_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="projectName"
            :label="$t('report.project_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="userName"
            :label="$t('report.user_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="createTime"
            sortable
            width="250"
            :label="$t('commons.create_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="triggerMode" width="150" :label="'触发方式'" column-key="triggerMode"
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
            width="150"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operator-button :tip="$t('api_report.detail')" icon="el-icon-s-data"
                                        @exec="handleEdit(scope.row)" type="primary"/>
              <ms-table-operator-button :is-tester-permission="true" :tip="$t('api_report.delete')"
                                        icon="el-icon-delete" @exec="handleDelete(scope.row)" type="danger"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsTablePagination from "../../common/pagination/TablePagination";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsPerformanceReportStatus from "./PerformanceReportStatus";
import {_filter, _sort} from "../../../../common/js/utils";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import ReportTriggerModeItem from "../../common/tableItem/ReportTriggerModeItem";
import {REPORT_CONFIGS} from "../../common/components/search/search-components";
import MsTableHeader from "../../common/components/MsTableHeader";
import {LIST_CHANGE, PerformanceEvent} from "@/business/components/common/head/ListEvent";
import ShowMoreBtn from "../../track/case/components/ShowMoreBtn";

export default {
  name: "PerformanceTestReport",
  components: {
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
    this.initTableData();
  },
  data() {
    return {
      result: {},
      queryPath: "/performance/report/list/all",
      deletePath: "/performance/report/delete/",
      condition: {
        components: REPORT_CONFIGS
      },
      projectId: null,
      tableData: [],
      multipleSelection: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      loading: false,
      testId: null,
      statusFilters: [
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      triggerFilters: [
        {text: '手动', value: 'MANUAL'},
        {text: '定时任务', value: 'SCHEDULE'},
        {text: 'API', value: 'API'}
      ],
      buttons: [
        {
          name: this.$t('report.batch_delete'), handleClick: this.handleBatchDelete
        }
      ],
      selectRows: new Set(),
    }
  },
  watch: {
    '$route'(to) {
      this.projectId = to.params.projectId;
      this.initTableData();
    }
  },
  methods: {
    initTableData() {
      if (this.testId !== 'all') {
        this.condition.testId = this.testId;
      }
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
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
    handleEdit(report) {
      if (report.status === "Error") {
        this.$warning(this.$t('report.generation_error'));
        return false
      } else if (report.status === "Starting") {
        this.$info(this.$t('report.being_generated'))
        return false
      }
      this.$router.push({
        path: '/performance/report/view/' + report.id
      })
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
    _handleDelete(report) {
      this.result = this.$post(this.deletePath + report.id, {}, () => {
        this.$success(this.$t('commons.delete_success'));
        this.initTableData();
        // 发送广播，刷新 head 上的最新列表
        PerformanceEvent.$emit(LIST_CHANGE);
      });
    },
    sort(column) {
      _sort(column, this.condition);
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

      let arr = Array.from(this.selectRows);

      // 选中1个以上的用例时显示更多操作
      if (this.selectRows.size === 1) {
        this.$set(arr[0], "showMore", false);
      } else if (this.selectRows.size === 2) {
        arr.forEach(row => {
          this.$set(row, "showMore", true);
        })
      }
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        if (selection.length === 1) {
          this.selectRows.add(selection[0]);
        } else {
          this.tableData.forEach(item => {
            this.$set(item, "showMore", true);
            this.selectRows.add(item);
          });
        }
      } else {
        this.selectRows.clear();
        this.tableData.forEach(row => {
          this.$set(row, "showMore", false);
        })
      }
    },
    handleBatchDelete() {
      this.$alert(this.$t('report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let ids = Array.from(this.selectRows).map(row => row.id);
            this.result = this.$post('/performance/report/batch/delete', {ids: ids}, () => {
              this.selectRows.clear();
              this.$success(this.$t('commons.delete_success'));
              this.search();
              // 发送广播，刷新 head 上的最新列表
              PerformanceEvent.$emit(LIST_CHANGE);
            });
          }
        }
      });
    }
  }
}
</script>

<style scoped>

.test-content {
  width: 100%;
}

</style>
