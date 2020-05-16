<template>

  <el-aside class="ms-aside-container">
    <slot></slot>
  </el-aside>

</template>

<script>
    export default {
      name: "MsAsideContainer"
    }
</script>

<style scoped>

  .ms-aside-container {
    border: 1px solid #EBEEF5;
    border-radius: 2px;
    background: white;
  }

</style>
