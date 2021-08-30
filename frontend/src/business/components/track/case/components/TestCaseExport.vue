<template>
  <el-dialog class="testcase-import" :title="$t('test_track.case.import.case_export')" :visible.sync="dialogVisible"
             @close="close">

    <el-row class="import-row" style="margin-left: 34px">
      <el-radio v-model="exportType" label="excel">{{$t('commons.excelFile')}}</el-radio>
      <el-radio v-model="exportType" label="xmind">{{$t('commons.xmindFile')}}</el-radio>
    </el-row>

    <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">{{$t('commons.cancel')}}</el-button>
    <el-button type="primary" @click="exportTestCase">{{$t('commons.export')}}</el-button>
  </span>
  </el-dialog>
</template>

<script>
import ElUploadList from "element-ui/packages/upload/src/upload-list";
import MsTableButton from '../../../../components/common/components/MsTableButton';
import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
import {TokenKey} from '../../../../../common/js/constants';
import axios from "axios";

export default {
  name: "TestCaseImport",
  components: {ElUploadList, MsTableButton},
  data() {
    return {
      exportType:"excel",
      dialogVisible: false,
      projectId:"",
    }
  },
  created() {
  },
  activated() {
  },
  methods: {
    handleError(err, file, fileList) {
      this.isLoading = false;
      this.$error(err.message);
    },
    open() {
      listenGoBack(this.close);
      this.projectId = getCurrentProjectID();
      this.dialogVisible = true;
    },
    close() {
      removeGoBackListener(this.close);
      this.dialogVisible = false;
    },
    exportTestCase(){
      this.$emit('exportTestCase',this.exportType);
      this.close();
    }
  }
}
</script>

<style>
</style>

<style scoped>

.download-template {
  padding-top: 0px;
  padding-bottom: 10px;
}

.import-row {
  padding-top: 20px;
}

.testcase-import >>> .el-dialog {
  width: 400px;
}

.testcase-import-img {
  width: 614px;
  height: 312px;
  size: 200px;
}


</style>
