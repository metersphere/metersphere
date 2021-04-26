<template>
  <div>
    <el-button class="add-btn" type="primary" size="mini" @click="appIssue">添加缺陷</el-button>
    <el-tooltip class="item" effect="dark"
                :content="$t('test_track.issue.platform_tip')"
                placement="right">
      <i class="el-icon-info"/>
    </el-tooltip>

    <ms-table
      v-loading="result.loading"
      :show-select-all="false"
      :data="issues"
      :enable-selection="false"
      @refresh="getIssues">

      <ms-table-column
        :label="$t('test_track.issue.id')"
        prop="id">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.title')"
        prop="title">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.status')"
        prop="status">
        <template v-slot="scope">
          <span>{{ issueStatusMap[scope.row.status] ? issueStatusMap[scope.row.status] : scope.row.status }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.platform')"
        prop="platform">
      </ms-table-column>

      <issue-description-table-item/>

      <el-table-column :label="$t('test_track.issue.operate')">
        <template v-slot:default="scope">
          <el-tooltip :content="$t('test_track.issue.close')"
                      placement="top" :enterable="false">
            <el-button type="danger" icon="el-icon-circle-close" size="mini"
                       circle v-if="scope.row.platform === 'Local'"
                       @click="closeIssue(scope.row)"
            />
          </el-tooltip>
          <el-tooltip :content="$t('test_track.issue.delete')"
                      placement="top" :enterable="false">
            <el-button type="danger" icon="el-icon-delete" size="mini"
                       circle v-if="scope.row.platform === 'Local'"
                       @click="deleteIssue(scope.row)"
            />
          </el-tooltip>
        </template>
      </el-table-column>

    </ms-table>

    <test-plan-issue-edit :case-id="caseId" @refresh="getIssues" ref="issueEdit"/>
  </div>
</template>

<script>
import TestPlanIssueEdit from "@/business/components/track/case/components/TestPlanIssueEdit";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import {ISSUE_STATUS_MAP} from "@/common/js/table-constants";
export default {
  name: "TestCaseIssueRelate",
  components: {IssueDescriptionTableItem, MsTableColumn, MsTable, TestPlanIssueEdit},
  data() {
    return {
      issues: [],
      result: {},
    }
  },
  props: ['caseId'],
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
  },
  methods: {
    getIssues() {
      if (this.caseId) {
        this.result = this.$get("/issues/get/" + this.caseId, (response) => {
          this.issues = response.data;
        });
      }
    },
    appIssue() {
      if (!this.caseId) {
        this.$warning('请先保存用例');
        return;
      }
      this.$refs.issueEdit.open();
    },
    closeIssue(row) {
      if (row.status === 'closed') {
        this.$success(this.$t('test_track.issue.close_success'));
      } else {
        this.result = this.$get("/issues/close/" + row.id, () => {
          this.getIssues();
          this.$success(this.$t('test_track.issue.close_success'));
        });
      }
    },
    deleteIssue(row) {
      this.result = this.$post("/issues/delete", {id: row.id, caseId: this.caseId}, () => {
        this.getIssues();
        this.$success(this.$t('commons.delete_success'));
      })
    },
  }
}
</script>

<style scoped>
.add-btn {
  display: inline-block;
  margin-right: 5px;
}
</style>
