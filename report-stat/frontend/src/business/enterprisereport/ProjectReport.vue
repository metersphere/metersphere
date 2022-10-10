<template>
  <div>
    <el-container>
      <el-main>
        <project-report-list @createProjectReport="createProjectReport" @editProjectReport="editProjectReport"
                             ref="projectReportList"/>
      </el-main>
    </el-container>
    <project-report-edit-dialog @refresh="refreshTable"
                                ref="projectReportEditDialog"/>
  </div>
</template>

<script>
import ProjectReportList from "@/business/enterprisereport/components/list/ProjectReportList";
import ProjectReportEditDialog from "@/business/enterprisereport/components/dialog/ProjectReportEditDialog";

export default {
  name: "ProjectReport",
  components: {ProjectReportList, ProjectReportEditDialog},
  data() {
    return {}
  },
  created() {
    this.init();
  },
  methods: {
    init() {
    },
    createProjectReport() {
      this.$refs.projectReportEditDialog.open();
    },
    editProjectReport(projectReport) {
      this.$refs.projectReportEditDialog.open(projectReport);
    },
    refreshTable() {
      this.$refs.projectReportList.initTableData();
    }
  },
  activated() {
    this.refreshTable();
  },
}
</script>

<style scoped>
.ms-span {
  margin: 10px 10px 0px
}

.tip {
  float: left;
  font-size: 14px;
  border-radius: 2px;
  border-left: 2px solid #783887;
  margin: 10px 20px 0px;
}
</style>
