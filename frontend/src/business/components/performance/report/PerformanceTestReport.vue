<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <div>
            <el-row type="flex" justify="space-between" align="middle">
              <span class="title">{{$t('commons.report')}}</span>
              <span class="search">
              <el-input type="text" size="small" :placeholder="$t('report.search_by_name')"
                        prefix-icon="el-icon-search"
                        maxlength="60"
                        v-model="condition.name" @change="search" clearable/>
            </span>
            </el-row>
          </div>
        </template>

        <el-table :data="tableData" class="test-content"
                  @sort-change="sort"
                  @filter-change="filter"
        >
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
              <ms-table-operator-button :tip="$t('api_report.detail')" icon="el-icon-s-data" @exec="handleEdit(scope.row)" type="primary"/>
              <ms-table-operator-button :is-tester-permission="true" :tip="$t('api_report.delete')" icon="el-icon-delete" @exec="handleDelete(scope.row)" type="danger"/>
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

  export default {
    name: "PerformanceTestReport",
    components: {MsTableOperatorButton, MsPerformanceReportStatus, MsTablePagination, MsContainer, MsMainContainer},
    created: function () {
      this.initTableData();
    },
    data() {
      return {
        result: {},
        queryPath: "/performance/report/list/all",
        deletePath: "/performance/report/delete/",
        condition: {},
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
        ]
      }
    },
    methods: {
      initTableData() {
        this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        });
      },
      search() {
        this.initTableData();
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
    }
  }
</script>

<style scoped>

  .test-content {
    width: 100%;
  }

</style>
