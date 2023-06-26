<template>
  <ms-table-column
    :label="$t('test_track.issue.platform_status')"
    :filters="filters"
    :filter-method="filterMethod"
    prop="platformStatus">
    <template v-slot="scope">
       <span v-if="scope.row.platform === 'Tapd'">
        {{ scope.row.platformStatus ? tapdIssueStatusMap[scope.row.platformStatus] : '--' }}
      </span>
      <span v-else-if="scope.row.platform ==='Local'">
            {{ scope.row.platformStatus ? issueStatusMap[scope.row.platformStatus] : '--' }}
          </span>
      <span v-else-if="platformStatusMap && platformStatusMap.get(scope.row.platformStatus)">
            {{ platformStatusMap.get(scope.row.platformStatus) }}
          </span>
      <span v-else>
            {{ scope.row.platformStatus ? scope.row.platformStatus : '--' }}
          </span>
    </template>
  </ms-table-column>
</template>
<script>

import {ISSUE_STATUS_MAP, TAPD_ISSUE_STATUS_MAP} from "metersphere-frontend/src/utils/table-constants";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {getPlatformStatus} from "@/api/issue";

export default {
  name: "IssuePlatformStatusColumn",
  components: {MsTableColumn},
  data() {
    return {
      isThirdPart: false,
      platformStatus: [],
      platformStatusMap: new Map(),
    }
  },
  props: {
    filters: Array,
    filterMethod: Function
  },
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    },
    tapdIssueStatusMap() {
      return TAPD_ISSUE_STATUS_MAP;
    },
  },
  methods: {
    getPlatformStatus() {
      getPlatformStatus({
        projectId: getCurrentProjectID(),
        workspaceId: getCurrentWorkspaceId()
      }).then((r) => {
        this.platformStatus = r.data;
        this.platformStatusMap = new Map();
        if (this.platformStatus) {
          this.platformStatus.forEach(item => {
            this.platformStatusMap.set(item.value, item.label);
          });
        }
      });

    }
  }
}
</script>
<style scoped>

</style>
