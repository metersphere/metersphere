<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName">
      <el-tab-pane label="项目管理" name="project">
        <ms-project/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import MsProject from "@/business/components/project/MsProject";
export default {
  name: "Project",
  components: {MsProject},
  data() {
    return {
      activeName: 'project'
    }
  }
}
</script>

<style scoped>

</style>
