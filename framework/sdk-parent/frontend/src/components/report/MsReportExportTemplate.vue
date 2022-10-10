<template>
  <div class="report-export">
    <ms-report-title :title="title" :type="type" :report="report" :project-env-map="projectEnvMap"/>
    <slot></slot>
  </div>
</template>

<script>
import MsReportTitle from "./MsReportTitle";

export default {
  name: "MsReportExportTemplate",
  components: {MsReportTitle},
  props: {title: String, type: String, report: Object, projectEnvMap: {}},
}
</script>

<style scoped>

.report-export {
  padding: 30px;
}

.report-title {
  margin-bottom: 30px;
}

.report-export {
  background: white;
}

@page {
  margin-bottom: 30px;
  margin-top: 30px;
}
</style>
