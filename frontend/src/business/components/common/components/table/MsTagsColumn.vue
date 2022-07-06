<template>
  <ms-table-column
    prop="tags"
    :field="field"
    :fields-width="fieldsWidth"
    sortable
    :label="$t('api_test.automation.tag')"
    min-width="200px">
    <template v-slot:default="scope">
      <ms-tag v-for="(name, index)  in scope.row.tags" :key="index" type="success" effect="plain"
              :content="name" style="margin-left: 0px; margin-right: 2px"/>
      <span/>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTag from "@/business/components/common/components/MsTag";
export default {
  name: "MsTagsColumn",
  components: {MsTag, MsTableColumn},
  props: {
    field: Object,
    fieldsWidth: Object,
  }
}
</script>

<style scoped>

</style>
