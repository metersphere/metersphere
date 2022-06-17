<template>
  <el-table v-loading="!data.hasLoadIssue" border class="adjust-table" :data="data.issuesContent" style="width: 100%">
    <ms-table-column prop="title" :label="$t('test_track.issue.title')" show-overflow-tooltip/>
    <ms-table-column prop="description" :label="$t('test_track.issue.description')">
      <template v-slot:default="scope">
        <el-popover
          placement="right"
          width="500"
          trigger="hover"
          popper-class="issues-popover">
          <ms-mark-down-text
            prop="description"
            :data="scope.row"
            :disabled="true"/>
          <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
        </el-popover>
      </template>
    </ms-table-column>
    <ms-table-column prop="platform" :label="$t('test_track.issue.platform')"/>
  </el-table>
</template>

<script>

import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsMarkDownText from "@/business/components/track/case/components/MsMarkDownText";

export default {
  name: "TestPlanCaseIssueItem",
  components: {MsMarkDownText, MsTableColumn},
  props: {
    data: Object
  }
}
</script>

<style scoped>
</style>
