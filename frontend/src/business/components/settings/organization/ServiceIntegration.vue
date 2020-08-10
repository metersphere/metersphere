<template>
  <div>
    <el-tabs class="system-setting" v-model="activeName">
      <el-tab-pane label="缺陷管理平台" name="defect">
        <defect-management/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>

  import DefectManagement from "./DefectManagement";

  export default {
    name: "ServiceIntegration",
    components: {
      DefectManagement
    },
    data() {
      return {
        activeName: 'defect'
      }
    }
  }
</script>

<style scoped>

</style>
