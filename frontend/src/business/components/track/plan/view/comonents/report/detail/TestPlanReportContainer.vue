<template>
  <div>
    <ms-form-divider :title="title"/>
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
