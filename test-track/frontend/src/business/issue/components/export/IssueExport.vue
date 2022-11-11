<template>
  <el-dialog class="issue-export"
             v-loading="loading"
             :title="$t('test_track.issue.export')"
             :visible.sync="dialogVisible"
             @close="close">
    <issue-export-field-select ref="issueExportFieldSelect"/>

    <span slot="footer" class="dialog-footer">
    <el-button size="mini" @click="dialogVisible = false">{{ $t('commons.cancel') }}</el-button>
    <el-button type="primary" size="mini" @click="exportIssue">{{ $t('commons.export') }}</el-button>
  </span>
  </el-dialog>
</template>

<script>
import ElUploadList from "element-ui/packages/upload/src/upload-list";
import MsTableButton from 'metersphere-frontend/src/components/MsTableButton';
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token"
import IssueExportFieldSelect from "@/business/issue/components/export/IssueExportFieldSelect";

export default {
  name: "IssueExport",
  components: {IssueExportFieldSelect, ElUploadList, MsTableButton},
  data() {
    return {
      dialogVisible: false,
      projectId: "",
      loading: false
    }
  },
  activated() {
  },
  methods: {
    open() {
      listenGoBack(this.close);
      this.projectId = getCurrentProjectID();
      this.dialogVisible = true;
      this.loading = false;
    },
    close() {
      removeGoBackListener(this.close);
      this.dialogVisible = false;
      this.loading = false;
    },
    exportIssue() {
      let param = null;
      if (this.$refs.issueExportFieldSelect) {
        param = this.$refs.issueExportFieldSelect.getExportParam();
      }
      this.close();
      this.$emit('export', param);
    }
  }
}
</script>

<style>
</style>

<style scoped>
.issue-export :deep(.el-dialog) {
  width: 600px;
}

.issue-export :deep(.el-dialog .el-dialog__title) {
  font-weight: bold;
}

.issue-export :deep(.el-dialog .el-dialog__body) {
  padding: 20px;
}
</style>
