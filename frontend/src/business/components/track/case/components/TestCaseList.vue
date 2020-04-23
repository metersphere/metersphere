<template>

  <div>
    <el-card v-loading="result.loading">
      <template v-slot:header>

        <ms-table-header :condition.sync="condition" @search="initTableData"
                         :create-tip="$t('test_track.case.create')" @create="testCaseCreate">
          <template v-slot:title>
            <node-breadcrumb class="table-title" :node-names="selectNodeNames" @refresh="refresh"/>
          </template>
          <template v-slot:button>
            <test-case-import :projectId="currentProject == null? null : currentProject.id" @refresh="refresh"/>
            <!--<test-case-export/>-->
          </template>
        </ms-table-header>

      </template>

      <el-table
        :data="tableData"
        class="test-content">
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          :label="$t('test_track.case.priority')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="type"
          :label="$t('test_track.case.type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span v-if="scope.row.type == 'functional'">{{$t('test_track.case.functional_test')}}</span>
            <span v-if="scope.row.type == 'performance'">{{$t('commons.performance')}}</span>
            <span v-if="scope.row.type == 'api'">{{$t('commons.api')}}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="method"
          :label="$t('test_track.case.method')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span v-if="scope.row.method == 'manual'">{{$t('test_track.case.manual')}}</span>
            <span v-if="scope.row.method == 'auto'">{{$t('test_track.case.auto')}}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="nodePath"
          :label="$t('test_track.case.module')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
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
            <el-button @click="handleDelete(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

    </el-card>
  </div>
</template>

<script>

  import MsCreateBox from '../../../settings/CreateBox';
  import TestCaseImport from '../components/TestCaseImport';
  import TestCaseExport from '../components/TestCaseExport';
  import MsTablePagination from '../../../../components/common/pagination/TablePagination';
  import NodeBreadcrumb from '../../common/NodeBreadcrumb';
  import MsTableHeader from '../../../../components/common/components/MsTableHeader';

  export default {
    name: "TestCaseList",
    components: {MsCreateBox, TestCaseImport, TestCaseExport, MsTablePagination, NodeBreadcrumb, MsTableHeader},
    data() {
      return {
        result: {},
        deletePath: "/test/case/delete",
        condition: {},
        tableData: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
      }
      },
      props: {
        currentProject: {
          type: Object
        },
        selectNodeIds: {
          type: Array
        },
        selectNodeNames: {
          type: Array
        }
      },
      created: function () {
        this.initTableData();
      },
      watch: {
        currentProject() {
          this.initTableData();
        },
        selectNodeIds() {
          this.initTableData();
        }
      },
      methods: {
        initTableData() {
          this.condition.nodeIds = this.selectNodeIds;
          if (this.currentProject) {
            this.condition.projectId = this.currentProject.id;
            this.result = this.$post(this.buildPagePath('/test/case/list'), this.condition, response => {
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
        testCaseCreate() {
          this.$emit('openTestCaseEditDialog');
        },
        handleEdit(testCase) {
          this.$emit('testCaseEdit', testCase);
        },
        handleDelete(testCase) {
          this.$alert(this.$t('test_track.case.delete_confirm') + testCase.name + "？", '', {
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
        },
        refresh() {
          this.condition = {};
          this.$emit('refresh');
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

  .operate-button {
    float: right;
  }

  .operate-button > div {
    display: inline-block;
    margin-left: 10px;
  }

  .search {
    margin-left: 10px;
    width: 240px;
  }

</style>
