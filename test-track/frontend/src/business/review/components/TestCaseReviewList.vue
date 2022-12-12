<template>
  <el-card class="table-card" v-loading="page.result.loading">
    <template v-slot:header>
      <ms-table-header :create-permission="['PROJECT_TRACK_REVIEW:READ+CREATE']" :condition.sync="condition"
                       @search="search" @create="testCaseReviewCreate"
                       :create-tip="$t('test_track.review.create_review')"/>
    </template>

    <ms-table
      operator-width="220px"
      row-key="id"
      :data="tableData"
      :condition="condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :operators="operators"
      :fields.sync="fields"
      :screen-height="screenHeight"
      :remember-order="true"
      :field-key="tableHeaderKey"
      :show-select-all="false"
      :enable-selection="false"
      @order="initTableData"
      @filter="search"
      @handleRowClick="intoReview">
      <span v-for="item in fields" :key="item.key">
        <ms-table-column
          prop="name"
          :field="item"
          :label="$t('test_track.review.review_name')"/>

       <ms-table-column
         prop="reviewer"
         :field="item"
         :label="$t('test_track.review.reviewer')"/>

        <ms-table-column
          prop="projectName"
          :field="item"
          :label="$t('test_track.review.review_project')"/>

        <ms-table-column
          prop="creatorName"
          :field="item"
          :filters="userFilter"
          :label="$t('test_track.review.creator')"/>

        <ms-table-column
          prop="status"
          :field="item"
          :filters="statusFilters"
          :label="$t('test_track.review.review_status')">
           <template v-slot:default="scope">
          <span class="el-dropdown-link">
            <plan-status-table-item :value="scope.row.status"/>
          </span>
          </template>
        </ms-table-column>

        <ms-tags-column :field="item"/>

        <ms-table-column
          prop="follow"
          :field="item"
          :label="$t('test_track.review.review_follow_people')"/>

        <ms-create-time-column
          :field="item"/>

        <ms-update-time-column
          :field="item"/>

         <ms-table-column
           prop="caseCount"
           :field="item"
           :fields-width="fieldsWidth"
           :label="$t('api_test.definition.api_case_number')"
           min-width="200px">
        </ms-table-column>

        <ms-table-column
          prop="endTime"
          :field="item"
          :label="$t('test_track.review.end_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.endTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

         <ms-table-column
           prop="passRate"
           :field="item"
           :fields-width="fieldsWidth"
           :label="$t('commons.pass_rate')"
           min-width="120px">
        </ms-table-column>
      </span>

      <template v-slot:opt-behind="scope">
        <ms-table-follow-operator
          :row="scope.row"
          @saveFollow="saveFollow"/>
      </template>

    </ms-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                         :total="page.total"/>
    <ms-delete-confirm :title="$t('test_track.review.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </el-card>
</template>

<script>
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {
  deepClone,
  getCustomTableHeader,
  getCustomTableWidth,
  getLastTableSortField,
  getPageInfo,
} from "metersphere-frontend/src/utils/tableUtils";
import PlanStatusTableItem from "../../common/tableItems/plan/PlanStatusTableItem";
import {Test_Case_Review} from "@/business/model/JsonData";
import {TEST_CASE_REVIEW_LIST} from "metersphere-frontend/src/utils/constants";
import HeaderCustom from "metersphere-frontend/src/components/head/HeaderCustom";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {TEST_REVIEW} from "metersphere-frontend/src/components/search/search-components";
import {getProjectMemberUserFilter} from "@/api/user";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import MsTableFollowOperator from "metersphere-frontend/src/components/table/MsTableFollowOperator";
import MsTagsColumn from "metersphere-frontend/src/components/table/MsTagsColumn";
import {
  deleteTestCaseReview,
  editTestCaseReviewFollows,
  getTestCaseReviewFollow,
  getTestCaseReviewProject,
  getTestCaseReviewReviewer,
  testReviewList
} from "@/api/test-review";

