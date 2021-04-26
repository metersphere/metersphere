<template>
  <ms-table-column
    :label="$t('test_track.issue.description')"
    prop="description">
    <template v-slot:default="scope">
      <el-popover
        placement="right"
        width="500"
        trigger="hover"
        popper-class="issues-popover"
      >
        <ckeditor :editor="editor" disabled :config="readConfig"
                  v-model="scope.row.description"/>
        <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
      </el-popover>
    </template>
  </ms-table-column>
</template>

<script>
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
export default {
  name: "IssueDescriptionTableItem",
  components: {MsTableColumn},
  data() {
    return {
      editor: ClassicEditor,
      readConfig: {toolbar: []},
    }
  }
}
</script>

<style scoped>

</style>
