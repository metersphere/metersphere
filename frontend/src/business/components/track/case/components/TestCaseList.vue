<template>

  <el-main>
    <el-card v-loading="result.loading">
      <template v-slot:header>
        <div>
          <el-row type="flex" justify="space-between" align="middle">
            <el-col :span="5">
              <span class="title">{{$t('test_track.test_case')}}</span>
            </el-col>

            <el-col :span="2" :offset="10">
              <el-button icon="el-icon-circle-plus-outline" size="small" round
                         @click="$emit('openTestCaseEditDialog')" >{{$t('commons.create')}}</el-button>
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
        class="test-content">
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          width="120"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          :label="$t('test_track.priority')"
          width="120"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="type"
          :label="$t('test_track.type')"
          width="120"
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
          width="120"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span v-if="scope.row.method == 'manual'">{{$t('test_track.manual')}}</span>
            <span v-if="scope.row.method == 'auto'">{{$t('test_track.auto')}}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="nodePath"
          :label="$t('test_track.module')"
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
          width="100"
          :label="$t('commons.operating')">
          <template v-slot:default="scope">
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
  </el-main>
</template>

<script>
  import {CURRENT_PROJECT} from '../../../../../common/constants';

  export default {
      name: "TestCaseList",
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
      props: {
        currentProject: {
          type: Object
        }
      },
      created: function () {
        this.initTableData();
      },
      watch: {
        currentProject() {
          this.initTableData();
        }
      },
      methods: {
        initTableData(nodeIds) {
          let param = {
            name: this.condition,
          };
          param.nodeIds = nodeIds;

          if (this.currentProject) {
            param.projectId = this.currentProject.id;
            this.result = this.$post(this.buildPagePath('/test/case/list'), param, response => {
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
          this.$emit('testCaseEdit', testCase);
        },
        handleDelete(testCase) {
          this.$alert(this.$t('load_test.delete_confirm') + testCase.name + "？", '', {
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
          this.$post('/test/case/delete/' + testCaseId, {}, () => {
            this.initTableData();
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

</style>
