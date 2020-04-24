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
        @row-click="showDetail"
        class="test-content">
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          :filters="priorityFilters"
          :filter-method="filter"
          :label="$t('test_track.case.priority')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="type"
          :filters="typeFilters"
          :filter-method="filter"
          :label="$t('test_track.case.type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <type-table-item :value="scope.row.type"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="method"
          :filters="methodFilters"
          :filter-method="filter"
          :label="$t('test_track.case.method')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <method-table-item :value="scope.row.method"/>
          </template>
        </el-table-column>
        <el-table-column
          prop="nodePath"
          :label="$t('test_track.case.module')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="updateTime"
          sortable
          :label="$t('commons.update_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator @editClick="handleEdit(scope.row)" @deleteClick="handleDelete(scope.row)">
              <template v-slot:middle>
                <ms-table-operator-button :tip="$t('commons.copy')" icon="el-icon-document-copy"
                                          type="success" @exec="handleCopy(scope.row)"/>
              </template>
            </ms-table-operator>
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
  import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
  import TypeTableItem from "../../common/tableItems/planview/TypeTableItem";
  import MethodTableItem from "../../common/tableItems/planview/MethodTableItem";
  import MsTableOperator from "../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";

  export default {
    name: "TestCaseList",
    components: {
      MsTableOperatorButton,
      MsTableOperator,
      MethodTableItem,
      TypeTableItem,
      PriorityTableItem,
      MsCreateBox, TestCaseImport, TestCaseExport, MsTablePagination, NodeBreadcrumb, MsTableHeader},
      data() {
        return {
          result: {},
          deletePath: "/test/case/delete",
          condition: {},
          tableData: [],
          currentPage: 1,
          pageSize: 5,
          total: 0,
          priorityFilters: [
            {text: 'P0', value: 'P0'},
            {text: 'P1', value: 'P1'},
            {text: 'P2', value: 'P2'}
          ],
          methodFilters: [
            {text: this.$t('test_track.case.manual'), value: 'manual'},
            {text: this.$t('test_track.case.auto'), value: 'auto'}
          ],
          typeFilters: [
            {text: this.$t('commons.functional'), value: 'functional'},
            {text: this.$t('commons.performance'), value: 'performance'},
            {text: this.$t('commons.api'), value: 'api'}
          ]
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
          this.$emit('testCaseEdit');
        },
        handleEdit(testCase) {
          this.$emit('testCaseEdit', testCase);
        },
        handleCopy(testCase) {
          this.$emit('testCaseCopy', testCase);
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
        },
        filter(value, row, column) {
          const property = column['property'];
          return row[property] === value;
        },
        showDetail(row, event, column) {
          this.$emit('testCaseDetail', row);
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

  .el-table {
    cursor:pointer;
  }

</style>
