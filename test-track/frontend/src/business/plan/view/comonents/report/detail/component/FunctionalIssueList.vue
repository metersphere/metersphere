<template>
  <div class="container">
    <ms-table
      v-loading="loading"
      :show-select-all="false"
      :data="data"
      :screen-height="null"
      :enable-selection="false"
      @refresh="getIssues">

      <ms-table-column
        :label="$t('test_track.issue.id')"
        :sortable="true"
        prop="num">
      </ms-table-column>

      <ms-table-column
        :label="$t('test_track.issue.title')"
        :sortable="true"
        prop="title">
      </ms-table-column>

      <issue-platform-status-column
        v-if="isThirdPart"
        :filters="statusFilters"
        :filter-method="filterStatus"
        ref="issuePlatformStatus"/>


      <ms-table-column
        v-else
        :filters="statusFilters"
        :label="$t('test_track.issue.status')"
        :filter-method="filterStatus"
        prop="status">
        <template v-slot="scope">
          <span>{{ issueStatusMap[scope.row.status] ? issueStatusMap[scope.row.status] : scope.row.status }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :filters="platformFilters"
        :filter-method="filterPlatform"
        :label="$t('test_track.issue.platform')"
        prop="platform">
      </ms-table-column>

      <issue-description-table-item/>

      <ms-create-time-column/>
    </ms-table>

  </div>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import {ISSUE_PLATFORM_OPTION, ISSUE_STATUS_MAP} from "metersphere-frontend/src/utils/table-constants";
import {getIssuesByPlanId, getShareIssuesByPlanId} from "@/api/issue";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import IssuePlatformStatusColumn from "@/business/issue/IssuePlatformStatusColumn.vue";

export default {
  name: "FunctionalIssueList",
  components: {IssuePlatformStatusColumn, MsCreateTimeColumn, IssueDescriptionTableItem, MsTableColumn, MsTable},
  data() {
    return {
      data: [],
      loading: false,
      isThirdPart: false
    }
  },
  props: ['planId', 'isTemplate', 'report', 'isShare', 'shareId', 'isDb'],
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    platformFilters() {
      let platforms = this.data.map(i => i.platform);
      let option = [...ISSUE_PLATFORM_OPTION];
      option.push({value: 'Local', text: 'Local'});
      return option.filter(i => platforms.indexOf(i.value) > -1);
    },
    statusFilters() {
      let statusFilter = [];
      this.data.forEach(item => {
        let status = item.platform === 'Local' ? item.status : item.platformStatus;
        if (status) {
          let values = statusFilter.map(i => i.value);
          if (values.indexOf(status) == -1) {
            statusFilter.push({
              value: status,
              text: ISSUE_STATUS_MAP[status] && item.platform === 'Local' ? ISSUE_STATUS_MAP[status] : status
            });
          }
        }
      });
      return statusFilter;
    },
  },
  mounted() {
    this.isThirdPart = this.report.isThirdPartIssue;
    this.getIssues();
  },
  watch: {
    data() {
      if (this.data) {
        this.$emit('setSize', this.data.length);
      }
    }
  },
  methods: {
    filterStatus(value, row) {
      let status = this.isThirdPart ? row.platformStatus : row.status;
      return status === value;
    },
    filterPlatform(value, row) {
      return row.platform === value;
    },
    getIssues() {
      if (this.isTemplate || this.isDb) {
        this.data = this.report.issueList ? this.report.issueList : [];
      } else if (this.isShare) {
        this.loading = true;
        getShareIssuesByPlanId(this.shareId, this.planId)
          .then((r) => {
            this.loading = false;
            this.data = r.data;
          })
      } else {
        this.loading = true;
        getIssuesByPlanId(this.planId)
          .then((r) => {
            this.loading = false;
            this.data = r.data;
          })
      }
    },
  }
}
</script>

<style scoped>
</style>
