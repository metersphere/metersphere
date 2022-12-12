<template>
  <el-main class="ms-main-container">
    <slot></slot>
  </el-main>
</template>

<script>
export default {
  name: "MsMainContainer"
}
</script>

<style scoped>
.el-main.ms-main-container {
  padding: 0;
  z-index: 9;
}

:deep(.el-card.card-content.is-always-shadow) {
  border: none;
  border-radius: 0 4px 4px 0;
  height: -webkit-fill-available;
}
</style>