export default {
  name: "TestCaseReviewList",
  components: {
    MsTagsColumn,
    MsTableFollowOperator,
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
    MsTableColumn,
    MsTable,
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
  data() {
    return {
      page: getPageInfo(),
      type: TEST_CASE_REVIEW_LIST,
      headerItems: Test_Case_Review,
      tableLabel: [],
      tableHeaderKey: "TEST_CASE_REVIEW",
      condition: {
        components: TEST_REVIEW
      },
      tableData: [],
      isTestManagerOrTestUser: false,
      screenHeight: 'calc(100vh - 160px)',
      userFilter: [],
      fields: getCustomTableHeader('TEST_CASE_REVIEW'),
      fieldsWidth: getCustomTableWidth('TEST_CASE_REVIEW'),
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan.plan_status_running'), value: 'Underway'},
        {text: this.$t('test_track.plan.plan_status_completed'), value: 'Completed'},
        {text: this.$t('test_track.plan.plan_status_finished'), value: 'Finished'},
        {text: this.$t('test_track.plan.plan_status_archived'), value: 'Archived'}
      ],
      operators: [
        {
          tip: this.$t('commons.edit'),
          icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_REVIEW:READ+EDIT'],
        },
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_REVIEW:READ+DELETE']
        },
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
    this.condition.orders = getLastTableSortField(this.tableHeaderKey);
    getProjectMemberUserFilter((data) => {
      this.userFilter = data;
    });
    this.initTableData();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    customHeader() {
      const list = deepClone(this.tableLabel);
      this.$refs.headerCustom.open(list);
    },
    initTableData() {
      this.condition.workspaceId = getCurrentWorkspaceId();
      if (!this.projectId) {
        return;
      }
      this.condition.projectId = this.projectId;
      testReviewList(this.page.currentPage, this.page.pageSize, this.condition)
        .then((response) => {
          let data = response.data;
          this.page.total = data.itemCount;
          let tableData = data.listObject;
          tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
            item.passRate = item.passRate + '%';
            if (item.reviewers) {
              item.reviewer = item.reviewers.map(reviewer => reviewer.name).join("、");
              item.userIds = item.reviewers.map(reviewer => reviewer.id);
            }
          });
          this.tableData = tableData;
          for (let i = 0; i < this.tableData.length; i++) {
            let param = {id: this.tableData[i].id};
            getTestCaseReviewProject(param)
              .then((res) => {
                let arr = res.data;
                let projectIds = arr.filter(d => d.id !== this.tableData[i].projectId).map(data => data.id);
                this.$set(this.tableData[i], "projectIds", projectIds);
              });
            getTestCaseReviewFollow(param)
              .then((res) => {
                let arr = res.data;
                let follow = arr.map(data => data.name).join("、");
                let followIds = arr.map(data => data.id);
                let showFollow = false;
                if (arr) {
                  arr.forEach(d => {
                    if (this.currentUser().id === d.id) {
                      showFollow = true;
                    }
                  })
                }
                this.$set(this.tableData[i], "follow", follow);
                this.$set(this.tableData[i], "followIds", followIds);
                this.$set(this.tableData[i], "showFollow", showFollow);
              })
          }
        })
    },
    intoReview(row, column, event) {
      if (column.label !== this.$t('commons.operating')) {
        this.$router.push('/track/review/view/' + row.id);
      }
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
    handleDelete(caseReview) {
      this.$refs.deleteConfirm.open(caseReview);
    },
    _handleDelete(caseReview) {
      let reviewId = caseReview.id;
      deleteTestCaseReview(reviewId)
        .then(() => {
          this.initTableData();
          this.$success(this.$t('commons.delete_success'));
        })
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.initTableData();
    },
    saveFollow(row) {
      let param = {};
      param.id = row.id;
      if (row.showFollow) {
        row.showFollow = false;
        for (let i = 0; i < row.followIds.length; i++) {
          if (row.followIds[i] === this.currentUser().id) {
            row.followIds.splice(i, 1)
            break;
          }
        }
        param.followIds = row.followIds
        editTestCaseReviewFollows(param)
          .then(() => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        return
      }
      if (!row.showFollow) {
        row.showFollow = true;
        if (!row.followIds) {
          row.followIds = [];
        }
        row.followIds.push(this.currentUser().id);
        param.followIds = row.followIds
        editTestCaseReviewFollows(param)
          .then(() => {
            this.$success(this.$t('commons.follow_success'));
          });
      }
    }
  }
};
</script>

<style scoped>

</style>
