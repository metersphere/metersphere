<template>
  <ms-table-column :field="field"
                   :fields-width="fieldsWidth"
                   prop="updateTime"
                   min-width="140px"
                   sortable
                   :label="$t('api_test.mock.table.update_time')">
    <template v-slot:default="scope">
      <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
export default {
  name: "MsUpdateTimeColumn",
  components: {MsTableColumn},
  props: {
    field: Object,
    fieldsWidth: Object,
    sortable: {
      type: Boolean,
      default() {
        return true;
      }
    }
  }
}
</script>

<style scoped>

</style>
