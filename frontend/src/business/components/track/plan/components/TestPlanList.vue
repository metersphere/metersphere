<template>
  <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition"
                           @search="initTableData" @create="testPlanCreate"
                           :create-tip="$t('test_track.plan.create_plan')"
                           :title="$t('test_track.plan.test_plan')"/>
        </template>

        <el-table
          :data="tableData"
          @row-click="intoPlan">
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="principal"
            :label="$t('test_track.plan.plan_principal')"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="status"
            :label="$t('test_track.plan.plan_status')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <plan-status-table-item :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column
            prop="stage"
            :label="$t('test_track.plan.plan_stage')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <plan-stage-table-item :stage="scope.row.stage"/>
            </template>
          </el-table-column>
          <el-table-column
            prop="projectName"
            :label="$t('test_track.plan.plan_project')"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            :label="$t('commons.create_time')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('commons.update_time')"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operator :is-tester-permission="true" @editClick="handleEdit(scope.row)" @deleteClick="handleDelete(scope.row)"/>
            </template>
          </el-table-column>
        </el-table>

        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
</template>

<script>
  import MsCreateBox from '../../../settings/CreateBox';
  import MsTablePagination from '../../../../components/common/pagination/TablePagination';
  import MsTableHeader from "../../../common/components/MsTableHeader";
  import MsDialogFooter from "../../../common/components/MsDialogFooter";
  import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
  import MsTableOperator from "../../../common/components/MsTableOperator";
  import PlanStatusTableItem from "../../common/tableItems/plan/PlanStatusTableItem";
  import PlanStageTableItem from "../../common/tableItems/plan/PlanStageTableItem";

  export default {
      name: "TestPlanList",
      components: {
        PlanStageTableItem,
        PlanStatusTableItem,
        MsTableOperator, MsTableOperatorButton, MsDialogFooter, MsTableHeader, MsCreateBox, MsTablePagination},
      data() {
        return {
          result: {},
          queryPath: "/test/plan/list",
          deletePath: "/test/plan/delete",
          condition: {},
          currentPage: 1,
          pageSize: 5,
          total: 0,
          tableData: [],
        }
      },
      watch: {
        '$route'(to, from) {
          if (to.path.indexOf("/track/plan/all") >= 0){
            this.initTableData();
          }
        }
      },
      created() {
        this.projectId = this.$route.params.projectId;
        this.initTableData();
      },
      methods: {
        initTableData() {
          this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
            let data = response.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
          });
        },
        buildPagePath(path) {
          return path + "/" + this.currentPage + "/" + this.pageSize;
        },
        testPlanCreate() {
          this.$emit('openTestPlanEditDialog');
        },
        handleEdit(testPlan) {
          this.$emit('testPlanEdit', testPlan);
        },
        handleDelete(testPlan) {
          this.$alert(this.$t('test_track.plan.plan_delete_confirm') + testPlan.name + "？", '', {
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
            this.$success(this.$t('commons.delete_success'));
          });
        },
        intoPlan(row, event, column) {
          this.$router.push('/track/plan/view/' + row.id);
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

  .el-table {
    cursor:pointer;
  }
</style>
