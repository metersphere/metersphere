<template>
  <el-dialog class="testcase-import"
             v-loading="loading"
             :title="$t('test_track.case.import.case_export')"
             :visible.sync="dialogVisible"
             @close="close">

    <span class="format-title">
      {{ $t('test_track.case.import.import_format') }}
    </span>
    <el-row class="import-row">
      <el-col :span="12">
        <el-radio v-model="exportType" label="excel">{{ $t('commons.excelFile') }}</el-radio>
      </el-col>
      <el-col :span="12">
        <el-radio v-model="exportType" label="xmind">{{ $t('commons.xmindFile') }}</el-radio>
      </el-col>
    </el-row>

    <test-case-export-field-select
      v-if="exportType === 'excel'"
      ref="testCaseExportFieldSelect"/>


    <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">{{ $t('commons.cancel') }}</el-button>
    <el-button type="primary" @click="exportTestCase">{{ $t('commons.export') }}</el-button>
  </span>
  </el-dialog>
</template>

<script>
import ElUploadList from "element-ui/packages/upload/src/upload-list";
import MsTableButton from '../../../../common/components/MsTableButton';
import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "../../../../../../common/js/utils";
import TestCaseExportFieldSelect from "@/business/components/track/case/components/export/TestCaseExportFieldSelect";

export default {
  name: "TestCaseImport",
  components: {TestCaseExportFieldSelect, ElUploadList, MsTableButton},
  data() {
    return {
      exportType: "excel",
      dialogVisible: false,
      projectId: "",
      loading: false
    }
  },
  activated() {
  },
  methods: {
    handleError(err, file, fileList) {
      this.loading = false;
      this.$error(err.message);
    },
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
    exportTestCase() {
      let param = null;
      if (this.$refs.testCaseExportFieldSelect) {
        param = this.$refs.testCaseExportFieldSelect.getExportParam();
      }
      this.$emit('exportTestCase', this.exportType, param);
      this.loading = true;
    }
  }
}
</script>

<style>
</style>

<style scoped>

.import-row {
  padding-top: 20px;
}

.testcase-import >>> .el-dialog {
  width: 600px;
}

.testcase-import >>> .el-dialog .el-dialog__title {
  font-weight: bold;
}

.testcase-import >>> .el-dialog .el-dialog__body {
  padding: 20px;
}

.format-title {
  font-size: 16px;
  font-weight: bold;
}
</style>
