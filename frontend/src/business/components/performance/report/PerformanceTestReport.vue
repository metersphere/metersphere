<template>
  <div class="testreport-container" v-loading="result.loading">

    <div class="main-content">
      <el-card>
        <div slot="header">
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
            <template slot-scope="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="250"
            :label="$t('commons.update_time')">
            <template slot-scope="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            :label="$t('commons.status')">
            <template slot-scope="{row}">
              <el-tag size="mini" type="primary" v-if="row.status === 'Starting'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="success" v-else-if="row.status === 'Running'">
                {{ row.status }}
              </el-tag>
              <el-tooltip placement="top" v-else-if="row.status === 'Error'" effect="light">
                <div slot="content">{{row.description}}</div>
                <el-tag size="mini" type="danger">
                  {{ row.status }}
                </el-tag>
              </el-tooltip>
              <el-tag size="mini" type="info" v-else>
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            width="150"
            :label="$t('commons.operating')">
            <template slot-scope="scope">
              <el-button @click="handleEdit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
              <el-button @click="handleDelete(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
            </template>
          </el-table-column>
        </el-table>
        <div>
          <el-row>
            <el-col :span="22" :offset="1">
              <div class="table-page">
                <el-pagination
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                  :current-page.sync="currentPage"
                  :page-sizes="[5, 10, 20, 50, 100]"
                  :page-size="pageSize"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="total">
                </el-pagination>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

  </div>
</template>

<script>
  export default {
    name: "PerformanceTestReport",
    created: function () {
      this.initTableData();
    },
    data() {
      return {
        result: {},
        queryPath: "/report/list/all",
        deletePath: "/report/delete/",
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
      handleSizeChange(size) {
        this.pageSize = size;
        this.initTableData();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.initTableData();
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      handleEdit(report) {
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

  .testreport-container {
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .test-content {
    width: 100%;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

</style>
