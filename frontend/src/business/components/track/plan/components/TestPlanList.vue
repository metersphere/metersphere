<template>

  <div class="testplan-container">
    <el-main class="main-content">
      <el-card>
        <template v-slot:header>
          <div>
            <el-row type="flex" justify="space-between" align="middle">
              <el-col :span="5">
                <span class="title">{{$t('test_track.test_plan')}}</span>
              </el-col>

              <el-col :span="1" :offset="8">
                <el-button icon="el-icon-circle-plus-outline" size="small" round
                           @click="$emit('openTestPlanEditDialog')" >{{$t('commons.create')}}</el-button>
              </el-col>

              <el-col :span="1" >
                <el-button
                  icon="el-icon-refresh" size="small" round
                  @click="initTableData()">{{$t('commons.refresh')}}</el-button>
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
          :data="tableData"
          class="test-content"
          @row-click="intoPlan">
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="130"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="principal"
            :label="$t('test_track.plan_principal')"
            width="130"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="status"
            :label="$t('test_track.plan_status')"
            width="130"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <span v-if="scope.row.status == 'Prepare'">{{$t('test_track.plan_status_prepare')}}</span>
              <span v-if="scope.row.status == 'Running'">{{$t('test_track.plan_status_running')}}</span>
              <span v-if="scope.row.status == 'Completed'">{{$t('test_track.plan_status_completed')}}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="stage"
            :label="$t('test_track.plan_stage')"
            width="130"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <span v-if="scope.row.stage == 'smoke'">{{$t('test_track.smoke_test')}}</span>
              <span v-if="scope.row.stage == 'functional'">{{$t('test_track.functional_test')}}</span>
              <span v-if="scope.row.stage == 'integration'">{{$t('test_track.integration_testing')}}</span>
              <span v-if="scope.row.stage == 'system'">{{$t('test_track.system_test')}}</span>
              <span v-if="scope.row.stage == 'version'">{{$t('test_track.version_validation')}}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="projectName"
            :label="$t('test_track.plan_project')"
            width="160"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            width="160"
            :label="$t('commons.create_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="160"
            :label="$t('commons.update_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="160"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <el-button @click="handleEdit(scope.row)"
                         @click.stop="deleteVisible = true" type="primary"
                         icon="el-icon-edit" size="mini" circle/>
              <el-button @click="handleDelete(scope.row)"
                         @click.stop="deleteVisible = true" type="danger"
                         icon="el-icon-delete" size="mini" circle/>
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
  </div>
</template>

<script>
    export default {
      name: "TestPlanList",
      data() {
        return {
          result: {},
          queryPath: "/test/plan/list",
          deletePath: "/test/plan/delete",
          condition: "",
          tableData: [],
          multipleSelection: [],
          currentPage: 1,
          pageSize: 5,
          total: 0,
          loadingRequire: {project: true, testCase: true},
          testId: null,
        }
      },
      created() {
        this.projectId = this.$route.params.projectId;
        this.initTableData();
      },
      methods: {
        initTableData(nodeIds) {
          let param = {
            name: this.condition,
          };
          param.nodeIds = nodeIds;

          this.$post(this.buildPagePath(this.queryPath), param, response => {
            this.loadingRequire.testCase = false;
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
          this.$emit('testPlanEdit', testPlan);
        },
        handleDelete(testPlan) {
          this.$alert(this.$t('load_test.delete_confirm') + testPlan.name + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                this._handleDelete(testPlan);
              }
            }
          });
        },
        _handleDelete(testPlan) {
          let testPlanId = testPlan.id;
          this.$post('/test/plan/delete/' + testPlanId, {}, () => {
            this.initTableData();
            this.$message({
              message: this.$t('commons.delete_success'),
              type: 'success'
            });
          });
        },
        intoPlan(row, event, column) {
          this.$router.push('/track/plan/view/' + row.id);
        }
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

  .test-content {
    width: 100%;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
