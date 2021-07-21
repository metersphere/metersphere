<template>
  <div>
    <el-button class="add-btn" v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']" :disabled="readOnly" type="primary" size="mini" @click="appIssue">{{ $t('test_track.issue.add_issue') }}</el-button>
    <el-button class="add-btn" v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"  :disabled="readOnly" type="primary" size="mini" @click="relateIssue">{{ $t('test_track.case.relate_issue') }}</el-button>
    <el-tooltip class="item" v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"  effect="dark"
                :content="$t('test_track.issue.platform_tip')"
                placement="right">
      <i class="el-icon-info"/>
    </el-tooltip>

    <ms-table
      v-loading="page.result.loading"
      :show-select-all="false"
      :data="page.data"
      :enable-selection="false"
      @refresh="getIssues">

      <ms-table-column
        :label="$t('test_track.issue.id')"
        prop="id" v-if="false">
      </ms-table-column>
      <ms-table-column
        :label="$t('test_track.issue.id')"
        prop="num">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.title')"
        prop="title">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.platform_status')"
        v-if="isThirdPart"
        prop="platformStatus">
        <template v-slot="scope">
          {{ scope.row.platformStatus ? scope.row.platformStatus : '--'}}
        </template>
      </ms-table-column>

      <ms-table-column
        v-else
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
                       circle :disabled="scope.row.platform !== 'Local'"
                       @click="closeIssue(scope.row)"
            />
          </el-tooltip>
          <el-tooltip :content="$t('test_track.case.unlink')"
                      placement="top" :enterable="false">
            <el-button type="danger" icon="el-icon-unlock" size="mini"
                       circle :disabled="scope.row.platform !== 'Local'"
                       @click="deleteIssue(scope.row)"
            />
          </el-tooltip>
        </template>
      </el-table-column>

    </ms-table>

    <test-plan-issue-edit :plan-id="planId" :case-id="caseId" @refresh="getIssues" ref="issueEdit"/>
    <IssueRelateList :is-third-part="isThirdPart" :case-id="caseId"  @refresh="getIssues" ref="issueRelate"/>
  </div>
</template>

<script>
import TestPlanIssueEdit from "@/business/components/track/case/components/TestPlanIssueEdit";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import {ISSUE_STATUS_MAP} from "@/common/js/table-constants";
import IssueRelateList from "@/business/components/track/case/components/IssueRelateList";
import {getIssuesByCaseId} from "@/network/Issue";
import {getIssueTemplate} from "@/network/custom-field-template";
export default {
  name: "TestCaseIssueRelate",
  components: {IssueRelateList, IssueDescriptionTableItem, MsTableColumn, MsTable, TestPlanIssueEdit},
  data() {
    return {
      page: {
        data: [],
        result: {},
      },
      isThirdPart: false
    }
  },
  props: ['caseId', 'readOnly','planId'],
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
  },
  created() {
    getIssueTemplate()
      .then((template) => {
        if (template.platform === 'metersphere') {
          this.isThirdPart = false;
        } else {
          this.isThirdPart = true;
        }
      });
  },
  methods: {
    getIssues() {
      let result = getIssuesByCaseId(this.caseId, this.page);
      if (result) {
        this.page.result = result;
      }
    },
    appIssue() {
      if (!this.caseId) {
        this.$warning(this.$t('api_test.automation.save_case_info'));
        return;
      }
      this.$refs.issueEdit.open();
    },
    relateIssue() {
      if (!this.caseId) {
        this.$warning(this.$t('api_test.automation.save_case_info'));
        return;
      }
      this.$refs.issueRelate.open();
    },
    closeIssue(row) {
      if (row.status === 'closed') {
        this.$success(this.$t('test_track.issue.close_success'));
      } else {
        this.page.result = this.$get("/issues/close/" + row.id, () => {
          this.getIssues();
          this.$success(this.$t('test_track.issue.close_success'));
        });
      }
    },
    deleteIssue(row) {
      this.page.result = this.$post("/issues/delete/relate", {id: row.id, caseId: this.caseId}, () => {
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
