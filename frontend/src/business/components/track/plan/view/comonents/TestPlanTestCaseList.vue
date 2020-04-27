<template>
  <el-card v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initTableData" :show-create="false">
          <template v-slot:title>
            <node-breadcrumb class="table-title" :node-names="selectNodeNames" @refresh="refresh"/>
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
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          :filters="priorityFilters"
          :filter-method="filter"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" ref="priority"/>
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
          prop="executor"
          :label="$t('test_track.plan_view.executor')">
        </el-table-column>

        <el-table-column
          prop="status"
          :filters="statusFilters"
          :filter-method="filter"
          :label="$t('test_track.plan_view.execute_result')">
          <template v-slot:default="scope">
            <status-table-item :value="scope.row.status"/>
          </template>
        </el-table-column>

        <el-table-column
          sortable
          prop="updateTime"
          :label="$t('commons.update_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator-button :tip="$t('commons.edit')" icon="el-icon-edit" @exec="handleEdit(scope.row)" />
            <ms-table-operator-button :tip="$t('test_track.plan_view.cancel_relevance')" icon="el-icon-unlock" type="danger" @exec="handleDelete(scope.row)"/>
            <!--<ms-table-operator @editClick="handleEdit(scope.row, scope.$index)" @deleteClick="handleDelete(scope.row)"/>-->
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

      <test-plan-test-case-edit
        ref="testPlanTestCaseEdit"
        :search-param="condition"
        @refresh="refresh"
        @refreshTable="search"/>

    </el-card>
</template>

<script>
  import ExecutorEdit from './ExecutorEdit';
  import StatusEdit from './StatusEdit';
  import TestPlanTestCaseEdit from "./TestPlanTestCaseEdit";
  import MsTipButton from '../../../../common/components/MsTipButton';
  import MsTablePagination from '../../../../common/pagination/TablePagination';
  import MsTableHeader from '../../../../common/components/MsTableHeader';
  import MsTableButton from '../../../../common/components/MsTableButton';
  import NodeBreadcrumb from '../../../common/NodeBreadcrumb';

  import {TokenKey} from '../../../../../../common/js/constants';
  import {tableFilter} from '../../../../../../common/js/utils';
  import PriorityTableItem from "../../../common/tableItems/planview/PriorityTableItem";
  import StatusTableItem from "../../../common/tableItems/planview/StatusTableItem";
  import TypeTableItem from "../../../common/tableItems/planview/TypeTableItem";
  import MethodTableItem from "../../../common/tableItems/planview/MethodTableItem";
  import MsTableOperator from "../../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";

  export default {
      name: "TestPlanTestCaseList",
      components: {
        MsTableOperatorButton,
        MsTableOperator,
        MethodTableItem,
        TypeTableItem,
        StatusTableItem,
        PriorityTableItem, StatusEdit, ExecutorEdit, MsTipButton, MsTablePagination,
        TestPlanTestCaseEdit, MsTableHeader, NodeBreadcrumb, MsTableButton},
      data() {
        return {
          result: {},
          deletePath: "/test/case/delete",
          condition: {},
          showMyTestCase: false,
          tableData: [],
          currentPage: 1,
          pageSize: 10,
          total: 0,
          selectIds: new Set(),
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
          ],
          statusFilters: [
            {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
            {text: this.$t('test_track.plan_view.pass'), value: 'Pass'},
            {text: this.$t('test_track.plan_view.failure'), value: 'Failure'},
            {text: this.$t('test_track.plan_view.blocking'), value: 'Blocking'},
            {text: this.$t('test_track.plan_view.skip'), value: 'Skip'},
          ]
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
              this.selectIds.clear();
            });
          }
        },
        refresh() {
          this.condition = {};
          this.selectIds.clear();
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
            this.$success(this.$t('commons.delete_success'));
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
            this.$warning(this.$t('test_track.plan_view.select_manipulate'));
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
        },
        filter(value, row, column) {
          const property = column['property'];
          return row[property] === value;
        }
      }
    }
</script>

<style scoped>

  .search {
    margin-left: 10px;
    width: 240px;
  }

</style>
