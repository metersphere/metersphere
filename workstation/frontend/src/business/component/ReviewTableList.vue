<template>
  <el-card class="table-card" v-loading="result.loading">
    <el-table
      border
      class="adjust-table"
      :data="tableData"
      @filter-change="filter"
      :height="screenHeight"
      @row-click="intoReview">
      <template v-for="(item, index) in tableLabel">
        <el-table-column
          v-if="item.id==='name'"
          prop="name"
          :label="$t('test_track.review.review_name')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id==='reviewer'"
          prop="reviewer"
          :label="$t('test_track.review.reviewer')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
        <el-table-column
          v-if="item.id==='creatorName'"
          prop="creatorName"
          :label="$t('test_track.review.review_creator')"
          show-overflow-tooltip
          :key="index"
        >
        </el-table-column>
        <el-table-column
          v-if="item.id==='status'"
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

        <el-table-column v-if="item.id === 'tags' && isShowAllColumn" prop="tags"
                         :label="$t('api_test.automation.tag')" :key="index">
          <template v-slot:default="scope">
            <ms-tag v-for="(itemName,index)  in scope.row.tags" :key="index" type="success" effect="plain"
                    :content="itemName" style="margin-left: 0; margin-right: 2px"></ms-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id==='createTime'"
          prop="createTime"
          :min-width="110"
          :label="$t('commons.create_time')"
          show-overflow-tooltip
          :key="index"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id==='endTime'"
          prop="endTime"
          :label="$t('test_track.review.end_time')"
          show-overflow-tooltip
          :key="index">
          <template v-slot:default="scope">
            <span>{{ scope.row.endTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="item.id==='projectName'"
          prop="projectName"
          :label="$t('test_track.review.review_project')"
          show-overflow-tooltip
          :key="index">
        </el-table-column>
      </template>

    </el-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

  </el-card>
</template>

<script>

import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {
  _filter,
  getLastTableSortField,
} from "metersphere-frontend/src/utils/tableUtils";

import {Test_Case_Review} from "@/model/JsonData";
import {TEST_CASE_REVIEW_LIST} from "metersphere-frontend/src/utils/constants";
import HeaderCustom from "metersphere-frontend/src/components/head/HeaderCustom";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import PlanStatusTableItem from "@/business/module/plan/PlanStatusTableItem";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {getCustomTableHeaderByXpack} from "@/business/component/js/table-head-util";
import {getTestCaseReviewer, getTestCaseReviewPages, getTestCaseReviewProject,} from "@/api/test-case";

export default {
  name: "ReviewTableList",
  components: {
    MsTag,
    HeaderLabelOperate,
    HeaderCustom,
    MsDeleteConfirm,
    MsTableOperator,
    MsTableOperatorButton,
    MsDialogFooter,
    MsTableHeader,
    MsTablePagination,
    PlanStatusTableItem
  },
  props:{
    isFocus:{
      type: Boolean,
      default: false,
    },
    isCreation:{
      type: Boolean,
      default: false,
    },
    isShowAllColumn:{
      type: Boolean,
      default: true,
    },
    isSelectAll:{
      type: Boolean,
      default: false,
    },
    screenHeight: {
      type: [Number, String],
      default() {
        return 'calc(100vh - 160px)';
      }
    },
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
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'}
      ],
    };
  },
  watch: {

  },
  created() {
    this.isTestManagerOrTestUser = true;
    this.condition.orders = getLastTableSortField(this.tableHeaderKey);

    this.initTableData();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    initTableData() {
      let lastWorkspaceId = getCurrentWorkspaceId();
      this.condition.workspaceId = lastWorkspaceId;
      if (!this.projectId) {
        return;
      }
      if(this.isFocus){
        if(this.condition.filters){
          delete this.condition.filters['user_id']
        }
        if(this.condition.reviewerId){
          delete this.condition.reviewerId
        }
        this.condition.combine= {followPeople: {operator: "current user", value: "current user",}}
      }else if(this.isCreation){
        if(this.condition.filters){
          delete this.condition.filters['user_id']
        }
        if(this.condition.reviewerId){
          delete this.condition.reviewerId
        }
        this.condition.combine= { creator: {operator: "current user", value: "current user",}}
      } else {
        if(!this.condition.filters){
          this.condition.filters={status: ["Prepare", "Underway"]}
        }
        if(!this.condition.reviewerId){
          this.condition.reviewerId = "currentUserId"
        }
      }
      if(this.isSelectAll===false){
        this.condition.projectId = this.projectId;
      }
      this.result =getTestCaseReviewPages(this.currentPage,this.pageSize,this.condition).then(response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
        for (let i = 0; i < this.tableData.length; i++) {
          getTestCaseReviewProject({id: this.tableData[i].id}).then(res => {
            let arr = res.data;
            let projectIds = arr.filter(d => d.id !== this.tableData[i].projectId).map(data => data.id);
            this.$set(this.tableData[i], "projectIds", projectIds);
          });
          getTestCaseReviewer({id: this.tableData[i].id}).then(res => {
            let arr = res.data;
            let reviewer = arr.map(data => data.name).join("、");
            let userIds = arr.map(data => data.id);
            this.$set(this.tableData[i], "reviewer", reviewer);
            this.$set(this.tableData[i], "userIds", userIds);
          });
        }
      });
      this.tableLabel = getCustomTableHeaderByXpack('TEST_CASE_REVIEW_HEAD');
    },
    intoReview(row) {
      let reviewResolve = this.$router.resolve({path:'/track/review/view/' + row.id,query:{projectId:row.projectId}});
      window.open(reviewResolve.href, '_blank');
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },

  }
};
</script>

<style scoped>

</style>
