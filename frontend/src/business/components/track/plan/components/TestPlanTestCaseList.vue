<template>
  <el-card v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initTableData" :show-create="false">
          <template v-slot:title>
            <node-breadcrumb :node-names="selectNodeNames" @refresh="refresh"/>
          </template>
          <template v-slot:button>
            <ms-table-button v-if="!showMyTestCase" icon="el-icon-s-custom" :content="$t('test_track.plan_view.my_case')" @click="searchMyTestCase"/>
            <ms-table-button v-if="showMyTestCase" icon="el-icon-files" :content="$t('test_track.plan_view.all_case')" @click="searchMyTestCase"/>
            <ms-table-button icon="el-icon-connection" :content="$t('test_track.plan_view.relevance_test_case')" @click="$emit('openTestCaseRelevanceDialog')"/>
            <ms-table-button icon="el-icon-edit-outline" :content="$t('test_track.plan_view.change_execution_results')" @click="handleBatch('status')"/>
            <ms-table-button icon="el-icon-user" :content="$t('test_track.plan_view.change_executor')" @click="handleBatch('executor')"/>
          </template>
        </ms-table-header>
      </template>

      <executor-edit ref="executorEdit" :select-ids="selectIds" @refresh="initTableData"/>
      <status-edit ref="statusEdit" :select-ids="selectIds" @refresh="initTableData"/>

      <el-table
        @select-all="handleSelectAll"
        @select="handleSelectionChange"
        row-key="id"
        :data="tableData">

        <el-table-column
          type="selection"></el-table-column>

        <el-table-column
          prop="name"
          :label="$t('commons.name')">
        </el-table-column>
        <el-table-column
          prop="priority"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <el-tag v-if="scope.row.priority == 'P0'"
                    type="danger"
                    effect="dark"
                    size="mini">{{scope.row.priority}}</el-tag>
            <el-tag v-if="scope.row.priority == 'P1'"
                    type="danger"
                    effect="light"
                    size="mini">{{scope.row.priority}}</el-tag>
            <el-tag v-if="scope.row.priority == 'P2'"
                    type="warning"
                    effect="dark"
                    size="mini">{{scope.row.priority}}</el-tag>
            <el-tag v-if="scope.row.priority == 'P3'"
                    type="warning"
                    effect="light"
                    size="mini">{{scope.row.priority}}</el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="type"
          :label="$t('test_track.case.type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span v-if="scope.row.type == 'functional'">{{$t('commons.functional')}}</span>
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
          prop="executor"
          :label="$t('test_track.plan_view.executor')">
        </el-table-column>

        <el-table-column
          prop="status"
          :label="$t('test_track.plan_view.execute_result')">
          <template v-slot:default="scope">
            <el-tag v-if="scope.row.status == 'Prepare'"
                    type="info"
                    effect="dark"
                    size="mini">{{$t('test_track.plan.plan_status_prepare')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Pass'"
                    type="success"
                    effect="dark"
                    size="mini">{{$t('test_track.plan_view.pass')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Failure'"
                    type="danger"
                    effect="dark"
                    size="mini">{{$t('test_track.plan_view.failure')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Blocking'"
                    type="warning"
                    effect="dark"
                    size="mini">{{$t('test_track.plan_view.blocking')}}</el-tag>
            <el-tag v-if="scope.row.status == 'Skip'"
                    type="info"
                    effect="dark"
                    size="mini">{{$t('test_track.plan_view.skip')}}</el-tag>
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
            <el-button @click="handleEdit(scope.row, scope.$index)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="handleDelete(scope.row)" type="danger" icon="el-icon-unlock" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

      <test-plan-test-case-edit
        ref="testPlanTestCaseEdit"
        :search-param="condition"
        @refresh="refresh"/>

    </el-card>
</template>

<script>
  import PlanNodeTree from '../../common/PlanNodeTree';
  import ExecutorEdit from './ExecutorEdit';
  import StatusEdit from './StatusEdit';
  import TestPlanTestCaseEdit from "../components/TestPlanTestCaseEdit";
  import MsTipButton from '../../../../components/common/components/MsTipButton';
  import MsTablePagination from '../../../../components/common/pagination/TablePagination';
  import MsTableHeader from '../../../../components/common/components/MsTableHeader';
  import MsTableButton from '../../../../components/common/components/MsTableButton';
  import NodeBreadcrumb from '../../common/NodeBreadcrumb';

  import {TokenKey} from '../../../../../common/js/constants';

  export default {
      name: "TestPlanTestCaseList",
      components: {PlanNodeTree, StatusEdit, ExecutorEdit, MsTipButton, MsTablePagination,
        TestPlanTestCaseEdit, MsTableHeader, NodeBreadcrumb, MsTableButton},
      data() {
        return {
          result: {},
          deletePath: "/test/case/delete",
          condition: {},
          showMyTestCase: false,
          tableData: [],
          currentPage: 1,
          pageSize: 5,
          total: 0,
          selectIds: new Set()
        }
      },
      props:{
        planId: {
          type: String
        },
        selectNodeIds: {
          type: Array
        },
        selectNodeNames: {
          type: Array
        }
      },
      watch: {
        planId() {
          this.initTableData();
        },
        selectNodeIds() {
          this.search();
        }
      },
      created: function () {
        this.initTableData();
      },
      methods: {
        initTableData() {
          if (this.planId) {
            this.condition.planId = this.planId;
            this.condition.nodeIds = this.selectNodeIds;
            this.result = this.$post(this.buildPagePath('/test/plan/case/list'), this.condition, response => {
              let data = response.data;
              this.total = data.itemCount;
              this.tableData = data.listObject;
            });
          }
        },
        refresh() {
          this.condition = {};
          this.$emit('refresh');
        },
        search() {
          this.initTableData();
        },
        buildPagePath(path) {
          return path + "/" + this.currentPage + "/" + this.pageSize;
        },
        handleEdit(testCase, index) {
          this.$refs.testPlanTestCaseEdit.openTestCaseEdit(testCase);
        },
        handleDelete(testCase) {
          this.$alert(this.$t('test_track.plan_view.confirm_cancel_relevance') + ' ' + testCase.name + " ？", '', {
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
            this.$emit("refresh");
            this.$message({
              message: this.$t('commons.delete_success'),
              type: 'success'
            });
          });
        },
        handleSelectAll(selection) {
          if(selection.length > 0) {
            this.tableData.forEach(item => {
              this.selectIds.add(item.id);
            });
          } else {
            this.selectIds.clear();
          }
        },
        handleSelectionChange(selection, row) {
          if(this.selectIds.has(row.id)){
            this.selectIds.delete(row.id);
          } else {
            this.selectIds.add(row.id);
          }
        },
        handleBatch(type){
          if (this.selectIds.size < 1) {
            this.$message.warning(this.$t('test_track.plan_view.select_manipulate'));
            return;
          }
          if (type === 'executor'){
            this.$refs.executorEdit.openExecutorEdit();
          } else if (type === 'status'){
            this.$refs.statusEdit.openStatusEdit();
          }
        },
        searchMyTestCase() {
          this.showMyTestCase = !this.showMyTestCase;
          if (this.showMyTestCase) {
            let user =  JSON.parse(localStorage.getItem(TokenKey));
            this.condition.executor = user.id;
          } else {
            this.condition.executor = null;
          }
          this.initTableData();
        }
      }
    }
</script>

<style scoped>

  .search {
    margin-left: 10px;
    width: 240px;
  }

  .title {
    height: 50px;
  }

</style>
