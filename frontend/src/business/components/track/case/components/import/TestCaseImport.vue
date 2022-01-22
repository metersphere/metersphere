<template>
  <el-dialog class="testcase-import" :title="$t('test_track.case.import.case_import')" :visible.sync="dialogVisible" @close="close">
    <el-tabs v-model="activeName" simple>

      <el-tab-pane :label="$t('test_track.case.import.excel_title')" name="excelImport">
        <test-case-common-import
          name="excel"
          tab-name="excelImport"
          @close="close"
          ref="excelImport"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('test_track.case.import.xmind_title')" name="xmindImport">
        <test-case-common-import
          name="xmind"
          tab-name="xmindImport"
          @close="close"
          ref="xmindImport"/>
      </el-tab-pane>

    </el-tabs>
  </el-dialog>
</template>

<script>
  import MsTableButton from '../../../../common/components/MsTableButton';
  import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "../../../../../../common/js/utils";
  import TestCaseCommonImport from "@/business/components/track/case/components/import/TestCaseCommonImport";

  export default {
    name: "TestCaseImport",
    components: {TestCaseCommonImport, MsTableButton},
    data() {
      return {
        activeName: 'excelImport',
        dialogVisible: false,
        isLoading: false
      }
    },
    methods: {
      open() {
        listenGoBack(this.close);
        this.projectId = getCurrentProjectID();
        this.dialogVisible = true;
        this.init();
      },
      init() {
        if (this.$refs.excelImport) {
          this.$refs.excelImport.init();
        }
        if (this.$refs.xmindImport) {
          this.$refs.xmindImport.init();
        }
      },
      close(isUpdated) {
        removeGoBackListener(this.close);
        this.dialogVisible = false;
        if (isUpdated) {
          //通过excel导入更新过数据的话就刷新页面
          this.$emit("refreshAll");
        }
      },
    }
  }
</script>

<style>
</style>

<style scoped>

.testcase-import >>> .el-dialog {
  width: 650px;
}

</style>
