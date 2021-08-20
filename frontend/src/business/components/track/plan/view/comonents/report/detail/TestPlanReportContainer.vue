<template>
  <div class="report-container">
    <ms-form-divider :title="title">
      <slot name="title"></slot>
    </ms-form-divider>
<!--    <el-main>-->
      <slot/>
<!--    </el-main>-->
  </div>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
export default {
  name: "TestPlanReportContainer",
  components: {MsFormDivider},
  props: ['title']
}
</script>

<style scoped>
.el-main {
  /*padding: 20px;*/
}
</style>
