<template>
  <el-dialog class="testcase-import"
             v-loading="loading"
             :title="$t('test_track.case.export.export_to_excel')"
             :visible.sync="dialogVisible"
             width="40%"
             @close="close">
    <span class="select-row">{{!exportAll ? $t('test_track.batch_operate_select_row_count', [size]) : $t('test_track.select_all_row')}}</span>
    <test-case-export-field-select-table
      v-if="exportType === 'excel'"
      ref="testCaseExportFieldSelectTable"/>
    <div class="other-field-tip">
      {{ $t('test_track.case.import.other_field_tip') }}
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false" size="small">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="exportTestCase" size="small">{{ $t('commons.export') }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import ElUploadList from "element-ui/packages/upload/src/upload-list";
import MsTableButton from 'metersphere-frontend/src/components/MsTableButton';
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token"
import TestCaseExportFieldSelectTable from "@/business/case/components/export/TestCaseExportFieldSelectTable";

export default {
  name: "TestCaseImport",
  components: {TestCaseExportFieldSelectTable, ElUploadList, MsTableButton},
  data() {
    return {
      exportType: "excel",
      exportAll: false,
      dialogVisible: false,
      projectId: "",
      loading: false,
      size: 0
    }
  },
  activated() {
  },
  methods: {
    handleError(err, file, fileList) {
      this.loading = false;
      this.$error(err.message);
    },
    open(size, exportAll) {
      this.size = size;
      this.exportAll = exportAll;
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
      if (this.$refs.testCaseExportFieldSelectTable) {
        param = this.$refs.testCaseExportFieldSelectTable.getExportParam();
      }
      param.exportAll = this.exportAll
      this.$emit('exportTestCase', this.exportType, param);
    }
  }
}
</script>

<style>
</style>

<style scoped>
.select-row {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #646A73;
  flex: none;
  order: 1;
  align-self: center;
  flex-grow: 0;
}

.other-field-tip {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #646A73;
  flex: none;
  order: 0;
  flex-grow: 0;
  margin-top: 10px;
  margin-bottom: 24px;
}
</style>
