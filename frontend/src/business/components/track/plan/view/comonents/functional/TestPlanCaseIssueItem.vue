<template>
  <el-table v-loading="!data.hasLoadIssue" border class="adjust-table" :data="data.issuesContent" style="width: 100%">
    <ms-table-column prop="title" :label="$t('test_track.issue.title')" show-overflow-tooltip/>
    <ms-table-column prop="description" :label="$t('test_track.issue.description')">
      <template v-slot:default="scope">
        <el-popover
          placement="left"
          width="400"
          trigger="hover"
        >
          <ckeditor :editor="editor" disabled :config="editorConfig"
                    v-model="scope.row.description"/>
          <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
        </el-popover>
      </template>
    </ms-table-column>
    <ms-table-column prop="platform" :label="$t('test_track.issue.platform')"/>
  </el-table>
</template>

<script>

import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";

export default {
  name: "TestPlanCaseIssueItem",
  components: {MsTableColumn},
  props: {
    data: Object
  },
  data() {
    return {
      visible: true,
      editor: ClassicEditor,
      editorConfig: {
        toolbar: [],
      },
    }
  },
}
</script>

<style scoped>
</style>
