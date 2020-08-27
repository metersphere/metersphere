<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName">
      <el-tab-pane :label="$t('organization.defect_manage')" name="defect">
        <defect-management/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>

  import DefectManagement from "./IssuesManagement";

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
