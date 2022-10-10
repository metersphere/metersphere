<template>
  <ms-table-column
    :prop="prop"
    :filters="statusFilters"
    :field="field"
    :min-width="minWidth"
    :fields-width="fieldsWidth"
    :label="$t('test_track.plan_view.execute_result')">
    <template v-slot:default="scope">
            <span class="el-dropdown-link">
              <status-table-item :value="scope.row[prop] ? scope.row[prop] : 'Prepare'"/>
            </span>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
export default {
  name: "TestPlanCaseStatusTableItem",
  components: {StatusTableItem, MsTableColumn},
  props: {
    minWidth: {
      type: [String, Number],
      default: '120px'
    },
    fieldsWidth: Object,
    prop: {
      type: String,
      default: 'status'
    },
    field: {
      type: Object,
      default: null
    },
  },
  data() {
    return {
      statusFilters: [
        {text: this.$t('test_track.plan.plan_status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.plan_view.pass'), value: 'Pass'},
        {text: this.$t('test_track.plan_view.failure'), value: 'Failure'},
        {text: this.$t('test_track.plan_view.blocking'), value: 'Blocking'},
        {text: this.$t('test_track.plan_view.skip'), value: 'Skip'}
      ],
    }
  }
}
</script>

<style scoped>

</style>
