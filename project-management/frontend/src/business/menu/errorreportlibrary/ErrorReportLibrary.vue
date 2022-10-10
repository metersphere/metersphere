<template>
  <div>
    <el-container>
      <el-main>
        <error-report-library-list  @createErrorReport="createErrorReport" @editErrorReport="editErrorReport" ref="errorReportLibrary"/>
      </el-main>
    </el-container>
    <error-report-library-maintain-dialog @refresh="reloadTable" ref="maintainDialog"/>
  </div>
</template>

<script>
import ErrorReportLibraryList
  from "./components/base/ErrorReportLibraryList";
import ErrorReportLibraryMaintainDialog
  from "./components/dialog/ErrorReportLibraryMaintainDialog";

export default {
  name: "ErrorReportLibrary",
  components: {ErrorReportLibraryList,ErrorReportLibraryMaintainDialog},
  data() {
    return {
    }
  },
  created() {
    this.init();
  },
  methods: {
    init(){
    },
    createErrorReport(data){
      this.$refs.maintainDialog.open(data);
    },
    editErrorReport(data){
      this.$refs.maintainDialog.open(data);
    },
    reloadTable(){
      this.$refs.errorReportLibrary.initTableData();
    }
  },
}
</script>

<style scoped>
</style>
