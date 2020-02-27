<template>
  <div class="testplan-container" v-loading="result.loading">
    <div class="main-content">
      <el-card>
        <div slot="header">
          <el-row type="flex" justify="space-between" align="middle">
            <span class="title">测试</span>
            <span class="search">
            <el-input type="text" size="small" placeholder="根据名称搜索" prefix-icon="el-icon-search" maxlength="60"
                      v-model="condition" @change="search" clearable/>
          </span>
          </el-row>
        </div>
        <el-table :data="tableData" style="width: 100%">
          <el-table-column
            type="selection"
            width="55">
          </el-table-column>
          <el-table-column
            prop="name"
            label="名称"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="description"
            label="描述"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="projectName"
            label="所属项目"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            width="250"
            label="创建时间">
            <template slot-scope="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="250"
            label="更新时间">
            <template slot-scope="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="150"
            label="操作">
            <template slot-scope="scope">
              <el-button @click="handleEdit(scope.row)" type="text" size="small">编辑</el-button>
              <el-button @click="handleDelete(scope.row)" type="text" size="small">删除</el-button>
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
    data() {
      return {
        result: {},
        queryPath: "/testplan/list",
        deletePath: "/testplan/delete",
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
    watch: {
      '$route'(to) {
        this.projectId = to.params.projectId;
        this.initTableData();
      }
    },
    created: function () {
      this.projectId = this.$route.params.projectId;
      this.initTableData();
    },
    methods: {
      initTableData() {
        let param = {
          name: this.condition,
        };

        if (this.projectId !== 'all') {
          param.projectId = this.projectId;
        }

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
      handleEdit(testPlan) {
        this.$router.push({
          path: '/editTest/' + testPlan.id,
          params: {
            testId: testPlan.id,
            testPlanObj: testPlan
          }
        })
      },
      handleDelete(testPlan) {
        this.$alert('确认删除测试: ' + testPlan.name + "？", '', {
          confirmButtonText: '确定',
          callback: (action) => {
            if (action === 'confirm') {
              this._handleDelete(testPlan);
            }
          }
        });
      },
      _handleDelete(testPlan) {
        let data = {
          id: testPlan.id
        };

        this.result = this.$post(this.deletePath, data, () => {
          this.$message({
            message: '删除成功！',
            type: 'success'
          });
          this.initTableData();
        });
      },
    }
  }
</script>

<style scoped>
  .testplan-container {
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

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
