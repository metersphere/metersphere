<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <ms-table-header :create-permission="['PROJECT_TRACK_REVIEW:READ+CREATE']" :condition.sync="condition"
                       @search="initTableData" @create="testCaseReviewCreate"
                       :create-tip="$t('test_track.review.create_review')"/>
    </template>

    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      :height="screenHeight"
      @sort-change="sort"
      @row-click="intoReview">
      <template v-for="(item, index) in tableLabel">
        <el-table-column
          v-if="item.id=='name'"
          prop="name"
          :label="$t('test_track.review.review_name')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id=='reviewer'"
          prop="reviewer"
          :label="$t('test_track.review.reviewer')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id=='projectName'"
          prop="projectName"
          :label="$t('test_track.review.review_project')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id=='creatorName'"
          prop="creatorName"
          :label="$t('test_track.review.review_creator')"
          show-overflow-tooltip
          :key="index"
        >
        </el-table-column>
        <el-table-column
          v-if="item.id=='status'"
          prop="status"
          column-key="status"
          :label="$t('test_track.review.review_status')"
          show-overflow-tooltip
          :key="index"
        >
          <template v-slot:default="scope">
          <span class="el-dropdown-link">
            <plan-status-table-item :value="scope.row.status"/>
          </span>
          </template>
        </el-table-column>
        <el-table-column v-if="item.id == 'tags'" prop="tags"
                         :label="$t('api_test.automation.tag')" :key="index">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0px; margin-right: 2px"></ms-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id=='createTime'"
          prop="createTime"
          :label="$t('commons.create_time')"
          show-overflow-tooltip
          :key="index"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id=='endTime'"
          prop="endTime"
          :label="$t('test_track.review.end_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.endTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </template>
      <el-table-column
        min-width="100"
        :label="$t('commons.operating')">
        <template slot="header">
          <header-label-operate @exec="customHeader"/>
        </template>
        <template v-slot:default="scope">
          <div>

            <ms-table-operator :edit-permission="['PROJECT_TRACK_REVIEW:READ+EDIT']"
                               :delete-permission="['PROJECT_TRACK_REVIEW:READ+DELETE']"
                               @editClick="handleEdit(scope.row)"
                               @deleteClick="handleDelete(scope.row)">
            </ms-table-operator>
          </div>

        </template>
      </el-table-column>
      <header-custom ref="headerCustom" :initTableData="initTableData" :optionalFields=headerItems
                     :type=type></header-custom>
    </el-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <ms-delete-confirm :title="$t('test_track.review.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </el-card>
</template>

<script>
import MsDeleteConfirm from "../../../common/components/MsDeleteConfirm";
import MsTableOperator from "../../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
import MsDialogFooter from "../../../common/components/MsDialogFooter";
import MsTableHeader from "../../../common/components/MsTableHeader";
import MsCreateBox from "../../../settings/CreateBox";
import MsTablePagination from "../../../common/pagination/TablePagination";
import {getCurrentProjectID, getCurrentWorkspaceId} from "@/common/js/utils";
import {_filter, _sort, deepClone, getLabel, getLastTableSortField,saveLastTableSortField} from "@/common/js/tableUtils";
import PlanStatusTableItem from "../../common/tableItems/plan/PlanStatusTableItem";
import {Test_Case_Review} from "@/business/components/common/model/JsonData";
import {TEST_CASE_REVIEW_LIST} from "@/common/js/constants";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "TestCaseReviewList",
  components: {
    MsTag,
    HeaderLabelOperate,
    HeaderCustom,
    MsDeleteConfirm,
    MsTableOperator,
    MsTableOperatorButton,
    MsDialogFooter,
    MsTableHeader,
    MsCreateBox,
    MsTablePagination,
    PlanStatusTableItem
  },
  data() {
    return {
      type: TEST_CASE_REVIEW_LIST,
      headerItems: Test_Case_Review,
      tableLabel: [],
      tableHeaderKey:"TEST_CASE_REVIEW",
      result: {},
      condition: {},
      tableData: [],
      isTestManagerOrTestUser: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 200px)',
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'}
      ],
    };
  },
  watch: {
    '$route'(to) {
      if (to.path.indexOf("/track/review/all") >= 0) {
        this.initTableData();
      }
    }
  },
  created() {
    this.isTestManagerOrTestUser = true;
    let orderArr = this.getSortField();
    if(orderArr){
      this.condition.orders = orderArr;
    }
    this.initTableData();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },

    initTableData() {
      let lastWorkspaceId = getCurrentWorkspaceId();
      this.condition.workspaceId = lastWorkspaceId;
      if (!this.projectId) {
        return;
      }
      this.condition.projectId = this.projectId;
      this.result = this.$post("/test/case/review/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
        for (let i = 0; i < this.tableData.length; i++) {
          let path = "/test/case/review/project";
          this.$post(path, {id: this.tableData[i].id}, res => {
            let arr = res.data;
            let projectIds = arr.filter(d => d.id !== this.tableData[i].projectId).map(data => data.id);
            this.$set(this.tableData[i], "projectIds", projectIds);
          });
          this.$post('/test/case/review/reviewer', {id: this.tableData[i].id}, res => {
            let arr = res.data;
            let reviewer = arr.map(data => data.name).join("、");
            let userIds = arr.map(data => data.id);
            this.$set(this.tableData[i], "reviewer", reviewer);
            this.$set(this.tableData[i], "userIds", userIds);
          });
        }
      });
      getLabel(this, TEST_CASE_REVIEW_LIST);
    },
    intoReview(row) {
      this.$router.push('/track/review/view/' + row.id);
    },
    testCaseReviewCreate() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$emit('openCaseReviewEditDialog');
    },
    handleEdit(caseReview) {
      this.$emit('caseReviewEdit', caseReview);
    },
    statusChange() {

    },
    handleDelete(caseReview) {
      this.$refs.deleteConfirm.open(caseReview);
    },
    _handleDelete(caseReview) {
      let reviewId = caseReview.id;
      this.$get('/test/case/review/delete/' + reviewId, () => {
        this.initTableData();
        this.$success(this.$t('commons.delete_success'));
      });
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.saveSortField(this.tableHeaderKey,this.condition.orders);
      this.initTableData();
    },
    saveSortField(key,orders){
      saveLastTableSortField(key,JSON.stringify(orders));
    },
    getSortField(){
      let orderJsonStr = getLastTableSortField(this.tableHeaderKey);
      let returnObj = null;
      if(orderJsonStr){
        try {
          returnObj = JSON.parse(orderJsonStr);
        }catch (e){
          return null;
        }
      }
      return returnObj;
    }
  }
};
</script>

<style scoped>

</style>
