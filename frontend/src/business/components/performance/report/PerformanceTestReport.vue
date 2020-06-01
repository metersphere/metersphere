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
                        v-model="condition" @change="search" clearable/>
            </span>
            </el-row>
          </div>
        </template>

        <el-table :data="tableData" class="test-content">
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="200"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="testName"
            :label="$t('report.test_name')"
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
            width="250"
            :label="$t('commons.create_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="250"
            :label="$t('commons.update_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            :label="$t('commons.status')">
            <template v-slot:default="{row}">
              <ms-performance-report-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column
            width="150"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <el-button :is-tester-permission="true" @click="handleEdit(scope.row)" type="primary" icon="el-icon-s-data" size="mini" circle/>
              <el-button :is-tester-permission="true" @click="handleDelete(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
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

  export default {
    name: "PerformanceTestReport",
    components: {MsPerformanceReportStatus, MsTablePagination, MsContainer, MsMainContainer},
    created: function () {
      this.initTableData();
    },
    data() {
      return {
        result: {},
        queryPath: "/performance/report/list/all",
        deletePath: "/performance/report/delete/",
        condition: "",
        projectId: null,
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        loading: false,
        testId: null,
      }
    },
    methods: {
      initTableData() {
        let param = {
          name: this.condition,
        };
        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
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
    }
  }
</script>

<style scoped>

  .test-content {
    width: 100%;
  }

</style>
