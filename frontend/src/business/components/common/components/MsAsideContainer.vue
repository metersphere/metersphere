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
    border: 1px solid #E6E6E6;
    padding: 10px;
    border-radius: 2px;
    box-sizing: border-box;
    background-color: #FFF;
    height: calc(100vh - 80px);
  }

</style>
