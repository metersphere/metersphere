<template>
  <ms-table-column :field="field"
                   :fields-width="fieldsWidth"
                   prop="createTime"
                   min-width="140px"
                   sortable="custom"
                   :label="$t('commons.create_time')">
    <template v-slot:default="scope">
      <span>{{ scope.row.createTime | timestampFormatDate }}</span>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
export default {
  name: "MsCreateTimeColumn",
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
