<template>
  <div class="diff-box">
    <ms-table
      :show-select-all="false"
      :data="tableData"
      :enable-selection="false"
    >
      <ms-table-column
        prop="num"
        label="ID"
        sortable
        min-width="100px"
        width="100px"
      >
        <template v-slot:default="{ row }">
          <div :class="row.diffStatus == 2 ? 'line-through' : ''">
            {{ row.num }}
          </div>
          <div style="width: 32px" v-if="row.diffStatus > 0">
            <case-diff-status :diffStatus="row.diffStatus"></case-diff-status>
          </div>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="name"
        :label="$t('case.case_name')"
        min-width="316px"
        width="316px"
        sortable
      >
        <template v-slot:default="{ row }">
          <div :class="row.diffStatus == 2 ? 'line-through' : ''">
            {{ row.name }}
          </div>
        </template>
      </ms-table-column>

      <ms-table-column
        prop="projectName"
        :label="$t('commons.project')"
        sortable
        min-width="180px"
        width="180px"
      />

      <ms-table-column
        v-xpack
        sortable
        prop="versionName"
        :label="$t('commons.version')"
        min-width="100px"
        width="100px"
      />

      <ms-table-column :label="$t('test_resource_pool.type')" prop="type">
        <template v-slot:default="{ row }">
          {{ typeMap[row.testType] }}
        </template>
      </ms-table-column>
    </ms-table>
  </div>
</template>
<script>
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import CaseDiffStatus from "./CaseDiffStatus";
export default {
  name: "CaseDiffTestRelate",
  components: {
    MsTableColumn,
    MsTable,
    CaseDiffStatus,
  },
  props: {
    tableData: Array,
  },
  data() {
    return {
      typeMap: {
        testcase: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.api"
        ),
        automation: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.scene"
        ),
        performance: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.load"
        ),
        uiAutomation: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.ui"
        ),
      },
    };
  },
};
</script>
<style lang="scss" scoped>
.diff-box {
  margin-top: 22px;
}
.line-through {
  text-decoration-line: line-through;
  color: #8f959e !important;
  font-size: 14px;
  font-weight: 400;
  font-size: 14px;
}
</style>
