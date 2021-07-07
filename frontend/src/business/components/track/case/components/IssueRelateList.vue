<template>
  <ms-edit-dialog
    :append-to-body="true"
    :visible.sync="visible"
    :title="$t('test_track.case.relate_issue')"
    @confirm="save"
    ref="relevanceDialog">
    <ms-table
      v-loading="page.result.loading"
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

    </ms-table>

    <ms-table-pagination :change="getIssues" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize" :total="page.total"/>

  </ms-edit-dialog>
</template>

<script>
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {getRelateIssues, testCaseIssueRelate} from "@/network/Issue";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import {ISSUE_STATUS_MAP} from "@/common/js/table-constants";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {getPageInfo} from "@/common/js/tableUtils";
import {getCurrentProjectID} from "@/common/js/utils";
export default {
  name: "IssueRelateList",
  components: {MsTablePagination, IssueDescriptionTableItem, MsTableColumn, MsTable, MsEditDialog},
  data() {
    return {
      page: getPageInfo(),
      visible: false
    }
  },
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    }
  },
  props: ['caseId', 'isThirdPart'],
  methods: {
    open() {
      this.getIssues();
      this.visible = true;
    },
    getIssues() {
      this.page.condition.projectId = this.projectId;
      this.page.condition.caseId = this.caseId;
      this.page.result = getRelateIssues(this.page);
    },
    save() {
      let param = {};
      param.caseId = this.caseId;
      param.issueIds = Array.from(this.$refs.table.selectRows).map(i => i.id);
      param.caseId = this.caseId;
      testCaseIssueRelate(param, () => {
        this.visible = false;
        this.$emit('refresh');
      });
    }
  }
}
</script>

<style scoped>

</style>
