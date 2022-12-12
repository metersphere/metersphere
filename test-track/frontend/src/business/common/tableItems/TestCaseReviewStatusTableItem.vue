<template>
  <ms-table-column
    :prop="prop"
    :field="field"
    :filters="reviewStatusFilters"
    :min-width="minWidth"
    :fields-width="fieldsWidth"
    :label="$t('test_track.case.status')">
    <template v-slot:default="scope">
            <span class="el-dropdown-link">
              <review-status :value="scope.row[prop]"/>
            </span>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import ReviewStatus from "@/business/case/components/ReviewStatus";
export default {
  name: "TestCaseReviewStatusTableItem",
  components: {ReviewStatus, MsTableColumn},
  props: {
    minWidth: {
      type: [String, Number],
      default: '140px'
    },
    fieldsWidth: Object,
    prop: {
      type: String,
      default: 'reviewStatus'
    },
    field: {
      type: Object,
      default: null
    },
  },
  data() {
    return {
      reviewStatusFilters: [
        {text: this.$t('test_track.review.prepare'), value: 'Prepare'},
        {text: this.$t('test_track.review.again'), value: 'Again'},
        {text: this.$t('test_track.review.pass'), value: 'Pass'},
        {text: this.$t('test_track.review.un_pass'), value: 'UnPass'},
        {text: this.$t('test_track.review.underway'), value: 'Underway'},
      ]
    }
  }
}
</script>

<style scoped>

</style>
