<template>

  <el-main class="main-content">
    <el-card v-loading="result.loading">
      <template v-slot:header>
        <div>
          <el-row type="flex" justify="space-between" align="middle">
            <el-col :span="5">
              <span class="title">{{$t('test_track.test_case')}}</span>
            </el-col>

            <el-col :span="2" :offset="8">
              <el-button icon="el-icon-connection" size="small" round
                         @click="$emit('openTestCaseRelevanceDialog')" >{{$t('test_track.relevance_test_case')}}</el-button>
            </el-col>

            <el-col :span="5">
                  <span class="search">
                    <el-input type="text" size="small" :placeholder="$t('load_test.search_by_name')"
                              prefix-icon="el-icon-search"
                              maxlength="60"
                              v-model="condition" @change="search" clearable/>
                  </span>
            </el-col>
          </el-row>
        </div>
      </template>

      <el-table
        :data="tableData">

        <el-table-column
          prop="name"
          :label="$t('commons.name')">
        </el-table-column>
        <el-table-column
          prop="priority"
          :label="$t('test_track.priority')">
        </el-table-column>

        <el-table-column
          prop="type"
          :label="$t('test_track.type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span v-if="scope.row.type == 'functional'">{{$t('test_track.functional_test')}}</span>
            <span v-if="scope.row.type == 'performance'">{{$t('commons.performance')}}</span>
            <span v-if="scope.row.type == 'api'">{{$t('commons.api')}}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="method"
          :label="$t('test_track.method')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span v-if="scope.row.method == 'manual'">{{$t('test_track.manual')}}</span>
            <span v-if="scope.row.method == 'auto'">{{$t('test_track.auto')}}</span>
          </template>
        </el-table-column>


        <el-table-column
          prop="executor"
          :label="$t('test_track.executor')">
        </el-table-column>

        <el-table-column
          prop="status"
          :label="$t('test_track.execute_result')">
          <template v-slot:default="scope">
            <el-tag v-if="scope.row.status == 'Prepare'"
                    e ffect="info"
                    size="mini">{{$t('test_track.plan_status_prepare')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Pass'"
                    type="success"
                    effect="dark"
                    size="mini">{{$t('test_track.pass')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Failure'"
                    type="danger"
                    effect="dark"
                    size="mini">{{$t('test_track.failure')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Blocking'"
                    type="warning"
                    effect="dark"
                    size="mini">{{$t('test_track.blocking')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Skip'"
                    type="info"
                    effect="dark"
                    size="mini">{{$t('test_track.skip')}}</el-tag>
          </template>
        </el-table-column>

        <el-table-column
          :label="$t('commons.update_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <el-button @click="handleEdit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="handleDelete(scope.row)" type="danger" icon="el-icon-unlock" size="mini" circle/>
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
  </el-main>
</template>

<script>
  import PlanNodeTree from './PlanNodeTree';

  export default {
      name: "TestPlanTestCaseList",
      components: {PlanNodeTree},
      data() {
        return {
          result: {},
          deletePath: "/test/case/delete",
          condition: "",
          tableData: [],
          multipleSelection: [],
          currentPage: 1,
          pageSize: 5,
          total: 0,
          loadingRequire: {project: true, testCase: true},
          testId: null
        }
      },
      props:{
        planId: {
          type: String
        }
      },
      watch: {
        planId() {
          this.initTableData();
        }
      },
      created: function () {
        this.initTableData();
      },
      methods: {
        initTableData(nodeIds) {
          if (this.planId) {
            let param = {
              name: this.condition,
            };
            param.nodeIds = nodeIds;
            param.planId = this.planId;
            this.result = this.$post(this.buildPagePath('/test/plan/case/list'), param, response => {
              this.loadingRequire.testCase = false;
              let data = response.data;
              this.total = data.itemCount;
              this.tableData = data.listObject;
            });
          }
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
        handleEdit(testCase) {
          this.$emit('editTestPlanTestCase', testCase);
        },
        handleDelete(testCase) {
          this.$alert(this.$t('test_track.confirm_cancel_relevance') + ' ' + testCase.name + " ？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                this._handleDelete(testCase);
              }
            }
          });
        },
        _handleDelete(testCase) {
          let testCaseId = testCase.id;
          this.$post('/test/plan/case/delete/' + testCaseId, {}, () => {
            this.initTableData();
            this.$emit("refresh");
            this.$message({
              message: this.$t('commons.delete_success'),
              type: 'success'
            });
          });
        }
      }
    }
</script>

<style scoped>

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }


  /*.main-content {*/
    /*margin: 0 auto;*/
    /*width: 100%;*/
    /*max-width: 1200px;*/
  /*}*/

</style>
