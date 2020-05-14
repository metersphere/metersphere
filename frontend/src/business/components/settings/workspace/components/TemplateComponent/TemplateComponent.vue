<template>
  <div>

    <!--模版-->
    <div v-if="!metric">
      <base-info-component :is-report="false" v-if="preview.id == 1"/>
      <test-result-component v-if="preview.id == 2"/>
      <test-result-chart-component v-if="preview.id == 3"/>
      <rich-text-component :preview="preview" v-if="preview.type != 'system'"/>
    </div>

    <!--报告-->
    <div v-if="metric">
      <base-info-component :report-info="metric" v-if="preview.id == 1"/>
      <test-result-component :test-results="metric.moduleExecuteResult" v-if="preview.id == 2"/>
      <test-result-chart-component :execute-result="metric.executeResult" v-if="preview.id == 3"/>
      <rich-text-component :is-report-view="isReportView" :preview="preview" v-if="preview.type != 'system'"/>
    </div>

  </div>
</template>

<script>
    import BaseInfoComponent from "./BaseInfoComponent";
    import TestResultComponent from "./TestResultComponent";
    import TestResultChartComponent from "./TestResultChartComponent";
    import RichTextComponent from "./RichTextComponent";
    export default {
      name: "TemplateComponent",
      components: {RichTextComponent, TestResultChartComponent, TestResultComponent, BaseInfoComponent},
      props: {
        preview: {
          type: Object
        },
        metric: {
          type: Object
        },
        isReport: {
          type: Boolean,
          default: true
        },
        isReportView: {
          type: Boolean,
          default: true
        },
      }
    }
</script>

<style scoped>

  .el-card {
    margin: 5px auto;
    min-height: 300px;
    width: 80%;
  }

  .el-card:hover {
    box-shadow: 0 0 2px 2px #409EFF;
  }

</style>
