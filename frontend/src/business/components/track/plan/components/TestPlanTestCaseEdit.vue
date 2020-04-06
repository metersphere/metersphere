<template>

  <div>
    <el-drawer
      title="我是标题"
      :visible.sync="drawer"
      :direction="direction"
      :before-close="handleClose">
      <span>我来啦!</span>
    </el-drawer>
  </div>

</template>

<script>
    export default {
      name: "TestPlanTestCaseEdit",
      data() {
        return {
          drawer: false,
          direction: 'rtl',
        };
      }
    }
</script>

<style scoped>

</style>
