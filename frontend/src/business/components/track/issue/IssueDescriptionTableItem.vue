<template>
  <ms-table-column
    :label="$t('test_track.issue.description')"
    prop="description"
    :field="field"
    min-width="120"
    :fields-width="fieldsWidth">
    <template v-slot:default="scope">
      <el-popover
        placement="right"
        width="500"
        trigger="hover"
        popper-class="issues-popover">
        <ms-mark-down-text prop="description" :data="scope.row" :disabled="true"/>
        <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
      </el-popover>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";
export default {
  name: "IssueDescriptionTableItem",
  components: {MsMarkDownText, MsTableColumn},
  props: {
    field: Object,
    fieldsWidth: Object
  }
}
</script>

<style scoped>

</style>
