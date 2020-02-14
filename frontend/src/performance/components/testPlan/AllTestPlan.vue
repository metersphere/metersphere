<template>
  <div class="testplan-container">
    <div>
      <el-row>
        <el-col :span="22" :offset="1">
          <el-table
            stripe
            ref="multipleTable"
            :data="tableData"
            tooltip-effect="dark"
            style="width: 100%"
            @selection-change="handleSelectionChange">
            <el-table-column
              type="selection"
              width="55">
            </el-table-column>
            <el-table-column
              prop="name"
              label="名称">
            </el-table-column>
            <el-table-column
              prop="description"
              label="描述"
              show-overflow-tooltip>
            </el-table-column>
            <el-table-column
              prop="projectName"
              label="所属项目">
            </el-table-column>
            <el-table-column
              label="创建时间">
              <template slot-scope="scope">
                <span>{{ scope.row.createTime | timestampFormatDate }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="updateTime"
              label="更新时间">
              <template slot-scope="scope">
                <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
              </template>
            </el-table-column>
            <el-table-column
              label="操作">
              <template slot-scope="scope">
                <el-button @click="handleEdit(scope.row)" type="text" size="small">编辑</el-button>
                <el-button @click="handleDelete(scope.row)" type="text" size="small">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
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
  </div>
</template>

<script>
  export default {
    data() {
      return {
        queryPath: "/testplan/list",
        deletePath: "/testplan/delete",
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
      }
    },
    created: function () {
      this.initTableData();
    },
    methods: {
      initTableData() {
        /// todo: 查询参数
        let param = {};

        this.$post(this.buildPagePath(this.queryPath), param).then(response => {
          if (response.data.success) {
            let data = response.data.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
          } else {
            this.$message.error(response.message);
          }
        })
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
          name: 'createTest',
          params: {
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

        this.$post(this.deletePath, data).then(response => {
          if (response.data.success) {
            this.$message({
              message: '删除成功！',
              type: 'success'
            });
            this.initTableData();
          } else {
            this.$message.error(response.message);
          }
        });
      },
    }
  }
</script>

<style scoped>
  .testplan-container {
    background: white;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
