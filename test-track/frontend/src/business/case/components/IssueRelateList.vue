<template>
  <ms-edit-dialog
    :append-to-body="true"
    :visible.sync="visible"
    :title="$t('test_track.case.relate_issue')"
    @confirm="save"
    ref="relevanceDialog">
    <ms-search
      :base-search-tip="$t('commons.search_by_name_or_id')"
      :condition.sync="page.condition"
      @search="getIssues"/>
    <ms-table
      v-loading="page.loading"
      :data="page.data"
      :condition="page.condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :show-select-all="false"
      @handlePageChange="getIssues"
      @refresh="getIssues"
      ref="table">

      <ms-table-column
        :label="$t('test_track.issue.id')"
        prop="id" v-if="false">
      </ms-table-column>
      <ms-table-column
        :label="$t('test_track.issue.id')"
        sortable
        prop="num">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.title')"
        prop="title"
        sortable
        min-width="200px">
      </ms-table-column>

      <issue-platform-status-column  v-if="isThirdPart" ref="issuePlatformStatus"/>

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

    </ms-table>

    <ms-table-pagination :change="getIssues" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                         :total="page.total"/>

  </ms-edit-dialog>
</template>
<style scoped>
.search-input {
  float: right;
  width: 300px;
}
</style>
<script>
import MsEditDialog from "metersphere-frontend/src/components/MsEditDialog";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {
  getPlatformOption,
  getRelateIssues,
  isThirdPartEnable,
  testCaseIssueRelate
} from "@/api/issue";
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import {ISSUE_STATUS_MAP} from "metersphere-frontend/src/utils/table-constants";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {getPageInfo} from "metersphere-frontend/src/utils/tableUtils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {TEST_CASE_RELEVANCE_ISSUE_LIST} from "@/business/utils/sdk-utils";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";
import {setIssuePlatformComponent} from "@/business/issue/issue";
import IssuePlatformStatusColumn from "@/business/issue/IssuePlatformStatusColumn.vue";
export default {
  name: "IssueRelateList",
  components: {
    IssuePlatformStatusColumn,
    MsTablePagination, IssueDescriptionTableItem, MsTableColumn, MsTable, MsEditDialog, MsSearch},
  data() {
    return {
      page: getPageInfo({
        components: TEST_CASE_RELEVANCE_ISSUE_LIST
      }),
      visible: false,
      isThirdPart: false,
    }
  },
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  props: {
    caseId: String,
    planCaseId: String,
    notInIds: Array,
  },
  created() {
    isThirdPartEnable((data) => {
      this.isThirdPart = data;
    });
  },
  methods: {
    open() {
      this.getIssues();
      this.visible = true;

      getPlatformOption()
        .then((r) => {
          setIssuePlatformComponent(r.data, this.page.condition.components);
        });

      this.$nextTick(() => {
        if (this.$refs.issuePlatformStatus) {
          this.$refs.issuePlatformStatus.getPlatformStatus();
        }
      });
    },
    getIssues() {
      this.page.condition.projectId = this.projectId;
      this.page.condition.notInIds = this.notInIds;
      this.page.loading = true;
      getRelateIssues(this.page);
    },
    getCaseResourceId() {
      return this.planCaseId ? this.planCaseId : this.caseId;
    },
    save() {
      let param = {};
      param.issueIds = Array.from(this.$refs.table.selectRows).map(i => i.id);
      param.caseResourceId = this.getCaseResourceId();
      param.isPlanEdit = !!this.planCaseId;
      param.refId = this.planCaseId ? this.caseId : null;
      testCaseIssueRelate(param)
        .then(() => {
          this.visible = false;
          this.$emit('refresh', this.$refs.table.selectRows);
        });
    }
  }
}
</script>

<style scoped>

</style>
