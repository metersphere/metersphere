<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="search"
                           :title="$t('api_report.title')"
                           :show-create="false"/>
        </template>
        <el-table :data="tableData" class="table-content" @sort-change="sort"
                  @filter-change="filter" @row-click="handleView">
          <el-table-column :label="$t('commons.name')" width="200" show-overflow-tooltip prop="name">
          </el-table-column>
          <el-table-column prop="testName" :label="$t('api_report.test_name')" width="200" show-overflow-tooltip/>
          <el-table-column prop="projectName" :label="$t('load_test.project_name')" width="150" show-overflow-tooltip/>
          <el-table-column prop="userName" :label="$t('api_test.creator')" width="150" show-overflow-tooltip/>
          <el-table-column width="250" :label="$t('commons.create_time')" sortable
                           prop="createTime">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('commons.status')"
                           column-key="status"
                           :filters="statusFilters">
            <template v-slot:default="{row}">
              <ms-api-report-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column width="150" :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <el-button :is-tester-permission="true" @click="handleView(scope.row)" type="primary"
                         icon="el-icon-s-data" size="mini" circle/>
              <el-button :is-tester-permission="true" @click="handleDelete(scope.row)" type="danger"
                         icon="el-icon-delete" size="mini" circle/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import MsApiReportStatus from "./ApiReportStatus";
  import {_filter, _sort} from "../../../../common/js/utils";

  export default {
    components: {MsApiReportStatus, MsMainContainer, MsContainer, MsTableHeader, MsTablePagination},
    data() {
      return {
        result: {},
        condition: {},
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        loading: false,
        statusFilters: [
          {text: 'Saved', value: 'Saved'},
          {text: 'Starting', value: 'Starting'},
          {text: 'Running', value: 'Running'},
          {text: 'Reporting', value: 'Reporting'},
          {text: 'Completed', value: 'Completed'},
          {text: 'Error', value: 'Error'}
        ]
      }
    },

    watch: {
      '$route': 'init',
    },

    methods: {
      search() {
        if (this.testId !== 'all') {
          this.condition.testId = this.testId;
        }

        let url = "/api/report/list/" + this.currentPage + "/" + this.pageSize
        this.result = this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        });
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      handleView(report) {
        this.$router.push({
          path: '/api/report/view/' + report.id,
        })
      },
      handleDelete(report) {
        this.$alert(this.$t('api_report.delete_confirm') + report.name + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.result = this.$post("/api/report/delete", {id: report.id}, () => {
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

    },

    created() {
      this.init();
    }
  }
</script>

<style scoped>
  .table-content {
    width: 100%;
  }
</style>
