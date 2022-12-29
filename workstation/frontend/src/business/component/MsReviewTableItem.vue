<template>
  <el-popover
    placement="right"
    width="500"
    trigger="hover"
    popper-class="issues-popover">
    <ms-mark-down-text :prop="prop" :data="data" :disabled="true"/>
    <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
  </el-popover>
</template>

<script>
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";

export default {
  name: "MsReviewTableItem",
  components: {MsMarkDownText, MsTableColumn},
  props: {
    data: Object,
    prop: String,
  }
}
</script>

<style scoped>

</style>
