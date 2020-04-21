<template>

  <div>
    <el-card v-loading="result.loading">
      <template v-slot:header>
        <div>
          <el-row type="flex" justify="space-between" align="middle">
            <span class="title">{{$t('test_track.case.test_case')}}
              <ms-tip-button v-if="!showMyTestCase"
                            :tip="$t('test_track.plan_view.my_case')"
                            icon="el-icon-s-custom" @click="searchMyTestCase"/>
              <ms-tip-button v-if="showMyTestCase"
                           :tip="$t('test_track.plan_view.all_case')"
                           icon="el-icon-files" @click="searchMyTestCase"/></span>

            <span class="operate-button">
              <el-button icon="el-icon-connection" size="small" round
                         @click="$emit('openTestCaseRelevanceDialog')" >{{$t('test_track.plan_view.relevance_test_case')}}</el-button>

              <el-button icon="el-icon-edit-outline" size="small" round
                         @click="handleBatch('status')" >{{$t('test_track.plan_view.change_execution_results')}}</el-button>

              <el-button icon="el-icon-user" size="small" round
                         @click="handleBatch('executor')" >{{$t('test_track.plan_view.change_executor')}}</el-button>

              <el-input type="text" size="small"
                        class="search"
                        :placeholder="$t('load_test.search_by_name')"
                        prefix-icon="el-icon-search"
                        maxlength="60"
                        v-model="condition.name" @change="search" clearable/>
            </span>
          </el-row>

          <executor-edit
            ref="executorEdit"
            :select-ids="selectIds"
            @refresh="initTableData"/>
          <status-edit
            ref="statusEdit"
            :select-ids="selectIds"
            @refresh="initTableData"/>

        </div>
      </template>

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
        :table-data="tableData"
        @refresh="initTableData"/>

    </el-card>
  </div>
</template>

<script>
  import PlanNodeTree from './PlanNodeTree';
  import ExecutorEdit from './ExecutorEdit';
  import StatusEdit from './StatusEdit';
  import TestPlanTestCaseEdit from "../components/TestPlanTestCaseEdit";
  import MsTipButton from '../../../../components/common/components/MsTipButton';
  import MsTablePagination from '../../../../components/common/pagination/TablePagination';
  import {TokenKey} from '../../../../../common/js/constants';

  export default {
      name: "TestPlanTestCaseList",
      components: {PlanNodeTree, StatusEdit, ExecutorEdit, MsTipButton, MsTablePagination, TestPlanTestCaseEdit},
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
          currentDataIndex: 0,
          selectIds: new Set(),
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
            let param = {};
            Object.assign(param, this.condition);
            param.nodeIds = nodeIds;
            param.planId = this.planId;
            this.result = this.$post(this.buildPagePath('/test/plan/case/list'), param, response => {
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
        handleEdit(testCase, index) {
          this.currentDataIndex = index;
          this.$refs.testPlanTestCaseEdit.index = index;
          this.$refs.testPlanTestCaseEdit.getTestCase(index);
          this.$refs.testPlanTestCaseEdit.showDialog = true;
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
            this.initTableData();
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

  .operate-button {
    float: right;
  }


</style>
