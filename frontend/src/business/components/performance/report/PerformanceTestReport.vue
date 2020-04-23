<template>
  <div class="container" v-loading="result.loading">

    <div class="main-content">
      <el-card>
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
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="testName"
            :label="$t('report.test_name')"
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
              <el-tag size="mini" type="primary" v-if="row.status === 'Starting'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="success" v-else-if="row.status === 'Running'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="success" v-else-if="row.status === 'Reporting'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="info" v-else-if="row.status === 'Completed'">
                {{ row.status }}
              </el-tag>
              <el-tooltip placement="top" v-else-if="row.status === 'Error'" effect="light">
                <template v-slot:content>
                  <div>{{row.description}}</div>
                </template>
                <el-tag size="mini" type="danger">
                  {{ row.status }}
                </el-tag>
              </el-tooltip>
              <span v-else>
                {{ row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            width="150"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <el-button @click="handleEdit(scope.row)" type="primary" icon="el-icon-s-data" size="mini" circle/>
              <el-button @click="handleDelete(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </div>

  </div>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";

  export default {
    name: "PerformanceTestReport",
    components: {MsTablePagination},
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
          this.$message({
            type: 'warning',
            message: "报告生成错误,无法查看！"
          });
          return false
        } else if (report.status === "Starting") {
          this.$message({
            type: 'info',
            message: "报告生成中..."
          });
          return false
        }
        this.$router.push({
          path: '/performance/report/view/' + report.id
        })
      },
      handleDelete(report) {
        this.$alert(this.$t('load_test.delete_confirm') + report.name + "？", '', {
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
          this.$message({
            message: this.$t('commons.delete_success'),
            type: 'success'
          });
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

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

</style>
