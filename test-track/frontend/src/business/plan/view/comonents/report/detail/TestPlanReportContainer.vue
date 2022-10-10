<template>
  <div class="report-container">
    <ms-form-divider :title="title">
      <slot name="title"></slot>
    </ms-form-divider>
    <slot/>
  </div>
</template>

<script>
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
export default {
  name: "TestPlanReportContainer",
  components: {MsFormDivider},
  props: ['title']
}
</script>

<style scoped>
</style>
